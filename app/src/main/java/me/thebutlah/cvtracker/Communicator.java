package me.thebutlah.cvtracker;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import me.aflak.bluetooth.Bluetooth;

/**
 * Created by ryan on 5/18/17.
 */

public class Communicator implements Bluetooth.CommunicationCallback, Bluetooth.DiscoveryCallback{

    public final String TAG;

    private Bluetooth adapter;
    private Activity context;

    public Communicator(Activity context) {
        this.context = context;
        this.TAG = context.getResources().getString(R.string.app_name) + "::Comm";
        adapter = new Bluetooth(context);
        adapter.enableBluetooth();
        adapter.setCommunicationCallback(this);
        adapter.setDiscoveryCallback(this);
        adapter.scanDevices();
        adapter.connectToName("HC-06");
    }

    public boolean send(byte... b) {
        if (b==null || b.length == 0 || !adapter.isConnected()) return false;
        String msg = new String(b);
        Log.d(TAG, String.format("Sending: %s", msg));
        adapter.send(msg);
        return true;
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Log.d(TAG, String.format("Connected: %s", device.getName()));
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Log.d(TAG, String.format("Disconnected: %s", device.getName()));
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, String.format("Message: %s", message));
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "Finished!");
    }

    @Override
    public void onDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Device: %s", device.getName()));
    }

    @Override
    public void onPair(BluetoothDevice device) {
        Log.d(TAG, String.format("Pair: %s", device.getName()));
    }

    @Override
    public void onUnpair(BluetoothDevice device) {
        Log.d(TAG, String.format("Unpair: %s", device.getName()));
    }

    @Override
    public void onError(String message) {
        Log.d(TAG, String.format("Error: %s", message));
    }

    @Override
    public void onConnectError(BluetoothDevice device, String message) {
        Log.d(TAG, String.format("ConnectError: %s", message));
    }
    //END COMMUNICATION CALLBACK
}
