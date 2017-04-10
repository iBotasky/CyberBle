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

/**
 * 连接到GATT服务端，服务端/主机不是手机，是设备
 */
public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = ConnectActivity.class.getSimpleName();
    public static final String DEVICE_ADDRESS = "device_address";

    private TextView mTvDeviceName;
    private Button connect;
    private Button disconnect;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mDeviceAddress;

    private int mConnectState;

    private ExpandableListView mExpanList;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

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
                dispalayGattServices();
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
        }


        /**
         * 当服务端的特征改变时触发的回调
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }


    };

    private void dispalayGattServices() {
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
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpanList.setAdapter(gattServiceAdapter);

            }
        });
    }


    private List<BluetoothGattService> getGattService() {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.getServices();
        }
        Log.e(TAG, "Gatt is null");
        return null;

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
        mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mExpanList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.diconnect);

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

    private void disconnect(){
        if (mBluetoothGatt != null && mBluetoothAdapter != null){
            mBluetoothGatt.disconnect();
        }
    }
}
