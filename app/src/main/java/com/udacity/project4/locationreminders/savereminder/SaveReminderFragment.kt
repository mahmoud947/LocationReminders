package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private lateinit var mContext: Context
    var requestCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(mContext)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }


        binding.saveReminder.setOnClickListener {
            onRequestLocationPermissions()
        }
    }

    private fun addGeoFence() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(
            title = title,
            description = description,
            location = location,
            latitude = latitude,
            longitude = longitude
        )
        addGeofenceForClue(reminder = reminder)
        _viewModel.validateAndSaveReminder(reminder)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue(reminder: ReminderDataItem) {
        if (reminder.latitude != null && reminder.longitude != null) {
            val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(
                    reminder.latitude!!,
                    reminder.longitude!!,
                    Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnCompleteListener {
                it.addOnSuccessListener {
                    Toast.makeText(mContext, "added geo successfully", Toast.LENGTH_SHORT).show()
                }
                it.addOnFailureListener {
                    Toast.makeText(mContext, "Field to add geo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onRequestLocationPermissions() {
        var perm = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (mContext.isRunningQorLatter()) {
            perm += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            requestCode = REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        if (EasyPermissions.hasPermissions(mContext, *perm)) {
            requestOpenDeviceLocationSettings()
        } else {
            EasyPermissions.requestPermissions(
                this, "this app required location permission to work",
                requestCode, *perm
            )
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
                        Constants.REQUEST_TURN_DEVICE_LOCATION_ON,
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
            addGeoFence()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                addGeoFence()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
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
        if (requestCode == this.requestCode) {
            Toast.makeText(mContext, "permission Granted", Toast.LENGTH_SHORT).show()
            requestOpenDeviceLocationSettings()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == this.requestCode) {
            AppSettingsDialog.Builder(this).build().show()
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}
