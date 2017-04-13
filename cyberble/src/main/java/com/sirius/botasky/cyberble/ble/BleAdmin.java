package com.sirius.botasky.cyberble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Bluetooth的总类，ble的一些操作，扫描跟设备管理类
 * Created by botasky on 13/04/2017.
 */

public class BleAdmin implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = BleAdmin.class.getSimpleName();


    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private int mDeviceCount;


    private ScanCallback mScanCallBack;
    private List<BluetoothDevice> mScanDevices;

    public BleAdmin(Context mContext) {
        this.mContext = mContext;
        initialize();
    }

    public void setScanCallBack(ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
    }

    /**
     * 初始化蓝牙一些操作
     */
    private void initialize() {
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null || mContext.getApplicationContext()
//                        .getPackageManager()
//                        .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
//            Log.e(TAG, " Device is not support ble");
//            return;
//        }
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    private boolean isBleEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * open ble
     */
    public void openBle() {
        if (!isBleEnable()) {
            mBluetoothAdapter.enable();
        }
    }


    /**
     * disable ble
     */
    public void disableBle() {
        if (isBleEnable()) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * 查找指定的UUID的设备
     *
     * @param specifyUUID
     */
    public void startScanWithSpecifyDevice(String specifyUUID, ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
        UUID[] sp = {UUID.fromString(specifyUUID)};
        startScan(sp);
    }

    public void startScanAllDevice(ScanCallback mScanCallBack) {
        this.mScanCallBack = mScanCallBack;
        startScan(null);
    }

    private void startScan(UUID[] specify) {
        //初始化mScanDevices
        mDeviceCount = 0;
        if (mScanDevices == null) {
            mScanDevices = new ArrayList<>();
        }
        mScanDevices.clear();

        if (specify == null) {
            mBluetoothAdapter.startLeScan(this);
        } else {
            mBluetoothAdapter.startLeScan(specify, this);
        }
    }

    /**
     * 取消查找
     */
    public void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.e(TAG, " found" + mScanDevices.size());

        if (!mScanDevices.contains(device)) {
            mScanDevices.add(device);
        }
        if (mScanDevices.size() > mDeviceCount) {
            mScanCallBack.onDeviceFound(mScanDevices);
            mDeviceCount++;
        }
    }

}
