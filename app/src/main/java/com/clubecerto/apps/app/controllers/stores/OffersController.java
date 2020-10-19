package com.clubecerto.apps.app.controllers.stores;

import com.clubecerto.apps.app.classes.Offer;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Droideve on 11/12/2017.
 */

public class OffersController {


    public static Offer findOfferById(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Offer.class).equalTo("id", id).findFirst();
    }

    public static List<Offer> findOffersByStoreId(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Offer> result = realm.where(Offer.class).equalTo("store_id", id).findAllSorted("id", Sort.DESCENDING);
        List<Offer> array = new ArrayList<>();
        array.addAll(result.subList(0, result.size()));
        return array;
    }

    public static void deleteAllOffers(int id) {
        Realm realm = Realm.getDefaultInstance();
        final RealmResults<Offer> result = realm.where(Offer.class).equalTo("store_id", id).findAllSorted("id", Sort.DESCENDING);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                result.deleteAllFromRealm();
            }
        });
    }

    public static boolean insertOffers(final RealmList<Offer> list) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Offer offer : list) {
                    realm.copyToRealmOrUpdate(offer);
                }
            }
        });
        return true;
    }


    public static void removeAll() {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Offer> result = realm.where(Offer.class).findAll();
                result.deleteAllFromRealm();

            }
        });

    }

}
