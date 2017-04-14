package com.sirius.botasky.cyberble.ble;

import java.util.UUID;

/**
 * 用来封装蓝牙的一些操作动作，READ，WRITE，NOTIFY
 * Created by botasky on 14/04/2017.
 */

public class BleDeviceService {
    private String mDeviceAddress;
    private UUID mCharacteristicUUID;
    private OperateType mOperationType;

    private boolean isCharacteristicOperating = false;

    public BleDeviceService(String mDeviceAddress,UUID mCharacteristicUUID, OperateType operateType) {
        this.mDeviceAddress = mDeviceAddress;
        this.mCharacteristicUUID = mCharacteristicUUID;
        this.mOperationType = operateType;
    }

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }


    public UUID getmCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public OperateType getmOperationType() {
        return mOperationType;
    }

    /**
     * 操作状态
     * @return
     */
    public boolean isCharacteristicOperating() {
        return isCharacteristicOperating;
    }

    public void setCharacteristicOperating(boolean characteristicOperating) {
        isCharacteristicOperating = characteristicOperating;
    }



    public enum OperateType{
        Read("Read"),
        Write("Write"),
        Notify("Notify");


        private String desc;

        OperateType(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return this.desc.toString();
        }
    }
}
