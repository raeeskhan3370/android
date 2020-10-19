package com.clubecerto.apps.app.adapter.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.classes.PaymentGateway;

import java.util.List;


public class PayGWAdapter extends RecyclerView.Adapter<PayGWAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<PaymentGateway> data;
    private Context context;
    private ClickListener clickListener;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int lastPosition = -1;
    private boolean on_attach = true;

    private int parent_width = 0;
    private int rest = 0;

    public PayGWAdapter(Context context, List<PaymentGateway> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }


    public List<PaymentGateway> getData() {
        return data;
    }

    @Override
    public PayGWAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = infalter.inflate(R.layout.item_payment_gateway, parent, false);
        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }


    @Override
    public void onBindViewHolder(PayGWAdapter.mViewHolder holder, int position) {

        PaymentGateway mPG = data.get(position);

        if (mPG.getImages() != null && !mPG.getImages().equals("null")) {

            Glide.with(context).load(mPG.getImages())
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .fitCenter().into(holder.image);
        }


        holder.payment.setText(mPG.getPayment());
        holder.description.setText(mPG.getDescription());


        holder.item_payment_layout.setSelected(selectedPos == position);
        holder.checked.setVisibility(selectedPos == position ? View.VISIBLE : View.GONE);


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

    public PaymentGateway getItemDetail(int position) {
        if (position >= 0) {
            return data.get(position);
        }
        return null;
    }

    public void addItem(PaymentGateway item) {

        int index = (data.size());
        data.add(item);
        notifyItemInserted(index);
    }


    public void addAll(final List<PaymentGateway> paymentList) {
        int size = paymentList.size();

        data.clear();
        if (size > 0) {
            //remove all data before adding new items
            for (int i = 0; i < size; i++) {
                data.add(paymentList.get(i));
            }

            notifyDataSetChanged();
        }


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clicklistener) {

        this.clickListener = clicklistener;

    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }

    public class mViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView payment;
        TextView description;
        LinearLayout item_payment_layout;
        ImageView checked;


        public mViewHolder(View itemView) {
            super(itemView);

            item_payment_layout = itemView.findViewById(R.id.item_payment_layout);
            image = itemView.findViewById(R.id.image);
            payment = itemView.findViewById(R.id.payment);
            description = itemView.findViewById(R.id.description);
            checked = itemView.findViewById(R.id.icon_checked);


            item_payment_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //item_payment_layout.setSelected(true);

                    notifyItemChanged(selectedPos);
                    selectedPos = getLayoutPosition();
                    notifyItemChanged(selectedPos);

                    checked.setVisibility(View.VISIBLE);

                    if (clickListener != null) {
                        clickListener.itemClicked(v, getPosition());
                    }
                }
            });

        }


    }


}
