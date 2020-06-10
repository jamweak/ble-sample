package com.sample.ble.library.utils;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import com.sample.ble.library.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class ScanUtil {
    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    public static List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(ParcelUuid.fromString(Constants.SBM_Service_UUID));
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    public static ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }
}
