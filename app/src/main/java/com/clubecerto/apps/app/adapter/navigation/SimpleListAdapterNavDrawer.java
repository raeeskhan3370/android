package com.clubecerto.apps.app.adapter.navigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.EditProfileActivity;
import com.clubecerto.apps.app.activities.LoginActivity;
import com.clubecerto.apps.app.classes.HeaderItem;
import com.clubecerto.apps.app.classes.Item;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.utils.Session;
import com.github.siyamed.shapeimageview.CircularImageView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;


public class SimpleListAdapterNavDrawer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater infalter;
    private List<Item> data;
    private Context context;
    private ClickListener clickListener;

    public SimpleListAdapterNavDrawer(Context context, List<Item> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == 1) {
            View rootView = infalter.inflate(R.layout.menu_custom_row_drawer, parent, false);
            return new mViewHolder(rootView);
        } else {
            View rootView = infalter.inflate(R.layout.navigation_drawer_header, parent, false);
            return new mHeaderViewHolder(rootView);
        }


    }


    public List<Item> getData() {

        return this.data;
    }

    public void update(int position, Item item) {
        this.data.get(position).setNotify(item.getNotify());
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderViews, int position) {


        if (holderViews instanceof mViewHolder) {

            mViewHolder holder = (mViewHolder) holderViews;
            holder.nameItem.setText(data.get(position).getName());

            if (!data.get(position).isEnabled()) {
                holder.root.setVisibility(View.GONE);
            }

            if (data.get(position).getNotify() > 0) {
                holder.notify.setVisibility(View.VISIBLE);
                holder.notify.setText(String.valueOf(data.get(position).getNotify()));
            } else {
                holder.notify.setVisibility(View.GONE);
            }


            if (data.get(position).getIconDraw() != null) {

                Drawable yourDrawable = MaterialDrawableBuilder.with(context) // provide a context
                        .setIcon(data.get(position).getIconDraw()) // provide an icon
                        .setColor(context.getResources().getColor(R.color.grey)) // set the icon color
                        .setSizeDp(24) // set the icon size
                        .build();


                holder.imageItem.setImageDrawable(yourDrawable);
                if (!data.get(position).isEnabled()) {

                    holder.root.setVisibility(View.GONE);
                }
            }

        } else if (holderViews instanceof mHeaderViewHolder) {

            mHeaderViewHolder holder = (mHeaderViewHolder) holderViews;
            if (!data.get(position).isEnabled()) {
                holder.root.setVisibility(View.GONE);
            }

            if(SessionsController.isLogged()){
                User user = SessionsController.getSession().getUser();
                String user_label  = user.getName();
                String user_sub_label  = user.getEmail();
                holder.user_label.setText(user_label);
                holder.user_sub_label.setText(user_sub_label);
                holder.user_sub_label.setVisibility(View.VISIBLE);

                if(user.getImages()!=null){
                    Glide.with(context).load(user.getImages().getUrl500_500())
                            .placeholder(R.drawable.profile_placeholder)
                            .centerCrop().into(holder.user_image);
                }

            }else{
                Session session;
                session=new Session(context);
                holder.user_label.setText(session.getNome());


                holder.frame.setBackgroundColor(Color.parseColor(session.getCorBackgroundCabecalho()));
//                holder.user_label.setText(context.getString(R.string.login_create_account));
                holder.user_sub_label.setVisibility(View.GONE);
                Glide.with(context).load(R.drawable.profile_placeholder).centerCrop().into(holder.user_image);
            }

        }

    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && data.get(position) instanceof HeaderItem) {
            return 0;
        }
        return 1;
    }

    public void setClickListener(ClickListener clicklistener) {

        this.clickListener = clicklistener;

    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }

    public class mHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View root;

        TextView user_label;
        TextView user_sub_label;
        FrameLayout frame;

        CircularImageView user_image;

        mHeaderViewHolder(View itemView) {
            super(itemView);
            root = itemView;

            user_label = itemView.findViewById(R.id.user_label);
            user_sub_label = itemView.findViewById(R.id.user_sub_label);
            user_image = itemView.findViewById(R.id.user_image);
            frame=itemView.findViewById(R.id.frame);

            user_image.setOnClickListener(this);
            user_label.setOnClickListener(this);
            user_sub_label.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.user_image
                    || v.getId() == R.id.user_label
                    || v.getId() == R.id.user_sub_label){

//                if (!SessionsController.isLogged()) {
//                    Intent intent = new Intent(context, LoginActivity.class);
//                   context.startActivity(intent);
//                }else{
//                    context.startActivity(new Intent(context, EditProfileActivity.class));
//                }

            }
        }
    }

    public class mViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //for item menu
        TextView nameItem;
        ImageView imageItem;
        TextView notify;
        View root;


        public mViewHolder(View itemView) {
            super(itemView);

            root = itemView;
            itemView.setOnClickListener(this);
            //for item menu
            nameItem = itemView.findViewById(R.id.itemtext);
            imageItem = itemView.findViewById(R.id.itemimage);
            notify = itemView.findViewById(R.id.notify);


        }


        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }
        }
    }


}



