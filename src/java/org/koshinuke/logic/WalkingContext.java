package org.koshinuke.logic;

/**
 * @author taichi
 */
class WalkingContext {
	final String rev;
	String root = "";
	String resource = "";

	public WalkingContext(String rev) {
		this.rev = rev;
	}
}