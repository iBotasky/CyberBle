package com.sirius.botasky.bledemo.callbacks;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by botasky on 12/05/2017.
 * 根据自己需求写需要回调的接口
 */

public interface OperationResultCallback {
    //address用来判断多个设备时区分那个设备
    void onDeviceDiscoverService(String address, List<BluetoothGattService> services);
    void onDeviceReadResult(String address, BluetoothGattCharacteristic characteristic);
    void onDeviceWriteResult(String address, boolean isSuccess);
    void onDevoiceNotifyData(String address, BluetoothGattCharacteristic characteristic);
}
