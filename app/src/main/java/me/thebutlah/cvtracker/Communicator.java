package me.thebutlah.cvtracker;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import me.aflak.bluetooth.Bluetooth;

/**
 * Created by ryan on 5/18/17.
 */

public class Communicator implements Bluetooth.CommunicationCallback{

    public static final String TAG = R.string.app_name + "::Comm";

    private Bluetooth adapter;
    private Activity context;

    public Communicator(Activity context) {
        this.context = context;
        adapter = new Bluetooth(context);
        adapter.enableBluetooth();
        adapter.setCommunicationCallback(this);
        adapter.connectToName("HC-06");
    }

    public void send(byte... b) {
        if (b == null || b.length == 0 || !adapter.isConnected()) return;
        adapter.send(String.valueOf(b));
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
    public void onError(String message) {
        Log.d(TAG, String.format("Error: %s", message));
    }

    @Override
    public void onConnectError(BluetoothDevice device, String message) {
        Log.d(TAG, String.format("ConnectError: %s", message));
    }
}
