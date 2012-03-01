package org.koshinuke.jgit.server;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefAdvertiser;

/**
 * @author taichi
 */
public class EachRefPack {

	protected final Repository repository;

	public EachRefPack(Repository repository) {
		this.repository = repository;
	}

	public void sendAdvertisedRefs(RefAdvertiser advertiser) throws IOException {
		Map<String, Ref> refs = this.repository.getAllRefs();
		refs.remove(Constants.HEAD);
		advertiser.send(refs);
	}

	public void dispose() {
		this.repository.close();
	}
}
