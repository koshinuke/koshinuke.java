package org.koshinuke.util;

import java.io.IOException;

/**
 * @author taichi
 */
public class IORuntimeException extends RuntimeException {

	private static final long serialVersionUID = 8520889821506439967L;

	public IORuntimeException(IOException cause) {
		super(cause);
	}
}
