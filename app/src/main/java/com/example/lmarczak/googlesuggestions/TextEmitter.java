package com.example.lmarczak.googlesuggestions;

/**
 * Created by l.marczak
 *
 * @since 8/30/16.
 */
public interface TextEmitter {
    rx.Observable<CharSequence> emit();
}
