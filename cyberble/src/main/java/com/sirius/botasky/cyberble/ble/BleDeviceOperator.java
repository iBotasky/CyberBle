package com.sirius.botasky.cyberble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 这个类用来对一个蓝牙设备进行操作的Operator,包括Connect,Read,Write,Notify,
 * Created by botasky on 13/04/2017.
 */

public class BleDeviceOperator {
    private static final String TAG = BleDeviceOperator.class.getSimpleName();

    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;


    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private Context mContext;
    private BluetoothGattCallback mBluetoothGattCallback;
    private int mConnectState = STATE_DISCONNECTED;
    private List<BleDeviceService> mOpeationService;
    private List<BluetoothGattService> mBluetoothGattService;


//    private BluetoothGattCallback mBluetoothCattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                mConnectState = STATE_CONNECTED;
//                mDeviceConnectCallback.onDeviceConnected(BluetoothProfile.STATE_CONNECTED);
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                mConnectState = STATE_DISCONNECTED;
//                mDeviceConnectCallback.onDeviceConnected(BluetoothProfile.STATE_DISCONNECTED);
//            }
//
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicRead(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//        }
//    };

    /**
     * 蓝牙设备操作类
     *
     * @param mBluetoothDevice
     * @param mContext
     * @param mBluetoothCattCallback
     */
    public BleDeviceOperator(BluetoothDevice mBluetoothDevice,
                             Context mContext,
                             BluetoothGattCallback mBluetoothCattCallback) {
        this.mBluetoothDevice = mBluetoothDevice;
        this.mContext = mContext;
        this.mBluetoothGattCallback = mBluetoothCattCallback;
        initialize();

    }

    private void initialize() {
        if (mBluetoothDevice == null) {
            throw new IllegalArgumentException("BleDevice is null");
        }
        mBluetoothDeviceAddress = mBluetoothDevice.getAddress();
        mOpeationService = new ArrayList<>();
    }

    /**
     * 连接这个蓝牙设备,得到Gatt
     */
    public void connectDevice() {
        mConnectState = STATE_CONNECTING;
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback);
    }

    /**
     * 发现这个设备的服务
     */
    public void discoverDeviceServices() {
        if (isGattValid()) {
            mBluetoothGatt.discoverServices();
        }
    }


    /***
     * 蓝牙操作处理入口，分别分发到READ，NOTIFY，WRITE
     * @param bleDeviceService
     */
    public void processService(BleDeviceService bleDeviceService) {
        if (mBluetoothGattService == null || mBluetoothGattService.size() == 0) {
            Log.e(TAG, "The service is not valid");
            return;
        }
        UUID characteristicUUID = bleDeviceService.getmCharacteristicUUID();
        BluetoothGattCharacteristic processCharacteristic = null;
        for (BluetoothGattService service : mBluetoothGattService) {
            if (service.getCharacteristic(characteristicUUID) == null) {
                continue;
            } else {
                processCharacteristic = service.getCharacteristic(characteristicUUID);
                break;
            }
        }

        if (isProcessValid(processCharacteristic, bleDeviceService)) {
            switch (bleDeviceService.getmOperationType()) {
                case Read:
                    readCharacteristic(processCharacteristic, bleDeviceService.getmOperationType());
            }
        }

    }

    /**
     * BleAdmin服务回调成功后调用，用来获得Service
     */
    public void setSerivce() {
        if (isGattValid()) {
            mBluetoothGattService = mBluetoothGatt.getServices();
        }
    }

    /**
     * 读操作
     *
     * @param characteristic
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic, BleDeviceService.OperateType type) {
        if (isGattValid()) {
            if (type != BleDeviceService.OperateType.Read) {
                Log.e(TAG, "The process is wrong");
            }
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }


    /**
     * 判定Process是否有效
     *
     * @param characteristic
     * @param service
     * @return
     */
    private boolean isProcessValid(BluetoothGattCharacteristic characteristic, BleDeviceService service) {
        if (characteristic == null) {
            Log.e(TAG, "the charatsic is not valid");
            return false;
        }
        int charaProp = characteristic.getProperties();
        switch (service.getmOperationType()) {
            case Read:
                return (charaProp & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
            case Notify:
                return (charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            case Write:
                return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                        && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
        }
        return false;
    }

    /**
     * 校验gatt是否存在
     *
     * @return
     */
    private boolean isGattValid() {
        if (mBluetoothGatt == null) {
            throw new IllegalArgumentException("The Gatt is unvalid");
        }
        return true;
    }


}
