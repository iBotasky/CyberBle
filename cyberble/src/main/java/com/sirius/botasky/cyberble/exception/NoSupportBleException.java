package com.sirius.botasky.cyberble.exception;

/**
 * Created by botasky on 13/04/2017.
 */

public class NoSupportBleException extends Exception {
    public NoSupportBleException() {
        super("The device is not support ble");
    }
}
