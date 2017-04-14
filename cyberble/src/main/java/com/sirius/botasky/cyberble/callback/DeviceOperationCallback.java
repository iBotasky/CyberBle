package com.sirius.botasky.cyberble.callback;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by botasky on 13/04/2017.
 */

public interface DeviceOperationCallback {
    void onDeviceServiceDiscover(String deviceAddress, List<BluetoothGattService> services);

    void onDeviceCharacteristicRead(String deviceAddress, BluetoothGattCharacteristic characteristic);

    void onDeviceCharacteristicWrite(String deviceAddress);

    void onDeviceCharacteristicNotify(String deviceAddress, BluetoothGattCharacteristic characteristic);
}
