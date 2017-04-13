package com.sirius.botasky.cyberble.callback;

/**
 * Created by botasky on 13/04/2017.
 */

public interface DeviceConnectStateCallback {
    void onDeviceConnected(String address);

    void onDeviceDisconnected();
}
