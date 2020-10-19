package com.clubecerto.apps.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.LoginActivity;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.dtmessenger.MessengerHelper;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;


/**
 * Created by Droideve on 7/10/2017.
 */

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);


        Button btnLogin = rootView.findViewById(R.id.login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.search_icon).setVisible(false);
        updateBadge();
    }

    private void updateBadge() {
        if (MessengerHelper.NbrMessagesManager.getNbrTotalMessages() > 0) {
            ActionItemBadge.update(getActivity(), MainActivity.mainMenu.findItem(R.id.messenger_action),
                    CommunityMaterial.Icon.cmd_bell_ring_outline,
                    ActionItemBadge.BadgeStyles.RED,
                    MessengerHelper.NbrMessagesManager.getNbrTotalMessages());
        } else {
            ActionItemBadge.hide(MainActivity.mainMenu.findItem(R.id.messenger_action));
        }
    }


}
