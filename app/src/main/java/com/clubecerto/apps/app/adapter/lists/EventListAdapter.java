package com.clubecerto.apps.app.adapter.lists;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.animation.ItemAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Event;
import com.clubecerto.apps.app.utils.DateUtils;

import java.util.List;


public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Event> data;
    private Context context;
    private ClickListener clickListener;
    private int lastPosition = -1;
    private boolean on_attach = true;


    public EventListAdapter(Context context, List<Event> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public EventListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = infalter.inflate(R.layout.item_event_custom, parent, false);

        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(EventListAdapter.mViewHolder holder, int position) {

        setAnimation(holder.itemView, position);


        int size = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

        if (Configuration.SCREENLAYOUT_SIZE_XLARGE == size) {
            holder.image.getLayoutParams().height = (int) (MainActivity.width / 2.5);
        } else {
            holder.image.getLayoutParams().height = (int) (MainActivity.width / 2.1);
        }


        if (this.data.get(position).getListImages() != null && data.get(position).getListImages().size() > 0) {

            if (AppConfig.APP_DEBUG) {
                Log.e("image", data.get(position).getListImages()
                        .get(0).getUrl500_500());

            }

            Glide.with(context)
                    .load(this.data.get(position).getListImages()
                            .get(0).getUrl500_500())
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(holder.image);

        } else {
            Glide.with(context).load(R.drawable.def_logo).into(holder.image);
        }

        if (this.data.get(position).getListImages() == null)
            if (data.get(position).getType() == 1 && data.get(position).getType() == 2) {
                holder.image.setImageResource(R.drawable.def_logo);
            } else if (data.get(position).getType() == 3) {
                holder.image.setImageResource(R.drawable.def_logo);
            }


        if (this.data.get(position).getListImages().size() > 0) {

            Glide.with(context)
                    .load(this.data.get(position).getListImages().get(0).getUrl500_500())
                    .into(holder.image);
        }

        if (DateUtils.isLessThan24(this.data.get(position).getDateB(), null)) {
            holder.upcoming.setVisibility(View.VISIBLE);
        } else {
            holder.upcoming.setVisibility(View.GONE);
        }


        holder.name.setText(data.get(position).getName());


        String mDateEvent = String.format(context.getString(R.string.FromTo),
                DateUtils.getDateByTimeZone(data.get(position).getDateB(), "dd MMMM yyyy") + "",
                DateUtils.getDateByTimeZone(data.get(position).getDateE(), "dd MMMM yyyy")
        );

        holder.address.setText(Html.fromHtml(mDateEvent));

        String symbole = com.clubecerto.apps.app.utils.Utils.getDistanceBy(
                data.get(position).getDistance()
        );
        if (data.get(position).getDistance() != null) {
            String distanceFormated = "N/A";
            if (data.get(position).getDistance() > 0) {
                String distance = com.clubecerto.apps.app.utils.Utils.preparDistance(
                        data.get(position).getDistance()
                );
                distanceFormated = context.getString(R.string.eventIn) + " " + distance + " " + symbole.toLowerCase();
            }
            holder.distance.setText(distanceFormated);
        }


        //Utils.setFont(.+);


        if (data.get(position).getFeatured() == 0) {
            holder.featured.setVisibility(View.GONE);
        } else {
            holder.featured.setVisibility(View.VISIBLE);
        }

    }

    public void removeAll() {
        int size = this.data.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.data.remove(0);
            }

            if (size > 0)
                this.notifyItemRangeRemoved(0, size);


        }


    }

    public Event getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addItem(Event item) {

        int index = (data.size());
        data.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clicklistener) {

        this.clickListener = clicklistener;

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, ItemAnimation.FADE_IN);
            lastPosition = position;
        }

    }
    public interface ClickListener {
        void itemClicked(View view, int position);
    }

    public class mViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public ImageView image;
        public TextView name;
        public TextView address;
        public TextView distance;
        //public ImageView location;
        public TextView featured;
        public TextView upcoming;


        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            //location = (ImageView) itemView.findViewById(R.id.location);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            distance = itemView.findViewById(R.id.distance);
            featured = itemView.findViewById(R.id.featured);
            upcoming = itemView.findViewById(R.id.upcoming);


            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {


            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

            //delete(getPosition());


        }
    }

}
