package org.koshinuke.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class ServletUtil {

	public static void redirect(HttpServletResponse res, String path) {
		try {
			res.sendRedirect(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
