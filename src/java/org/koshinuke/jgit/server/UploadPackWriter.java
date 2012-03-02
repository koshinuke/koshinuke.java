package org.koshinuke.jgit.server;

import java.io.IOException;

import javax.ws.rs.ext.Provider;

import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.UploadPack;

/**
 * @author taichi
 */
@Provider
public class UploadPackWriter extends AbstractPackInfoWriter<UploadPack> {

	public UploadPackWriter() {
		super(GitHttpdService.UPLOAD_PACK);
	}

	@Override
	protected void advertise(UploadPack pack, RefAdvertiser advertiser)
			throws IOException {
		try {
			pack.sendAdvertisedRefs(advertiser);
		} finally {
			pack.getRevWalk().release();
		}
	}
}
