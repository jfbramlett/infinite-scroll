package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;
import java.util.Properties;

import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.writer.pdf.PdfFont;

/**
 * General purpose page layout that writes an image up top centered
 * in the page followed by the post and then any comments
 *
 * @author John Bramlett
 */
public class LeadingImagesCenteredTopRestBelowPageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(LeadingImagesCenteredTopRestBelowPageWriter.class);

	protected static final String kLeadingImages = "Max Leading Images";
	protected int leadingImages = 1;
	
	
	/**
	 * Constructor
	 */
	public LeadingImagesCenteredTopRestBelowPageWriter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.layouts.BasePageWriter#getProperties()
	 */
	@Override
	public Properties getProperties() {
		Properties props = super.getProperties();
		if (props == null) {
			props = new Properties();
		}
		props.put(kLeadingImages, Integer.toString(leadingImages));
		return props;
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.layouts.BasePageWriter#isConfigurable()
	 */
	@Override
	public boolean isConfigurable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.layouts.BasePageWriter#setProperties(java.util.Properties)
	 */
	@Override
	public void setProperties(Properties newProps) {
		super.setProperties(newProps);
		String val = newProps.getProperty(kLeadingImages);
		if (NumberUtils.isNumber(val)) {
			leadingImages = NumberUtils.createInteger(val);
		}
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
				for (int i = 0 ; i < leadingImages; i++) {					
					// write out our image
					if (i < images.size()) {
						final Image img = createImage(blog, content, images.get(i), Image.ALIGN_MIDDLE);
						if (img != null) {
							chpt.add(img);
						}
					}
				}
			}
			
			// and finally our content
			chpt.add(createContent(content));
			
			// now add our comments if we have any
			//if (includeComments && content.hasComments()) {
			//	chpt.add(createComments(content.getComments()));
			//}

			// handle the remaining images
			if (images.size() > leadingImages) {
				for (int i = leadingImages; i < images.size(); i++) {
					BlogImage image = images.get(i);
		
					// see if we need to do a new page
					if ((i % 2) == 1) {
						// go to a new page
						logger.debug("Adding new page");
						chpt.add(Chunk.NEXTPAGE);

						// add our posting date
						Paragraph postingDateParagraph2 = new Paragraph(convertDate(content.getPublished().getTime()) + " (cont'd)", PdfFont.kPostingDateFont.getFont());
						postingDateParagraph2.setIndentationLeft(kContentIndent);
						chpt.add(postingDateParagraph2);
					}
	
					// now add our image
					final Image img = createImage(blog, content, image, Image.ALIGN_MIDDLE);
					if (img != null) {
						chpt.add(img);
					}
				}
			}
			
			logger.debug("Added post");
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the leadingImages
	 * @return int The leadingImages
	 */
	public int getLeadingImages() {
		return leadingImages;
	}

	/**
	 * Sets the leadingImages
	 * @param leadingImages the leadingImages to set
	 */
	public void setLeadingImages(int leadingImages) {
		this.leadingImages = leadingImages;
	}


}
