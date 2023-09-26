package com.example.locationpermission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {

    val TAG = "MainTag"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var progressBar: ProgressBar

    val callback = object : LocationCallback() {

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
        }

        override fun onLocationResult(result: LocationResult) {
            progressBar.visibility = View.GONE
            val lastLocation = result.lastLocation
            findViewById<TextView>(R.id.latTextView).text =
                "Latitude :" + lastLocation!!.latitude.toString()
            findViewById<TextView>(R.id.lngTextView).text =
                "Longitude :" + lastLocation!!.longitude.toString()

            super.onLocationResult(result)
        }
    }

    private lateinit var locationRequest: LocationRequest
    private val REQUEST_CHECK_SETTING = 1001

    var Listener: ReceiverListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        progressBar = findViewById(R.id.progressBar)
        fetchLocation()

    }


    fun fetchLocation() {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //progressBar.visibility = View.VISIBLE
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                photoPermissionLauncher.launch(
                    arrayOf<String>(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                checkLocationSetting()
            }
        //}
    }

    var photoPermissionLauncher = registerForActivityResult<Array<String>, Map<String, Boolean>>(
        ActivityResultContracts.RequestMultiplePermissions(),
        ActivityResultCallback<Map<String, Boolean>> { result: Map<String, Boolean> ->
            var granted = true
            for ((_, value) in result) {
                if (!value) {
                    granted = false
                }
            }
            if (granted) {
                checkLocationSetting()
            }
        }
    )



    @SuppressLint("MissingPermission")
    fun checkLocationSetting() {

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()

        //fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper())

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(applicationContext).checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
                Toast.makeText(this@MainActivity, "GPS is On", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "checkSetting: GPS On")
                progressBar.visibility = View.VISIBLE
                fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.myLooper())

            } catch (e: ApiException) {

                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                        val resolvableApiException = e as ResolvableApiException
                        /*resolvableApiException.startResolutionForResult(
                            this@MainActivity,
                            REQUEST_CHECK_SETTING
                        )*/
                        val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                        resolutionForResult.launch(intentSenderRequest)
                        Log.d(TAG, "checkSetting: RESOLUTION_REQUIRED")
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // USER DEVICE DOES NOT HAVE LOCATION OPTION
                    }
                }

            }
        }

       /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTING -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(this@MainActivity, "GPS is Turned on", Toast.LENGTH_SHORT)
                            .show()
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(
                            this@MainActivity,
                            "GPS is Required to use this app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }*/

    }


    private val resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                Log.d(TAG, "123455")
                checkLocationSetting()

            }
            else {
                Log.d(TAG, "656565665")
                checkLocationSetting()
            }
        }
}