package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.animation.ItemAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.FeaturedStore;
import com.clubecerto.apps.app.classes.Store;

import java.text.DecimalFormat;
import java.util.List;


public class StoreListAdapterHoriTest extends RecyclerView.Adapter<StoreListAdapterHoriTest.mViewHolder> {


    private LayoutInflater infalter;
    private List<FeaturedStore> data;
    private Context context;
    private ClickListener clickListener;
    private int lastPosition = -1;
    private boolean on_attach = true;


    public StoreListAdapterHoriTest(Context context, List<FeaturedStore> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public StoreListAdapterHoriTest.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = infalter.inflate(R.layout.item_estab_destaque, parent, false);
        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }

    @Override
    public void onBindViewHolder(StoreListAdapterHoriTest.mViewHolder holder, int position) {


        //set animation
        setAnimation(holder.itemView, position);

        if (this.data.get(position).getImage() != null) {

            if (AppConfig.APP_DEBUG) {
                Log.e("image", data.get(position).getImage());
            }


            Glide.with(context)
                    .load(this.data.get(position).getImage())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(holder.image);

        } else {
            holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));
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

    public FeaturedStore getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addItem(FeaturedStore item) {

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
        public TextView detail;
//        public TextView distance;
//        public TextView rate;
//        public RatingBar ratingBar;
//        public TextView nbrOffer;
        public TextView offer;
//        public ImageView featured;


        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.category_image);


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
