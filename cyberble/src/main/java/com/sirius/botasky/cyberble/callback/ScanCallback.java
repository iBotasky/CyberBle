package com.sirius.botasky.cyberble.callback;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by botasky on 13/04/2017.
 */

public interface ScanCallback {
    void onDeviceFound(List<BluetoothDevice> devices);
}
