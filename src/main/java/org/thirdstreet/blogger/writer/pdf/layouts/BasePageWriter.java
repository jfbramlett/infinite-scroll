package org.thirdstreet.blogger.writer.pdf.layouts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.writer.pdf.IPdfPageWriter;
import org.thirdstreet.blogger.writer.pdf.PdfColor;
import org.thirdstreet.blogger.writer.pdf.PdfFont;


/**
 * Base class for our page writers (page writers are used to manage the
 * layouts for a page). Just provides some common functions that are
 * used across all or most page writers
 *
 * @author jbramlet
 */
public abstract class BasePageWriter implements IPdfPageWriter{
	private static final Logger logger = LoggerFactory.getLogger(BasePageWriter.class);

	protected static final String kImageBorderWidth = "Image Border Width";
	protected static final String kImageBorder = "Use Image Border";
	
	protected static final String kPrintDateFormat = "EEEE, MMMM dd, yyyy";
	protected static final float kContentIndent = 45f;

	protected static final Paragraph kNewLine = new Paragraph();
	
	protected float imageBorderWidth = 10;
	protected boolean imageBorder = false;
	protected Document document;

	// there has been some problem adding chunks without fonts
	// so this is a bit of a workaround
	static {
		kNewLine.setFont(PdfFont.kContentFont.getFont());
		//kNewLine.add(Chunk.NEWLINE);
		kNewLine.add(new Chunk(" "));
	}
	
	/**
	 * Constructor
	 */
	public BasePageWriter() {
		super();
	}

	/**
	 * Routine to write our page contents
	 * @param chpt The chapter we are writing the contents to
	 * @param blog The blog we are writing
	 * @param post The post we are writing
	 * @param comments The comments
	 * @param includeComments Flag indicating if comments should be included
 	 * @throws Exception
	 */
	protected abstract void writeChapterContents(Chapter chpt, Blog blog, BlogPost post, List<BlogComment> comments, boolean includeComments)throws Exception;
	
	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.IPdfPageWriter#getProperties()
	 */
	public Properties getProperties() {		
		Properties props = new Properties();
		props.put(kImageBorder, Boolean.toString(imageBorder));
		props.put(kImageBorderWidth, Float.toString(imageBorderWidth));
		return props;
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.IPdfPageWriter#isConfigurable()
	 */
	public boolean isConfigurable() {		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.IPdfPageWriter#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties newProps) {
		String imageBorderVal = newProps.getProperty(kImageBorder);
		if (BooleanUtils.toBoolean(imageBorderVal)) {
			imageBorder = true;			
		}
		else {
			imageBorder = false;
		}
		
		String imageBorderWidthVal = newProps.getProperty(kImageBorderWidth);
		if (NumberUtils.isNumber(imageBorderWidthVal)) {
			imageBorderWidth = NumberUtils.toFloat(imageBorderWidthVal);
		}
	}

	/* (non-Javadoc)
	 * @see org.thirdstreet.blogger.writer.pdf.IPdfPageWriter#writeChapter(com.lowagie.text.Document, org.thirdstreet.blogger.blog.model.BlogPost, int, boolean)
	 */
	public void writeChapter(final Document document, final Blog blog, final BlogPost e,
							 final List<BlogComment> comments,
							 final int chapter, final boolean includeComments) throws Exception {
		// set our document
		this.document = document;
		
		// initialize our chapter
		Chapter chpt = initializeChapter(e, chapter);
		
		// now write our chapter contents
		writeChapterContents(chpt, blog, e, comments, includeComments);
		
		// now add this chapter to our document
		document.add(Chunk.NEXTPAGE);
		document.add(chpt);
	}

	/**
	 * Converts our google date time to a date for display
	 * 
	 * @param dt The google date time
	 * @return String Our format of a ui date string
	 */
	protected String convertDate(final Date dt) {
		try {
			SimpleDateFormat uidf = new SimpleDateFormat(kPrintDateFormat);

			// now convert back for display
			return uidf.format(dt);
		}
		catch (Exception e) {
			logger.error("Failed converting date", e);
			return dt.toString();
		}
	}
	
	/**
	 * Creates our chapter and writes the header info
	 * @param content The blog post
	 * @param chapter The chapter number
	 * @return Chapter The new chapter
	 */
	protected Chapter initializeChapter(final BlogPost content, final int chapter) throws Exception {
		// create our title
		Paragraph title = new Paragraph(content.getTitle(), PdfFont.kContentTitleFont.getFont());
		title.setAlignment(Paragraph.ALIGN_CENTER);
		Chapter chpt = new Chapter(title, chapter);
		chpt.setNumberDepth(0);

		chpt.add(kNewLine);
		
		// add our posting date
		Paragraph postingDateParagraph = new Paragraph(convertDate(content.getPublished().getTime()), PdfFont.kPostingDateFont.getFont());
		//postingDateParagraph.setIndentationLeft(kContentIndent);
		postingDateParagraph.setAlignment(Paragraph.ALIGN_CENTER);
		chpt.add(postingDateParagraph);
		// add a newline
		chpt.add(kNewLine);
		
		return chpt;
	}
	/**
	 * Creates our content body
	 * @param content The post we are creating the content block for
	 * @return Paragraph The content block for our post
	 */
	protected  Paragraph createContent(final BlogPost content) {
		Paragraph contentParagraph = new Paragraph("", PdfFont.kContentFont.getFont());
		contentParagraph.setIndentationLeft(kContentIndent);
		contentParagraph.setIndentationRight(kContentIndent);
		
		// and finally our content
		Chunk contentChunk = new Chunk(content.getText(), PdfFont.kContentFont.getFont());
		contentParagraph.add(contentChunk);
		contentParagraph.add(kNewLine);
		
		return contentParagraph;
	}
	/**
	 * Creates the element containing our comments
	 * @param comments The set of comments
	 * @return Paragraph Our comments
	 */
	protected Paragraph createComments(final List<BlogComment> comments) {
		Paragraph contentParagraph = new Paragraph("", PdfFont.kContentFont.getFont());
		contentParagraph.setIndentationLeft(kContentIndent);
		contentParagraph.setIndentationRight(kContentIndent);
		
		// now add our comments
		Chunk commentTitle = new Chunk("Comments:", PdfFont.kCommentTitleFont.getFont());
		contentParagraph.add(commentTitle);

		// add a newline
		contentParagraph.add(kNewLine);

		// now add our posts
		for (BlogComment comment : comments) {
			Chunk commentChunk = new Chunk("by " + comment.getAuthor() + " -- " + comment.getText(), PdfFont.kCommentFont.getFont());
			contentParagraph.add(commentChunk);
			contentParagraph.add(kNewLine);
		}

		return contentParagraph;
	}
	/**
	 * Adds the image as a new row to the table
	 * @param blog The blog
	 * @param post The post
	 * @param image The image we are adding
	 * @param alignmentStyle The alignment style
	 * @return Image The image element
	 */
	protected Image createImage(final Blog blog, final BlogPost post, final BlogImage image, final int alignmentStyle) {
		try {
			logger.debug("Adding image " + image.getImageName() + " to pdf output");

			final File localImage = new File(image.getTempImageDir() + File.separator + image.getImageName());
			if (localImage.exists()) {
				Image img = Image.getInstance(localImage.getAbsolutePath());
				img.setAlignment(alignmentStyle);

				if (imageBorder) {
					img.enableBorderSide(Rectangle.BOX);
					img.setBorderWidth(imageBorderWidth);
					img.setBorderColor(PdfColor.kImageBorderColor.getColor());
				}

				return img;
			}

			return null;
		}
		catch (Exception e) {
			logger.error("Failed adding image " + image.getImageName() + " to PDF", e);
			throw new RuntimeException("Failed adding image " + image.getImageName() + " to PDF.\nCause: " + e.getMessage(), e);
		}
	}
	
}
