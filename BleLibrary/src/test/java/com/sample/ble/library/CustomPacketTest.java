package com.sample.ble.library;

import com.sample.ble.library.utils.DigestEncodingUtils;

import org.junit.Assert;
import org.junit.Test;

public class CustomPacketTest {
    @Test
    public void testDataLength() {
        CustomPacket.newPacket(DigestEncodingUtils.fromHexString("A1B2")).build();
    }
}
