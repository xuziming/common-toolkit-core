package com.simon.credit.toolkit.codec.binary;

import com.simon.credit.toolkit.lang.tuple.MyPair;

import java.io.File;
import java.io.IOException;

/**
 * 抽象文件混合编码解码器
 * @author SimonX 2020-07-14
 */
public abstract class FileMixCodec implements FileEncoder, FileDecoder {

    abstract void encode(File originFile, File encodeFile) throws IOException;

    abstract void decode(File encodeFile, File originFile) throws IOException;

    /**
     * 获取文件名与扩展名
     * @return
     */
    public MyPair<String, String> getBaseNameAndExtension(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null!");
        }

        String fileName  = file.getName();
        String baseName  = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return MyPair.of(baseName, extension);
    }

    @Override
    public File encode(File source) throws IOException {
        MyPair<String, String> baseNameAndExtension = getBaseNameAndExtension(source);
        File target = new File(source.getParent() + File.separator +
            baseNameAndExtension.getLeft() + "-encode" + "." + baseNameAndExtension.getRight());
        encode(source, target);
        return target;
    }

    @Override
    public Object encode(Object source) throws EncoderException {
        if (!(source instanceof File)) {
            throw new EncoderException("parameter supplied to file encode is not a file");
        }
        try {
            return encode((File) source);
        } catch (IOException e) {
            throw new EncoderException("io operation error!");
        }
    }

    @Override
    public File decode(File source) throws IOException {
        MyPair<String, String> baseNameAndExtension = getBaseNameAndExtension(source);
        File target = new File(source.getParent() + File.separator +
            baseNameAndExtension.getLeft() + "-decode" + "." + baseNameAndExtension.getRight());
        decode(source, target);
        return target;
    }

    @Override
    public Object decode(Object source) throws DecoderException {
        if (!(source instanceof File)) {
            throw new DecoderException("parameter supplied to file decode is not a file");
        }
        try {
            return decode((File) source);
        } catch (IOException e) {
            throw new DecoderException("io operation error!");
        }
    }

}