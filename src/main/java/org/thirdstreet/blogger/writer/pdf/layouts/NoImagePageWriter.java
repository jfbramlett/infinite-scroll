package org.thirdstreet.blogger.writer.pdf.layouts;

import com.lowagie.text.Chapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogPost;

import java.util.List;

/**
 * General purpose page layout that ignores any images just writing
 * the content and any posted comments
 *
 * @author John Bramlett
 */
public class NoImagePageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(NoImagePageWriter.class);

	/**
	 * Constructor
	 */
	public NoImagePageWriter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.layouts.BasePageWriter#writeChapterContents(com.lowagie.text.Chapter, org.thirdstreet.blogger.blog.model.BlogPost, boolean)
	 */
	@Override
	protected void writeChapterContents(Chapter chpt, Blog blog, BlogPost content,
										List<BlogComment> comments, boolean includeComments) throws Exception {
		try {
			logger.debug("Adding new post to pdf file");

			// and finally our content
			chpt.add(createContent(content));
			
			// now add our comments if we have any
			//if (includeComments && content.hasComments()) {
			//	chpt.add(createComments(content.getComments()));
			//}
						
			logger.debug("Added post");
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}
}
