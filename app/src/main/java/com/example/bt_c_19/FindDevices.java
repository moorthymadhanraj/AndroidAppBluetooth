package com.example.bt_c_19;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FindDevices extends AppCompatActivity {

    // bluetooth adapter var
    private BluetoothAdapter btAdapter;

    ArrayList<String> newDevices = new ArrayList<>();
    ArrayList<String> allDevices = new ArrayList<>();

    //Defining list UI item
    ListView btList;
    private boolean isDiscoverable;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_devices);

        btList = (ListView) findViewById(R.id.list1);
        btAdapter = BluetoothAdapter.getDefaultAdapter();                   //Initializing adapter

        // Registering for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        if (allDevices != null) {

            allDevices.clear();                                             //Clearing array to show fresh in the list
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},   //Ask for permission to turn on location
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    public void scanNewDevices(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            setBluetooth(true);
        } else {
            Toast.makeText(FindDevices.this, "Scanning nearby devices...", Toast.LENGTH_SHORT).show();
            //Scan for unpaired available bluetooth devices when "button" is clicked
            if (!isDiscoverable) {
    //            discoverOn();//Set bluetooth to discoverable
                isDiscoverable = true;
            }

            allDevices.clear();
            if (btAdapter.isDiscovering()) {
                //Bluetooth is already in mode discovery mode, we cancel to restart it again
                btAdapter.cancelDiscovery();
            }
            btAdapter.startDiscovery();                                         //Start scanning

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            isReceiverRegistered = true;
            registerReceiver(receiver, filter);
        }
    }


    //Create a BroadcastReceiver for ACTION_FOUND.
    //RSSI
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Discovery has found a device. Get the BluetoothDevice
                //object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (newDevices != null) {
                    newDevices.clear();                                     //Clear earlier list
                }

                if (device != null) {
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    if (rssi > -62 && rssi < -40) {
                        newDevices.add(device.getName() + '\n' + device.getAddress() + '\n' +  rssi);
                    }
                    allDevices.addAll(newDevices);                          //Update new devices in final display list

                    showList();                                             //Display list
                }

            }
        }
    };


    // Bluetooth state
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        // Bluetooth has been turned off;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Bluetooth is turning off;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // Bluetooth is on
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Bluetooth is turning on
                        break;
                }
            }
        }
    };


    //When application is closed,
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister the ACTION_FOUND receiver.
        if (receiver != null && isReceiverRegistered)
            unregisterReceiver(receiver);
        // Unregister broadcast listeners
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }


    //Method to set device list to ListView UI
    public void showList() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allDevices);
        btList.setAdapter(adapter);
    }

    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }
}