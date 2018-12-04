package com.example.monicaravichandran.finalproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.design.widget.Snackbar
import android.view.*
import android.widget.TextView
import com.example.monicaravichandran.finalproject.dummy.PlacesContent
import kotlinx.android.synthetic.main.activity_place_list.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.place_list_content.view.*

import kotlinx.android.synthetic.main.place_list.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [PlaceDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class PlaceListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_1 -> {
                if(LocationTrackingService.placeChangedBool) {
                    place_list.adapter.notifyDataSetChanged()
                    LocationTrackingService.placeChangedBool = false
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_2 -> {
                if(LocationTrackingService.placeChangedBool) {
                    place_list.adapter.notifyDataSetChanged()
                    LocationTrackingService.placeChangedBool = false
                }
                //Toast.makeText(this, "Menu 2 is selected", Toast.LENGTH_SHORT).show()
                val mapsIntent = Intent(this, MapsActivity::class.java)
                startActivity(mapsIntent)

                return true
            }
            R.id.menu_3 -> {
                if(LocationTrackingService.placeChangedBool) {
                    place_list.adapter.notifyDataSetChanged()
                    LocationTrackingService.placeChangedBool = false
                }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        if (place_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true
        }

        setupRecyclerView(place_list)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, PlacesContent.ITEMS, mTwoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val mParentActivity: PlaceListActivity,
                                        private val mValues: List<PlacesContent.Place>,
                                        private val mTwoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val mOnClickListener: View.OnClickListener

        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as PlacesContent.Place
                if (mTwoPane) {
                    val fragment = PlaceDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(PlaceDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    mParentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.place_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, PlaceDetailActivity::class.java).apply {
                        putExtra(PlaceDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.place_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.id
            holder.mContentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.id_text
            val mContentView: TextView = mView.content
        }
    }
}
