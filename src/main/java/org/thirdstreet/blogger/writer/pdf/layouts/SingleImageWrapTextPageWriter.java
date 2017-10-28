package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;

import com.lowagie.text.Chapter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.writer.pdf.PdfFont;

/**
 * Base class for our single image layouts that wrap the text to the
 * left or right
 *
 * @author John Bramlett
 */
public class SingleImageWrapTextPageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(SingleImageWrapTextPageWriter.class);
	
	protected int alignment;
	
	/**
	 * Constructor
	 * @param alignment The alignment
	 */
	public SingleImageWrapTextPageWriter(int alignment) {
		super();
		this.alignment = alignment;
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

			Image img = null;
			if (images.size() > 0) {
				// get our image
				img = createImage(blog, content, images.get(0), alignment | Image.TEXTWRAP);

				if (img != null) {
					// write out our images
					chpt.add(img);
				}
			}
		
			// the following is work to make sure the text wrap
			// works correctly - for some reason if the text
			// is too short to wrap then it screws everything
			// up, the text ends up overlaying the image, out
			// title gets skewed, all sorts of garbage. So this
			// code attempts to add enough newlines to make it
			// seem like it is wrapping
			// kind of hacky but seems to work
			
			// get our content
			Paragraph paragraph = createContent(content);

			// determine our text area width
			float textWidth = getTextWidth(img);
			
			// determine how many lines we have
			int lineCount = getLineCount(content.getText(), PdfFont.kContentFont,textWidth);
			
			// calculate the height of our content
			
			// now get the line height - seems like totalLeading will tell us
			// the size of the line which is the font size plus the spacing
			float lineHeight = paragraph.getTotalLeading();
				
			// and from the lines calculate the projected height
			float textHeight = lineCount * lineHeight; 
			
			// now add CR's to space this out
			// if that is less than the image height add some carriage returns
			float imageHeight = getImageHeight(img, lineHeight);
			
			if (textHeight < imageHeight) {
				// see how many newlines to add
				float newLines = (imageHeight-textHeight)/lineHeight;
				for (int i = 0; i < newLines + 1; i++) {
					paragraph.add(kNewLine);
				}
				
			}
			
			// and finally our content
			chpt.add(paragraph);
			
			// now handle our comments
			if (includeComments && comments.size() > 0) {
				chpt.add(createComments(comments));
			}
			
			// now let's see what we have in the way of comments
			// now add our comments if we have any
			//if (content.hasComments()) {
			//	Paragraph comments = createComments(content.getComments());
				
			//	List<BlogComment> blogComments = content.getComments();
				
				// it is made up of a comment line
				//int totalHeaderLine = blogComments.size() * 
				
				//chpt.add(createComments(content.getComments()));
			//}			
						
			logger.debug("Added post");
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the available text width with the image on the same line
	 * @param img The image we are working with
	 * @return float Our text width
	 */
	protected float getTextWidth(Image img) {		
		// now figure our the size of the text block - it
		// is basically the content width minus the image size
		float docWidth = document.getPageSize().getWidth();
		float leftMargin = document.leftMargin() * 1.5f;
		float rightMargin = document.rightMargin() * 1.5f;
		float pageWidth = docWidth - leftMargin - rightMargin;
		
		// the following to determine our text width, we take the page width
		// (minus our margins) and remove the space used by the image we then
		// add a little padding to account for the space between the image
		// and the text - this might be better served using the paragraph
		// indent but the math worked out ok with this
		float imgWidth = 0;
		if (img != null) {
			imgWidth = img.getWidth();
		}
		
		return pageWidth - imgWidth - rightMargin;		
	}
	
	/**
	 * Gets our image height
	 * @param img The image we are getting the height for
	 * @param lineHeight The line height - we use this to pad our height a little
	 * as we don't want the image to be just slightly smaller than the text, we
	 * need the text to have a full line beneath the image
	 * @return float The image height
	 */
	protected float getImageHeight(Image img, float lineHeight) {
		float result = 0;
		if (img != null) {
			result = img.getHeight();
		}
		
		// add a bit of padding
		result = result + lineHeight - 1;
		
		return result;
	}
	
	/**
	 * Gets a projected line count for the content in the given text area
	 * @param content The content
	 * @param contentFont The font for the content
	 * @param textWidth The available text area
	 * @return int The number of lines the content would equate to
	 */
	protected int getLineCount(String content, PdfFont contentFont, float textWidth) {
		int lines = 0;

		// the following is some math to figure out
		// how many lines of text we have and compare that
		// with the image size and add the newlines accordingly
		
		// we can only do this if we can get our base
		// font
		// now calculate our text size if strung out over a
		// single line
		BaseFont font = contentFont.getBaseFont();
		
		// if we get our base font then we can do this
		// math, otherwise we will just add and hope for the
		// best!
		if (font != null) {						
			// determine the number of lines we have - we do this by looping over our
			// content and find when we think the lines will wrap and just count up
			// based on that
			float currentWidth = 0;
			float spaceWidth = font.getWidthPoint(" ", contentFont.getFontSize());
			String[] textBlock = StringUtils.split(content);
			for (String s : textBlock) {
				float sWidth = font.getWidthPoint(s, contentFont.getFontSize());
				
				// now see if this will wrap us to the next line
				if (currentWidth + sWidth > textWidth) {
					lines++;
					currentWidth = sWidth;
				}
				else {
					currentWidth = currentWidth + spaceWidth + sWidth;
				}
			}
		}
		
		return lines;
	}
}
