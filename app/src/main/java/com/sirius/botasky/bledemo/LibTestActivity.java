package com.sirius.botasky.bledemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sirius.botasky.cyberble.ble.BleAdmin;
import com.sirius.botasky.cyberble.callback.DeviceConnectCallback;
import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibTestActivity extends AppCompatActivity {
    private Button mScanButton, mStopButton, mDisconnecte, mWrite;
    private EditText mEditData;
    private ExpandableListView mExpanListView;
    private RecyclerView mDevicesRecycler;
    private BleDeviceAdapter mRecyclerAdapter;
    private BleAdmin mBleAdmin;
    private TextView mNotifyData;
    private ConstraintLayout discover, connect;

    private DeviceConnectCallback mDeviceCallBack = new DeviceConnectCallback() {
        @Override
        public void onDeviceConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    discover.setVisibility(View.GONE);
                    connect.setVisibility(View.VISIBLE);
                }
            });

        }

        @Override
        public void onDeviceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    discover.setVisibility(View.VISIBLE);
                    connect.setVisibility(View.GONE);
                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mBleAdmin = new BleAdmin(this, mDeviceCallBack);
        setupView();

    }


    private void setupView(){
        mScanButton = (Button) findViewById(R.id.start_scan);
        mDevicesRecycler = ((RecyclerView) findViewById(R.id.device_list));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDevicesRecycler.setLayoutManager(linearLayoutManager);
        mRecyclerAdapter = new BleDeviceAdapter();
        mDevicesRecycler.setAdapter(mRecyclerAdapter);
        mStopButton = (Button) findViewById(R.id.stop_scan);

        mDisconnecte = (Button) findViewById(R.id.diconnect);
        mWrite = (Button) findViewById(R.id.write);
        mEditData = (EditText) findViewById(R.id.write_data);
        mExpanListView = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mNotifyData = (TextView) findViewById(R.id.data);


        discover = (ConstraintLayout) findViewById(R.id.discover);
        connect = (ConstraintLayout) findViewById(R.id.connect_layout);

        //开始扫描
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleAdmin.openBle();
                mBleAdmin.startScanAllDevice(new ScanCallback() {
                    @Override
                    public void onDeviceFound(final List<BluetoothDevice> devices) {
                        Log.e("Found", " " + devices.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerAdapter.setDevices(devices);
                            }
                        });

                    }
                });
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleAdmin.stopScan();
            }
        });
    }



    private class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
        private List<BluetoothDevice> devices;
        private LayoutInflater mInflator;
        private Map<String, Integer> mDevicesRssi;

        public BleDeviceAdapter() {
            this.devices = new ArrayList<>();
            this.mDevicesRssi = new HashMap<>();
            this.mInflator = LibTestActivity.this.getLayoutInflater();
        }

        private void addDevice(BluetoothDevice device, int rssi) {
            if (!devices.contains(device)) {
                devices.add(device);
                mDevicesRssi.put(device.getAddress(), rssi);
                notifyDataSetChanged();
            }
        }

        private void setDevices(List<BluetoothDevice> devices){
            this.devices = devices;
            notifyDataSetChanged();
        }


        private void clear() {
            devices.clear();
        }


        @Override
        public BleDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflator.inflate(R.layout.item_device, parent, false);
            BleDeviceAdapter.ViewHolder item = new BleDeviceAdapter.ViewHolder(view);
            return item;
        }

        @Override
        public void onBindViewHolder(BleDeviceAdapter.ViewHolder holder, final int position) {
            BleDeviceAdapter.ViewHolder viewHolder = ((BleDeviceAdapter.ViewHolder) holder);
            final BluetoothDevice device = devices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknow");
            viewHolder.deviceAddress.setText(device.getAddress());

            viewHolder.deviceRssi.setText("" + mDevicesRssi.get(devices.get(position).getAddress()));

            viewHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBleAdmin.connectDevice(devices.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout content;
            private TextView deviceName;
            private TextView deviceAddress;
            private TextView deviceRssi;

            public ViewHolder(View itemView) {
                super(itemView);
                content = ((LinearLayout) itemView.findViewById(R.id.content));
                deviceAddress = ((TextView) itemView.findViewById(R.id.device_address));
                deviceName = (TextView) itemView.findViewById(R.id.device_name);
                deviceRssi = (TextView) itemView.findViewById(R.id.device_rssi);
            }

        }
    }
}
