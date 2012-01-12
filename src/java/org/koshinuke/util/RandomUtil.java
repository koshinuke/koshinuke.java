package org.koshinuke.util;

import java.io.IOException;
import java.security.SecureRandom;

import net.iharder.Base64;

/**
 * @author taichi
 */
public class RandomUtil {

	static final SecureRandom random = new SecureRandom(
			SecureRandom.getSeed(512));

	public static String nextString(int byteLength) {
		try {
			byte[] bytes = new byte[byteLength];
			random.nextBytes(bytes);
			return Base64.encodeBytes(bytes, Base64.URL_SAFE);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String nextString() {
		return nextString(18);
	}
}
