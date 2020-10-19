package com.clubecerto.apps.app.adapter.ListExpand;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.OrderCheckoutActivity;
import com.clubecerto.apps.app.adapter.lists.ProductsAdapter;
import com.clubecerto.apps.app.animation.ViewAnimation;
import com.clubecerto.apps.app.classes.Order;
import com.clubecerto.apps.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class AdapterListExpand extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Order> data = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public AdapterListExpand(Context context, List<Order> data) {
        this.data = data;
        ctx = context;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_expand, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            final Order mOrder = data.get(position);
            view.orderID.setText(
                    String.format(ctx.getString(R.string.statusID), mOrder.getId())
            );

            //set status with color
            String[] arrayStatus = mOrder.getStatus().split(";");
            if (arrayStatus.length > 0) {
                view.status.setText(  arrayStatus[0].substring(0, 1).toUpperCase() + arrayStatus[0].substring(1));
                if (arrayStatus[1] != null && !arrayStatus[0].equals("null")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.status.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(arrayStatus[1])));
                    }
                }
            }


            //set status with color
            String[] arrayPayStatus = mOrder.getPayment_status().split(";");
            if (arrayPayStatus.length > 0) {

                view.order_payment_status.setText(arrayPayStatus[0].substring(0, 1).toUpperCase() + arrayPayStatus[0].substring(1));

                if (arrayPayStatus[1] != null && !arrayPayStatus[0].equals("null")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.order_payment_status.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(arrayPayStatus[1])));
                    }
                }

                //showup a pay now button when the order is not paid yet
                if (arrayPayStatus[0].equals("unpaid") ) {
                    view.btn_pay_now.setVisibility(View.VISIBLE);
                    view.btn_pay_now.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Send The Message Receiver ID & Name
                            Intent intent = new Intent(v.getContext(), OrderCheckoutActivity.class);
                            intent.putExtra("fragmentToOpen", "fragment_payment");
                            intent.putExtra("order_id", mOrder.getId());
                            if (mOrder.getCart() != null && !mOrder.getCart().equals("null")) {
                                try {
                                    JSONArray cartArray = new JSONArray(mOrder.getCart());
                                    if (cartArray.length() > 0) {
                                        JSONObject cartObj = cartArray.getJSONObject(0);
                                        if (cartObj.getString("module").equals("offer")) {
                                            intent.putExtra("offer_id", cartObj.getInt("module_id"));
                                        }

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            v.getContext().startActivity(intent);
                        }
                    });
                } else {
                    view.btn_pay_now.setVisibility(View.GONE);

                }
            }


            //setup product items in recyclerview
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ctx, 1);
            ProductsAdapter mProductAdapter = new ProductsAdapter(ctx, mOrder.getItems());
            view.list_items.setHasFixedSize(true);
            view.list_items.setLayoutManager(mLayoutManager);
            view.list_items.setItemAnimator(new DefaultItemAnimator());
            view.list_items.setAdapter(mProductAdapter);

            if (mProductAdapter.getTotalPrice() != null) {
                view.total_price_items.setText(mProductAdapter.getTotalPrice());
            } else {
                view.total_price_layout.setVisibility(View.GONE);
                view.btn_pay_now.setVisibility(View.GONE);

            }


            view.bt_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!mOrder.expanded, v, view.lyt_expand);
                    data.get(position).expanded = show;
                }
            });


            // void recycling view
            if (mOrder.expanded) {
                view.lyt_expand.setVisibility(View.VISIBLE);
            } else {
                view.lyt_expand.setVisibility(View.GONE);
            }
            Utils.toggleArrow(mOrder.expanded, view.bt_expand, false);

        }
    }

    public Order getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addItem(Order item) {

        int index = (data.size());
        data.add(item);
        notifyItemInserted(index);

    }

    public void addAllItems(RealmList<Order> listCats) {

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

    private boolean toggleLayoutExpand(boolean show, View view, View lyt_expand) {
        Utils.toggleArrow(show, view);
        if (show) {
            ViewAnimation.expand(lyt_expand);
        } else {
            ViewAnimation.collapse(lyt_expand);
        }
        return show;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Order obj, int position);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        TextView orderID;
        TextView status;
        TextView order_payment_status;
        ImageButton bt_expand;
        View lyt_expand;
        View lyt_parent;
        RecyclerView list_items;
        TextView total_price_items;
        LinearLayout total_price_layout;
        AppCompatButton btn_pay_now;

        public OriginalViewHolder(View v) {
            super(v);
            orderID = (TextView) v.findViewById(R.id.order_id);
            status = (TextView) v.findViewById(R.id.order_status);
            order_payment_status = (TextView) v.findViewById(R.id.order_payment_status);
            bt_expand = (ImageButton) v.findViewById(R.id.bt_expand);
            lyt_expand = (View) v.findViewById(R.id.lyt_expand);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            list_items = (RecyclerView) v.findViewById(R.id.list_items);
            total_price_items = (TextView) v.findViewById(R.id.total_price_items);
            total_price_layout = (LinearLayout) v.findViewById(R.id.total_price_layout);
            btn_pay_now = (AppCompatButton) v.findViewById(R.id.btn_pay_now);
        }
    }


}