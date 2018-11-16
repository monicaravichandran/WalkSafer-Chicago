package com.example.monicaravichandran.finalproject

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.monicaravichandran.finalproject.dummy.CrimesContent
import com.example.monicaravichandran.finalproject.dummy.PlacesContent
import kotlinx.android.synthetic.main.activity_places.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
val CONNECTON_TIMEOUT_MILLISECONDS = 60000
var counter = 0
class PlacesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)
        if(LocationTrackingService.latt!=null && LocationTrackingService.lonn!=null){
            var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=" + LocationTrackingService.latt +"," + LocationTrackingService.lonn +
                    "&radius=3000" +
                    "&types=police" +
                    "&key=AIzaSyCXWBa2-Zuk9xmIBR3Odp23YPuYgOwS71g"
            println("PLACES URL: " +url)
            GetPlaces().execute(url)


        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_1 -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
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
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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
                    placetv.text = name
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
                if(counter==10)
                    counter=0
            } catch (ex: Exception) {
                println("JSON parsing exception" + ex.printStackTrace())
            }
        }

        override fun onPostExecute(result: String?) {

        }
    }

}
