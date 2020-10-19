package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ItemAnimation;
import com.clubecerto.apps.app.classes.Category;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;


public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Category> data;
    private Context context;
    private ClickListener clickListener;
    private boolean rectCategoryView = false;
    private Map<String, Object> optionalParams;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int lastPosition = -1;
    private boolean on_attach = true;

    public CategoriesListAdapter(Context context, List<Category> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;

    }


    public CategoriesListAdapter(Context context, List<Category> data, boolean rectCategoryView, Map<String, Object> optionalParams) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.rectCategoryView = rectCategoryView;
        this.optionalParams = optionalParams;
    }

    @Override
    public CategoriesListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = null;
        if (rectCategoryView) {
            rootView = infalter.inflate(R.layout.item_category_rect, parent, false);
        } else {
            rootView = infalter.inflate(R.layout.item_category, parent, false);
        }

        // TypefaceHelper.typeface(rootView);
        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }

    @Override
    public void onBindViewHolder(final CategoriesListAdapter.mViewHolder holder, final int position) {


        //set animation
        setAnimation(holder.itemView, position);


        if (optionalParams != null && optionalParams.containsKey("displayCatTitle") && !((Boolean) optionalParams.get("displayCatTitle"))) {
            holder.name.setVisibility(View.GONE);
        } else {
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(data.get(position).getNameCat());
        }


        if (data.get(position).getImages() != null) {
            Glide.with(context).load(data.get(position).getImages().getUrl500_500())
                    .placeholder(R.drawable.def_logo)
                    .centerCrop().into(holder.image);
        } else {
            holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));
        }


        if (optionalParams != null && optionalParams.containsKey("displayStoreNumber") && !((Boolean) optionalParams.get("displayStoreNumber"))) {
            holder.stores.setVisibility(View.GONE);
        } else {

            Drawable storeDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_map_marker)
                    .color(ResourcesCompat.getColor(context.getResources(), R.color.white, null))
                    .sizeDp(12);


            if (AppController.isRTL()) {
                holder.stores.setCompoundDrawables(null, null, storeDrawable, null);
                holder.stores.setCompoundDrawablePadding(10);
            } else {
                holder.stores.setCompoundDrawables(storeDrawable, null, null, null);
                holder.stores.setCompoundDrawablePadding(10);
            }

            holder.stores.setText(String.format(
                    context.getString(R.string.nbr_stores_message),
                    String.valueOf(data.get(position).getNbr_stores())
            ));

            holder.stores.setVisibility(View.VISIBLE);

        }


        if (rectCategoryView) {
            holder.mainLayout.setSelected(selectedPos == position);
        }

    }

    public Category getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void clear() {

        data = new ArrayList<Category>();
        notifyDataSetChanged();

    }

    public void addItem(Category item) {

        int index = (data.size());
        data.add(item);
        notifyItemInserted(index);
    }

    public void addAllItems(RealmList<Category> listCats) {

        data.addAll(listCats);
        notifyDataSetChanged();

       /* selectedItems = new int[data.size()];
        initializeSeledtedItems();*/
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

    public class mViewHolder extends RecyclerView.ViewHolder {


        public TextView name;
        public ImageView image;
        public TextView stores;
        public View mainLayout;


        public mViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.cat_name);
            image = itemView.findViewById(R.id.image);
            stores = itemView.findViewById(R.id.stores);
            mainLayout = itemView.findViewById(R.id.mainLayout);

            stores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.itemClicked(view, getPosition());
                    }

                }
            });

            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (clickListener != null) {
                        clickListener.itemClicked(view, getPosition());
                    }

                    if (rectCategoryView) {
                        notifyItemChanged(selectedPos);
                        selectedPos = getLayoutPosition();
                        notifyItemChanged(selectedPos);
                    }

                }
            });
        }


    }
}