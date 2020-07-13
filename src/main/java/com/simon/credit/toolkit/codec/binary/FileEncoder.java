package com.simon.credit.toolkit.codec.binary;

import java.io.File;
import java.io.IOException;

/**
 * 文件编码器
 * @author SimonX 2020-07-14
 */
public interface FileEncoder extends Encoder {

    File encode(File source) throws IOException;

}