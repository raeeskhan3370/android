package com.clubecerto.apps.app.controllers.stores;

import com.clubecerto.apps.app.classes.SavedStores;
import com.clubecerto.apps.app.classes.Store;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Droideve on 7/12/2017.
 */

public class StoreController {


    public static boolean insertStores(final RealmList<Store> list) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Store str : list) {
                    Store hasStore = realm.where(Store.class).equalTo("id", str.getId()).findFirst();

                    if (hasStore != null && hasStore.isLoaded()) {
                        str.setSaved(hasStore.isSaved());
                        realm.copyToRealmOrUpdate(hasStore);
                    } else {
                        realm.copyToRealmOrUpdate(str);
                    }
                }

            }
        });
        return true;
    }

    public static Store findStoreById(int id) {

        Realm realm = Realm.getDefaultInstance();
        Store obj = realm.where(Store.class).equalTo("id", id).findFirst();

        return obj;
    }

    public static Store getStore(int id) {

        Realm realm = Realm.getDefaultInstance();
        Store obj = realm.where(Store.class).equalTo("id", id).findFirst();

        return obj;
    }


    public static boolean doSave(int id) {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Store obj = realm.where(Store.class).equalTo("id", id).findFirst();
        obj.setSaved(true);
        realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();


        SavedStores saved_stores = realm.where(SavedStores.class).findFirst();

        if (saved_stores == null) {
            saved_stores = new SavedStores();
            saved_stores.setId(1);
        }

        if (saved_stores != null) {
            if (!saved_stores.isExist(id)) {
                realm.beginTransaction();
                saved_stores.getListID().add(id);
                realm.copyToRealmOrUpdate(saved_stores);
                realm.commitTransaction();
            }
        }


        return false;
    }


    public static boolean isSaved(int id) {

        Realm realm = Realm.getDefaultInstance();

        SavedStores saved_stores = realm.where(SavedStores.class).findFirst();

        if (saved_stores == null) {
            saved_stores = new SavedStores();
            saved_stores.setId(1);
        }

        if (saved_stores != null) {
            return saved_stores.isExist(id);
        }

        return false;
    }


    public static String getSavedStoresAsString() {
        Realm realm = Realm.getDefaultInstance();

        SavedStores saved_stores = realm.where(SavedStores.class).findFirst();

        if (saved_stores == null) {
            saved_stores = new SavedStores();
            saved_stores.setId(1);
        }

        String ids = "";

        if (saved_stores != null && saved_stores.getListID() != null)
            for (int i = 0; i < saved_stores.getListID().size(); i++) {
                ids = ids + "," + saved_stores.getListID().get(i);
            }


        return ids;
    }

    public static boolean doDelete(int id) {

        Realm realm = Realm.getDefaultInstance();
        final Store obj = realm.where(Store.class).equalTo("id", id).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                obj.setSaved(false);
                realm.copyToRealm(obj);
            }
        });


        SavedStores saved_stores = realm.where(SavedStores.class).findFirst();

        if (saved_stores == null) {
            saved_stores = new SavedStores();
            saved_stores.setId(1);
        }

        if (saved_stores != null) {
            if (saved_stores.isExist(id)) {
                realm.beginTransaction();
                saved_stores.delete(id);
                realm.copyToRealmOrUpdate(saved_stores);
                realm.commitTransaction();
            }
        }


        return false;
    }

    public static void removeAll() {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Store> result = realm.where(Store.class).findAll();
                for (Store o : result) {
                    o.deleteFromRealm();
                }
            }
        });

    }

}
