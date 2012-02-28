package org.koshinuke.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.lib.Constants;
import org.koshinuke.model.Auth;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 * @see org.eclipse.jgit.http.server.GitSmartHttpTools
 */
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

	static final String segment = "([\\w\\-\\+\\.]|%[0-9a-fA-F]{2})";

	static final String basePath = "/{project: " + segment + "+}/{repository: "
			+ segment + "+}.git/";

	@Path(basePath + "git-upload-pack")
	@Consumes("application/x-git-upload-pack-request")
	@Produces("application/x-git-upload-pack-result")
	public Response uploadPack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@Path(basePath + "git-receive-pack")
	@Consumes("application/x-git-receive-pack-request")
	@Produces("application/x-git-receive-pack-result")
	public Response receivePack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@Path(basePath + Constants.INFO_REFS)
	public Response infoRefs(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@Path(basePath + "{file: objects/info/(http\\-)?alternates|HEAD}")
	public Response infoFile(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("file") String file) {
		return null;
	}

	@Path(basePath + "objects/info/packs")
	public Response infoPacks(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@Path(basePath + "objects/{oid: [0-9a-f]{2}/[0-9a-f]{38}}")
	public Response objectsLoose(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	static final String prefix_pack = "objects/{oid: pack/pack-[0-9a-f]{40}";

	@Path(basePath + prefix_pack + "\\.pack}")
	public Response objectsPack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@Path(basePath + prefix_pack + "\\.idx}")
	public Response objectsIdx(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}
}
