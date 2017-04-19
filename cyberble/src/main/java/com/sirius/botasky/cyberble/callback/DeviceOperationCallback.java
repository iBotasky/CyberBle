package com.sirius.botasky.cyberble.callback;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by botasky on 13/04/2017.
 */

public interface DeviceOperationCallback {
    /**
     * 发现BLE设备的服务的回调
     * @param deviceAddress
     * @param services
     */
    void onDeviceServiceDiscover(String deviceAddress, List<BluetoothGattService> services);

    /**
     * 对一个BLE设备进行READ操作的回调
     * @param deviceAddress
     * @param characteristic
     */
    void onDeviceCharacteristicRead(String deviceAddress, BluetoothGattCharacteristic characteristic);

    /**
     * 对一个BLE设备进行WRITE操作的回调
     * @param deviceAddress
     */
    void onDeviceCharacteristicWrite(String deviceAddress);

    /**
     * 对一个BLE设备进行NOTIFY的回调
     * @param deviceAddress
     * @param characteristic
     */
    void onDeviceCharacteristicNotify(String deviceAddress, BluetoothGattCharacteristic characteristic);
}
