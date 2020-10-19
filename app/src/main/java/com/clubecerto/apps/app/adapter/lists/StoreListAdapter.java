package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.animation.ItemAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.utils.Utils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.DecimalFormat;
import java.util.List;


public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Store> data;
    private Context context;
    private ClickListener clickListener;
    private int lastPosition = -1;
    private boolean on_attach = true;


    public StoreListAdapter(Context context, List<Store> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public StoreListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = infalter.inflate(R.layout.fragment_store_custom_item, parent, false);
        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }

    @Override
    public void onBindViewHolder(StoreListAdapter.mViewHolder holder, int position) {


        //set animation
        setAnimation(holder.itemView, position);

        if (this.data.get(position).getImages() != null) {

            if (AppConfig.APP_DEBUG) {
                Log.e("image", data.get(position).getImages()
                        .getUrl200_200());
            }


            Glide.with(context)
                    .load(this.data.get(position).getImages().getUrl500_500())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(holder.image);

        } else {
            holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));
        }


        int size = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

        if (Configuration.SCREENLAYOUT_SIZE_XLARGE == size) {
            holder.image.getLayoutParams().height = (int) (MainActivity.width / 3.5);
        } else {
            holder.image.getLayoutParams().height = (int) (MainActivity.width / 2.8);
        }


        if (data.get(position).getDistance() >  0 ) {

            if (this.data.get(position).getLatitude() != 0 && this.data.get(position).getLongitude() != 0
                    && Utils.isNearMAXDistance(this.data.get(position).getDistance())) {
                holder.distance.setText(
                        Utils.preparDistance(this.data.get(position).getDistance())
                                + " " +
                                Utils.getDistanceBy(this.data.get(position).getDistance())
                );
            } else if (!Utils.isNearMAXDistance(this.data.get(position).getDistance())) {
                holder.distance.setText("+100 KM");
            } else {
                holder.distance.setVisibility(View.GONE);
            }

            holder.distance.setText(holder.distance.getText().toString().toLowerCase());

        } else {

            holder.distance.setVisibility(View.GONE);

        }



            /*if(data.get(position).getVotes() == 0)
            {
                holder.comment.setVisibility(View.GONE);
            }*/


        float rated = (float) data.get(position).getVotes();
        DecimalFormat decim = new DecimalFormat("#.##");


        holder.rate.setText(decim.format(rated) + "  (" + data.get(position).getNbr_votes() + ")");

        holder.ratingBar.setRating(rated);

        holder.name.setText(data.get(position).getName());
        holder.address.setText(data.get(position).getAddress());


        if (data.get(position).getNbrOffers() == 1) {
            holder.nbrOffer.setText(context.getString(R.string.oneOffer));
            holder.nbrOffer.setVisibility(View.VISIBLE);
        } else if (data.get(position).getNbrOffers() > 1) {
            String offers = String.format(context.getString(R.string.nbrOffers), data.get(position).getNbrOffers());
            holder.nbrOffer.setText(offers);
            holder.nbrOffer.setVisibility(View.VISIBLE);
        }


        if (data.get(position).getNbrOffers() == 0)
            holder.nbrOffer.setVisibility(View.GONE);


        if (data.get(position).getLastOffer().equals("")) {
            holder.offer.setVisibility(View.GONE);
        } else {
            holder.offer.setVisibility(View.VISIBLE);
            holder.offer.setText(data.get(position).getLastOffer());
        }


        if (data.get(position).getFeatured() == 0) {
            holder.featured.setVisibility(View.GONE);
        } else {
            holder.featured.setVisibility(View.VISIBLE);
        }


        Drawable locationDrawable = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_map_marker)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorGrayDefault, null))
                .sizeDp(12);

        holder.address.setCompoundDrawablePadding(10);
        holder.address.setCompoundDrawables(locationDrawable, null, null, null);

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

    public Store getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addItem(Store item) {

        int index = (data.size());
        data.add(item);
        notifyDataSetChanged();
        //notifyItemInserted(index);
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
        public TextView rate;
        public RatingBar ratingBar;
        public TextView nbrOffer;
        public TextView offer;
        public ImageView featured;


        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            rate = itemView.findViewById(R.id.rate);
            distance = itemView.findViewById(R.id.distance);
            ratingBar = itemView.findViewById(R.id.ratingBar2);
            nbrOffer = itemView.findViewById(R.id.offers);
            offer = itemView.findViewById(R.id.offer);
            featured = itemView.findViewById(R.id.featured);

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
