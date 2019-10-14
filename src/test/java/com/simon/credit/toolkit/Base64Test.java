package com.simon.credit.toolkit;

import com.simon.credit.toolkit.codec.binary.Base64X;

public class Base64Test {

	public static void main(String[] args) throws Exception {
		// final BASE64Encoder encoder = new BASE64Encoder();
		// final BASE64Decoder decoder = new BASE64Decoder();
		// final String text = "字串文字";
		// final byte[] textByte = text.getBytes("UTF-8");
		// // 编码
		// final String encodedText = encoder.encode(textByte);
		// System.out.println(encodedText);
		// // 解码
		// System.out.println(new String(decoder.decodeBuffer(encodedText), "UTF-8"));

		final Base64X base64 = new Base64X();
		final String text = "字串文字";
		final byte[] textByte = text.getBytes("UTF-8");
		// 编码
		final String encodedText = base64.encodeToString(textByte);
		System.out.println(encodedText);
		// 解码
		System.out.println(new String(base64.decode(encodedText), "UTF-8"));
	}

}
