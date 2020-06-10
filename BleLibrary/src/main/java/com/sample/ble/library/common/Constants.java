package com.sample.ble.library.common;

public class Constants {
    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     * <p>
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final String SBM_Service_UUID = "000ffa0-0000-1000-8000-00805f9b34fb";
    public static final String SBM_WRITE_CHARACTERISTIC_UUID = "0000ffa1-0000-1000-8000-00805f9b34fb";
    public static final String SBM_READ_CHARACTERISTIC_UUID = "0000ffa2-0000-1000-8000-00805f9b34fb";
}
