package pl.pp.tt

import android.Manifest
import android.app.LoaderManager
import android.content.AsyncTaskLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


/**
 * This sample app displays a list of data (major cities from a particular country) that's
 * extracted from an XML response via a web service.
 */
class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<PPEvent>> {

    private var carrierTel = ""
    private var parcelNum = ""  //TODO on change off call and sms button
    private val requestReceiveSms = 2
    // Log tag constant.
    private val LOG_TAG = MainActivity::class.java.simpleName

    // Loader ID constant.
    private val LOADER_ID = 1

    override fun onResume() {
        super.onResume()
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("parcelNum")) {
                //val i = intent
                parcelNum = intent.getStringExtra("parcelNum")
                input_query.setText(parcelNum)
                ReadParcelEvents()

            }
            if (extras.containsKey("carrierTel")) {
                //val i = intent
                carrierTel = intent.getStringExtra("carrierTel")
            }
        }
        if (carrierTel.length == 0) {
            call_button.visibility = View.INVISIBLE; sms_button.visibility = View.INVISIBLE
        } else {
            call_button.visibility = View.VISIBLE; sms_button.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        // Runs UI initializations/
        init()
    }

    private fun init() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), requestReceiveSms)
        }

        // Makes the RecyclerView scroll down linearally as well as have a fixed size.
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        // Sets the adapter on the RecyclerView so its list can be populated with UI.
        recycler_view.adapter = EventAdapter(mutableListOf<PPEvent>())

        // Sets the button with the following functionality.
        search_button.setOnClickListener {
            ReadParcelEvents()
        }

        call_button.setOnClickListener {
            if (carrierTel.length > 0) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + carrierTel))
                startActivity(intent)
            }

        }

        sms_button.setOnClickListener {

            if (carrierTel.length > 0) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + carrierTel))
                intent.putExtra("sms_body", "Przesyłkę o numerze: " + parcelNum + " będę mógł odebrać po godzinie    lub jutro");
                startActivity(intent)
            }
        }

    }

    private fun ReadParcelEvents() {
        // Initializes/restarts the Loader to begin a background thread for networking purposes.
        loaderManager.restartLoader(LOADER_ID, null, this)

        // Hides the following views if they're not already for new search queries.
        empty_state_textview.visibility = View.INVISIBLE
        recycler_view.visibility = View.INVISIBLE

        // Views the progress bar for a loading UI.
        progress_bar.visibility = View.VISIBLE
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<List<PPEvent>> {
        return object : AsyncTaskLoader<List<PPEvent>>(this) {

            override fun onStartLoading() {
                super.onStartLoading()

                //Log.d(LOG_TAG, "onStartLoading()")

                // Speaks for itself - forces the Loader to load.
                forceLoad()
            }

            override fun loadInBackground(): List<PPEvent>? {
                //Log.d(LOG_TAG, "loadInBackground()")

                // Returns null and exits out of this thread immediately should the user not enter
                // any input.

                if (input_query.text.toString().isEmpty()) {
                    return null
                }
                // Log.d("Async", input_query.text.toString())
                // Returns XML response data via the following Utils object method.
                return QueryUtils.fetchEventData(input_query.text.toString())
            }
        }
    }

    override fun onLoadFinished(loader: Loader<List<PPEvent>>, responseData: List<PPEvent>?) {

        // Hides the progress bar after the background thread completes.
        progress_bar.visibility = View.GONE

        // Displays the RecyclerView of data. Otherwise, displays an empty state TextView instead.
        if (responseData != null) {
            recycler_view.visibility = View.VISIBLE
            recycler_view.adapter = EventAdapter(responseData)
        } else {
            empty_state_textview.visibility = View.VISIBLE
        }
    }

    override fun onLoaderReset(loader: Loader<List<PPEvent>>) {


        // "Clears" out the existing data since the loader resetted.
        recycler_view.adapter = EventAdapter(mutableListOf<PPEvent>())
    }
}
