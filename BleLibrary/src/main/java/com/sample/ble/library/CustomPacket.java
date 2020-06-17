package com.sample.ble.library;

import android.util.Log;

import com.sample.ble.library.utils.DigestEncodingUtils;

import java.awt.font.TextAttribute;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CustomPacket {
    private static int HEAD_MSG_NUMBER = 0xA1;
    private static int HEAD_CHANNEL_WATCH_TO_SBM = 0x30;
    private static int HEAD_CHANNEL_SBM_TO_WATCH = 0x31;
    private static int MAX_SEQ_NUMBER = 65535;
    private static int seqNumber = 0;

    private byte[] packet;

    public CustomPacket(byte[] packet) {
        this.packet = packet;
    }

    public byte[] getPacket() {
        return packet;
    }

    public static Builder newPacket(byte[] data) {
        Builder builder = new Builder();
        builder.data = data;
        return builder;
    }

    public static byte[] parseData(byte[] packet) {
        if (packet[0] == (byte) HEAD_CHANNEL_SBM_TO_WATCH) {
            //TODO Length is uint16 type,which means it use packet[2] and packet[3], currently we only
            // read packet[3] for Convenient
            int dataLen = packet[3];
            byte[] data = new byte[dataLen];
            System.arraycopy(packet, 6, data, 0, dataLen);
            //System.out.println(DigestEncodingUtils.encodeWithHex(data));
            return data;
        } else {
            //System.out.println("Error! unexpected message: " + DigestEncodingUtils.encodeWithHex(packet));
            return packet;
        }
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
            int len = computeLength(data);
            packet.put((byte) (len >> 8));
            packet.put((byte) len);
            packet.put((byte) (seqNumber >> 8));
            packet.put((byte) seqNumber);
            if (++seqNumber > MAX_SEQ_NUMBER) {
                seqNumber = 0;
            }
            return packet.array();
        }

        private byte[] intToBytes(int intValue) {
            return new byte[]{(byte) (intValue >> 24), (byte) (intValue >> 16),
                    (byte) (intValue >> 8), (byte) (intValue & 0xff)};
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
