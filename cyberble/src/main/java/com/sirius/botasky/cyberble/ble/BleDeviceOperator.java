package com.sirius.botasky.cyberble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.sirius.botasky.cyberble.Constant;

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

    public void disconnectDevice(){
        mConnectState = STATE_DISCONNECTED;
        mBluetoothGatt.disconnect();
//        mBluetoothGatt.close();
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
    public synchronized void processService(BleDeviceService bleDeviceService) {
        mOpeationService.add(bleDeviceService);
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
                    readCharacteristic(processCharacteristic);
                    break;
                case Notify:
                case Indicate:
                    notifyCharacteristic(
                            processCharacteristic,
                            BleDeviceService.OperateType.Notify != bleDeviceService.getmOperationType());
                    break;
                case Write:
                    writeCharacteristic(processCharacteristic, bleDeviceService.getWriteData());
                    break;
            }
        }

    }

    /**
     * Read操作
     *
     * @param characteristic
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (isGattValid()) {
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    /**
     * Notify/Indicate操作
     *
     * @param characteristic
     */
    private void notifyCharacteristic(BluetoothGattCharacteristic characteristic, boolean isIndicate) {
        if (isGattValid()) {
            if (mBluetoothGatt.setCharacteristicNotification(characteristic, true)) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(Constant.NOTIFY_OR_INDICATE_DESCRIPTOR_UUID);
                if (descriptor != null) {
                    byte[] writingValue = isIndicate ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    descriptor.setValue(writingValue);
                    mBluetoothGatt.writeDescriptor(descriptor);
                } else {
                    Log.e(TAG, characteristic.getUuid() + "上在找不到Config Descriptor");
                }
            }
        }
    }

    /**
     * Write操作
     * @param characteristic
     * @param writeData
     */
    private boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] writeData){
        if (isGattValid()){
            characteristic.setValue(writeData);
            boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
            return status;
        }
        return false;
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
        Log.e(TAG, " Operation type is not support for the charachteristic");
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
