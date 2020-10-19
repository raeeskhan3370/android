package com.clubecerto.apps.app.fragments.orderFrags;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.utils.OfferUtils;
import com.clubecerto.apps.app.utils.Utils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.DecimalFormat;
import java.util.Map;

import static com.clubecerto.apps.app.activities.OrderCheckoutActivity.orderFields;


public class ConfirmationFragment extends Fragment {

    private Offer mOffer;
    private int offer_id;
    private Context mContext;

    public ConfirmationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_confirmation, container, false);
        mContext = root.getContext();


        Bundle args = getArguments();
        if (args != null) {
            offer_id = args.getInt("offer_id");
            parserInputViews(root, offer_id);
        }


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();


    }


    private void parserInputViews(View view, int offer_id) {

        mOffer = OffersController.findOfferById(offer_id);
        if (mOffer != null) {

            //offer image
            Glide.with(mContext)
                    .load(mOffer.getImages()
                            .getUrl100_100())
                    .placeholder(ImageLoaderAnimation.glideLoader(mContext))
                    .centerCrop()
                    .into(((ImageView) view.findViewById(R.id.image_product)));

            //offer title
            ((TextView) view.findViewById(R.id.title_product)).setText(mOffer.getName());

            //offer description
            Drawable locationDrawable = new IconicsDrawable(mContext)
                    .icon(CommunityMaterial.Icon.cmd_map_marker)
                    .color(ResourcesCompat.getColor(mContext.getResources(), R.color.white, null))
                    .sizeDp(12);


            ((TextView) view.findViewById(R.id.desc_product)).setText(
                    String.format(getString(R.string.order_qty), 1)
            );

            if (!AppController.isRTL())
                ((TextView) view.findViewById(R.id.desc_product)).setCompoundDrawables(locationDrawable, null, null, null);
            else
                ((TextView) view.findViewById(R.id.desc_product)).setCompoundDrawables(null, null, locationDrawable, null);


            //offer PRICE
            if (mOffer.getCurrency() != null) {

                if (mOffer.getValue_type().equalsIgnoreCase("Percent") && (mOffer.getOffer_value() > 0 || mOffer.getOffer_value() < 0)) {
                    DecimalFormat decimalFormat = new DecimalFormat("#0");
                    ((TextView) view.findViewById(R.id.price_product)).setText(OfferUtils.parseCurrencyFormat(
                            0,
                            mOffer.getCurrency()));

                    ((TextView) view.findViewById(R.id.total_price)).setText(OfferUtils.parseCurrencyFormat(
                            0,
                            mOffer.getCurrency()));

                    /*((TextView) view.findViewById(R.id.price_product)).setText(decimalFormat.format(mOffer.getOffer_value()) + "%");
                    ((TextView) view.findViewById(R.id.total_price)).setText(decimalFormat.format(mOffer.getOffer_value()) + "%");*/
                } else {
                    if (mOffer.getValue_type().equalsIgnoreCase("Price") && mOffer.getOffer_value() != 0) {

                        ((TextView) view.findViewById(R.id.price_product)).setText(OfferUtils.parseCurrencyFormat(
                                mOffer.getOffer_value(),
                                mOffer.getCurrency()));

                        ((TextView) view.findViewById(R.id.total_price)).setText(OfferUtils.parseCurrencyFormat(
                                mOffer.getOffer_value(),
                                mOffer.getCurrency()));

                    } else {
                        ((TextView) view.findViewById(R.id.price_product)).setText(getString(R.string.promo));
                        ((TextView) view.findViewById(R.id.total_price)).setText(getString(R.string.promo));
                    }
                }

            }
        }


        generateCustomFields(view, mContext);

    }

    private void generateCustomFields(View view, Context mContext) {
        LinearLayout itemWrapper = (LinearLayout) view.findViewById(R.id.inputs_fields_wrapper);
        itemWrapper.setOrientation(LinearLayout.VERTICAL);
        itemWrapper.setPaddingRelative((int) getResources().getDimension(R.dimen.spacing_large), 0, 0, 0);
        LinearLayout.LayoutParams layout_336 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemWrapper.setLayoutParams(layout_336);


        if (orderFields != null) {

            for (Map.Entry<String, String> entry : orderFields.entrySet()) {


                LinearLayout itemLayoutView = new LinearLayout(mContext);
                itemLayoutView.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layout_379 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout_379.setMargins(Utils.dpToPx(5), Utils.dpToPx(10), Utils.dpToPx(5), Utils.dpToPx(10));
                itemLayoutView.setLayoutParams(layout_379);

                TextView titleField = new TextView(mContext);
                titleField.setText(entry.getKey() + ": ");
                titleField.setTypeface(titleField.getTypeface(), Typeface.BOLD);
                titleField.setTextColor(getResources().getColor(R.color.black));
                LinearLayout.LayoutParams layout_143 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                titleField.setLayoutParams(layout_143);
                itemLayoutView.addView(titleField);


                TextView valueField = new TextView(mContext);
                String value = entry.getValue().trim();

                //handle the case location : city ; lat ; lng
                if (value != null && value.split(";").length >= 2) {
                    value = value.split(";")[0];
                }


                valueField.setText(value);
                valueField.setTextColor(getResources().getColor(R.color.grey_90));
                LinearLayout.LayoutParams layout_655 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout_655.leftMargin = (int) getResources().getDimension(R.dimen.spacing_large);
                valueField.setLayoutParams(layout_655);
                itemLayoutView.addView(valueField);


                itemWrapper.addView(itemLayoutView);


            }
        }


    }


}