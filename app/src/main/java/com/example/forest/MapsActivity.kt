package com.example.forest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.forest.databinding.ActivityMapsBinding
import android.content.DialogInterface

import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Lifecycle

import com.example.forest.data.TRouteLatLng

import android.text.TextUtils
import java.util.*
import com.example.forest.data.AppDatabase
import com.google.android.gms.maps.model.*
import kotlin.collections.ArrayList

class MapsActivity : BaseLocationProviderActivity(), OnMapReadyCallback , LocationListener,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, AsyncTaskExportDB.ExportDBEventsListener,
    GoogleMap.OnPolygonClickListener {

    private val PERTH = LatLng(-31.952854, 115.857342)
    private val SYDNEY = LatLng(-33.87365, 151.20689)
    private val BRISBANE = LatLng(-27.47093, 153.0235)

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentLocMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2

    private val PATTERN_GAP_LENGTH_PX = 20
    private val DOT: PatternItem = Dot()
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())

    // Create a stroke pattern of a gap followed by a dot.
    private val PATTERN_POLYLINE_DOTTED = listOf(GAP, DOT)

    private var markerPerth: Marker? = null
    private var markerSydney: Marker? = null
    private var markerBrisbane: Marker? = null
    private var mapFragment : SupportMapFragment? = null;
    var  isGPS : Boolean = false
    var latitude // latitude
            = 0.0
    var longitude // longitude
            = 0.0
    private val MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 101
    private var database: AppDatabase? = null
    var latLng : LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

         mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        database = AppDatabase.getDatabase(this);
        title = "KotlinApp"
        val button: Button = findViewById(R.id.usingNetworkpro)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE)
        }

        button.setOnClickListener {
            //getLocation()
          /*  isGPS = false
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS()
            } else {
                getLocation()
            }*/
            if (ActivityCompat.checkSelfPermission(
                    this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationPermissionCode
                )
            } else {
                createLocationCallback()
            }

            //addLocation()
        }

        val stopService : Button = findViewById(R.id.btnStopService)
        stopService.setOnClickListener {

            stopLocationUpdates()

            AppExecutors.getInstance().diskIO().execute {
                val routeLatLngList = database?.routeLatLngDao()?.getRouteLatLngOfRoute()

                AppExecutors.getInstance().mainThread().execute {
                    if (!this@MapsActivity.getLifecycle().getCurrentState()
                            .isAtLeast(Lifecycle.State.STARTED)
                    ) {
                        return@execute
                    }

                    if (routeLatLngList == null) {
                        return@execute
                    }

                    val arrayList = ArrayList<LatLng>()
                    for (routeLatLng in routeLatLngList) {
                       latLng = LatLng(routeLatLng.lat,routeLatLng.lng)
                       //Creating an empty arraylist
                        arrayList.add(latLng!!)
                        // Position the map's  camera near Alice Springs in the center of Australia,
                        // and set the zoom factor so most of Australia shows on the screen.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(routeLatLng.lat, routeLatLng.lng), 25f))
                        //addMarker(routeLatLng.lat, routeLatLng.lng, routeLatLng.locName)
                    }

                    val polyline1 = mMap.addPolyline(
                        PolylineOptions()
                            .clickable(true)
                            .color(Color.RED)
                            .addAll(arrayList))
                   // Set listeners for click events.
                    mMap.setOnPolylineClickListener(this)
                    mMap.setOnPolygonClickListener(this)
                }
            }

            Toast.makeText(this, "Service Stopped Successfully", Toast.LENGTH_SHORT).show()
        }

        val gpsButton: Button = findViewById(R.id.usingGps)
        gpsButton.setOnClickListener {
            //getLocation()
          /*  isGPS = true
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS()
            } else {
                getLocation()
            }*/
            export();
        }
        /*// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
    }

   /* private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5f, this)
    }*/

    private fun getLocation() {
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            if (isGPS){
                var locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5f, this)
                if (locationGPS != null) {
                    latitude = locationGPS.latitude
                    longitude = locationGPS.longitude
                    //latitude = lat.toString()
                    // longitude = longi.toString()
                    //showLocation.setText("Your Location: \nLatitude: $locationGPS.latitude\nLongitude: $locationGPS.longitude")
                    tvGpsLocation = findViewById(R.id.textView)
                    // tvGpsLocation.text = "Your Location: \nLatitude: $locationGPS.latitude \nLongitude: $locationGPS.longitude";
                    tvGpsLocation.text = "Latitude: " + latitude + " , Longitude: " + longitude
                    println("Latitude: " + latitude + " , Longitude: " + longitude)
                    //  val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                    mapFragment!!.getMapAsync(this)
                } else {
                    Toast.makeText(this, "Unable to find GPS  location.", Toast.LENGTH_SHORT).show()
                }
            }else{
              var  locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 5f, this)
                if (locationGPS != null) {
                    latitude = locationGPS.latitude
                    longitude = locationGPS.longitude
                    //latitude = lat.toString()
                    //longitude = longi.toString()
                    //showLocation.setText("Your Location: \nLatitude: $locationGPS.latitude\nLongitude: $locationGPS.longitude")
                    tvGpsLocation = findViewById(R.id.textView)
                    // tvGpsLocation.text = "Your Location: \nLatitude: $locationGPS.latitude \nLongitude: $locationGPS.longitude";
                    tvGpsLocation.text = "Latitude: " + latitude + " , Longitude: " + longitude
                    println("Latitude: " + latitude + " , Longitude: " + longitude)
                    //  val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                    mapFragment!!.getMapAsync(this)
                } else {
                    Toast.makeText(this, "Unable to find Network location.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun OnGPS() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
    override fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.textView)
       // tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
    }

    private fun addLocation() {
      /*  val name: String = editTextLocationName.getText().toString().trim()
        editTextLocationName.setText("")
        if (TextUtils.isEmpty(name)) {
            ShowMessageUtils.showToast(this, "Enter bus stop name", MessageType.ERROR)
            return
        }
        val location = getmCurrentLocation()
        if (location == null) {
            ShowMessageUtils.showToast(this, "location is null", MessageType.ERROR)
            return
        }*/
        val dtStamp: String = Date().toString()
        val routeLatLng = TRouteLatLng()
        routeLatLng.locName = "One"
        routeLatLng.lat = latitude
        routeLatLng.lng = longitude
        routeLatLng.dtStamp = dtStamp
       // showProgress()
        AppExecutors.getInstance().diskIO().execute {
            var ids: LongArray = (database?.routeLatLngDao()?.insertAll(routeLatLng) ?: AppExecutors.getInstance().mainThread().execute {
                if (!this@MapsActivity.getLifecycle().getCurrentState()
                        .isAtLeast(Lifecycle.State.STARTED)
                ) {
                    return@execute
                }
                //  hideProgress()
                /*if (ids == null || ids.size == 0) {
                    showToast("Failed to insert record")
                    return@execute
                }
                val id = ids[0]
                if (id < 0) {
                    showToast("Failed to insert record")
                    return@execute
                }*/

                addMarker(latitude, longitude, "Name")
            }) as LongArray
        }

    }

    private fun addMarker(lat: Double, lng: Double, locName: String) {
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(locName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_schedule_location_marker))
        )


    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationUpdated(location: Location?) {
        updateCurrentLocMarker(location)
    }
    private fun updateCurrentLocMarker(location: Location?) {
        if (mMap == null || location == null) {
            return
        }
        val latLng = LatLng(location.latitude, location.longitude)
        if (currentLocMarker == null) {
            currentLocMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("You are here")
            )
        } else {
            currentLocMarker!!.setPosition(latLng)
        }
        val dtStamp: String = Date().toString()
        val routeLatLng = TRouteLatLng()
        routeLatLng.locName = "One"
        routeLatLng.lat = location.latitude
        routeLatLng.lng = location.longitude
        routeLatLng.dtStamp = dtStamp

        AppExecutors.getInstance().diskIO().execute {
            var ids = (database?.routeLatLngDao()?.insertLatLong(routeLatLng)
                ?: AppExecutors.getInstance().mainThread().execute {
                if (!this@MapsActivity.getLifecycle().getCurrentState()
                        .isAtLeast(Lifecycle.State.STARTED)
                ) {
                    return@execute
                }
                //  hideProgress()
                /*if (ids == null || ids.size == 0) {
                    showToast("Failed to insert record")
                    return@execute
                }
                val id = ids[0]
                if (id < 0) {
                    showToast("Failed to insert record")
                    return@execute
                }*/
                //addMarker(latitude, longitude, "Name")
            })
        }

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.


        val zoom = mMap.cameraPosition.zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude, longitude)
       // mMap.addMarker(MarkerOptions().position(sydney).title("Ratnagiri"))
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
       // val zoomLevel = 16.0f //This goes up to 21
      //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPolylineClick(polyline: Polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if (polyline.pattern == null || !polyline.pattern!!.contains(DOT)) {
            polyline.pattern = PATTERN_POLYLINE_DOTTED
        } else {
            // The default pattern is a solid stroke.
            polyline.pattern = null
        }
        Toast.makeText(this, "Route type " + polyline.tag.toString(),
            Toast.LENGTH_SHORT).show()
    }

    override fun onPolygonClick(p0: Polygon) {
        TODO("Not yet implemented")
    }

    override fun onExportSuccess() {
        Toast.makeText(this, "Database Exported Successfully.",
            Toast.LENGTH_SHORT).show()
    }

    override fun onExportFailed(s: String?) {
        Toast.makeText(this, "Error Occurred $s",
            Toast.LENGTH_SHORT).show()
    }

    override fun exportProgress(progress: Float) {
        TODO("Not yet implemented")
    }
}