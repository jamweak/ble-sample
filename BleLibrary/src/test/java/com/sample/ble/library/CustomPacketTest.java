package com.sample.ble.library;

import com.sample.ble.library.utils.DigestEncodingUtils;

import org.junit.Assert;
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

    @Test
    public void testParse() {
        byte[] test = DigestEncodingUtils.fromHexString("31A100020000A1B1");
        Assert.assertEquals(DigestEncodingUtils.encodeWithHex(CustomPacket.parseData(test)), "A1B1");

        byte[] test2 = DigestEncodingUtils.fromHexString("31A100040001A1B1A2B2");
        Assert.assertEquals(DigestEncodingUtils.encodeWithHex(CustomPacket.parseData(test2)), "A1B1A2B2");

        byte[] test3 = DigestEncodingUtils.fromHexString("31A100140002A1B1A2B2CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
        Assert.assertEquals(DigestEncodingUtils.encodeWithHex(CustomPacket.parseData(test3)), "A1B1A2B2CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
    }
}
