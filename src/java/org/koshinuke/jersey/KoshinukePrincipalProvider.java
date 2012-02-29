package org.koshinuke.jersey;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.koshinuke.jersey.auth.AuthenticationFilterFactory;
import org.koshinuke.model.KoshinukePrincipal;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Provider
@Singleton
public class KoshinukePrincipalProvider implements
		InjectableProvider<Context, Type> {

	@Context
	ThreadLocal<HttpServletRequest> requestInvoker;

	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<KoshinukePrincipal> getInjectable(ComponentContext ic,
			Context a, Type t) {
		if (KoshinukePrincipal.class.equals(t) == false) {
			return null;
		}
		return new Injectable<KoshinukePrincipal>() {
			@Override
			public KoshinukePrincipal getValue() {
				HttpServletRequest request = KoshinukePrincipalProvider.this.requestInvoker
						.get();
				return AuthenticationFilterFactory.getUserPrincipal(request);
			}
		};
	}
}
