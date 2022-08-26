package com.app.mapsexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.app.mapsexample.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMarkerDragListener{
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMapsBinding
    private var markersCoupon:MutableList<Marker> = mutableListOf()
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private var myLocation:LatLng = LatLng(0.0,0.0)
    private var listCoupon:MutableList<Coupon> = mutableListOf()
    private var locationChange:LatLng = LatLng(0.0,0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        listCoupon = viewModel.showCoupon().toMutableList()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.change_menu,menu)
        return true
    }
    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.search_mylocation ->{
                mMap.clear()
                mMap.isMyLocationEnabled = true
                binding.search.visibility = View.GONE
                showCoupon(myLocation)
            }
            R.id.search_change_location -> {
                mMap.isMyLocationEnabled = false
                mMap.clear()
                binding.search.visibility = View.VISIBLE
                mMap.addMarker(MarkerOptions().position(myLocation).title("Вы здесь").draggable(true))
            }
        }
        return true
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
        getDeviceLocation()
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
        }
        binding.search.setOnClickListener {
            showCoupon(locationChange)
        }
        val positionDushanbe:CameraPosition = CameraPosition.Builder()
            .target(LatLng(38.5765, 68.7786))
            .zoom(15f)
            .bearing(0f)
            .tilt(45f)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(positionDushanbe))
        mMap.setOnMarkerDragListener(this)
    }
    private fun showCoupon(location: LatLng) {
        for (i in listCoupon){
            val pos = LatLng(i.latitube,i.longitube)
            val example = LatLng(i.latitube, i.longitube)
            val distance = SphericalUtil.computeDistanceBetween(location,example)
            if (distance.toInt()/1000 < 3){
                markersCoupon.add(mMap.addMarker(MarkerOptions().position(pos).title(i.coupon))!!)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val radius = task.result
                    myLocation = LatLng(radius.latitude,radius.longitude)
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    @SuppressLint("MissingPermission")
    private fun checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //mMap.isMyLocationEnabled = true
            Toast.makeText(this,"Already Enabled",Toast.LENGTH_SHORT).show()
        }else{
            requestPermission()
        }
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }
    @SuppressLint("MissingPermission",)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 1){
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"Granted!",Toast.LENGTH_SHORT).show()
            mMap.isMyLocationEnabled = true
        }else{
            Toast.makeText(this,"Granted!",Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMarkerDrag(p0: Marker) {
    }
    override fun onMarkerDragEnd(p0: Marker) {
    }
    override fun onMarkerDragStart(p0: Marker) {
        locationChange = LatLng(p0.position.latitude,p0.position.longitude)
        for (mark in markersCoupon){
            mark.remove()
        }
        markersCoupon.clear()
    }
}