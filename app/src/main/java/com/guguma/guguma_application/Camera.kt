package com.guguma.guguma_application

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
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
import androidx.core.app.ActivityCompat
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
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Camera() : AppCompatActivity(), Parcelable {

    private lateinit var viewFinder: PreviewView  // 카메라 미리보기를 위한 PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService  // 카메라 작업을 위한 Executor
    private val client = OkHttpClient()  // OkHttpClient 초기화
    private val STORAGE_PERMISSION_CODE = 1001 // 권한 코드 정의

    // 권한 요청 런처 추가
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    constructor(parcel: Parcel) : this() {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        requestStoragePermission()

        // 카메라 미리보기 뷰 초기화
        viewFinder = findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 시작
        checkCameraPermission()

        // X 버튼 설정
        val closeButton: ImageButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            finish() // 카메라 화면 종료
        }

        // 갤러리 버튼 설정
        val galleryButton: ImageButton = findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            checkPermissionAndOpenGallery()
        }


        // 셔터 버튼 설정
        val shutterButton: ImageButton = findViewById(R.id.shutter_button)
        shutterButton.setOnClickListener {
            takePicture()
        }

    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                .setTitle("카메라 권한 필요")
                .setMessage("카메라 기능을 사용하려면 권한이 필요합니다.")
                .setPositiveButton("권한 허용") { _, _ ->
                    // 권한 요청 실행
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .setNegativeButton("취소") { dialog, _ ->
                    // 사용자가 거부할 경우
                    dialog.dismiss()
                    Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .create()
                .show()
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider 초기화
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider) // PreviewView의 surfaceProvider 설정
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA  // 후면 카메라 설정

            try {
                // 기존 바인딩 제거 및 새 바인딩 설정
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                Log.d("CameraXApp", "카메라가 성공적으로 시작되었습니다.")
            } catch (exc: Exception) {
                Log.e("CameraXApp", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun takePicture() {
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    Log.d("DebugCamera", "Image captured successfully")
                    val originalBitmap = imageProxy.toBitmap()
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees  // 회전 정보 가져오기
                    imageProxy.close()

                    // Bitmap 해상도 축소 및 회전 적용
                    val rotatedBitmap = originalBitmap?.let {
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(rotationDegrees.toFloat())  // 회전 정보 적용
                        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                    }

                    // 해상도 절반으로 축소
                    val reducedBitmap = rotatedBitmap?.let {
                        Bitmap.createScaledBitmap(it, it.width / 2, it.height / 2, true)
                    }

                    cameraExecutor.execute {
                        sendImageToServer(reducedBitmap)  // 서버로 전송하는 함수 호출
                    }
                    //sendImageToServer(bitmap)  // 서버로 전송하는 함수 호출
                    Log.d("CameraXApp", "사진이 성공적으로 촬영되었습니다.")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("DebugCamera", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    // Bitmap으로 변환하는 확장 함수 추가
    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun sendImageToServer(bitmap: Bitmap?) {
        if (bitmap == null) {
            Log.e("DebugNetwork", "Bitmap is null, cannot send to server.")
            return
        }
        Log.d("DebugNetwork", "Preparing to send image to server")

        bitmap?.let {
            // 압축 품질을 80%로 설정하여 JPEG로 압축
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream) // 품질을 80%로 설정
            val byteArray = stream.toByteArray()

            //val requestBody = buildMultipartBody(it)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg", byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url(BuildConfig.API_PLANT_DETECT)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.use { res ->
                        val responseString = res.body?.string() ?: "null"
                        Log.d("DebugNetwork", "Server response: $responseString")
                        try {
                            // JSON 응답 파싱
                            val jsonResponse = JSONObject(responseString)
                            var plantName = jsonResponse.optString("name", "이름 정보 없음")

                            // 정규 표현식으로 한글만 추출
                            plantName = plantName.replace("[^가-힣]".toRegex(), "")

                            val status = jsonResponse.optString("status", "상태 정보 없음")
                            val remedy = jsonResponse.optString("remedy", "대처법 정보 없음")

                            // 로그 출력
                            Log.d("DebugNetwork", "식물 이름: $plantName, 상태: $status, 대처법: $remedy")

                            runOnUiThread {

                            }
                        } catch (e: Exception) {
                            Log.e("DebugNetwork", "Error parsing JSON response: ${e.message}", e)
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("DebugNetwork", "Failed to send image to server: ${e.message}", e)
                    //e.printStackTrace()
                    runOnUiThread {
                        showFailurePopup()  // Show failure alert if image transmission fails
                    }
                }
            })
        }
    }

    private fun showEdgePopup(message: String) {
        AlertDialog.Builder(this)
            .setTitle("경고")
            .setMessage(message)
            .setPositiveButton("재촬영") { dialog, _ ->
                dialog.dismiss()
                //takePicture()
            }
            .create()
            .show()
    }

    private fun showConfirmationPopup(bitmap: Bitmap) {
        AlertDialog.Builder(this)
            .setTitle("식물 등록")
            .setMessage("식물을 등록하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                dialog.dismiss()
                val uri = saveImageToGallery(bitmap)
                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val userUuid = prefs.getString("userUuid", null) ?: ""
                ImageStatusAndSave(bitmap, userUuid)
                if (uri != null) {
                    // AddPlantActivity로 이동하며 URI 전달
                    val intent = Intent(this, AddPlantActivity::class.java).apply {
                        putExtra("imageUri", uri.toString())
                    }
                    startActivity(intent)

                    // 토스트 메시지 표시
                    Toast.makeText(this, "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
                //takePicture()
            }
            .create()
            .show()
    }

    private fun showFailurePopup() {
        AlertDialog.Builder(this)
            .setTitle("오류")
            .setMessage("이미지를 서버로 전송하는 데 실패했습니다. 네트워크 연결을 확인하고 다시 시도해 주세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Guguma_Application")
            } else {
                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Guguma_Application")
                if (!directory.exists()) directory.mkdirs()
                put(MediaStore.Images.Media.DATA, File(directory, "captured_image_${System.currentTimeMillis()}.jpg").absolutePath)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }

        return uri
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()  // Executor 종료
    }

    // 갤러리 접근 권한 체크 및 열기
    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용된 경우
                navigatePhotos()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // 권한 거부 후 추가 설명이 필요한 경우
                showPermissionContextPopup(permission)
            }
            else -> {
                // 권한 요청 실행
                requestPermissions(arrayOf(permission), 1000)
            }
        }
    }

    // 권한 요청 교육용 팝업
    private fun showPermissionContextPopup(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("갤러리 권한 필요")
            .setMessage("갤러리에서 사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("권한 허용") { _, _ ->
                requestPermissions(arrayOf(permission), 1000)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "갤러리 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    private fun requestStoragePermission() {
        when {
            // Android 13 이상에서는 READ_MEDIA_IMAGES 권한 요청
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        STORAGE_PERMISSION_CODE
                    )
                }
            }
            // Android 10 미만에서는 WRITE_EXTERNAL_STORAGE와 READ_EXTERNAL_STORAGE 권한 요청
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        STORAGE_PERMISSION_CODE
                    )
                }
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "저장소 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리 열기
    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,2000)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Camera> {
        override fun createFromParcel(parcel: Parcel): Camera {
            return Camera(parcel)
        }

        override fun newArray(size: Int): Array<Camera?> {
            return arrayOfNulls(size)
        }
    }

    private fun ImageStatusAndSave(bitmap: Bitmap, userUuid: String) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))
            .addFormDataPart("userUuid", userUuid)
            .build()

        val request = Request.Builder()
            .url(BuildConfig.API_CAMERA_STATUS)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val responseString = res.body?.string() ?: "null"
                    try {
                        val jsonResponse = JSONObject(responseString)
                        val savedPlant = jsonResponse.optJSONObject("savedPlant")
                        val plantId = savedPlant?.optInt("id")

                        if (plantId != null) {
                            runOnUiThread {
                                val intent = Intent(this@Camera, AddPlantActivity::class.java)
                                intent.putExtra("plantId", plantId)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@Camera, "식물 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Camera", "JSON 파싱 오류: ${e.message}", e)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DebugNetwork", "Failed to send image to server: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@Camera, "서버 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })



}}