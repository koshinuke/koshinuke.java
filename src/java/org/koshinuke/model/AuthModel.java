package org.koshinuke.model;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import com.sun.jersey.api.view.Viewable;

/**
 * @author taichi
 */
public class AuthModel {

	public final String csrf;

	public final String name;

	private AuthModel(String token) {
		this.csrf = token;
		this.name = "";
	}

	private AuthModel(String token, String name) {
		this.csrf = token;
		this.name = name;
	}

	public static Viewable of(String path, HttpSession session) {
		return new Viewable(path, new AuthModel(session.getId()));
	}

	public static Viewable of(String path, HttpSession session, Principal p) {
		return new Viewable(path, new AuthModel(session.getId(), p.getName()));
	}
}
