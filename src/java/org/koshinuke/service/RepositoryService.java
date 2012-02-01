package org.koshinuke.service;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.util.StringUtils;
import org.koshinuke.conf.Configuration;
import org.koshinuke.logic.GitDelegate;
import org.koshinuke.model.BlameModel;
import org.koshinuke.model.BlobModel;
import org.koshinuke.model.BranchHistoryModel;
import org.koshinuke.model.CommitModel;
import org.koshinuke.model.DiffModel;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.model.NodeModel;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Singleton
@Path("/dynamic")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	static final Logger LOG = Logger.getLogger(RepositoryService.class
			.getName());

	GitDelegate git;

	public RepositoryService(@Context Configuration config) {
		this.git = new GitDelegate(config);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable forwardToIndex(@Context HttpServletResponse res) {
		ServletUtil.redirect(res, "/");
		return null;
	}

	@GET
	public List<RepositoryModel> listRepository() {
		return this.git.listRepository();
	}

	@POST
	public Response init(@Context KoshinukePrincipal p,
			@FormParam("rn") String name, @FormParam("rr") String readme) {
		if (StringUtils.isEmptyOrNull(name) == false
				&& StringUtils.isEmptyOrNull(readme) == false) {
			String[] ary = name.split("/");
			if (ary.length == 2 && this.git.initRepository(p, name, readme)) {
				return Response.status(HttpServletResponse.SC_CREATED)
						.entity(this.listRepository()).build();
			}
		}
		return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
	}

	protected static Pattern isNumeric = Pattern.compile("[0-9]+");

	protected static int to(String s, int dv) {
		if (s != null && isNumeric.matcher(s).matches()) {
			return Integer.parseInt(s);
		}
		return dv;
	}

	static final String REV_PART = "{rev: ([a-zA-Z0-9/_\\-\\+\\.]|%[0-9a-fA-F]{2})+}";

	@GET
	@Path("/{project}/{repository}/tree/" + REV_PART)
	public Response tree(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev, @QueryParam("offset") String offset,
			// TODO config ?
			@QueryParam("limit") String limit) {

		List<NodeModel> list = this.git.listRepository(project, repository,
				rev, to(offset, 0), to(limit, 512));
		if (list == null) {
			return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
		} else {
			return Response.ok(list).build();
		}
	}

	@GET
	@Path("/{project}/{repository}/blob/" + REV_PART)
	public Response blob(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev) {
		BlobModel blob = this.git.getBlob(project, repository, rev);
		if (blob == null) {
			return Response.noContent().build();
		}
		return Response.ok(blob).build();
	}

	@POST
	@Path("/{project}/{repository}/blob/" + REV_PART)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response commit(@Context KoshinukePrincipal p,
			@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev, BlobModel input) {
		if (StringUtils.isEmptyOrNull(input.getContent()) == false
				&& StringUtils.isEmptyOrNull(input.getMessage()) == false) {
			BlobModel blob = this.git.modifyBlob(p, project, repository, rev,
					input);
			if (blob != null) {
				return Response.ok(blob).build();
			}
		}
		return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
	}

	@GET
	@Path("/{project}/{repository}/history")
	public Response histories(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		List<BranchHistoryModel> list = this.git.getHistories(project,
				repository);
		if (list == null) {
			return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
		}
		return Response.ok(list).build();
	}

	@GET
	@Path("/{project}/{repository}/commits/" + REV_PART)
	public Response commits(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev, @QueryParam("offset") String offset,
			// TODO config ?
			@QueryParam("limit") String limit) {
		List<CommitModel> list = this.git.getCommits(project, repository, rev,
				offset, to(limit, 128));
		if (list == null) {
			return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
		}
		return Response.ok(list).build();
	}

	@GET
	@Path("/{project}/{repository}/commit/{commitid: [a-zA-Z0-9]{2,40}}")
	public Response diff(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("commitid") String commitid) {
		DiffModel model = this.git.getDiff(project, repository, commitid);
		if (model == null) {
			return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
		}
		return Response.ok(model).build();
	}

	@GET
	@Path("/{project}/{repository}/blame/" + REV_PART)
	public Response blame(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev) {
		List<BlameModel> list = this.git.getBlame(project, repository, rev);
		if (list == null) {
			return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
		}
		return Response.ok(list).build();
	}
}
