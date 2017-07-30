// IOnNewBookArrivedListener.aidl
package com.shenxm.bookaidl.aidl;

// Declare any non-default types here with import statements
import com.shenxm.bookaidl.aidl.Book;

interface IOnNewBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void onNewBookArraived(in Book newBook);
}
