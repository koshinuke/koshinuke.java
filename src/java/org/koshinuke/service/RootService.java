package org.koshinuke.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.koshinuke.model.Auth;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("")
public class RootService {

	@GET
	public Viewable index(@Context KoshinukePrincipal p,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		if (p == null) {
			ServletUtil.redirect(res, "/login");
			return null;
		}
		return Auth.of("/repos", req.getSession(), p);
	}

}
