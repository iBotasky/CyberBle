package com.sirius.botasky.cyberble.callback;

/**
 * Created by botasky on 13/04/2017.
 */

public interface DeviceConnectStateCallback {
    /**
     * 设备连接上后的回调
     * @param address
     */
    void onDeviceConnected(String address);

    /**
     * 断开设备时的回调
     */
    void onDeviceDisconnected();
}
