package com.sirius.botasky.bledemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.sirius.botasky.bledemo.callbacks.ConnectResultCallback;
import com.sirius.botasky.bledemo.callbacks.OperationResultCallback;
import com.sirius.botasky.cyberble.ble.BleAdmin;
import com.sirius.botasky.cyberble.callback.DeviceConnectStateCallback;
import com.sirius.botasky.cyberble.callback.DeviceOperationCallback;
import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.List;

/**
 * Created by botasky on 28/04/2017.
 */

public class BleManager {
    private static BleAdmin mBleAdmin;
    private static BleManager mInstance;
    private static Context mContext;

    private DeviceConnectStateCallback mDeviceConnectStateCallback = new DeviceConnectStateCallback() {
        @Override
        public void onDeviceConnected(String address) {
            mConnectResultCallback.connectResult("地址：" + address + "连接成功", true);
        }

        @Override
        public void onDeviceDisconnected(String address) {
            mConnectResultCallback.connectResult("地址：" + address + "断开连接", false);
        }
    };

    private DeviceOperationCallback mDeviceOperationCallback = new DeviceOperationCallback() {
        @Override
        public void onDeviceServiceDiscover(boolean isSuccess, String deviceAddress, List<BluetoothGattService> services) {
            mOperationResultCallback.onDeviceDiscoverService(deviceAddress, services);
        }

        @Override
        public void onDeviceCharacteristicRead(boolean isSuccess, String deviceAddress, BluetoothGattCharacteristic characteristic) {
            mOperationResultCallback.onDeviceReadResult(deviceAddress, characteristic);
        }

        @Override
        public void onDeviceCharacteristicWrite(boolean isSuccess, String deviceAddress, BluetoothGattCharacteristic characteristic) {
            mOperationResultCallback.onDeviceWriteResult(deviceAddress, isSuccess);
        }

        @Override
        public void onDeviceCharacteristicNotify(String deviceAddress, BluetoothGattCharacteristic characteristic) {
            mDeviceOperationCallback.onDeviceCharacteristicNotify(deviceAddress, characteristic);
        }
    };


    private ConnectResultCallback mConnectResultCallback;
    private OperationResultCallback mOperationResultCallback;

    public void setmConnectResultCallback(ConnectResultCallback mConnectResultCallback) {
        this.mConnectResultCallback = mConnectResultCallback;
    }

    public void setmOperationResultCallback(OperationResultCallback mOperationResultCallback) {
        this.mOperationResultCallback = mOperationResultCallback;
    }

    public static BleManager getInstance() {
        if (mInstance == null) {
            mInstance = new BleManager();
        }
        return mInstance;
    }

    public static void init(Context context) {
        mContext = context;
        mBleAdmin = new BleAdmin(mContext);
    }

    public void startScan(ScanCallback scanCallback) {
        mBleAdmin.openBle();
        mBleAdmin.startScanAllDevice(scanCallback);
    }


    public void stopScan() {
        mBleAdmin.stopScan();
    }


    public void connect(BluetoothDevice device) {
        mBleAdmin.connectDevice(device);
        mBleAdmin.stopScan();
        mBleAdmin.setCallbacks(mDeviceOperationCallback, mDeviceConnectStateCallback);
    }

    public void disconnect(String address){
        mBleAdmin.disconnectDevice(address);
    }

}
