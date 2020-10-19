package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.animation.ItemAnimation;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.utils.OfferUtils;
import com.clubecerto.apps.app.utils.Utils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Offer> data;
    private Context context;
    private ClickListener clickListener;
    private boolean isHorizontalList = false;
    private int width = 0, height = 0;
    private int lastPosition = -1;
    private boolean on_attach = true;

    public OfferListAdapter(Context context, List<Offer> data, boolean isHorizontalList) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
    }


    public OfferListAdapter(Context context, List<Offer> data, boolean isHorizontalList, int width, int height) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
        this.width = width;
        this.height = height;
    }

    @Override
    public OfferListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = null;
        if (isHorizontalList) rootView = infalter.inflate(R.layout.v2_item_offer, parent, false);
        else rootView = infalter.inflate(R.layout.fragment_offer_custom_item, parent, false);


        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }

    @Override
    public void onBindViewHolder(OfferListAdapter.mViewHolder holder, int position) {

        setAnimation(holder.itemView, position);

        if (!isHorizontalList) {
            int size = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

            if (Configuration.SCREENLAYOUT_SIZE_XLARGE == size) {
                holder.image.getLayoutParams().height = (int) (MainActivity.width / 3);
            } else {
                holder.image.getLayoutParams().height = (int) (MainActivity.width /2);
            }
        }


        if (height > 0 && width > 0) {
            //set set the dp dimention
            int dp1 = Utils.dip2pix(context, 1);
            CardView.LayoutParams params = new CardView.LayoutParams(width * dp1, height * dp1);
            params.setMargins((5 * dp1), (5 * dp1), (5 * dp1), (5 * dp1));
            holder.itemView.setLayoutParams(params);
        }


        if (data.get(position).getCurrency() != null) {

            if (data.get(position).getValue_type() != null && !data.get(position).getValue_type().equals("")) {
                if (data.get(position).getValue_type().equalsIgnoreCase("Percent") && (data.get(position).getOffer_value() > 0 || data.get(position).getOffer_value() < 0)) {
                    DecimalFormat decimalFormat = new DecimalFormat("#0");
                    holder.offer.setText(decimalFormat.format(data.get(position).getOffer_value()) + "%");
                } else {
                    if (data.get(position).getValue_type().equalsIgnoreCase("Price") && data.get(position).getOffer_value() != 0) {

                        holder.offer.setText(OfferUtils.parseCurrencyFormat(
                                data.get(position).getOffer_value(),
                                data.get(position).getCurrency()));

                    } else {
                        holder.offer.setText(context.getString(R.string.promo));
                    }
                }
            }

        } else if (data.get(position).getValue_type().equalsIgnoreCase("unspecifie")) {
            holder.offer.setText(context.getString(R.string.promo));
        } else {
            Glide.with(context).load(R.drawable.def_logo).into(holder.image);
        }


        if (!isHorizontalList && data.get(position).getIs_deal() == 1) {
            String diff_end_offer = prepareCountDownView(data.get(position));
            if (diff_end_offer != "") {
                holder.deal_cd.setText(R.string.deal);
                //holder.deal_cd.setText(diff_end_offer);
                holder.deal_cd.setVisibility(View.VISIBLE);
            } else {
                holder.deal_cd.setVisibility(View.GONE);
            }

        } else {
            holder.deal_cd.setVisibility(View.GONE);
        }

        /*if (data.get(position).getDistance() != null) {
            String distanceFormated = "N/A";
            if (data.get(position).getDistance() > 0) {

                String symbole = com.droideve.apps.dealify.utils.Utils.getDistanceBy(
                        data.get(position).getDistance()
                );
                String distance = com.droideve.apps.dealify.utils.Utils.preparDistance(
                        data.get(position).getDistance()
                );


                distanceFormated = String.format(context.getString(R.string.offerIn), distance + " " + symbole.toUpperCase());
            }
            holder.deal_cd.setText(String.format(distanceFormated));
        }*/

        holder.name.setText(data.get(position).getName());
        holder.description.setText(data.get(position).getStore_name());

        Drawable locationDrawable = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_map_marker)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.white, null))
                .sizeDp(12);

        if (isHorizontalList) {
            holder.description.setCompoundDrawables(null, null, null, null);
        } else {
            if (!AppController.isRTL())
                holder.description.setCompoundDrawables(locationDrawable, null, null, null);
            else
                holder.description.setCompoundDrawables(null, null, locationDrawable, null);

        }

        holder.description.setCompoundDrawablePadding(14);

        if (data.get(position).getImages() != null) {
            Glide.with(context).load(data.get(position).getImages().getUrl500_500())
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .centerCrop().into(holder.image);
        } else {

            Glide.with(context).load(R.drawable.def_logo)
                    .into(holder.image);
        }


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

    public Offer getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addItem(Offer item) {
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

    private String prepareCountDownView(Offer mOffer) {


        String result = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date event_date = dateFormat.parse(mOffer.getDate_end());
            Date current_date = new Date();
            if (!current_date.after(event_date)) {
                long diff = event_date.getTime() - current_date.getTime();
                long Days = diff / (24 * 60 * 60 * 1000);
                long Hours = diff / (60 * 60 * 1000) % 24;
                long Minutes = diff / (60 * 1000) % 60;
                long Seconds = diff / 1000 % 60;

                result = String.format("%02d", Days) + ":" +
                        String.format("%02d", Hours) + ":" +
                        String.format("%02d", Minutes) + ":" +
                        String.format("%02d", Seconds);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;

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
        public TextView description;
        public TextView deal_cd;
        public TextView offer;
        public ImageView featured;


        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.address);
            deal_cd = itemView.findViewById(R.id.deals);
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
