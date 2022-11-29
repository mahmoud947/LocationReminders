package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
private const val TAG = "ReminderDescriptionActi"
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var map: GoogleMap

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        setUpMap()
        reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        binding.reminderDataItem = reminderDataItem

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        val location = LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        map.addMarker(
            MarkerOptions().position(
                location
            ).title(reminderDataItem.title)
        )
    }
    
    
    
    private fun setMapStyle(map: GoogleMap) {
        try {
            val isSuccess =
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
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


    private fun setUpMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.reminder_details_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

}
