package com.simon.credit.toolkit.codec.binary;

import java.io.File;
import java.io.IOException;

/**
 * 文件解码器
 * @author SimonX 2020-07-14
 */
public interface FileDecoder extends Decoder {

    File decode(File source) throws IOException;

}