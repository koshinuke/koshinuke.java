package org.koshinuke.soy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.parseinfo.SoyFileInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.sun.jersey.api.view.Viewable;

/**
 * @author taichi
 */
public class SoyTemplatesModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<SoyFileInfo> mb = Multibinder.newSetBinder(binder(),
				SoyFileInfo.class);
		mb.addBinding().toInstance(LoginSoyInfo.getInstance());
		mb.addBinding().toInstance(RepoSoyInfo.getInstance());
	}

	public static Viewable of(SoyTemplateInfo t) {
		return of(t, null);
	}

	public static Viewable of(SoyTemplateInfo t, SoyMapData model) {
		return new Viewable("/" + t.getName(), model);
	}
}
