package com.example.lmarczak.googlesuggestions;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    static long time0;

    GoogleApiClient mGoogleApiClient;

    RxSuggestor rxSuggestor;

    rx.Subscription subscription;

    SearchViewEmitter emitter;

    PoiAdapter poiAdapter;

    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        emitter = (SearchViewEmitter) findViewById(R.id.searchview);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        poiAdapter = new PoiAdapter();

        recyclerView.setAdapter(poiAdapter);

        time0 = System.currentTimeMillis();

        RxSuggestor.test().subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                long time1 = System.currentTimeMillis();
                System.out.println("Time elapsed: " + (time1 - time0));
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e);
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "onNext: " + integer);
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        handleSuggestions();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    private void handleSuggestions() {
        Log.i(TAG, "handleSuggestions: ");

        rxSuggestor = new RxSuggestor(mGoogleApiClient);

        subscription = emitter.emit().flatMap(new Func1<CharSequence, Observable<List<CharSequence>>>() {
            @Override
            public Observable<List<CharSequence>> call(CharSequence sequence) {
                return rxSuggestor.suggest(sequence);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<CharSequence>>() {
                    @Override
                    public void call(List<CharSequence> newItems) {
                        poiAdapter.refreshWith(newItems);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "error: ", throwable);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
