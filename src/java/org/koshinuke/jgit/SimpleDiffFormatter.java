package org.koshinuke.jgit;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.patch.FileHeader;

/**
 * @author taichi
 */
public class SimpleDiffFormatter extends DiffFormatter {

	public SimpleDiffFormatter(OutputStream out) {
		super(out);
	}

	@Override
	public void format(final FileHeader head, final RawText a, final RawText b)
			throws IOException {
		this.format(head.toEditList(), a, b);
	}
}
