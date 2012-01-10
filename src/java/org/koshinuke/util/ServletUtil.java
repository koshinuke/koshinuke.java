package org.koshinuke.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class ServletUtil {

	public static final String CSRFTOKEN = "csrfToken";

	public static void redirect(HttpServletResponse res, String path) {
		try {
			res.sendRedirect(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object setToken(HttpSession session) {
		Object o = session.getAttribute(CSRFTOKEN);
		if (o == null) {
			o = RandomUtil.nextString(20);
			session.setAttribute(CSRFTOKEN, o);
		}
		return o;
	}

	public static boolean verifyCsrf(HttpSession session, String client) {
		if (session == null) {
			return false;
		}
		String server = (String) session.getAttribute(CSRFTOKEN);
		return StringUtils.isEmptyOrNull(server) == false
				&& server.equals(client);
	}
}
