package com.example.monicaravichandran.finalproject
import android.Manifest
import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.location.Location
import android.net.Uri
import com.google.android.gms.location.LocationServices
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.monicaravichandran.finalproject.dummy.PlacesContent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*


/*
THINGS TO NOTE BEFORE RUNNING:
Steps:
- run with coordinates: (lat,lon) 41.9212,-87.76588
- if random coordinates pop up (random google location) then change the accelerometer to 41.9211,-87.76588
- gpx file is provided in the directory if necessary as chicago1.gpx to test user walking
 */

private const val PERMISSION_REQUEST = 10

@TargetApi(26)
class MainActivity : AppCompatActivity(),LocationTrackingService.AddLocationListener,LocationTrackingService.AddNotificationListener{
    private var notificationManager: NotificationManager? = null
    override fun sendNotification(send: Boolean) {
        if(send) {
            println("SENDING NOTIFICATION")
            notificationHelper()
        }
    }
    fun notificationHelper(){
        val notificationID = 101
        var reroute = PlacesContent.ITEMS[0]
        println("ADDRESS: " + reroute.address)
        var resultIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+reroute.address.replace(" ","+").replace(",","%2C")));
        resultIntent.setPackage("com.google.android.apps.maps")


        val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intentCancel = Intent(this,CancelActionReceiver::class.java)
        intentCancel.setAction("CANCEL")
        intentCancel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntentCancel = PendingIntent.getBroadcast(this, 1, intentCancel, PendingIntent.FLAG_CANCEL_CURRENT)

        val channelID = "com.example.monicaravichandran.finalproject"

        val icon: Icon = Icon.createWithResource(this, android.R.drawable.ic_dialog_info)
        val action: Notification.Action =
                Notification.Action.Builder(icon, "Open", pendingIntent).build()
        //notificationBuilder.addAction(R.drawable.ic_clear_black, "Cancel", pendingIntentCancel);
        val notification = Notification.Builder(this@MainActivity,
                channelID)
                .setContentTitle("Crimes Alert:There are more than 5 crimes near you!")
                .setContentText("Click to route to the nearest police station")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setChannelId(channelID)
                .setContentIntent(pendingIntent)
                .setActions(action)
                .addAction(android.R.drawable.ic_notification_clear_all,"Cancel",pendingIntentCancel)
                .setAutoCancel(true)
                .build()

        notificationManager?.notify(notificationID, notification)

    }

    private fun createNotificationChannel(id: String, name: String,
                                          description: String) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun updateLocation(lat: String,lon:String) {
        tvLatitude.text = lat
        tvLongitude.text = lon
    }
    var isBound = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    var myService: LocationTrackingService? = null
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
        tvLongitude.text = "Getting your longitude..."
        tvLatitude.text="Getting your latitude..."
        LocationTrackingService.registerListener(this)
        LocationTrackingService.registerNotificationListener(this)
        notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(
                "com.example.monicaravichandran.finalproject",
                "Crimes in Area",
                "Crimes App")
        var permission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                permission=true
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            LocationTrackingService.prevLat = location!!.latitude
                            LocationTrackingService.prevLon = location!!.longitude
                            LocationTrackingService.prevLatForSpeed=location!!.latitude
                            LocationTrackingService.prevLonForSpeed=location!!.longitude
                            tvLongitude.text = location?.longitude.toString()
                            tvLatitude.text = location?.latitude.toString()
                        }
                startLocationService()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            permission=true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        LocationTrackingService.prevLat = location!!.latitude
                        LocationTrackingService.prevLon = location!!.longitude
                        tvLongitude.text = location?.longitude.toString()
                        tvLatitude.text = location?.latitude.toString()
                    }
            startLocationService()
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
