package org.koshinuke.service;

import javax.ws.rs.core.Response;

import org.koshinuke.model.KoshinukePrincipal;

import com.sun.jersey.api.representation.Form;

/**
 * @author taichi
 */
public interface FormAction {

	Response execute(KoshinukePrincipal p, Form form);
}
