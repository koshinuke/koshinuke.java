package org.koshinuke.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class ServletUtil {

	public static void redirect(HttpServletResponse res, String path) {
		try {
			res.sendRedirect(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean verifyCsrf(HttpSession session, String client) {
		if (session == null) {
			return false;
		}
		return StringUtils.isEmptyOrNull(client) == false
				&& client.equals(session.getId());
	}
}
