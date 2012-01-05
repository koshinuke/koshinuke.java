package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.koshinuke.model.Repository;

public class RepositoryServiceTest {

	RepositoryService target;

	@Before
	public void setUp() {
		this.target = new RepositoryService();
	}

	@Test
	public void testName() {
		Repository r = this.target.name("a", "b");

		assertEquals(r.path, "a");
		assertEquals(r.name, "b");
	}

	@Test
	public void testTree() {
		assertNotNull(this.target.tree("a", "b", "c"));
	}

}
