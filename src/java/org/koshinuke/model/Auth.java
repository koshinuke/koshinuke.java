package org.koshinuke.model;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import com.sun.jersey.api.view.Viewable;

/**
 * @author taichi
 */
public class Auth {

	public final String csrf;

	public final String name;

	private Auth(String token) {
		this.csrf = token;
		this.name = "";
	}

	private Auth(String token, String name) {
		this.csrf = token;
		this.name = name;
	}

	public static Viewable of(String path, HttpSession session) {
		return new Viewable(path, new Auth(session.getId()));
	}

	public static Viewable of(String path, HttpSession session, Principal p) {
		return new Viewable(path, new Auth(session.getId(), p.getName()));
	}
}
