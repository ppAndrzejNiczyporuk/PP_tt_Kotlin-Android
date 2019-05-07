package pl.pp.tt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Adapter subclass of [RecyclerView] that renders a list of data (cities).
 */
class EventAdapter(private val mEventData: List<PPEvent>):
        RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    /**
     * Inflates a layout for the list items.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EventViewHolder(inflater.inflate(R.layout.list_item, parent, false))
    }

    /**
     * Binds each view from the ViewHolder onto the list item layout, and ultimately the
     * RecyclerView.
     */
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.mNameTV.text = mEventData[position].nazwa
        holder.mJNameTV.text = mEventData[position].jednostkaNazwa
        holder.mDataTV.text = mEventData[position].czas
    }

    override fun getItemCount(): Int {
        return mEventData.size
    }

    /**
     * RecyclerView's ViewHolder class.
     */
    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mNameTV: TextView = itemView.findViewById(R.id.name_event)
        val mDataTV: TextView = itemView.findViewById(R.id.data_event)
        val mJNameTV: TextView = itemView.findViewById(R.id.jName_event)
    }
}