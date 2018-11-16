package com.example.monicaravichandran.finalproject.dummy

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by monicaravichandran on 11/15/18.
 */
object PlacesContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<Place> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Place> = HashMap()

    //private val COUNT = 25

    init {
        // Add some sample items.
        //for (i in 1..COUNT) {
        //addItem(createDummyItem(i))
        //}
    }

    fun addItem(item: Place) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class Place(val id: String, val name: String, val place_id: String,
                     val lat: String, val lon: String,val address: String) {
        override fun toString(): String = name
    }
}
