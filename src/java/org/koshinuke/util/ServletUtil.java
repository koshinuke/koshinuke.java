package org.koshinuke.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class ServletUtil {

	/**
	 * The 422 (Unprocessable Entity) status code means the server understands
	 * the content type of the request entity (hence a 415(Unsupported Media
	 * Type) status code is inappropriate), and the syntax of the request entity
	 * is correct (thus a 400 (Bad Request) status code is inappropriate) but
	 * was unable to process the contained instructions. For example, this error
	 * condition may occur if an XML request body contains well-formed (i.e.,
	 * syntactically correct), but semantically erroneous, XML instructions.
	 * 
	 * @see http://tools.ietf.org/html/rfc4918#section-11.2
	 */
	public static int SC_UNPROCESSABLE_ENTITY = 422;

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
