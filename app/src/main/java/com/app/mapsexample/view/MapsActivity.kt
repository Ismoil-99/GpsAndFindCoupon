package com.app.mapsexample.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.mapsexample.DESCCOUPON
import com.app.mapsexample.DISTANCE
import com.app.mapsexample.model.Coupon
import com.app.mapsexample.viewmodel.MainViewModel
import com.app.mapsexample.R

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.app.mapsexample.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMarkerDragListener,GoogleMap.OnMarkerClickListener{
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMapsBinding
    private var markersCoupon:MutableList<Marker> = mutableListOf()
    private lateinit var customDialog: Dialog
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private var myLocation:LatLng = LatLng(0.0,0.0)
    private var listCoupon:MutableList<Coupon> = mutableListOf()
    private var locationChange:LatLng = LatLng(0.0,0.0)
    private var countMarker:Int = 0
    private var distanceLocation:LatLng = LatLng(0.0,0.0)

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
        customDialog = LoadingDialog(this)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.change_menu,menu)
        return true
    }
    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.search_mylocation ->{
                lifecycleScope.launch(Dispatchers.Main) {
                    customDialog.show()
                    mMap.clear()
                    mMap.isMyLocationEnabled = true
                    binding.search.visibility = View.GONE
                    showCoupon(myLocation)
                    distanceLocation = myLocation
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14f))
                    delay(2000)
                    customDialog.dismiss()
                }
            }
            R.id.search_change_location -> {
                lifecycleScope.launch{
                    customDialog.show()
                    mMap.isMyLocationEnabled = false
                    mMap.clear()
                    binding.search.visibility = View.VISIBLE
                    mMap.addMarker(
                        MarkerOptions().position(myLocation).title("Вы здесь").draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation))
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14f))
                    delay(2000)
                    customDialog.dismiss()
                }
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
            lifecycleScope.launch {
                customDialog.show()
                showCoupon(locationChange)
                delay(1000)
                distanceLocation = locationChange
                customDialog.dismiss()
            }

        }
        val positionDushanbe:CameraPosition = CameraPosition.Builder()
            .target(LatLng(38.5513, 68.7671))
            .zoom(12f)
            .bearing(0f)
            .tilt(45f)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(positionDushanbe))
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMarkerClickListener(this)
    }
    private fun showCoupon(location: LatLng) {
        for (i in listCoupon){
            val position = LatLng(i.latitube,i.longitube)
            val distance = SphericalUtil.computeDistanceBetween(location,position)
            if (distance.toFloat()/1000 < 3.0000){
                markersCoupon.add(countMarker,
                    mMap.addMarker(MarkerOptions().position(position).title(i.coupon).icon(getBitmapDescriptorFromVector(this,R.drawable.ic_coupon_svgrepo_com)))!!
                )
                countMarker++
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
            mMap.isMyLocationEnabled = true
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
            mMap.isMyLocationEnabled = true
        }else{
            Toast.makeText(this,"Granted!",Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMarkerDrag(p0: Marker) {
    }
    override fun onMarkerDragEnd(p0: Marker) {
        locationChange = LatLng(p0.position.latitude,p0.position.longitude)
    }
    override fun onMarkerDragStart(p0: Marker) {
        for (mark in markersCoupon){
            mark.remove()
        }
        markersCoupon.clear()
        countMarker = 0
    }
    private fun getBitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val distance:Double = SphericalUtil.computeDistanceBetween(distanceLocation,p0.position)
        showInfoCoupon(distance,p0.title)
        return true
    }

    private fun showInfoCoupon(distance: Double, titleCoupon: String?) {
        var getDistance = 0000.00f
        var getTitle:String? = null
        if (distance/ 1000 < 3){
            getDistance = distance.toFloat()
            getTitle = titleCoupon
        }
        val bundle = Bundle()
        val bottomSheet = BottomSheetShowCoupon()
        bundle.putFloat(DISTANCE,getDistance)
        bundle.putString(DESCCOUPON,getTitle)
        bottomSheet.arguments = bundle
        bottomSheet.show(
            this.supportFragmentManager,
            bottomSheet.tag
        )
    }
}