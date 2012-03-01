package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.ObjectDirectory;
import org.eclipse.jgit.storage.file.PackFile;
import org.koshinuke.conf.Configuration;
import org.koshinuke.jersey.auth.BasicAuth;
import org.koshinuke.util.GitUtil;

import com.google.common.base.Function;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 * @see <a
 *      href="http://schacon.github.com/git/git-http-backend.html">git-http-backend(1)</a>
 * @see <a
 *      href="https://github.com/gitster/git/blob/master/http-backend.c">http-backend.c</a>
 */
@BasicAuth
@Singleton
@Path("/{project: ([\\w\\-\\+\\.]|%[0-9a-fA-F]{2})+}/{repository: ([\\w\\-\\+\\.]|%[0-9a-fA-F]{2})+}.git/")
public class GitHttpdService {

	public static final String UPLOAD_PACK = "git-upload-pack";
	public static final String RECEIVE_PACK = "git-receive-pack";

	static final String SUB_TYPE_UPD = "x-" + UPLOAD_PACK;
	static final String SUB_TYPE_RCV = "x-" + RECEIVE_PACK;
	static final String CT_UPD = "application/" + SUB_TYPE_UPD;
	static final String CT_RCV = "application/" + SUB_TYPE_RCV;

	Map<String, InfoRefsAction> actions = new HashMap<>(2);
	{
		this.actions.put(UPLOAD_PACK, new InfoRefsAction() {
			@Override
			public Response execute(String project, String repository) {
				return GitHttpdService.this.uploadPackInfo(project, repository);
			}
		});
		this.actions.put(RECEIVE_PACK, new InfoRefsAction() {
			@Override
			public Response execute(String project, String repository) {
				return GitHttpdService.this
						.receivePackInfo(project, repository);
			}
		});
	}

	protected final Configuration config;

	public GitHttpdService(@Context Configuration config) {
		this.config = config;

	}

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
	@Produces("text/plain; charset=utf-8")
	public Response infoRefs(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@QueryParam("service") String service,
			@Context HttpServletResponse response) throws IOException {
		InfoRefsAction action = this.actions.get(service);
		if (action != null) {
			return action.execute(project, repository);
		}
		return Response.status(Status.FORBIDDEN).build();
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
	@Path("{file: HEAD|objects/info/(http\\-)?alternates}")
	@Produces("text/plain; charset=utf-8")
	public Response infoFile(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("file") String file) {
		return this.buildFileResponse(project, repository, file,
				new Function<File, Response>() {
					@Override
					public Response apply(File input) {
						return noCache(Status.OK).entity(input).build();
					}
				});
	}

	public static ResponseBuilder noCache(Status status) {
		return Response
				.status(status)
				.expires(new Date(0))
				.header("Pragma", "no-cache")
				.cacheControl(
						CacheControl
								.valueOf("no-cache, max-age=0, must-revalidate"));
	}

	@GET
	@Path("objects/info/packs")
	public Response infoPacks(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		return this.buildResponse(project, repository,
				new Function<Repository, Response>() {
					@Override
					public Response apply(Repository input) {
						StringBuilder stb = new StringBuilder();
						ObjectDatabase odb = input.getObjectDatabase();
						if (odb instanceof ObjectDirectory) {
							ObjectDirectory dir = (ObjectDirectory) odb;
							for (PackFile pack : dir.getPacks()) {
								stb.append("P ");
								stb.append(pack.getPackFile().getName());
								stb.append('\n');
							}
						}
						stb.append('\n');
						return noCache(Status.OK).entity(stb.toString())
								.build();
					}
				});
	}

	@GET
	@Path("objects/{path: [0-9a-f]{2}/[0-9a-f]{38}}")
	@Produces("application/x-git-loose-object")
	public Response objectsLoose(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("path") String path) {
		return this.buildObjectsResponse(project, repository, path);
	}

	static final String packPath = "objects/{path: pack/pack-[0-9a-f]{40}}\\.";
	static final String CT_PACK = "application/x-git-packed-objects";

	@GET
	@Path(packPath + "pack}")
	@Produces(CT_PACK)
	public Response objectsPack(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("path") String path) {
		return this.buildObjectsResponse(project, repository, path);
	}

	@GET
	@Path(packPath + "idx}")
	@Produces(CT_PACK + "-toc")
	public Response objectsIdx(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("path") String path) {
		return this.buildObjectsResponse(project, repository, path);
	}

	protected Response buildObjectsResponse(String project, String repository,
			final String path) {
		return this.buildFileResponse(project, repository, path,
				new Function<File, Response>() {
					@Override
					public Response apply(File input) {
						Calendar c = Calendar.getInstance();
						Date now = c.getTime();
						c.add(Calendar.YEAR, 1);
						return Response
								.ok(input)
								.header(HttpHeaders.DATE, now)
								.expires(c.getTime())
								.cacheControl(
										CacheControl
												.valueOf("public, max-age=31536000"))
								.build();
					}
				});
	}

	protected boolean isEnabledGetanyfile(Repository repository) {
		return repository.getConfig().getBoolean("http", "getanyfile", true);
	}

	protected Response buildFileResponse(String project, String repository,
			final String path, final Function<File, Response> handler) {
		return this.buildResponse(project, repository,
				new Function<Repository, Response>() {
					@Override
					public Response apply(Repository input) {
						File odir = ((ObjectDirectory) input
								.getObjectDatabase()).getDirectory();
						File f = new File(odir, path);
						if (f.exists() == false) {
							return Response.status(Status.NOT_FOUND).build();
						}
						return handler.apply(f);
					}
				});
	}

	protected Response buildResponse(String project, String repository,
			final Function<Repository, Response> handler) {
		return GitUtil.handleLocal(this.config.getRepositoryRootDir(), project,
				repository, new Function<Repository, Response>() {
					@Override
					public Response apply(Repository input) {
						if (GitHttpdService.this.isEnabledGetanyfile(input) == false) {
							return Response.status(Status.FORBIDDEN).build();
						}
						return handler.apply(input);
					}
				});
	}
}
