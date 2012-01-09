package org.koshinuke.service;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author taichi
 */
public class ServiceModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(LoginService.class).in(Singleton.class);
		binder.bind(RepositoryService.class).in(Singleton.class);
	}
}
