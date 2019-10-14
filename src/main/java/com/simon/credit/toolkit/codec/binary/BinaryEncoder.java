package com.simon.credit.toolkit.codec.binary;

public interface BinaryEncoder extends Encoder {

	byte[] encode(byte[] source) throws EncoderException;

}
