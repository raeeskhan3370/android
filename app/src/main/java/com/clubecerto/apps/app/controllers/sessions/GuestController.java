package com.clubecerto.apps.app.controllers.sessions;

import com.clubecerto.apps.app.classes.Guest;

import io.realm.Realm;

/**
 * Created by Droideve on 11/20/2017.
 */

public class GuestController {

    public static void saveGuest(final Guest guest) {

        SessionsController.getLocalDatabase.setGuestId(guest.getId());

        Realm mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(guest);
            }
        });

    }

    public static Guest getGuest() {

        Realm mRealm = Realm.getDefaultInstance();
        return mRealm.where(Guest.class).findFirst();

    }

    public static void clear() {

        SessionsController.getLocalDatabase.setGuestId(0);

        Realm mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Guest g = realm.where(Guest.class).findFirst();
                try {
                    g.deleteFromRealm();

                } catch (Exception e) {

                }
            }
        });

    }


    public static boolean isStored() {

        Realm mRealm = Realm.getDefaultInstance();
        Guest guest = mRealm.where(Guest.class).findFirst();

        return guest != null && guest.isValid() && guest.isManaged();
    }


}
