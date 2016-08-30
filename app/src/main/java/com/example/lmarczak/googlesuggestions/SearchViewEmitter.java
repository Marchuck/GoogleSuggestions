package com.example.lmarczak.googlesuggestions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SearchView;

import rx.AsyncEmitter;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by l.marczak
 *
 * @since 8/30/16.
 */
public class SearchViewEmitter extends SearchView implements TextEmitter {
    public SearchViewEmitter(Context context) {
        super(context);
    }

    public SearchViewEmitter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Observable<CharSequence> emit() {
         return Observable.fromAsync(new Action1<AsyncEmitter<CharSequence>>() {
            @Override
            public void call(final AsyncEmitter<CharSequence> charSequenceAsyncEmitter) {
                setOnQueryTextListener(new OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        charSequenceAsyncEmitter.onNext(newText);
                        return false;
                    }
                });
            }
        }, AsyncEmitter.BackpressureMode.LATEST);
    }

}
