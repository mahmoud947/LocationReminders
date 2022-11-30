package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.utils.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.isAccessFineLocationPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


private const val TAG = "SelectLocationFragment"

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedLocation = LatLng(30.044437, 31.235701) // cairo
    private var selectedLocationDescription = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)



        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        setUpMap()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveBtn.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun setUpMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(binding.mapFragment.id) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun onLocationSelected() {

        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        if (selectedLocationDescription.isNotEmpty()) {
            _viewModel.latitude.value = selectedLocation.latitude
            _viewModel.longitude.value = selectedLocation.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedLocationDescription

            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            Toast.makeText(requireContext(), getString(R.string.select_poi), Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // map setup

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        setOnPioClick(map)
        setOnMapLongClick(map)
        enableCurrentLocation()
        setOnMyLocationButtonClick(map)
    }



    private fun addMarker(map: GoogleMap, snippet: String?, latLng: LatLng) {
        map.clear()
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .snippet(snippet)
                .title(getString(R.string.dropped_pin))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
        ).showInfoWindow()
        map.addCircle(
            CircleOptions().center(latLng).radius(Constants.GEOFENCE_RADIUS_IN_METERS.toDouble())
                .fillColor(R.color.colorAccent)
                .strokeColor(R.color.colorAccent)
        )

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val isSuccess =
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (!isSuccess) {
                Log.d(TAG, "setMapStyle: failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d(TAG, "Can't find style. Error: $e")
        }
    }

    private fun setOnMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat:%1$.8f, Long: %2$.8f",
                it.latitude,
                it.longitude
            )

            addMarker(map, snippet, it)
            selectedLocation = it
            selectedLocationDescription = snippet
        }
    }


    private fun setOnPioClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            map.clear()
            val poiMarker: Marker? =
                map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            poiMarker?.showInfoWindow()
        }
    }

    private fun setOnMyLocationButtonClick(googleMap: GoogleMap){
        googleMap.setOnMyLocationButtonClickListener {
            enableCurrentLocation()
            true
        }
    }


    private fun enableCurrentLocation() {
        if (requireContext().isAccessFineLocationPermissionGranted()) {
            requestOpenDeviceLocationSettings()
        } else {
            onRequestPermissions()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(map: GoogleMap) {
        _viewModel.getCurrantLocation(fusedLocationProviderClient)
        _viewModel.location.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it == null) {
                onRequestPermissions()
            } else {
                map.isMyLocationEnabled = true
                val currentLocation = LatLng(it.latitude, it.longitude)
                selectedLocation = currentLocation
                val snippet = String.format(
                    Locale.getDefault(),
                    "Lat:%1$.8f, Long: %2$.8f",
                    it.latitude,
                    it.longitude
                )
                selectedLocationDescription = snippet

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                addMarker(map, snippet, currentLocation)
            }
        })

    }


    private fun onRequestPermissions() {
        val perm = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (EasyPermissions.hasPermissions(requireContext(), *perm)) {
            requestOpenDeviceLocationSettings()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "this app required location permission to work",
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE,
                *perm
            )
        }
    }


    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation(map)
            }else{
                map.isMyLocationEnabled =true
            }

        }
    }

    private fun requestOpenDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest =
            LocationRequest.create().apply { priority = LocationRequest.PRIORITY_LOW_POWER }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "checkDeviceLocationSettingsAndStartGeofence: ${sendEx.message}")
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location services must be enabled for this app",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            getCurrentLocation(map)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            requestOpenDeviceLocationSettings()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            AppSettingsDialog.Builder(this).build().show()
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}



