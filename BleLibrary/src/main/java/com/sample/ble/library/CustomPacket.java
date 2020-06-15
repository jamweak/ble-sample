package com.sample.ble.library;

import com.sample.ble.library.utils.DigestEncodingUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CustomPacket {
    private static int HEAD_MSG_NUMBER = 0xA1;
    private static int HEAD_CHANNEL_WATCH_TO_SBM = 0x30;
    private static int HEAD_CHANNEL_SBM_TO_WATCH = 0x31;

    private byte[] packet;

    public CustomPacket(byte[] packet) {
        this.packet = packet;
    }

    public byte[] getPacket() {
        return packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }

    public static Builder newPacket(byte[] data) {
        Builder builder = new Builder();
        builder.data = data;
        return builder;
    }

    public static class Builder {
        byte[] head; // ID, SEQ_NUM(0xA1), MSG_LENGTH, MSG_ORDER
        byte[] data;

        public CustomPacket build() {
            head = calculateHead();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(DigestEncodingUtils.encodeWithHex(head));
            stringBuilder.append(DigestEncodingUtils.encodeWithHex(data));
            return new CustomPacket(DigestEncodingUtils.fromHexString(stringBuilder.toString()));
        }

        private byte[] calculateHead() {
            ByteBuffer packet = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
            packet.put((byte) HEAD_CHANNEL_WATCH_TO_SBM);
            packet.put((byte) HEAD_MSG_NUMBER);
//            int len = computeLength(data);
            int len = 999999999;
            System.out.println(DigestEncodingUtils.encodeWithHex(intToBytes(len)));
            packet.put((byte) ((0xff00 & len) >> 2));
            packet.put((byte) (0x00ff & len));
            packet.put(DigestEncodingUtils.fromHexString("0000"));
            System.out.println(DigestEncodingUtils.encodeWithHex(packet.array()));
            return packet.array();
        }

        private byte[] intToBytes(int intValue) {
            return new byte[]{(byte) (intValue >> 24), (byte) (intValue >> 16), (byte) (intValue >> 8), (byte) (intValue & 0xff)};
        }
        private int computeLength(byte[] data) {
            String s = DigestEncodingUtils.encodeWithHex(data);
            return s.length() / 2;
        }
    }

    @Override
    public String toString() {
        return "CustomPacket{" +
                "packet == " + DigestEncodingUtils.encodeWithHex(packet) +
                '}';
    }
}
