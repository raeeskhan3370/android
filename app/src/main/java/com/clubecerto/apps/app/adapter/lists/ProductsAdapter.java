package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.OfferDetailActivity;
import com.clubecerto.apps.app.classes.Product;
import com.clubecerto.apps.app.utils.OfferUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Product> items = new ArrayList<>();
    private Context ctx;
    private ClickListener mClickListener;

    public ProductsAdapter(Context context, List<Product> items) {
        this.items = items;
        ctx = context;
    }

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    public void setClickListener(final ClickListener mItemClickListener) {
        this.mClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_product_item, parent, false);
        vh = new ProductViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ProductViewHolder) {
            ProductViewHolder view = (ProductViewHolder) holder;


            final Product product = items.get(position);
            view.title_product.setText(product.getName());
            view.desc_product.setText(
                    String.format(ctx.getString(R.string.product_qty), items.get(position).getQty()));

            if (product.getAmount() > 0) {
                //  Offer mOffer = OffersController.findOfferById(product.getId());
                view.price_product.setText(OfferUtils.parseCurrencyFormat(
                        (float) product.getAmount(),
                        product.getCurrency()));
            } else {
                view.price_product.setVisibility(View.GONE);
            }


            if (product.getImage() != null && !product.getImage().equals("")) {
                Glide.with(ctx)
                        .load(product.getImage())
                        .centerCrop().placeholder(R.drawable.def_logo)
                        .into(view.image_product);
            } else {
                Glide.with(ctx).load(R.drawable.def_logo)
                        .centerCrop().into(view.image_product);
            }


            view.product_detail_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentOffer = new Intent(ctx, OfferDetailActivity.class);
                    intentOffer.putExtra("id", product.getId());
                    Objects.requireNonNull(ctx).startActivity(intentOffer);
                }
            });


        }
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

    public String getTotalPrice() {
        double total_price = 0;
        if (items.size() > 0) {
            for (Product product : items) {
                total_price = total_price + product.getAmount();
            }

            if (total_price > 0)
                return OfferUtils.parseCurrencyFormat((float) total_price, items.get(0).getCurrency());
        }
        return null;
    }

    public void addItem(Product item) {

        int index = (items.size());
        items.add(item);
        notifyItemInserted(index);
    }

    public void addAll(final List<Product> productList) {
        int size = productList.size();

        items.clear();
        if (size > 0) {
            //remove all items before adding new items
            for (int i = 0; i < size; i++) {
                items.add(productList.get(i));
            }

            notifyDataSetChanged();
        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public interface ClickListener {
        void onItemClick(View view, int pos);

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        LinearLayout product_detail_layout;
        ImageView image_product;
        TextView title_product;
        TextView desc_product;
        TextView price_product;


        public ProductViewHolder(View v) {
            super(v);

            product_detail_layout = (LinearLayout) v.findViewById(R.id.product_detail_layout);
            image_product = (ImageView) v.findViewById(R.id.image_product);
            title_product = (TextView) v.findViewById(R.id.title_product);
            desc_product = (TextView) v.findViewById(R.id.desc_product);
            price_product = (TextView) v.findViewById(R.id.price_product);

        }

        /*@Override
        public void onClick(View view) {
            if (view.getId() == R.id.product_detail_layout) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, getPosition());
                }
            }
        }*/
    }


}