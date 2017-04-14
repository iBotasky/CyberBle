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
import com.sirius.botasky.cyberble.ble.BleDeviceService;
import com.sirius.botasky.cyberble.callback.DeviceConnectStateCallback;
import com.sirius.botasky.cyberble.callback.DeviceOperationCallback;
import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sirius.botasky.bledemo.ConnectActivity.UUID_HEART_RATE_MEASUREMENT;

public class LibTestActivity extends AppCompatActivity {
    public static final String TAG = LibTestActivity.class.getSimpleName();


    private Button mScanButton, mStopButton, mDisconnecte, mWrite;
    private EditText mEditData;
    private ExpandableListView mExpanListView;
    private RecyclerView mDevicesRecycler;
    private BleDeviceAdapter mRecyclerAdapter;
    private BleAdmin mBleAdmin;
    private TextView mNotifyData;
    private ConstraintLayout discover, connect;
    private String mCurrentDeviceAddress;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private DeviceConnectStateCallback mDeviceCallBack = new DeviceConnectStateCallback() {
        @Override
        public void onDeviceConnected(final String address) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBleAdmin.discoverDeviceService(address);
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
        public void onDeviceCharacteristicRead(String deviceAddress, BluetoothGattCharacteristic characteristic) {
            displayData(characteristic);
        }

        @Override
        public void onDeviceCharacteristicWrite(String deviceAddress) {

        }

        @Override
        public void onDeviceCharacteristicNotify(String deviceAddress, BluetoothGattCharacteristic characteristic) {
            displayData(characteristic);

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


    private void setupView() {
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
        mExpanListView.setOnChildClickListener(servicesListClickListner);
        mNotifyData = (TextView) findViewById(R.id.data);

        mWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleAdmin.processDeviceService(new BleDeviceService(
                        mCurrentDeviceAddress,
                        UUID.fromString(SampleGattAttributes.INSOLE_WRITE),
                        BleDeviceService.OperateType.Write,
                        mEditData.getText().toString().getBytes()));
            }
        });


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

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    /**
     * 如果一个GATT的特征被选中，
     */
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            //如果当前有一个NOTIFY的特征正在活动，要先关闭NOTIFY特征，再去开启READ特征
                            if (mNotifyCharacteristic != null) {
//                                setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            //开启读特征
                            mBleAdmin.processDeviceService(new BleDeviceService(mCurrentDeviceAddress, characteristic.getUuid(), BleDeviceService.OperateType.Read));
//                            readCharacteristic(characteristic);
                        }
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBleAdmin.processDeviceService(new BleDeviceService(mCurrentDeviceAddress, characteristic.getUuid(), BleDeviceService.OperateType.Notify));
                        }
//                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
//                                && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0){
//                            Log.e(TAG, "Characteristic this is a write characteristic");
//                            writeCharacteristic(characteristic);
//                        }

                        return true;
                    }
                    return false;
                }
            };


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


    /**
     * 展示READ或者NOTIFY特征返回的信息
     *
     * @param
     */
    private void displayData(BluetoothGattCharacteristic characteristic) {
        /**
         * 通用心率协议的NOTIFY特征需要特殊处理
         */
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNotifyData.setText(String.valueOf(heartRate));
                }
            });
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNotifyData.setText(new String(data) + "\n" + stringBuilder.toString());
                    }
                });

            }
        }
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

        private void setDevices(List<BluetoothDevice> devices) {
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
