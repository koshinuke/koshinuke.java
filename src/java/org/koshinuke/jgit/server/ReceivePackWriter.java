package org.koshinuke.jgit.server;

import java.io.IOException;

import javax.ws.rs.ext.Provider;

import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser;

/**
 * @author taichi
 */
@Provider
public class ReceivePackWriter extends AbstractPackInfoWriter<ReceivePack> {

	public ReceivePackWriter() {
		super(GitHttpdService.RECEIVE_PACK);
	}

	@Override
	protected void advertise(ReceivePack pack, RefAdvertiser advertiser)
			throws IOException {
		try {
			pack.sendAdvertisedRefs(advertiser);
		} finally {
			pack.getRevWalk().release();
		}
	}
}