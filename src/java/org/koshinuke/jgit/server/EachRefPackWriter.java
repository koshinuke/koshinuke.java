package org.koshinuke.jgit.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.jgit.transport.RefAdvertiser;

import com.google.common.base.Charsets;

/**
 * @author taichi
 */
@Provider
public class EachRefPackWriter implements MessageBodyWriter<EachRefPack> {

	public EachRefPackWriter() {
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return EachRefPack.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(EachRefPack t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(EachRefPack pack, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		try {
			final OutputStreamWriter out = new OutputStreamWriter(entityStream,
					Charsets.UTF_8.name());
			final RefAdvertiser advertiser = new RefAdvertiser() {
				@Override
				protected void writeOne(final CharSequence line)
						throws IOException {
					out.append(line.toString().replace(' ', '\t'));
				}

				@Override
				protected void end() {
				}
			};
			advertiser.init(pack.repository);
			advertiser.setDerefTags(true);
			try {
				pack.sendAdvertisedRefs(advertiser);
				out.flush();
			} finally {
				pack.dispose();
			}
		} finally {
			entityStream.flush();
		}
	}
}
