package com.clubecerto.apps.app.controllers.orders;

import com.clubecerto.apps.app.classes.PaymentGateway;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class PaymentController {

    public static List<PaymentGateway> getArrayList() {

        List<PaymentGateway> results = new ArrayList<>();
        RealmList<PaymentGateway> listCats = PaymentController.list();

        results.addAll(listCats.subList(0, listCats.size()));
        return results;
    }

    public static RealmList<PaymentGateway> list() {

        Realm realm = Realm.getDefaultInstance();
        RealmResults<PaymentGateway> result = realm.where(PaymentGateway.class).findAll();

        RealmList<PaymentGateway> results = new RealmList<PaymentGateway>();
        results.addAll(result.subList(0, result.size()));

        return results;
    }

    public static boolean insertPaymentGateway(final PaymentGateway cat) {

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

//               RealmResults<PaymentGateway> r = realm.where(PaymentGateway.class).findAll();
//                r.deleteAllFromRealm();

                realm.copyToRealmOrUpdate(cat);
            }
        });

        return true;
    }

    public static boolean insertPaymentGatewayList(final RealmList<PaymentGateway> list) {

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(list);
                
            }
        });

        return true;
    }


    public static PaymentGateway findId(int id) {

        Realm realm = Realm.getDefaultInstance();
        PaymentGateway obj = realm.where(PaymentGateway.class).equalTo("numCat", id).findFirst();

        return obj;
    }

    public static void removeAll() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<PaymentGateway> result = realm.where(PaymentGateway.class).findAll();
                for (PaymentGateway cat : result) {
                    cat.deleteFromRealm();
                }
            }
        });

    }
}
