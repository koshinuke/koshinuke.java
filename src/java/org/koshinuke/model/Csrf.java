package org.koshinuke.model;

import javax.servlet.http.HttpSession;

import com.sun.jersey.api.view.Viewable;

/**
 * @author taichi
 */
public class Csrf {

	public final String csrf;

	private Csrf(String token) {
		this.csrf = token;
	}

	public static Viewable of(String path, HttpSession session) {
		return new Viewable(path, new Csrf(session.getId()));
	}
}
