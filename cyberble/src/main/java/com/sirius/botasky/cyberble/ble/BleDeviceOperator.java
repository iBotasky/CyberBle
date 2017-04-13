package com.sirius.botasky.cyberble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.sirius.botasky.cyberble.callback.DeviceConnectCallback;

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
    private DeviceConnectCallback mDeviceConnectCallback;
    private BluetoothGattCallback mBluetoothGattCallback;
    private int mConnectState = STATE_DISCONNECTED;


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


    public BleDeviceOperator(BluetoothDevice mBluetoothDevice, Context mContext, BluetoothGattCallback mBluetoothCattCallback) {
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
    }

    /**
     * 连接这个蓝牙设备
     */
    public void connectDevice() {
        mConnectState = STATE_CONNECTING;
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback);
    }

}
