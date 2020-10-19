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
import com.clubecerto.apps.app.classes.Estable;
import com.clubecerto.apps.app.classes.Store;


import java.util.ArrayList;
import java.util.List;



public class EstablisAapter extends RecyclerView.Adapter<EstablisAapter.MyViewHolder> {
    public static Context context;
    private List<Estable> items;


    public static MyViewHolder.OnItemSkillClickListener mListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,detailsTextView,ui_counts,tv_status;
        ImageView category_image;


        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.name);
            detailsTextView = (TextView) view.findViewById(R.id.detail);
            ui_counts = (TextView) view.findViewById(R.id.price);

            category_image=view.findViewById(R.id.image);

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


    public EstablisAapter(Context context, ArrayList<Estable> cartList, MyViewHolder.OnItemSkillClickListener listener) {
        this.context = context;
        this.items = cartList;
        mListener = listener;



    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_offer, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        Estable postItems = items.get(position);


        holder.title.setText(postItems.getNome());
        holder.detailsTextView.setText(postItems.getCategoriaNome());
        holder.ui_counts.setText( postItems.getBeneficio() );
        if ( postItems.getMarca() != null) {




            Glide.with(context)
                    .load( postItems.getMarca())
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
    public void addItem(Estable item) {

        int index = (items.size());
        items.add(item);
        notifyDataSetChanged();
        //notifyItemInserted(index);
    }
    public void removeAll() {
        int size = this.items.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.items.remove(0);
            }

            if (size > 0)
                this.notifyItemRangeRemoved(0, size);


        }


    }



}
