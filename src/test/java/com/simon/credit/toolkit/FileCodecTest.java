package com.simon.credit.toolkit;

import com.simon.credit.toolkit.codec.binary.EncoderException;
import com.simon.credit.toolkit.codec.binary.FileMixCodec;
import com.simon.credit.toolkit.codec.binary.FileXCodec;

import java.io.File;
import java.io.IOException;

public class FileCodecTest {

    public static void main(String[] args) throws IOException, EncoderException {
        long start = System.currentTimeMillis();
        FileMixCodec codec = new FileXCodec();
        // 编码
//        File encodeFile = codec.encode(new File("C:\\Users\\Administrator\\Desktop\\test.avi"));
//        System.out.println(encodeFile.getPath());
        // 解码
        File decodeFile = codec.decode(new File("/Users/xuziming/ssh/download/pd-api-gateway-k-encode.tar"));
        System.out.println(decodeFile.getPath());
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

}