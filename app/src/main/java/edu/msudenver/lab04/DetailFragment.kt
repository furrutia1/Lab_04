package edu.msudenver.lab04

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        val textView = view.findViewById<TextView>(R.id.car_location)

        sharedViewModel.total.observe(viewLifecycleOwner) {
            sharedViewModel.coordinates.value?.let { coordinates ->
                textView.text = getString(R.string.carLocation, coordinates.latitude, coordinates.longitude)
            }
        }

        // Ensure additionalText is being updated constantly elsewhere
        // Example:
    }
}
