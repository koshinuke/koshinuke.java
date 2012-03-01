package org.koshinuke.jgit.server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;

/**
 * @author taichi
 * @param <T>
 */
public abstract class AbstractPackInfoWriter<T> implements MessageBodyWriter<T> {

	final String service;
	final Class<?> writable;

	@SafeVarargs
	public AbstractPackInfoWriter(String service, T... type) {
		this.service = service;
		this.writable = type.getClass().getComponentType();
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return this.writable.isAssignableFrom(type);
	}

	@Override
	public long getSize(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		try {
			final PacketLineOut out = new PacketLineOut(entityStream);
			out.writeString("# service=" + this.service + "\n");
			out.end();
			RefAdvertiser advertiser = new PacketLineOutRefAdvertiser(out);
			this.advertise(t, advertiser);
		} finally {
			entityStream.flush();
		}
	}

	protected abstract void advertise(T pack, RefAdvertiser advertiser)
			throws IOException;
}
