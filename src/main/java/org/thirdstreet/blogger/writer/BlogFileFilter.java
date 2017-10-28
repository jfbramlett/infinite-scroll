package org.thirdstreet.blogger.writer;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Utility class used to filter the set of files to only those
 * that are part of our blog
 * @author bramlej
 */
public class BlogFileFilter implements FilenameFilter {

	protected String blogId;

	/**
	 * Constructor
	 * 
	 */
	public BlogFileFilter(String blogId_) {
		super();

		this.blogId = blogId_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String filename) {
		boolean result = false;

		// files are valid only if they start with the blog id and
		// end with .xml
		if (filename.startsWith(blogId) && (filename.endsWith(".xml"))) {
			result = true;
		}
		return result;
	}

}
