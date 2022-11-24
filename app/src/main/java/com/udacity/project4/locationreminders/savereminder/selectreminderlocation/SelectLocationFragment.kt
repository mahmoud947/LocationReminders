package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
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

        setUpMap()

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

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
        enableCurrentLocation(map)
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
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .snippet(snippet)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            ).showInfoWindow()
            selectedLocation = it
            selectedLocationDescription = snippet
        }

    }

    private fun setOnPioClick(map: GoogleMap) {
        map.clear()
        map.setOnPoiClickListener {
            map.clear()
            val poiMarker: Marker? =
                map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            poiMarker?.showInfoWindow()
            selectedLocation = it.latLng
            selectedLocationDescription = poiMarker?.title.toString()
        }
    }


    private fun enableCurrentLocation(map: GoogleMap) {
        if (requireContext().isAccessFineLocationPermissionGranted()) {
            getCurrentLocation(map)
        } else {
            onRequestPermissions()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(map: GoogleMap) {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                map.isMyLocationEnabled = true
                val location = task.result

                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    selectedLocation = currentLocation
                    val snippet = String.format(
                        Locale.getDefault(),
                        "Lat:%1$.8f, Long: %2$.8f",
                        location.latitude,
                        location.longitude
                    )
                    selectedLocationDescription=snippet
                    map.clear()
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                    map.addMarker(
                        MarkerOptions()
                            .position(currentLocation)
                            .snippet(snippet)
                            .title(getString(R.string.dropped_pin))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    ).showInfoWindow()
                } else {
                    map.clear()
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    map.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(selectedLocation, 18f)
                    )
                    map.isMyLocationEnabled = false

                    val snippet = String.format(
                        Locale.getDefault(),
                        "Lat:%1$.8f, Long: %2$.8f",
                        selectedLocation.latitude,
                        selectedLocation.longitude
                    )
                    selectedLocationDescription=snippet
                    map.clear()
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 18f))
                    map.addMarker(
                        MarkerOptions()
                            .position(selectedLocation)
                            .snippet(snippet)
                            .title(getString(R.string.dropped_pin))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    ).showInfoWindow()
                }
            } else {
                Log.d(TAG, "Current location is null. Using defaults.")
                Log.e(TAG, "Exception: %s", task.exception)
                map.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(selectedLocation, 18f)
                )
                map.isMyLocationEnabled = false
            }
        }
    }


    private fun onRequestPermissions() {
        val perm = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (EasyPermissions.hasPermissions(requireContext(), *perm)) {

        } else {
            EasyPermissions.requestPermissions(this, "", REQUEST_LOCATION_PERMISSION, *perm)

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
        if (requestCode == REQUEST_LOCATION_PERMISSION && perms.isNotEmpty()) {
            requestOpenDeviceLocationSettings()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
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
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
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


        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                getCurrentLocation(map)
                Toast.makeText(requireContext(), "isSucceful", Toast.LENGTH_LONG).show()
            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            Toast.makeText(requireContext(), "isSucceful", Toast.LENGTH_LONG).show()
        }
    }
}


const val REQUEST_LOCATION_PERMISSION = 1
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

