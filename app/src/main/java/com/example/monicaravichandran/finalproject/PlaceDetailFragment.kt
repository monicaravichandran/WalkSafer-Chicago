package com.example.monicaravichandran.finalproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.monicaravichandran.finalproject.dummy.PlacesContent
import kotlinx.android.synthetic.main.activity_place_detail.*
import kotlinx.android.synthetic.main.place_detail.*
import kotlinx.android.synthetic.main.place_detail.view.*

/**
 * A fragment representing a single Place detail screen.
 * This fragment is either contained in a [PlaceListActivity]
 * in two-pane mode (on tablets) or a [PlaceDetailActivity]
 * on handsets.
 */
class PlaceDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var mItem: PlacesContent.Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                mItem = PlacesContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                activity?.toolbar_layout?.title = mItem?.name
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.place_detail, container, false)

        // Show the dummy content as text in a TextView.
        mItem?.let {
            rootView.address.text = it.address
        }

        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mItem?.let{
            var add = it.address
        }
        direction.setOnClickListener {
            mItem?.let {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+it.address.replace(" ","+").replace(",","%2C")));
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent);
            }
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
