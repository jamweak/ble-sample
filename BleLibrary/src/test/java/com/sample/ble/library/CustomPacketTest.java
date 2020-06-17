package com.sample.ble.library;

import com.sample.ble.library.utils.DigestEncodingUtils;

import org.junit.Test;

public class CustomPacketTest {
    @Test
    public void testGenerate() {
        CustomPacket a1 = CustomPacket.newPacket(DigestEncodingUtils.fromHexString("A1B1")).build();
        CustomPacket a2 = CustomPacket.newPacket(DigestEncodingUtils.fromHexString("A2B2")).build();
        CustomPacket a3 = CustomPacket.newPacket(DigestEncodingUtils.fromHexString("A3B3")).build();
        System.out.println(a1);
        System.out.println(a2);
        System.out.println(a3);
    }
}
