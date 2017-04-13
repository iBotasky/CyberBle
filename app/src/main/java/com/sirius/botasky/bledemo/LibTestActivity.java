package com.sirius.botasky.bledemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.sirius.botasky.cyberble.ble.BleAdmin;
import com.sirius.botasky.cyberble.callback.DeviceConnectStateCallback;
import com.sirius.botasky.cyberble.callback.DeviceOperationCallback;
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
    private String mCurrentDeviceAddress;

    private DeviceConnectStateCallback mDeviceCallBack = new DeviceConnectStateCallback() {
        @Override
        public void onDeviceConnected(final String address) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBleAdmin.discoverService(address);
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

    private DeviceOperationCallback mDeviceOperationCallback = new DeviceOperationCallback() {
        @Override
        public void onDeviceServiceDiscover(String deviceAddress, List<BluetoothGattService> services) {
            displayGattServices(services);
        }

        @Override
        public void onDeviceCharacteristicRead(String deviceAddress) {

        }

        @Override
        public void onDeviceCharacteristicWrite(String deviceAddress) {

        }

        @Override
        public void onDeviceCharacteristicNotify(String deviceAddress) {

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
        mBleAdmin = new BleAdmin(this, mDeviceCallBack, mDeviceOperationCallback);
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

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    /**
     * 将获取到的服务展示出来，根据SampleGattAttributes对比，看有；没有符合的服务
     */
    private void displayGattServices(List<BluetoothGattService> services) {

        if (services == null) return;
        String uuid = null;
        String unknownServiceString = "未知服务";
        String unknownCharaString = "未知特征";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        for (BluetoothGattService gattService : services) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpanListView.setAdapter(gattServiceAdapter);

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

        private void setDevices(List<BluetoothDevice> devices){
            this.devices = devices;
            notifyDataSetChanged();
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
                    mCurrentDeviceAddress = devices.get(position).getAddress();
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
