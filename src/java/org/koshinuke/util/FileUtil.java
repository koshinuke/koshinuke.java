package org.koshinuke.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author taichi
 */
public class FileUtil {

	public interface NameFilter {
		boolean accept(String name);
	}

	public interface FileHandler {
		void handle(File file);
	}

	public static final NameFilter NULL_FILTER = new NameFilter() {
		@Override
		public boolean accept(String path) {
			return true;
		}
	};

	public static class PatternFilter implements NameFilter {
		protected Pattern pattern;

		public PatternFilter(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		public PatternFilter(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean accept(String path) {
			return this.pattern.matcher(path).matches();
		}
	}

	public static class ReverseFilter implements NameFilter {
		protected NameFilter delegate;

		public ReverseFilter(NameFilter filter) {
			this.delegate = filter;
		}

		@Override
		public boolean accept(String path) {
			return this.delegate.accept(path) == false;
		}
	}

	public static void walk(String path, NameFilter filter, FileHandler handler) {
		File f = new File(path);
		if (filter.accept(f.getName())) {
			if (f.isDirectory()) {
				for (String s : f.list()) {
					walk(new File(f, s).getPath(), filter, handler);
				}
			}
			if (f.exists()) {
				handler.handle(f);
			}
		}
	}

	public static List<File> list(String path) {
		return list(path, NULL_FILTER);
	}

	public static List<File> list(String path, NameFilter filter) {
		final List<File> list = new ArrayList<File>();
		walk(path, filter, new FileHandler() {
			@Override
			public void handle(File file) {
				if (file.isFile()) {
					list.add(file);
				}
			}
		});
		return list;
	}

	public static void delete(String path) {
		delete(path, NULL_FILTER);
	}

	public static void delete(String path, NameFilter filter) {
		walk(path, filter, new FileHandler() {
			@Override
			public void handle(File file) {
				file.delete();
			}
		});
	}

}
