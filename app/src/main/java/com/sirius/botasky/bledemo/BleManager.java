package com.sirius.botasky.bledemo;

import android.content.Context;

import com.sirius.botasky.cyberble.ble.BleAdmin;
import com.sirius.botasky.cyberble.callback.ScanCallback;

/**
 * Created by botasky on 28/04/2017.
 */

public class BleManager {
    private static BleAdmin mBleAdmin;
    private static BleManager mInstance;
    private static Context mContext;
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

    public void startScan(ScanCallback scanCallback){
        mBleAdmin.openBle();
        mBleAdmin.startScanAllDevice(scanCallback);
    }


    public void stopScan(){
        mBleAdmin.stopScan();
    }
}
