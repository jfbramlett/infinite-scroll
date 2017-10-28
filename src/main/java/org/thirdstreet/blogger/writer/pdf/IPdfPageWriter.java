package org.thirdstreet.blogger.writer.pdf;

import java.util.List;
import java.util.Properties;

import com.lowagie.text.Document;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogPost;

/**
 * Interface defining the methods a page writer must implement - a
 * page writer works like a layout manager for a page and controls
 * how the content is written out thus allow us to plug in a page
 * writer to handle content
 *
 * @author John Bramlett
 */
public interface IPdfPageWriter {
	
	/**
	 * Writes a page/chapter to our document
	 * @param document The document we are writing to
	 * @param blog The blog we are writing
	 * @param e The post we are writing
	 * @param comments The post comments
	 * @param chapter The chapter number
	 * @param includeComments Flag indicating if the comments should be written
	 */
	public void writeChapter(Document document, Blog blog, BlogPost e, List<BlogComment> comments, int chapter, boolean includeComments) throws Exception;
	
	/**
	 * Returns a boolean value indicating if the writer
	 * has any configurable properties
	 * @return boolean Returns true if the writer has
	 * configurable properties, false otherwise
	 */
	public boolean isConfigurable();
	
	/**
	 * Gets the properties for the page writer
	 * @return Properties The properties for the page writer
	 */
	public Properties getProperties();
	
	/**
	 * Sets the properties for the page writer
	 * @param newProps The new properties
	 */
	public void setProperties(Properties newProps);
}
