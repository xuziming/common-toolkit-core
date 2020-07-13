package com.simon.credit.toolkit.codec.binary;

import com.simon.credit.toolkit.io.IOToolkits;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 文件编码解码器
 * @author SimonX 2020-07-14
 */
public class FileXCodec extends FileMixCodec {

    @Override
    public final void encode(File originFile, File encodeFile) throws IOException {
        byte[] data = IOToolkits.readFileToByteArray(originFile);
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] + 1);
        }
        IOToolkits.writeByteArrayToFile(encodeFile, data);
    }

    @Override
    public final void decode(File encodeFile, File originFile) throws IOException {
        byte[] data = IOToolkits.readFileToByteArray(encodeFile);
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] - 1);
        }
        IOToolkits.writeByteArrayToFile(originFile, data);
    }

    private final void encode1To2(File originFile, File encodeFile) throws IOException {
        byte[] data = IOToolkits.readFileToByteArray(originFile);
        System.out.println(data.length);
        byte[] encodeData = new byte[data.length * 2];
        for (int i = 0; i < encodeData.length - 1; i += 2) {
            encodeData[i] = data[i / 2];
            encodeData[i + 1] = (byte) new Random().nextInt(Byte.MAX_VALUE);
        }
        IOToolkits.writeByteArrayToFile(encodeFile, encodeData);
    }

    private final void decode2To1(File encodeFile, File originFile) throws IOException {
        byte[] encodeData = IOToolkits.readFileToByteArray(encodeFile);
        System.out.println(encodeData.length);
        byte[] data = new byte[encodeData.length / 2];

        for (int i = 0; i < encodeData.length - 1; i += 2) {
            data[i / 2] = encodeData[i];
        }
        IOToolkits.writeByteArrayToFile(originFile, data);
    }

    private final void encodeWithinHeader(File originFile, File encodeFile) throws IOException {
        byte[] header = "SimonX".getBytes();
        byte[] body = IOToolkits.readFileToByteArray(originFile);

        byte[] data = new byte[header.length + body.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(body, 0, data, header.length, body.length);

        IOToolkits.writeByteArrayToFile(encodeFile, data);
    }

    private final void decodeWithoutHeader(File encodeFile, File originFile) throws IOException {
        byte[] header = "SimonX".getBytes();
        byte[] data = IOToolkits.readFileToByteArray(encodeFile);

        byte[] body = new byte[data.length - header.length];
        System.arraycopy(data, header.length, body, 0, data.length - header.length);

        IOToolkits.writeByteArrayToFile(originFile, body);
    }

}