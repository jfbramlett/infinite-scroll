package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;

import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;

/**
 * Handles multiple images alternating between left and
 * right justification with text wrapped
 *
 * @author John Bramlett
 */
public class MultiImageAlternateWrapTextPageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(MultiImageAlternateWrapTextPageWriter.class);
	
	/**
	 * Constructor
	 */
	public MultiImageAlternateWrapTextPageWriter() {
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

			// add our images
			List<BlogImage> images = content.getValidImages();

			if (images.size() > 0) {
				// write out our images
				final Image img = createImage(blog, content, images.get(0), Image.LEFT | Image.TEXTWRAP);
				if (img != null) {
					chpt.add(img);
				}
			}
		
			// and finally our content
			chpt.add(createContent(content));
			
			// now add our comments if we have any
			//if (includeComments && content.hasComments()) {
			//	chpt.add(createComments(content.getComments()));
			//}
			
			for (int i = 1; i < images.size(); i++) {
				// alternate our image alignment
				int alignment = 0;
				if ((i % 2) == 0) {
					alignment = Image.LEFT | Image.TEXTWRAP;
				}
				else {
					alignment = Image.RIGHT | Image.TEXTWRAP; 						
				}
				final Image img = createImage(blog, content, images.get(i), alignment);
				if (img != null) {
					chpt.add(img);
					chpt.add(Chunk.NEWLINE);
				}
			}

			logger.debug("Added post");
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}
}
