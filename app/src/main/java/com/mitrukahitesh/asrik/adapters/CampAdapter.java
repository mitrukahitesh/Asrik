/*
    It is the adapter that is used with recycler view
    which renders a list of blood donation camps
 */

package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.blood.BloodCamp;
import com.mitrukahitesh.asrik.helpers.TimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CampAdapter extends RecyclerView.Adapter<CampAdapter.CustomVH> {

    private Context context;
    private final List<BloodCamp> camps;

    public CampAdapter(Context context, List<BloodCamp> camps) {
        this.context = context;
        this.camps = camps;
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
     */
    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_blood_camp, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the RecyclerView.ViewHolder.itemView
     * to reflect the item at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(camps.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return camps.size();
    }

    /**
     * Extend the abstract class RecyclerView.ViewHolder
     * to create ViewHolder objects and write custom
     * implementation
     */
    public class CustomVH extends RecyclerView.ViewHolder {

        private final TextView address, day, month, year, time;
        private final Button remind, locate;

        /**
         * Call super constructor, and
         * Set references and listeners to views
         */
        public CustomVH(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            address.setSelected(true);
            day = itemView.findViewById(R.id.day);
            month = itemView.findViewById(R.id.month);
            year = itemView.findViewById(R.id.year);
            time = itemView.findViewById(R.id.time);
            remind = itemView.findViewById(R.id.remind);
            locate = itemView.findViewById(R.id.locate);
            setListeners();
        }

        /**
         * Set listeners to views in view holder
         */
        private void setListeners() {
            remind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BloodCamp camp = camps.get(getAdapterPosition());
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.TITLE, "Blood Donation Camp");
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeZone(TimeZone.getDefault());
                    calendar1.set(camp.getYear(), camp.getMonth(), camp.getDay(), camp.getStartHour(), camp.getStartMin());
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTimeZone(TimeZone.getDefault());
                    calendar2.set(camp.getYear(), camp.getMonth(), camp.getDay(), camp.getEndhour(), camp.getEndMin());
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            calendar1.getTimeInMillis());
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                            calendar2.getTimeInMillis());
                    intent.putExtra(CalendarContract.Events.ALL_DAY, false);
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, camp.getAddress());
                    context.startActivity(intent);
                }
            });
            locate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri gmmIntentUri = Uri.parse("geo:" + camps.get(getAdapterPosition()).getLat() + ", " + camps.get(getAdapterPosition()).getLon() + "?q=" + camps.get(getAdapterPosition()).getAddress());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            context.startActivity(mapIntent);
                        }
                    });
                }
            });
        }

        /**
         * Renders blood camp data into view holder
         */
        private void setView(BloodCamp camp) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(camp.getYear(), camp.getMonth(), camp.getDay());
            address.setText(camp.getAddress());
            day.setText(String.format(Locale.getDefault(), "%d", camp.getDay()));
            month.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
            year.setText(String.format(Locale.getDefault(), "%d", camp.getYear()));
            time.setText(String.format(
                    Locale.getDefault(),
                    "%s - %s",
                    TimeFormatter.formatTime(camp.getStartHour(), camp.getStartMin()),
                    TimeFormatter.formatTime(camp.getEndhour(), camp.getEndMin())));
        }
    }
}
