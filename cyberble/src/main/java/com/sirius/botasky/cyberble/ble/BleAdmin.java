package com.sirius.botasky.cyberble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.sirius.botasky.cyberble.callback.DeviceConnectStateCallback;
import com.sirius.botasky.cyberble.callback.DeviceOperationCallback;
import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Bluetooth的总类，ble的一些操作，扫描跟设备管理类
 * Created by botasky on 13/04/2017.
 */

public class BleAdmin implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = BleAdmin.class.getSimpleName();


    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private int mDeviceCount;//搜索到的蓝牙设备的数量


    private ScanCallback mScanCallBack;
    private List<BluetoothDevice> mScanDevices;

    private Map<String, BleDeviceOperator> mConnectedDevice;
    private Map<String, BleDeviceOperator> mConnectingDevice;

    private DeviceConnectStateCallback mDeviceCallback;
    private DeviceOperationCallback mDeviceOperationCallback;

    public BleAdmin(Context mContext, DeviceConnectStateCallback deviceConnectCallback, DeviceOperationCallback mDeviceOperationCallback) {
        this.mContext = mContext;
        this.mDeviceCallback = deviceConnectCallback;
        this.mDeviceOperationCallback = mDeviceOperationCallback;
        initialize();
    }

    public void setScanCallBack(ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
    }

    /**
     * 初始化蓝牙一些操作
     */
    private void initialize() {
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mConnectedDevice = new HashMap<>();
        if (mBluetoothAdapter == null) {
            throw new IllegalArgumentException("Your device is not support ble");
        }
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    private boolean isBleEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * open ble
     */
    public void openBle() {
        if (!isBleEnable()) {
            mBluetoothAdapter.enable();
        }
    }


    /**
     * disable ble
     */
    public void disableBle() {
        if (isBleEnable()) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * 查找指定的UUID的设备
     *
     * @param specifyUUID
     */
    public void startScanWithSpecifyDevice(String specifyUUID, ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
        UUID[] sp = {UUID.fromString(specifyUUID)};
        startScan(sp);
    }

    public void startScanAllDevice(ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
        startScan(null);
    }

    private void startScan(UUID[] specify) {
        //初始化mScanDevices
        mDeviceCount = 0;
        if (mScanDevices == null) {
            mScanDevices = new ArrayList<>();
        }
        mScanDevices.clear();

        if (specify == null) {
            mBluetoothAdapter.startLeScan(this);
        } else {
            mBluetoothAdapter.startLeScan(specify, this);
        }
    }

    /**
     * 取消查找
     */
    public void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.e(TAG, " found" + mScanDevices.size());

        if (!mScanDevices.contains(device)) {
            mScanDevices.add(device);
        }
        if (mScanDevices.size() > mDeviceCount) {
            mScanCallBack.onDeviceFound(mScanDevices);
            mDeviceCount++;
        }
    }

    /**
     * 通过地址直接连接设备
     *
     * @param deviceAddress
     */
    public void connectDevice(String deviceAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        connectDevice(device);
    }

    /**
     * 通过设备直接连接
     *
     * @param device
     */
    public void connectDevice(final BluetoothDevice device) {
        BleDeviceOperator deviceOperator = new BleDeviceOperator(device, mContext, mBluetoothCattCallback);
        deviceOperator.connectDevice();
        if (mConnectingDevice == null) {
            mConnectingDevice = new HashMap<>();
        }
        if (!mConnectingDevice.containsKey(device.getAddress())) {
            mConnectingDevice.put(device.getAddress(), deviceOperator);
        }
    }

    /**
     * 发现服务
     *
     * @param address
     */
    public void discoverDeviceService(String address) {
        if (mConnectedDevice != null && mConnectedDevice.containsKey(address)) {
            mConnectedDevice.get(address).discoverDeviceServices();
        }
    }

//    /**
//     * 开启读特征
//     *
//     * @param address
//     * @param characteristic
//     */
//    public void readDeviceService(String address, BluetoothGattCharacteristic characteristic) {
//        if (!isReadCharacteristic(characteristic)) {
//            throw new IllegalArgumentException("The characteris is not a read charachteristic");
//        }
//        if (mConnectedDevice != null && mConnectedDevice.containsKey(address)) {
//            mConnectedDevice.get(address).
//        }
//    }

    /**
     * 蓝牙操作服务，
     * @param service
     */
    public void processDeviceService(BleDeviceService service){
        if (mConnectedDevice != null && mConnectedDevice.containsKey(service.getmDeviceAddress())){
            mConnectedDevice.get(service.getmDeviceAddress()).processService(service);
        }
    }




    //蓝牙连接状态
    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    private BluetoothGattCallback mBluetoothCattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String address = gatt.getDevice().getAddress();
            Log.e(TAG, " connect state is success");
            if (newState == STATE_CONNECTED) {
                Log.e(TAG, " connect state is " + newState);
                if (!mConnectedDevice.containsKey(address)) {
                    mConnectedDevice.put(address, mConnectingDevice.get(address));
                    mConnectingDevice.remove(address);
                }
                mDeviceCallback.onDeviceConnected(address);
            } else if (newState == STATE_DISCONNECTED) {
                Log.e(TAG, " connect state is fail");
                if (mConnectedDevice.containsKey(address)) {
                    mConnectedDevice.remove(address);
                }
                if (mConnectingDevice.containsKey(address)) {
                    mConnectingDevice.remove(address);
                }
                mDeviceCallback.onDeviceDisconnected();
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mConnectedDevice.get(address).setSerivce();
                mDeviceOperationCallback.onDeviceServiceDiscover(address, gatt.getServices());
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                mDeviceOperationCallback.onDeviceCharacteristicRead(gatt.getDevice().getAddress(), characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };


}
