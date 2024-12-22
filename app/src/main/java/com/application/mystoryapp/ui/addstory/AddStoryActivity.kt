package com.application.mystoryapp.ui.addstory

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.lifecycle.lifecycleScope
import com.application.mystoryapp.data.database.StoryDatabase
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.data.repository.UserRepository
import com.application.mystoryapp.pref.dataStore
import com.application.mystoryapp.data.retrofit.ApiConfig
import com.application.mystoryapp.databinding.ActivityAddStoryBinding
import com.application.mystoryapp.getImageUri
import com.application.mystoryapp.reduceFileImage
import com.application.mystoryapp.ui.ViewModelFactory
import com.application.mystoryapp.ui.story.StoryActivity
import com.application.mystoryapp.uriToFile
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var selectedUri: Uri? = null
    private var capturedImageUri: Uri? = null
    private var capturedImageFile: File? = null
    private var userLatitude: Float? = null
    private var userLongitude: Float? = null
    private lateinit var userPreferences: UserPreference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: AddStoryViewModel by viewModels {
        val database = StoryDatabase.getDatabase(this)
        ViewModelFactory(UserRepository
            (ApiConfig.getApiService(),
            UserPreference.getInstance(dataStore),
            database)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Please enable location services to add location to your story")
                .setPositiveButton("Open Settings") { _, _ ->
                    showLocationSettings()
                }
                .setNegativeButton("Continue Without Location") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Initialize UserPreference
        userPreferences = UserPreference.getInstance(applicationContext.dataStore)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permissions and fetch location
        checkPermissions()

        // create back button on actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonImage.setOnClickListener { useGalery = true; openGallery() }
        binding.buttonCamera.setOnClickListener { useGalery = false; openCamera() }
        binding.buttonUpload.setOnClickListener { if (validateInput()) { uploadStory() } }
    }

    // Add this UI helper function
    private fun showLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    // Add this check function
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Navigate back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var useGalery: Boolean = true
    private fun validateInput(): Boolean {
        if (useGalery && selectedUri == null){
            showToast("Pilih gambar terlebih dahulu.")
            return false
        }
        if (!useGalery && capturedImageUri == null) {
            showToast("Mohon foto terlebih dahulu.")
            return false
        }
        if (binding.postDescriptionEditText.text.toString().isEmpty()) {
            binding.postDescriptionEditText.error = "Deskripsi harus diisi."
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun uploadStory() {
        binding.buttonUpload.isEnabled = false

        // Get location first, then proceed with upload only after location is obtained
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    userLatitude = location.latitude.toFloat()
                    userLongitude = location.longitude.toFloat()
                    Log.d(TAG, "Location obtained: lat=$userLatitude, lon=$userLongitude")
                } else {
                    Log.d(TAG, "Location is null")
                }
            } else {
                Log.e(TAG, "Failed to get location", task.exception)
            }

            // Now proceed with upload after location attempt is complete
            proceedWithUpload()
        }
    }

    private fun proceedWithUpload() {
        lifecycleScope.launch {
            userPreferences.getSession().collect { user ->
                val token = user.token
                if (token.isNotEmpty()) {
                    val bearerToken = "Bearer $token"
                    val description = binding.postDescriptionEditText.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val body = when {
                        selectedUri != null -> {
                            val contentResolver = contentResolver
                            val inputStream = contentResolver.openInputStream(selectedUri!!)
                            val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
                            tempFile.outputStream().use { outputStream ->
                                inputStream?.copyTo(outputStream)
                            }

                            tempFile.reduceFileImage()

                            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("photo", tempFile.name, requestBody)
                        }
                        capturedImageUri != null -> {
                            capturedImageFile = uriToFile(capturedImageUri!!, this@AddStoryActivity)
                            val requestBody = capturedImageFile!!.reduceFileImage().asRequestBody("image/jpeg".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("photo", capturedImageFile!!.name, requestBody)
                        }
                        else -> null
                    }

                    if (body != null) {
                        Log.d(TAG,"Uploading with location: lat=$userLatitude, lon=$userLongitude")
                        viewModel.uploadStory(bearerToken, description, body, userLatitude, userLongitude)
                        observeUploadResult()
                    } else {
                        binding.buttonUpload.isEnabled = true
                        showToast("Gambar tidak ditemukan.")
                    }
                } else {
                    binding.buttonUpload.isEnabled = true
                    showToast("Authentication token missing.")
                    intent = Intent()
                    startActivity(intent)
                }
            }
        }
    }


    private fun observeUploadResult() {
        viewModel.uploadResult.observe(this) { result ->
            binding.buttonUpload.isEnabled = true

            result.onSuccess {
                Log.d("Upload", "Story uploaded successfully: $it")
                showToast("Story uploaded successfully!")

                val intent = Intent(this, StoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }.onFailure {
                Log.e("Upload", "Upload failed: ${it.message}")
                showToast("Upload failed: ${it.message}")
            }
        }
    }

    // ===============================================
    // ================== location ===================
    // ===============================================
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Define location request parameters
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
        }

        // Request location updates
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                userLatitude = it.latitude.toFloat()
                userLongitude = it.longitude.toFloat()
            } ?: run {
                Log.d(TAG, "Location is null")
                // Handle null location case
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting location", exception)
            // Handle location request failure
        }
    }

    // ===============================================
    // =============== Handle gallery ================
    // ===============================================
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    selectedUri = it
                    capturedImageFile
                    Glide.with(this)
                        .load(it)
                        .into(binding.previewImage)
                }
            } else {
                showToast("Gagal memilih gambar.")
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    // ===============================================
    // ================ Handle camera ================
    // ===============================================
    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                showImage()
            } else {
                // Reset the URI if the camera operation was canceled
                capturedImageUri = null
                showToast("Camera operation canceled.")
            }
        }

    private fun openCamera() {
        capturedImageUri = getImageUri(this)
        capturedImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        } ?: showToast("Gagal membuat URI untuk kamera.")
    }

    private fun showImage() {
        capturedImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImage.setImageURI(it)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    // ===================================================
    // ================ Handle Permission ================
    // ===================================================
    private fun checkPermissions() {
        val requiredPermissions = arrayOf(CAMERA, READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)
        val notGranted = requiredPermissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            requestPermissions(notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val TAG = "AddStoryActivity"
    }
}