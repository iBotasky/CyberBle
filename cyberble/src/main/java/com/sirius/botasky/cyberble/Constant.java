package com.sirius.botasky.cyberble;

import java.util.UUID;

/**
 * Created by botasky on 14/04/2017.
 */

public class Constant {
    //蓝牙在Notify或者Indicate的时候需要对一个Descriptor来操作Notify/Indicate的ENABLE/DISABLE
    public static final UUID NOTIFY_OR_INDICATE_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
