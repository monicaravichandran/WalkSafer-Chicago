package com.example.monicaravichandran.finalproject

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.example.monicaravichandran.finalproject.R.layout.activity_main
import com.example.monicaravichandran.finalproject.R.layout.item_list
import com.example.monicaravichandran.finalproject.dummy.CrimesContent
import com.example.monicaravichandran.finalproject.dummy.PlacesContent
import kotlinx.android.synthetic.main.activity_places.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

private const val PERMISSION_REQUEST = 10
class LocationTrackingService : Service() {
    var locationManager: LocationManager? = null
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        //val v = LayoutInflater.from(AppCompatActivity().applicationContext).inflate(R.layout.activity_main, null)
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (locationListener in locationListeners) { // <- fix
                try {
                    locationManager?.removeUpdates(locationListener)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    companion object {
        val TAG = "LocationTrackingService"
        //var locationChanged = false
        var locationChangedBool = false
        var placeChangedBool = false
        val INTERVAL = 1000.toLong() // In milliseconds
        val DISTANCE = 1.toFloat() // In meters
        lateinit var latt:String
        lateinit var lonn:String
        var changed = false
        val locationListeners = arrayOf(
                LTRLocationListener(LocationManager.GPS_PROVIDER),
                LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )
        fun locationChanged():String{
            if(changed)
                return(latt + "," + lonn)
            else
                return("NOT CHANGED")
        }
        class LTRLocationListener(provider: String):android.location.LocationListener {
            val CONNECTON_TIMEOUT_MILLISECONDS = 60000
            val lastLocation = Location(provider)
            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                Log.d("Service","LATITUDE: " + lastLocation.latitude + "\n\n")
                Log.d("Service","LONGITUDE: " + lastLocation.longitude + "\n\n")
                latt = lastLocation.latitude.toString()
                lonn = lastLocation.longitude.toString()
                CrimesContent.ITEMS.clear()
                CrimesContent.ITEM_MAP.clear()
                PlacesContent.ITEM_MAP.clear()
                PlacesContent.ITEMS.clear()
                changed = true
                locationChangedBool = true
                placeChangedBool = true
                var locationQuery = "\$where=within_circle(location," + latt + ", " + lonn + ", 500)"
                var dateQuery = getDate()
                var url = "https://data.cityofchicago.org/resource/6zsd-86xi.json?\$limit=20&" + locationQuery +
                    dateQuery + "&\$order=date DESC"

                GetCrimes().execute(url)
                // TODO: Do something here
            }
            override fun onProviderDisabled(provider: String?) {
            }
            override fun onProviderEnabled(provider: String?) {
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
            @TargetApi(26)
            fun getDate(): String{
                var now = LocalDateTime.now().toString()
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
                            //val userLoc = LatLng(mLocation!!.latitude.toDouble(),mLocation!!.longitude.toDouble())
                            val crimeObj = CrimesContent.Crime((i+1).toString(),desc,date,lat,lon,block,case)
                            CrimesContent.addItem(crimeObj)
                        }
                    } catch (ex: Exception) {
                        println("JSON parsing exception" + ex.printStackTrace())
                    }
                }

                override fun onPostExecute(result: String?) {
                    if(LocationTrackingService.latt!=null && LocationTrackingService.lonn!=null) {
                        var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                                "?location=" + LocationTrackingService.latt + "," + LocationTrackingService.lonn +
                                "&radius=3000" +
                                "&types=police" +
                                "&key=AIzaSyCXWBa2-Zuk9xmIBR3Odp23YPuYgOwS71g"
                        println("PLACES URL: " + url)
                        GetPlaces().execute(url)
                    }
                }
            }
            inner class GetPlaces : AsyncTask<String, String, String>() {

                override fun onPreExecute() {
                    // Before doInBackground
                }
                override fun doInBackground(vararg urls: String?): String {
                    var urlConnection: HttpURLConnection? = null

                    try {
                        val url = URL(urls[0])

                        urlConnection = url.openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS
                        urlConnection.readTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS

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
                        var obj = JSONObject(values[0])
                        var results = obj.getJSONArray("results")
                        for (i in 0..(results.length() - 1)) {
                            var place = results.getJSONObject(i)
                            var name = place.getString("name")
                            var place_id = place.getString("place_id")
                            var geo = place.getJSONObject("geometry")
                            var location = geo.getJSONObject("location")
                            var lat = location.getString("lat")
                            var lon = location.getString("lng")
                            val placeObj = PlacesContent.Place((i+1).toString(),name,place_id,lat,lon,"")
                            PlacesContent.addItem(placeObj)
                            println(name)
                            println(place_id)
                            //placetv.text = name
                            if(i==15)
                                break
                        }
                    } catch (ex: Exception) {
                        println("JSON parsing exception" + ex.printStackTrace())
                    }
                }

                override fun onPostExecute(result: String?) {
                    var url ="https://maps.googleapis.com/maps/api/place/details/json?"
                    var key = "&key=AIzaSyCXWBa2-Zuk9xmIBR3Odp23YPuYgOwS71g"
                    for(place in PlacesContent.ITEMS){
                        var finalurl = url + "&placeid="+place.place_id+key
                        println(finalurl)
                        GetPlaceId().execute(finalurl)
                    }
                }
            }
            inner class GetPlaceId : AsyncTask<String, String, String>() {

                override fun onPreExecute() {
                    // Before doInBackground
                }
                override fun doInBackground(vararg urls: String?): String {
                    var urlConnection: HttpURLConnection? = null

                    try {
                        val url = URL(urls[0])

                        urlConnection = url.openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS
                        urlConnection.readTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS

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
                        var obj = JSONObject(values[0])
                        var results = obj.getJSONObject("result")
                        var address = results.getString("formatted_address")
                        println("ADDRESS: "+address)
                        var place = PlacesContent.ITEMS[counter]
                        var changedPlace = PlacesContent.Place(place.id,place.name,place.place_id,place.lat,place.lon,address)
                        PlacesContent.ITEMS[counter] = changedPlace
                        PlacesContent.ITEM_MAP.remove(place.id)
                        PlacesContent.ITEM_MAP.put(place.id,changedPlace)
                        counter++
                        if(counter==15)
                            counter=0
                    } catch (ex: Exception) {
                        println("JSON parsing exception" + ex.printStackTrace())
                    }
                }

                override fun onPostExecute(result: String?) {


                    //GetDirections().execute(url)
                }
            }
            inner class GetDirections : AsyncTask<String, String, String>() {

                override fun onPreExecute() {
                    // Before doInBackground
                }
                override fun doInBackground(vararg urls: String?): String {
                    var urlConnection: HttpURLConnection? = null

                    try {
                        val url = URL(urls[0])

                        urlConnection = url.openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS
                        urlConnection.readTimeout = com.example.monicaravichandran.finalproject.CONNECTON_TIMEOUT_MILLISECONDS

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
                        println("DONE")
                    } catch (ex: Exception) {
                        println("JSON parsing exception" + ex.printStackTrace())
                    }
                }

                override fun onPostExecute(result: String?) {

                    println("in the post")
                }
            }
        }

    }
}