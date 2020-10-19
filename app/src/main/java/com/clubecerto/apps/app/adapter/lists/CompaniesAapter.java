package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
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
import com.clubecerto.apps.app.classes.Companies;
import com.clubecerto.apps.app.classes.Estable;

import java.util.ArrayList;
import java.util.List;


public class CompaniesAapter extends RecyclerView.Adapter<CompaniesAapter.MyViewHolder> {
    public static Context context;
    private List<Companies> items;


    public static MyViewHolder.OnItemSkillClickListener mListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,detailsTextView,ui_counts,tv_status;
        ImageView category_image;


        public MyViewHolder(View view) {
            super(view);


            category_image=view.findViewById(R.id.category_image);

            view.setOnClickListener(this);


        }

        public interface OnItemSkillClickListener {

            void OnItemSkillClickListener(View view, int position, long id, int viewType);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                mListener.OnItemSkillClickListener(v, position, getItemId(), getItemViewType());
            }
        }

    }


    public CompaniesAapter(Context context, ArrayList<Companies> cartList, MyViewHolder.OnItemSkillClickListener listener) {
        this.context = context;
        this.items = cartList;
        mListener = listener;



    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_latest_provider, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        Companies postItems = items.get(position);




        if ( postItems.getFrente() != null) {




            Glide.with(context)
                    .load( postItems.getFrente())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(holder.category_image);

        } else {
            holder.category_image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));
        }



//
    }




    @Override
    public int getItemCount() {

        return items.size();
    }

    public void removeItem(int position) {
        items.remove(position);

        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }





}
