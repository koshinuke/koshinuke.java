package org.koshinuke.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.lib.Constants;
import org.koshinuke.jersey.auth.BasicAuth;

import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 * @see <a
 *      href="http://schacon.github.com/git/git-http-backend.html">git-http-backend(1)</a>
 */
@BasicAuth
@Singleton
@Path("/{project: ([\\w\\-\\+\\.]|%[0-9a-fA-F]{2})+}/{repository: ([\\w\\-\\+\\.]|%[0-9a-fA-F]{2})+}.git/")
public class GitHttpdService {

	/** Name of the git-upload-pack service. */
	public static final String UPLOAD_PACK = "git-upload-pack";

	/** Name of the git-receive-pack service. */
	public static final String RECEIVE_PACK = "git-receive-pack";

	static final String SUB_TYPE_UPD = "x-" + UPLOAD_PACK;
	static final String SUB_TYPE_RCV = "x-" + RECEIVE_PACK;
	static final String CT_UPD = "application/" + SUB_TYPE_UPD;
	static final String CT_RCV = "application/" + SUB_TYPE_RCV;

	@POST
	@Path(UPLOAD_PACK)
	@Consumes(CT_UPD + "-request")
	@Produces(CT_UPD + "-result")
	public Response uploadPack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@POST
	@Path(RECEIVE_PACK)
	@Consumes(CT_RCV + "-request")
	@Produces(CT_RCV + "-result")
	public Response receivePack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@GET
	@Path(Constants.INFO_REFS)
	public Response infoRefs(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@QueryParam("service") String service) {
		return null;
	}

	static final MediaType UPLOAD_PACK_INFO = new MediaType("application",
			SUB_TYPE_UPD + "-advertisement");
	static final MediaType RECEIVE_PACK_INFO = new MediaType("application",
			SUB_TYPE_RCV + "-advertisement");

	protected Response uploadPackInfo(String project, String repository) {
		return null;
	}

	protected Response receivePackInfo(String project, String repository) {
		return null;
	}

	@GET
	@Path("{file: objects/info/(http\\-)?alternates|HEAD}")
	public Response infoFile(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("file") String file) {
		return null;
	}

	@GET
	@Path("objects/info/packs")
	public Response infoPacks(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@GET
	// @HEAD
	@Path("{oid: objects/[0-9a-f]{2}/[0-9a-f]{38}}")
	public Response objectsLoose(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("oid") String oid) {
		return null;
	}

	static final String prefix_pack = "{oid: objects/pack/pack-[0-9a-f]{40}";

	@GET
	// @HEAD
	@Path(prefix_pack + "\\.pack}")
	public Response objectsPack(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}

	@GET
	// @HEAD
	@Path(prefix_pack + "\\.idx}")
	public Response objectsIdx(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return null;
	}
}