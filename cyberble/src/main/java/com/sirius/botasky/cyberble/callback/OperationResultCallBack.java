package com.sirius.botasky.cyberble.callback;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by botasky on 13/04/2017.
 */

public interface OperationResultCallBack {
    void onDeviceServiceDiscover(List<BluetoothGattService> services);

    void onDeviceCharacteristicRead();

    void onDeviceCharacteristicWrite();

    void onDeviceCharacteristicNotify();
}
