package com.shenxm.bookaidl.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shenxm.bookaidl.aidl.Book;
import com.shenxm.bookaidl.aidl.IBookManager;
import com.shenxm.bookaidl.aidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SHEN XIAOMING on 2017/7/30.
 */
public class BookManagerService extends Service {

    private static final String TAG = "BMS";

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mListenerList.contains(listener)) {
//                mListenerList.add(listener);
//            } else {
//                Log.e(TAG, "already exits");
//            }
//            Log.e(TAG, "registerListener, size: " + mListenerList.size());
            mListenerList.register(listener);
            final int N = mListenerList.beginBroadcast();
            int count = 0;
            for (int i = 0 ; i < N; i++) {
                count++;
            }
            mListenerList.finishBroadcast();
            Log.e(TAG, "registerListener, size: " + count);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (mListenerList.contains(listener)) {
//                mListenerList.remove(listener);
//                Log.e(TAG, "unregister listener succeed.");
//            } else {
//                Log.e(TAG, "not found, cannot unregister");
//            }
//            Log.e(TAG, "unregisterListener, current size: " + mListenerList.size());
            mListenerList.unregister(listener);
            final int N = mListenerList.beginBroadcast();
            int count = 0;
            for (int i = 0 ; i < N; i++) {
                count++;
            }
            mListenerList.finishBroadcast();
            Log.e(TAG, "registerListener, size: " + count);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "iOS"));
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            //
            while (!mIsServiceDestroyed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
//        Log.e(TAG, "onNewBookArrived, notify listeners: " + mListenerList.size());
//        for (int i = 0 ; i < mListenerList.size(); i++) {
//            IOnNewBookArrivedListener listener = mListenerList.get(i);
//            Log.e(TAG, "onNewBookArrived, notify listener: " + listener);
//            listener.onNewBookArraived(book);
//        }
        final int N = mListenerList.beginBroadcast();
        for (int i = 0 ; i < N; i++) {
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
            if (l != null) {
                try {
                    l.onNewBookArraived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }
}
