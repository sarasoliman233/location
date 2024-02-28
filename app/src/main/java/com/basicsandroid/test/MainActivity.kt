    package com.basicsandroid.test

   import android.annotation.SuppressLint
   import android.content.Context
   import android.content.DialogInterface
   import android.content.pm.PackageManager
   import android.graphics.Bitmap
   import android.graphics.Canvas
   import android.location.Location
   import android.os.Build
   import android.os.Bundle
   import android.os.Looper
   import android.util.Log
   import android.widget.Toast
   import androidx.activity.result.contract.ActivityResultContracts
   import androidx.annotation.DrawableRes
   import androidx.annotation.RequiresApi
   import androidx.appcompat.app.AlertDialog
   import androidx.appcompat.app.AppCompatActivity
   import androidx.core.app.ActivityCompat
   import androidx.core.content.ContextCompat
   import com.basicsandroid.test.databinding.ActivityMainBinding
   import com.google.android.gms.location.CurrentLocationRequest
   import com.google.android.gms.location.FusedLocationProviderClient
   import com.google.android.gms.location.LocationCallback
   import com.google.android.gms.location.LocationRequest
   import com.google.android.gms.location.LocationResult
   import com.google.android.gms.location.LocationServices
   import com.google.android.gms.location.Priority
   import com.google.android.gms.maps.CameraUpdateFactory
   import com.google.android.gms.maps.GoogleMap
   import com.google.android.gms.maps.SupportMapFragment
   import com.google.android.gms.maps.model.BitmapDescriptor
   import com.google.android.gms.maps.model.BitmapDescriptorFactory
   import com.google.android.gms.maps.model.LatLng
   import com.google.android.gms.maps.model.Marker
   import com.google.android.gms.maps.model.MarkerOptions


    class MainActivity : AppCompatActivity() {
        lateinit var binding: ActivityMainBinding
        var googleMap:GoogleMap?=null
        var marker:Marker?=null
    val requestLocationCallback=registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isrGranted ->
        if (isrGranted){
       //user accepted
        // access feature
            getUserLocation()

        }else {
            //user Refused
            // you can't access feature

        }

    }

        fun requestPermission(){
         if(ContextCompat.checkSelfPermission(this,
                 android.Manifest.permission.ACCESS_FINE_LOCATION
                 )==PackageManager.PERMISSION_GRANTED){
           getUserLocation()
                 }
         else if(
             ActivityCompat.shouldShowRequestPermissionRationale(
                 this,
                 android.Manifest.permission.ACCESS_FINE_LOCATION
             )){
                 //show Dialoge
                 showDialog(
                     message = "We need a Location permission to allow us to find nearest driver",
                     postiveButtonText = "show again",
                     onPositveActionClick = {
                         requestLocationCallback.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                     },
                     negativeButtonText = "Cancel"

                 )

             }
         else {
             requestLocationCallback.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
             }

         }

  var dialog:AlertDialog?=null
        fun showDialog(
            message:String?=null,
            postiveButtonText:String?=null,
            onPositveActionClick:(()->Unit)?=null,
            negativeButtonText:String?=null,
            onNegativeActionClick:(()->Unit)?=null,
            isCancelable:Boolean=true
        ){
          val builder=AlertDialog.Builder(this)
            builder.setMessage(message)
            builder.setPositiveButton(postiveButtonText,object :DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                    onPositveActionClick?.invoke()

                }
            })
            builder.setNegativeButton(negativeButtonText
            ) { dialog, which ->
                dialog?.dismiss()
                onNegativeActionClick?.invoke()

            }
            builder.setCancelable(isCancelable)
          dialog=  builder.show()
        }
        @RequiresApi(Build.VERSION_CODES.S)
        @SuppressLint("MissingPermission")
        fun getUserLocation(){
            Toast.makeText(this,"can access user location",Toast.LENGTH_LONG).show()
            fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
            val bulider= CurrentLocationRequest.Builder()
            bulider.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            bulider.setDurationMillis(500)
            fusedLocationClient.getCurrentLocation(bulider.build(),null).addOnSuccessListener {location: Location?->
                Log.e("TAG","getUserLocation:${location?.longitude}")
                Log.e("TAG","getUserLocation:${location?.latitude}")
            }
            fusedLocationClient.lastLocation.addOnFailureListener{
                Log.e("Tag","getuserLocation: ${it.message}")
            }
            val locationRequest= LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,4000).build()
            val callback=object :LocationCallback(){ // حطيت كوسين عشان abstract class
                override fun onLocationResult(locationResult: LocationResult) {
                    Log.e("Location Tag",
                        "onLocation Result : Longitude ${locationResult.lastLocation?.longitude}")
                    Log.e("Location Tag",
                        "onLocation Result : Latitude ${locationResult.lastLocation?.latitude}")
                    putMarkerOnMap(
                        locationResult.lastLocation?.latitude ?: 0.0,
                        locationResult.lastLocation?.longitude ?: 0.0
                    )

                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
               Looper.getMainLooper()
            )
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync{ googleMap ->
           this.googleMap=googleMap
            }

        }

//google maps
        //create new project and api service
        //in android studio make ctrl +ctrl
        //take project ID in -> package name
        // search -> gradle signingreport
        //take SHA-1 in -> SHA-1

fun putMarkerOnMap(latitude:Double,longtuide:Double){
    if(marker==null) {
        val markerOptions = MarkerOptions()
        markerOptions.title("This is Driver Location")
        markerOptions.position(LatLng(latitude, longtuide))
        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_marker))
        marker=googleMap?.addMarker(markerOptions)
    }
    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude,longtuide),16.0F))
    marker?.position= LatLng(latitude,longtuide)
}
        private fun bitmapDescriptorFromVector(
            context: Context,
            @DrawableRes vectorResId: Int
        ): BitmapDescriptor {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable!!.setBounds(
                0,
                0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            )
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }


        private lateinit var fusedLocationClient:FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()

    }
}