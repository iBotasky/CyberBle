package com.sirius.botasky.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 连接到GATT服务端，服务端/主机不是手机，是设备
 */
public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = ConnectActivity.class.getSimpleName();
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_NAME = "device_name";

    private TextView mTvDeviceName;
    private Button connect;
    private Button disconnect;
    private Button write;
    private TextView mTvData;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mDeviceAddress;

    private int mConnectState;

    private ExpandableListView mExpanList;


    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public final static UUID UUID_INSOLE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.UUID_NOTIFY);

    //断开状态
    private static final int STATE_DISCONNECTED = 0;
    //连接中状态
    private static final int STATE_CONNECTING = 1;
    //已连接状态
    private static final int STATE_CONNECTED = 2;


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    /**
     * 连接到GATT服务端时，由BLE设备做主机，
     * 并返回一个BluetoothGatt实例，
     * 然后你可以使用这个实例来进行GATT客户端操作。
     * 请求方（Android app)是GATT客户端。
     * BluetoothGattCallback用于传递结果给用户，
     * 例如连接状态，以及任何进一步GATT客户端操作。
     */
    private final BluetoothGattCallback mBluetoothGattcallback = new BluetoothGattCallback() {
        /**
         * 服务端的蓝牙设备状态改变时的回调，连接上/断开
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG, "state change " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectState = STATE_CONNECTED;
                //调用去获取服务信息
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectState = STATE_DISCONNECTED;
            }

        }

        /**
         * 当发现服务端的新的服务/特征/描述的时候的回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(TAG, "GattServiceDiscover" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //展示获取到的服务
                displayGattServices();
            }
            Log.e(TAG, "GATTCallback  Service discover " + status);
        }

        /**
         * 返回一个特征的读操作的结果
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG,  " onCharacteristicRead call back" );
            if (status == BluetoothGatt.GATT_SUCCESS){
                displayData(characteristic);
            }

        }


        /**
         * 当服务端的特征改变时触发的回调
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG,  " onCharacteristicChange call back" );
            displayData(characteristic);
        }


        /**
         * 返回一个写特征的操作结果
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicWrite " +characteristic.getUuid() + " " +  status);
        }
    };

    /**
     * 将获取到的服务展示出来，根据SampleGattAttributes对比，看有；没有符合的服务
     */
    private void displayGattServices() {
        List<BluetoothGattService> services = getGattService();
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
                mExpanList.setAdapter(gattServiceAdapter);

            }
        });
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    private List<BluetoothGattService> getGattService() {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.getServices();
        }
        Log.e(TAG, "Gatt is null");
        return null;

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
            setDataMainThread(String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                setDataMainThread(new String(data) + "\n" + stringBuilder.toString());
            }
        }
    }

    /**
     * 数据展示要放在主线程展示，否则会报错
     *
     * @param data
     */
    private void setDataMainThread(final String data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvData.setText(data);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.e(TAG, "DeviceAddress" + getIntent().getStringExtra(DEVICE_ADDRESS));
        mDeviceAddress = getIntent().getStringExtra(DEVICE_ADDRESS);
        initiailizeView();
        initiailizeBle();

    }

    private void initiailizeView() {
//        mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mTvDeviceName.setText(getIntent().getStringExtra(DEVICE_NAME));
        mTvData = (TextView) findViewById(R.id.data);

        mExpanList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mExpanList.setOnChildClickListener(servicesListClickListner);
//        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.diconnect);
        write = (Button) findViewById(R.id.write);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                                setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            //开启读特征
                            readCharacteristic(characteristic);
                        }
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            setCharacteristicNotification(characteristic, true);
                        }
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                                && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0){
                            Log.e(TAG, "Characteristic this is a write characteristic");
                            writeCharacteristic(characteristic);
                        }

                        return true;
                    }
                    return false;
                }
            };

    /**
     * 写特征
     * @return
     */
    private boolean writeCharacteristic(BluetoothGattCharacteristic charac){

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
//        BluetoothGattService Service = mBluetoothGatt.getService(your Services);
//        if (Service == null) {
//            Log.e(TAG, "service not found!");
//            return false;
//        }
//        BluetoothGattCharacteristic charac = Service
//                .getCharacteristic(your characteristic);
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        String start = "ES";
        byte[] value = start.getBytes();
        charac.setValue(value);
        //这边的status是指发送命令的状态，不是指Write成功或者失败的状态， 所以还要一个onChartacteristicWrite的回调，在GattCallBack里面
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        Log.e(TAG, " write status " + status);
        return status;
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //通用协议心率的NOTIFY特征
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()) || UUID_INSOLE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 初始化
     */
    private void initiailizeBle() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Error " + "Unable to initialize BluetoothManager.");
                return;
            }
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mConnectState = STATE_DISCONNECTED;//设置当前连接状态为未连接
        }
    }

    private void connect() {
        //获取设备
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        //判断是否获取到设备
        if (device == null) {
            Snackbar.make(mTvDeviceName, "Device not found, Unable to connect", Snackbar.LENGTH_LONG).show();
        }
        //直连设备,获取设备的Gatt
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattcallback);
        mConnectState = STATE_CONNECTING;//设置当前状态为连接中
    }

    private void disconnect() {
        if (mBluetoothGatt != null && mBluetoothAdapter != null) {
            mBluetoothGatt.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 使用完蓝牙后要调用close，释放系统资源
         */
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }
}
