package com.arv.bluetoothreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextView searchStatus, bluetoothStatus;
    Button searchButton, powerButton;
    ListView listView;
    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> pairedDevices = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Bluetooth", "Action: " + action);
            Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                searchStatus.setText("Finished");
                searchButton.setEnabled(true);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                Log.i("Bluetooth Result", "Name: " + name + " Address: " + address + " RSSI: " + rssi);

                if (!addresses.contains(address)) {
                    addresses.add(address);
                    String deviceString = "";
                    if (name == null || name.equals("")) {
                        deviceString = address + " - RSSI " + rssi + "dBm";
                    } else {
                        deviceString = name + " - RSSI " + rssi + "dBm";
                    }

                    bluetoothDevices.add(deviceString);
                    //   arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void displayPairedDevices() {
        pairedDevices.clear();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            pairedDevices.add("NAME: " + device.getName() + "[" + device.getAddress() + "]");
        }
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Acquired!", Toast.LENGTH_SHORT).show();
                Log.i("Bluetooth", "Permission Acquired!");
            } else
                Toast.makeText(this, "Sorry don't have the permission yet!", Toast.LENGTH_SHORT).show();
            Log.i("Bluetooth", "Permission didn't acquire!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchStatus = findViewById(R.id.searchStatus);
        searchButton = findViewById(R.id.searchButton);
        listView = findViewById(R.id.listView);
        powerButton = findViewById(R.id.onBtn);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, pairedDevices);
        listView.setAdapter(arrayAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            bluetoothStatus.setVisibility(View.VISIBLE);
        } else {
            bluetoothStatus.setVisibility(View.GONE);
        }
        if (bluetoothAdapter.isEnabled()) {
            searchButton.setVisibility(View.VISIBLE);
            powerButton.setVisibility(View.GONE);
        } else {
            searchButton.setVisibility(View.GONE);
            powerButton.setVisibility(View.VISIBLE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void startSearching(View view) {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Log.i("Bluetooth", "Permission already Acquired!");
//            searchStatus.setText("SEARCHING...");
//            searchButton.setEnabled(false);
            bluetoothDevices.clear();
            addresses.clear();
            // bluetoothAdapter.startDiscovery();
            displayPairedDevices();
        }
    }

    public void turnONBluetooth(View view) {
        powerButton.setEnabled(false);
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning ON Bluetooth...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth is already ON!", Toast.LENGTH_SHORT).show();
    }

    /*
     For Turning Bluetooth Discoverable
        if(!bluetoothAdapter.isDiscovering()){
           Toast.makeText(MainActivity.this, "Making your Bluetooth Discoverable", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
           startActivityForResult(intent, 1);
        }
    */

    /*
     For Turning OFF Bluetooth
        bluetoothAdapter.disable();
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            powerButton.setEnabled(true);
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Bluetooth is now ON!", Toast.LENGTH_SHORT).show();
                searchButton.setVisibility(View.VISIBLE);
                powerButton.setVisibility(View.GONE);
            } else {
                Toast.makeText(MainActivity.this, "Couldn't turn ON the Bluetooth!", Toast.LENGTH_SHORT).show();
                searchButton.setVisibility(View.GONE);
                powerButton.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}