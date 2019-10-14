package com.simon.credit.toolkit;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.simon.credit.toolkit.codec.binary.Base64X;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

public class RSATest {

	/** RSA非对称加解密算法 */
	private static final String RSA_ALGORITHM 		= "RSA";
	/** 签名算法 */
	private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * 初始化公钥与秘钥
	 * @throws NoSuchAlgorithmException 
	 */
	public static KeyPair initKey() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * 获得公钥字符串
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static String getPublicKeyBase64String(KeyPair keyPair) throws Exception {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// base64编码
		return encryptBASE64(publicKey.getEncoded());
	}

	/**
	 * 获得私钥字符串
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static String getPrivateKeyBase64String(KeyPair keyPair) throws Exception {
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// base64编码
		return encryptBASE64(privateKey.getEncoded());
	}

	/**
	 * 解析公钥
	 * @param publicKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static PublicKey parsePublicKey(String publicKeyBase64String) throws Exception {
		byte[] keyBytes = decryptBASE64(publicKeyBase64String);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 解析私钥
	 * @param privateKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey parsePrivateKey(String privateKeyBase64String) throws Exception {
		byte[] keyBytes = decryptBASE64(privateKeyBase64String);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * base64解码
	 * @param base64String base64字符串
	 * @return 原文(字节数组形式)
	 * @throws Exception
	 */
	public static byte[] decryptBASE64(String base64String) {
		return Base64X.decodeBase64(base64String);
		// return (new BASE64Decoder()).decodeBuffer(key);
	}

	/**
	 * 编码返回字符串
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encryptBASE64(byte[] data) throws Exception {
		return Base64X.encodeBase64String(data);
		// return (new BASE64Encoder()).encodeBuffer(key);
	}

	/**
	 * 签名
	 * @param data
	 * @param privateKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(byte[] data, String privateKeyBase64String) throws Exception {
		PrivateKey privateKey = parsePrivateKey(privateKeyBase64String);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 验签
	 * @param data
	 * @param sign
	 * @param publicKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, byte[] sign, String publicKeyBase64String) throws Exception {
		PublicKey publicKey = parsePublicKey(publicKeyBase64String);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(sign);
	}

	/**
	 * 公钥加密
	 * @param plainText
	 * @param publicKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] plainText, String publicKeyBase64String) throws Exception {
		PublicKey publicKey = parsePublicKey(publicKeyBase64String);
		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		int inputLen = plainText.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		int i = 0;
		byte[] cache;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(plainText, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(plainText, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptText = out.toByteArray();
		out.close();
		return encryptText;
	}

	/**
	 * 私钥解密
	 * @param encryptText
	 * @param privateKeyBase64String
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptText, String privateKeyBase64String) throws Exception {
		PrivateKey privateKey = parsePrivateKey(privateKeyBase64String);
		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		int inputLen = encryptText.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptText, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptText, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] plainText = out.toByteArray();
		out.close();
		return plainText;
	}

	public static void main(String[] args) {
		String input = "Java World";
		try {
			KeyPair keyPair = initKey();
			String publicKeyBase64String = getPublicKeyBase64String(keyPair);
			System.out.println("公钥------------------");
			System.out.println(publicKeyBase64String);

			String privateKeyBase64String = getPrivateKeyBase64String(keyPair);
			System.out.println("私钥------------------");
			System.out.println(privateKeyBase64String);

			System.out.println("测试可行性-------------------");
			System.out.println("明文================" + input);

			// 公钥加密
			byte[] cipherText = encrypt(input.getBytes(), publicKeyBase64String);
			System.out.println("密文==================" + new String(cipherText));

			// 私钥解密
			byte[] originData = decrypt(cipherText, privateKeyBase64String);
			System.out.println("解密后明文===== " + new String(originData));

			System.out.println("验证签名----------------------------------");

			String str = "不积跬步无以至千里";
			System.out.println("\n原文:" + str);

			// 私钥签名
			byte[] signature = sign(str.getBytes(), privateKeyBase64String);

			// 公钥验签
			boolean status = verify(str.getBytes(), signature, publicKeyBase64String);
			System.out.println("验证情况：" + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}