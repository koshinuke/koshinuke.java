package org.koshinuke.model;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author taichi
 */
public class DefaultKoshinukePrincipal implements KoshinukePrincipal,
		Serializable {

	private static final long serialVersionUID = 6756819198900957569L;

	final String name;
	final String mail;

	public DefaultKoshinukePrincipal(Principal principal) {
		this.name = principal.getName();
		this.mail = ""; // TODO ユーザ属性情報をどうやって確保するか…
	}

	public DefaultKoshinukePrincipal(String name, String mail) {
		this.name = name;
		this.mail = mail;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getMail() {
		return this.mail;
	}

}
