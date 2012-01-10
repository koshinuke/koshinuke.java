package org.koshinuke.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import net.iharder.Base64;

import com.google.common.base.Preconditions;

/**
 * @author taichi
 */
public class RandomUtil {

	static final SecureRandom RANDOM = new SecureRandom(
			SecureRandom.getSeed(400));

	static final String fixedsalt = "thisIsthefixeDsAlt";
	static final int stretchCount = 10000;

	static void nextRandom(byte[] bytes) {
		Preconditions.checkNotNull(bytes, "bytes");
		RANDOM.nextBytes(bytes);
	}

	static byte[] getSalt(String uid) {
		return (uid + fixedsalt).getBytes();
	}

	public static String hash(String u, String p) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] salt = getSalt(u);
			md.update(p.getBytes());
			for (int i = 0; i < stretchCount; i++) {
				md.update(salt);
			}
			byte[] result = md.digest();
			return Base64.encodeBytes(result, Base64.URL_SAFE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String nextString() {
		return nextString(1024);
	}

	public static String nextString(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length");
		}
		try {
			byte[] bytes = new byte[length];
			nextRandom(bytes);
			return Base64.encodeBytes(bytes, Base64.URL_SAFE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
