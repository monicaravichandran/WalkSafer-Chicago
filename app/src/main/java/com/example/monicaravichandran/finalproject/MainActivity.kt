package com.example.monicaravichandran.finalproject
import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
private const val PERMISSION_REQUEST = 10
//View.OnClickListener,
@TargetApi(26)
class MainActivity : AppCompatActivity(),View.OnClickListener,com.google.android.gms.location.LocationListener {
    lateinit var latitude: EditText
    lateinit var longitude: EditText
    val CONNECTON_TIMEOUT_MILLISECONDS = 60000
    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }

    override fun onClick(v: View?) {
        if (!checkGPSEnabled()) {
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            getLocation();
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            tvLatitude.text = mLocation!!.latitude.toString()
            tvLongitude.text = mLocation!!.longitude.toString()
            var lat = mLocation!!.latitude.toString()
            var lon = mLocation!!.longitude.toString()
            var locationQuery = "\$where=within_circle(location," + lat + ", " + lon + ", 500)"

            //val currentTime =
            //println(currentTime)
            var dateQuery = getDate()
            var url = "https://data.cityofchicago.org/resource/6zsd-86xi.json?\$limit=20&" + locationQuery +
                    dateQuery + "&\$order=date DESC"
            GetCrimes().execute(url)
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }
    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btFetchLocation.setOnClickListener(this)
        buildGoogleApiClient()

        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
        var strLat:String = ""
        var strLon:String = ""
        latitude.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                strLat = latitude.getText().toString()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} })
        longitude.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                strLon = longitude.getText().toString()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} })


        /*EnterButton.setOnClickListener{
            var lat = strLat
            var lon = strLon
            var locationQuery = "\$where=within_circle(location," + lon + ", " + lat + ", 500)"

            //val currentTime =
            //println(currentTime)
            var dateQuery = getDate()
            var url = "https://data.cityofchicago.org/resource/6zsd-86xi.json?" + locationQuery +
                    dateQuery + "&\$order=date DESC"
            GetCrimes().execute(url)
        }*/

    }
    private fun buildGoogleApiClient() : Boolean {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient!!.connect()
        return true
    }
    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }
    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }
    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()
            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }
    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
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
                Toast.makeText(this, "Menu 4 is selected", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun getDate(): String{
        var now =LocalDateTime.now().toString()
        //$where=date between '2015-01-10T12:00:00' and '2015-01-10T14:00:00'
        println("TIME: " + now.toString())
        now = now.substring(0,now.length-1)
        var past = "'" + getPast(now) + "'"
        var current = "'" + now + "'"
        var dateQuery = "AND date between " + past + " and " + current
        println("DATE QUERY: " + dateQuery)
        return dateQuery
    }
    fun getPast(current : String):String{
        var index = current.indexOf('T')
        var currDate = current.substring(0,index)
        var time = current.substring(index)
        index = time.indexOf('.')
        time = time.substring(0,index)
        var currDateList = currDate.split("-")
        var year = currDateList[0]
        var day = currDateList[2]
        var month = currDateList[1].toInt()
        if(month==1){
            month = 12
        }
        else{
            month = month-1
        }
        var strMonth = ""
        if(month<10){
           strMonth = "0" + month.toString()
        }
        else{
            strMonth = month.toString()
        }
        //'2015-01-10T12:00:00'
        var past = year + "-" + strMonth + "-" + day + time
        println("PAST: " + past)
        return past
    }
    inner class GetCrimes : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            // Before doInBackground
        }

        override fun doInBackground(vararg urls: String?): String {
            var urlConnection: HttpURLConnection? = null

            try {
                val url = URL(urls[0])

                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = CONNECTON_TIMEOUT_MILLISECONDS
                urlConnection.readTimeout = CONNECTON_TIMEOUT_MILLISECONDS

                //var inString = streamToString(urlConnection.inputStream)

                // replaces need for streamToString()
                val inString = urlConnection.inputStream.bufferedReader().readText()

                publishProgress(inString)
            } catch (ex: Exception) {
                println("HttpURLConnection exception" + ex)
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect()
                }
            }

            return " "
        }
        override fun onProgressUpdate(vararg values: String?) {
            try {
                var crimes = JSONArray(values[0])
                println("here")
                for (i in 0..(crimes.length() - 1)) {
                    val crime = crimes.getJSONObject(i)
                    val desc = crime.getString("description")
                    val date = crime.getString("date")
                    val lat = crime.getString("latitude")
                    val lon = crime.getString("longitude")
                    val block = crime.getString("block")
                    val case = crime.getString("case_number")
                    println("Crime: " + desc + "\nDate: " + date)
                    val userLoc = LatLng(mLocation!!.latitude.toDouble(),mLocation!!.longitude.toDouble())
                    val crimeObj = CrimesContent.Crime((i+1).toString(),desc,date,lat,lon,block,case,userLoc)
                    CrimesContent.addItem(crimeObj)
                }
            } catch (ex: Exception) {
                println("JSON parsing exception" + ex.printStackTrace())
            }
        }

        override fun onPostExecute(result: String?) {
            // Done
        }
    }



}
