package com.shenxm.bookaidl.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.shenxm.bookaidl.R;
import com.shenxm.bookaidl.aidl.Book;
import com.shenxm.bookaidl.aidl.IBookManager;
import com.shenxm.bookaidl.aidl.IOnNewBookArrivedListener;
import com.shenxm.bookaidl.service.BookManagerService;

import java.util.List;

/**
 * Created by SHEN XIAOMING on 2017/7/30.
 */
public class BookManagerActivity extends AppCompatActivity {

    private static final String TAG = "BookManagerActivity";

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.e(TAG, "receive new book:" + msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManager bookManager = IBookManager.Stub.asInterface(service);
            try {
                mRemoteBookManager = bookManager;
                List<Book> list = bookManager.getBookList();
//                Log.e(TAG, "query book list, list type: " + list.getClass().getCanonicalName());
                Log.e(TAG, "query book list: " + list.toString());
                Book newBook = new Book(3, "art res");
                bookManager.addBook(newBook);
                Log.e(TAG, "add book: " + newBook);
                List<Book> newList = bookManager.getBookList();
                Log.e(TAG, "query book list: " + newList.toString());
                bookManager.registerListener(mOnNewBoookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.e(TAG, "binder died.");
        }
    };

    private IOnNewBookArrivedListener mOnNewBoookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
        }

        @Override
        public void onNewBookArraived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null
                && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.e(TAG, "unregister listener:" +mOnNewBoookArrivedListener);
                mRemoteBookManager.unregisterListener(mOnNewBoookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
