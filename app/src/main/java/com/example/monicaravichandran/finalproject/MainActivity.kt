package com.example.monicaravichandran.finalproject
import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.monicaravichandran.finalproject.dummy.CrimesContent
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule

private const val PERMISSION_REQUEST = 10
//View.OnClickListener,

@TargetApi(26)
class MainActivity : AppCompatActivity(){

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    var recycled: Boolean = false
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }
    fun startLocationService(){
        val serviceClass = LocationTrackingService::class.java
        // Initialize a new Intent instance
        val intent = Intent(applicationContext, serviceClass)
        if (!isServiceRunning(serviceClass)) {
            Log.d("Service","starting service")
            startService(intent)
        } else {
            println("service running")
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                startLocationService()
        }
    }
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLatitude.text = "0.0"
        tvLongitude.text = "0.0"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                startLocationService()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            startLocationService()
        }
        Timer("SettingUp", false).schedule(1000) {
            tvLatitude.text = LocationTrackingService.latt
            tvLongitude.text = LocationTrackingService.lonn
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_1 -> {
                return true
            }
            R.id.menu_2 -> {
                //Toast.makeText(this, "Menu 2 is selected", Toast.LENGTH_SHORT).show()
                val mapsIntent = Intent(this, MapsActivity::class.java)
                startActivity(mapsIntent)

                return true
            }
            R.id.menu_3 -> {
                val itemIntent = Intent(this, ItemListActivity::class.java)
                startActivity(itemIntent)
                return true
            }
            R.id.menu_4 -> {
                val placeIntent = Intent(this,PlaceListActivity::class.java)
                startActivity(placeIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
