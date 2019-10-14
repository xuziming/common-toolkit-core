package com.simon.credit.toolkit.codec.binary;

public interface BinaryDecoder extends Decoder {

	byte[] decode(byte[] source) throws DecoderException;

}
