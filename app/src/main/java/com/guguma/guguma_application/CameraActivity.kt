package com.guguma.guguma_application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    // 카메라 뷰 관련 변수
    private lateinit var viewFinder: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    // 식물 감지 루프
    private val handler = Handler(Looper.getMainLooper())
    private val detectInterval = 4000L // 2초마다 감지 실행
    private var retryDelay = 2000L //2초대기
    private var isDetecting = false


    // OkHttp 클라이언트 설정
    private val client = OkHttpClient()

    // 진동 설정 (API 레벨에 따라 분기)
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    // 카메라 권한 설정
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startCamera()
        else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 초기화
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        // 뷰 초기화
        viewFinder = findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()
        // 권한 확인 및 카메라 시작
        checkCameraPermission()
        // 버튼 이벤트 설정
        findViewById<ImageButton>(R.id.close_button).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.shutter_button).setOnClickListener {
            stopDetectLoop() // 실시간 감지 중지
            takePicture()    // 촬영된 이미지 처리
        }
        startDetectLoop()
    }

    private fun handleServerMessage(message: String, bitmap: Bitmap) {
        stopDetectLoop()
        when (message) {
            "식물을 등록하시겠습니까?" -> {
                showCompletedDialog(bitmap)
            }
            else -> {
                showWarningDialog(message)
                startDetectLoop()
            }
        }
        startDetectLoop()
    }

    // 카메라 권한 확인
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 카메라 시작
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // 프리뷰 설정
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraActivity", "카메라 시작 실패", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun showCompletedDialog(bitmap: Bitmap) {
        stopDetectLoop()
        val dialog = AlertDialog.Builder(this)
            .setTitle("촬영 완료")
            .setMessage("촬영이 완료되었습니다.\n확인 버튼을 눌러주세요.")
            .setPositiveButton("확인") { _, _ ->
                recognizePlant(bitmap) // 서버에 식물 이름 인식 요청
            }.setNegativeButton("취소") { _, _ ->
            }
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                it.requestFocus() // 메시지에 포커스 설정
            }

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }

        dialog.show()
    }

    private fun startDetectLoop() {
        if (!isDetecting) {
            isDetecting = true
            handler.post(object : Runnable {
                override fun run() {
                    if (isDetecting) {
                        takePictureForDetect() // 실시간 감지를 위한 촬영
                        handler.postDelayed(this, detectInterval)
                    }
                }
            })
        }
    }

    // 감지를 위한 사진 촬영
    private fun takePictureForDetect() {
        if (!::imageCapture.isInitialized) return

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()

                    bitmap?.let {
                        detectImage(it) // Detect API 호출 (식물 위치 확인)
                    }
                }
            }
        )
    }

    // 감지 루프 중지
    private fun stopDetectLoop() {
        isDetecting = false
        handler.removeCallbacksAndMessages(null)
    }

    // 사진 촬영
    private fun takePicture() {
        if (!::imageCapture.isInitialized) return

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()

                    bitmap?.let {
                        stopDetectLoop() // 실시간 감지 중지
                        showCompletedDialog(it) // 식물 등록 확인 대화상자 표시
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "사진 촬영 실패: ${exception.message}")
                }
            }
        )
    }

    // Image Proxy를 Bitmap으로 변환
    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            val buffer = planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        } catch (e: Exception) {
            Log.e("CameraActivity", "Bitmap 변환 실패", e)
            null
        }
    }

    // 이미지 감지 (Detect API 호출)
    private fun detectImage(bitmap: Bitmap) {
        Log.d("CameraActivity", "Detect API URL: ${BuildConfig.API_PLANT_DETECT}")

        if (BuildConfig.API_PLANT_DETECT.isEmpty()) {
            Log.e("CameraActivity", "Detect API URL이 설정되지 않았습니다.")
            return
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", "image.jpg",
                byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_DETECT)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.optString("message", "식별 실패")
                    val imageUrl = jsonResponse.optString("imageUrl", null)

                    runOnUiThread { handleServerMessage(message, bitmap) }
                } else {
                    runOnUiThread { showWarningDialog("서버 응답이 없습니다.") }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("CameraActivity", "서버 전송 실패: ${e.message}")
                runOnUiThread { showWarningDialog("네트워크 오류가 발생했습니다. 다시 시도해주세요.") }
            }
        })
    }

    // 식물 이름 인식 (Recognize API 호출)
    private fun recognizePlant(bitmap: Bitmap) {
        Log.d("CameraActivity", "Recognize API URL: ${BuildConfig.API_PLANT_RECOGNIZE}")

        if (BuildConfig.API_PLANT_RECOGNIZE.isEmpty()) {
            Log.e("CameraActivity", "Recognize API URL이 설정되지 않았습니다.")
            return
        }

        val byteArray = compressBitmapToUnder1MB(bitmap)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "imageFile", "plant_image.jpg",
                byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_RECOGNIZE)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showWarningDialog("식물 이름을 인식하는 중 오류가 발생했습니다.") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    val name = jsonResponse.optString("name", "식별 실패")
                    val imageUrl = jsonResponse.optString("imageUrl", null)

                    runOnUiThread {
                        if (name != "식별 실패" && imageUrl != null) {
                            goToCreatePlantNameActivity(name, imageUrl)
                        } else {
                            showWarningDialog("식물을 인식할 수 없습니다.")
                        }
                    }
                }
            }
        })
    }
    private var isDialogShowing = false

    private fun showWarningDialog(message: String) {
        // 현재 화면에 표시된 대화상자 가져오기
        val currentDialog = supportFragmentManager.findFragmentByTag("WarningDialog") as? AlertDialog
        if (currentDialog != null && currentDialog.isShowing) {
            // 이미 대화상자가 표시 중이라면 새로 생성하지 않음
            return
        }

        stopDetectLoop() // 감지 루프 중단

        val dialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                startDetectLoop() // 확인 후 감지 루프 재시작
            }
            .setCancelable(false)
            .create()

        // 접근성을 위한 초기 설정
        dialog.setOnShowListener {
            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                it.requestFocus() // 초기 포커스를 메시지에 설정
            }

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        dialog.show()
    }

    // 식물 이름 화면으로 이동
    private fun goToCreatePlantNameActivity(name: String, imageUrl: String) {
        val intent = Intent(this, CreatePlantNameActivity::class.java).apply {
            putExtra("plantName", name) // 식물 이름 전달
            putExtra("imageUrl", imageUrl) // 이미지 URL 전달
        }
        startActivity(intent)
        finish()
    }

    // 이미지 크기를 1MB 이하로 압축
    private fun compressBitmapToUnder1MB(bitmap: Bitmap): ByteArray {
        var quality = 80
        var stream: ByteArrayOutputStream
        var byteArray: ByteArray

        do {
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            byteArray = stream.toByteArray()
            quality -= 10 // 압축 품질 감소
        } while (byteArray.size > 1_000_000 && quality > 10) // 1MB 이하가 될 때까지 반복

        return byteArray
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetectLoop()
        cameraExecutor.shutdownNow()
    }
}