package org.koshinuke.test;

import javax.ws.rs.core.Application;

import com.sun.jersey.test.framework.AppDescriptor;

/**
 * @author taichi
 */
public class SimpleAppDescriptor extends AppDescriptor {

	public static class Builder extends
			AppDescriptor.AppDescriptorBuilder<Builder, SimpleAppDescriptor> {
		Class<? extends Application> clazz;

		public Builder(Class<? extends Application> clazz) {
			this.clazz = clazz;
		}

		@Override
		public SimpleAppDescriptor build() {
			return new SimpleAppDescriptor(this);
		}

	}

	Class<? extends Application> clazz;

	public SimpleAppDescriptor(Builder builder) {
		super(builder);
		this.clazz = builder.clazz;
	}

	public Class<? extends Application> getApplicationClass() {
		return this.clazz;
	}
}
