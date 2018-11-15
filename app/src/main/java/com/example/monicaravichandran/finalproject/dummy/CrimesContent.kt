package com.example.monicaravichandran.finalproject.dummy

import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object CrimesContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<Crime> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Crime> = HashMap()

    //private val COUNT = 25

    init {
        // Add some sample items.
        //for (i in 1..COUNT) {
           //addItem(createDummyItem(i))
        //}
    }

    fun addItem(item: Crime) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class Crime(val id: String, val description: String, val date: String,
                     val lat: String, val lon: String,val block: String,
                     val case_num:String, val userLocation: LatLng) {
        override fun toString(): String = description
    }
}
