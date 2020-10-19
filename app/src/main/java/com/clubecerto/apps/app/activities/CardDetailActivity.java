package com.clubecerto.apps.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.clubecerto.apps.app.DisplayNextView;
import com.clubecerto.apps.app.Flip3dAnimation;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.utils.Session;
import com.squareup.picasso.Picasso;

public class CardDetailActivity extends AppCompatActivity {

    private ImageView image1;
    private ImageView image2;

    private boolean isFirstImage = true;
    Session session;
    TextView tv_nome,tv_cpf;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        image1 = (ImageView) findViewById(R.id.ImageView01);
        image2 = (ImageView) findViewById(R.id.ImageView02);
        tv_nome=findViewById(R.id.tv_nome);
        tv_cpf=findViewById(R.id.tv_cpf);
        image2.setVisibility(View.GONE);
        tv_nome.setVisibility(View.GONE);
        tv_cpf.setVisibility(View.GONE);


        session = new Session(this);
        String pat=     session.getCard();
        String cardBack=     session.getCardBack();

        tv_nome.setText(session.getNome());
        tv_cpf.setText(session.getCPF());

        if ( pat != null) {
            Picasso.get().load(pat).into(image1);
//            pat=pat.replaceAll(" ", "%20");

//            Glide.with(this)
//                    .load( pat)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .centerCrop()
//                    .placeholder(ImageLoaderAnimation.glideLoader(getApplicationContext() ))
//                    .into(image1);

        } else {
            image1.setImageDrawable(ResourcesCompat.getDrawable( getResources(), R.drawable.def_logo, null));
        }
        if ( cardBack != null) {
            Picasso.get().load(cardBack).into(image2);
//            pat=pat.replaceAll(" ", "%20");

//            Glide.with(this)
//                    .load( cardBack)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .centerCrop()
//                    .placeholder(ImageLoaderAnimation.glideLoader(getApplicationContext() ))
//                    .into(image2);

        } else {
            image1.setImageDrawable(ResourcesCompat.getDrawable( getResources(), R.drawable.def_logo, null));
        }

        image1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isFirstImage) {
                    applyRotation(0, 90);
                    isFirstImage = !isFirstImage;

                } else {
                    applyRotation(0, -90);
                    isFirstImage = !isFirstImage;
                }
            }
        });
    }

    private void applyRotation(float start, float end) {
// Find the center of image
        final float centerX = image1.getWidth() / 2.0f;
        final float centerY = image1.getHeight() / 2.0f;

// Create a new 3D rotation with the supplied parameter
// The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation =
                new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstImage, image1, image2));

        if (isFirstImage)
        {
            image1.startAnimation(rotation);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_nome.setVisibility(View.VISIBLE);
                    tv_cpf.setVisibility(View.VISIBLE);

                }
            }, 1000);

        } else {
            image2.startAnimation(rotation);

            tv_nome.setVisibility(View.GONE);
            tv_cpf.setVisibility(View.GONE);
        }

    }
}


//        private Session session;
//        private ImageView category_image;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_card_detail);
//        category_image=findViewById(R.id.category_image);
//        session = new Session(this);
//        String pat=     session.getCard();
//        if ( pat != null) {
////            Picasso.get().load(pat).into(category_image);
////            pat=pat.replaceAll(" ", "%20");
//
//            Glide.with(this)
//                    .load( pat)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .centerCrop()
//                    .placeholder(ImageLoaderAnimation.glideLoader(getApplicationContext() ))
//                    .into(category_image);
//
//        } else {
//            category_image.setImageDrawable(ResourcesCompat.getDrawable( getResources(), R.drawable.def_logo, null));
//        }
//        }
//
//}
