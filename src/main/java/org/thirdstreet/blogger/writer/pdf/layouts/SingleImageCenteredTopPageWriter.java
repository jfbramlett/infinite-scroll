package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;

import com.lowagie.text.Chapter;
import com.lowagie.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;

/**
 * General purpose page layout that writes an image up top centered
 * in the page followed by the post and then any comments
 *
 * @author John Bramlett
 */
public class SingleImageCenteredTopPageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(SingleImageCenteredTopPageWriter.class);

	/**
	 * Constructor
	 */
	public SingleImageCenteredTopPageWriter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.layouts.BasePageWriter#writeChapterContents(com.lowagie.text.Chapter, org.thirdstreet.blogger.blog.model.BlogPost, boolean)
	 */
	@Override
	protected void writeChapterContents(Chapter chpt, Blog blog, BlogPost content, List<BlogComment> comments, boolean includeComments) throws Exception {
		try {
			logger.debug("Adding new post to pdf file");

			// add our images
			List<BlogImage> images = content.getValidImages();

			if (images.size() > 0) {
				// write out our first image
				final Image img = createImage(blog, content, images.get(0), Image.ALIGN_MIDDLE);
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
						
			logger.debug("Added post");
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}
}
