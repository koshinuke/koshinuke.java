package org.koshinuke.jgit.server;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.koshinuke.conf.Configuration;

/**
 * @author taichi
 */
public class GitHttpTransfer {

	protected Configuration config;

	public GitHttpTransfer(Configuration config) {
		this.config = config;
	}

	protected UploadPackFactory<HttpServletRequest> uploadPackFactory = new UploadPackFactory<HttpServletRequest>() {
		@Override
		public UploadPack create(HttpServletRequest req, Repository db)
				throws ServiceNotEnabledException,
				ServiceNotAuthorizedException {
			if (db.getConfig().getBoolean("http", "uploadpack", true)) {
				return new UploadPack(db);
			}
			throw new ServiceNotEnabledException();
		}
	};

	protected ReceivePackFactory<HttpServletRequest> receivePackFactory = new ReceivePackFactory<HttpServletRequest>() {
		@Override
		public ReceivePack create(HttpServletRequest req, Repository db)
				throws ServiceNotEnabledException,
				ServiceNotAuthorizedException {
			if (db.getConfig().getBoolean("http", "receivepack", false)) {
				ReceivePack rp = new ReceivePack(db);
				// TODO 送信ユーザのきちんとした情報を設定する。
				Principal p = req.getUserPrincipal();
				rp.setRefLogIdent(new PersonIdent(p.getName(), p.getName()
						+ "@" + req.getRemoteHost()));
				return rp;
			}
			throw new ServiceNotEnabledException();
		}
	};

}
