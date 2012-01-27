package org.koshinuke.jackson;

import org.codehaus.jackson.map.PropertyNamingStrategy.PropertyNamingStrategyBase;

/**
 * @author taichi
 */
public class LowerCaseStrategy extends PropertyNamingStrategyBase {

	@Override
	public String translate(String propertyName) {
		if (propertyName != null) {
			return propertyName.toLowerCase();
		}
		return null;
	}

}
