package com.example.lmarczak.googlesuggestions;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.AsyncEmitter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func9;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * Created by l.marczak
 *
 * @since 8/30/16.
 */
public class RxSuggestor {
    public static final String TAG = RxSuggestor.class.getSimpleName();
    private final LatLng northEastKrk = new LatLng(50.114825, 20.147381);
    private final LatLng southWestKrk = new LatLng(49.991820, 19.797192);
    private final GoogleApiClient apiClient;
    private final LatLngBounds latLngBounds = new LatLngBounds(southWestKrk, northEastKrk);

    public RxSuggestor(GoogleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public rx.Observable<List<CharSequence>> suggest(final CharSequence query) {
        return Observable.fromAsync(new Action1<AsyncEmitter<List<CharSequence>>>() {
            @Override
            public void call(final AsyncEmitter<List<CharSequence>> charSequenceAsyncEmitter) {
                Log.i(TAG, "handleSuggestions: ");
                PendingResult<AutocompletePredictionBuffer> result =
                        Places.GeoDataApi.getAutocompletePredictions(apiClient, query.toString(), latLngBounds, null);
                result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(@NonNull AutocompletePredictionBuffer buff) {
                        List<CharSequence> sequences = new ArrayList<>();
                        int indexOfSuggestion = 0;

                        try {
                            AutocompletePrediction nextPrediction = buff.get(indexOfSuggestion);
                            while (nextPrediction.isDataValid()) {
                                sequences.add(new PoiSequence(nextPrediction));
                                ++indexOfSuggestion;
                                nextPrediction = buff.get(indexOfSuggestion);
                            }
                        } catch (Exception x) {
                        } finally {
                            charSequenceAsyncEmitter.onNext(sequences);
                            buff.release();
                        }
                    }
                });
            }
        }, AsyncEmitter.BackpressureMode.LATEST)
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS);
    }


    public static Observable<Integer> test() {
        Log.d(TAG, "test: ");
        final int threadCt = Runtime.getRuntime().availableProcessors() + 1;

        //  final ExecutorService executor = Executors.newFixedThreadPool(threadCt);
        //final Scheduler scheduler = Schedulers.from(executor);

        final AtomicInteger batch = new AtomicInteger(0);
        return Observable.range(1, 1000).groupBy(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return batch.getAndIncrement() % threadCt;
            }
        }).flatMap(new Func1<GroupedObservable<Integer, Integer>, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(GroupedObservable<Integer, Integer> integerIntegerGroupedObservable) {
                //Log.d(TAG, "current thread:  " + Thread.currentThread().getName());
                return integerIntegerGroupedObservable.observeOn(Schedulers.io()).flatMap(new Func1<Integer, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer i) {
                        return getIntWithDelay(i, 100 * i);
                    }
                }).map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        Log.d(TAG, "__next(" + integer + ")__");
                        return integer;
                    }
                });
            }
        });

//            return Observable.range(1, 10).flatMap(new Func1<Integer, Observable<Integer>>() {
//                @Override
//                public Observable<Integer> call(Integer integer) {
//                    return getIntWithDelay(integer, 100 * integer);
//                }
//            }).toList().map(new Func1<List<Integer>, Integer>() {
//                @Override
//                public Integer call(List<Integer> integers) {
//                    int sum = 0;
//                    for (Integer s : integers) {
//                        sum += s;
//                    }
//                    return sum;
//                }
//            })

//        return Observable.just(true).flatMap(new Func1<Boolean, Observable<Integer>>() {
//            @Override
//            public Observable<Integer> call(Boolean aBoolean) {
//                return Observable.zip(getIntWithDelay(1, 100),
//                        getIntWithDelay(2, 200),
//                        getIntWithDelay(3, 300),
//                        getIntWithDelay(4, 400),
//                        getIntWithDelay(5, 500),
//                        getIntWithDelay(6, 600),
//                        getIntWithDelay(7, 700),
//                        getIntWithDelay(8, 800),
//                        getIntWithDelay(9, 900), new Func9<Integer, Integer, Integer, Integer, Integer, Integer,
//                                Integer, Integer, Integer, Integer>() {
//                            @Override
//                            public Integer call(Integer integer1, Integer integer2, Integer integer3, Integer integer4,
//                                                Integer integer5, Integer integer6, Integer integer7, Integer integer8, Integer integer9) {
//                                return integer1 + integer2 + integer3 + integer4 +
//                                        integer5 + integer6 + integer7 + integer8 + integer9;
//                            }
//                        });
//            }
//        })
//                    .subscribeOn(scheduler).doAfterTerminate(new Action0() {
//                        @Override
//                        public void call() {
//                            Log.d(TAG, "doAfterTerminate");
//                            executor.shutdown();
//                        }
//                    });
    }

    private static Observable<Integer> getIntWithDelay(final int intie, final long delay) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                Log.d(TAG, "getIntWithDelay: " + intie + "," + delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onNext(intie);
                subscriber.onCompleted();
            }
        });
    }

}
