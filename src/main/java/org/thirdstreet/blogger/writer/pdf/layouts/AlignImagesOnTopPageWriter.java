package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;
import java.util.Properties;

import com.lowagie.text.Chapter;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.writer.pdf.PdfColor;


/**
 * General purpose page layout that writes all images to the top of the
 * page horizontally with the content below
 *
 * @author John Bramlett
 */
public class AlignImagesOnTopPageWriter extends BasePageWriter  {
	private static final Logger logger = LoggerFactory.getLogger(AlignImagesOnTopPageWriter.class);

	protected static final String kImagesPerRow = "Images Per Row";
	protected static final String kUseImageBackground = "Use Image Background";

	private static final float kCellPadding = 10;
	
	protected int imagesPerRow = 2;
	protected boolean useImageBackground = false;

	/**
	 * Constructor
	 */
	public AlignImagesOnTopPageWriter() {
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
		props.put(kImagesPerRow, Integer.toString(imagesPerRow));
		props.put(kUseImageBackground, Boolean.toString(useImageBackground));
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
		String val = newProps.getProperty(kImagesPerRow);
		if (NumberUtils.isNumber(val)) {
			imagesPerRow = NumberUtils.createInteger(val);
		}
		val = newProps.getProperty(kUseImageBackground);
		useImageBackground = BooleanUtils.toBoolean(val);
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
				float docWidth = document.getPageSize().getWidth();
				float leftMargin = document.leftMargin();
				float rightMargin = document.rightMargin();
				chpt.add(createImages(blog, content, images, docWidth - leftMargin - rightMargin));
			}
			
			// and finally our content
			chpt.add(createContent(content));
			
			// now write additional images
			if (images.size() > imagesPerRow)
			
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
	/**
	 * Adds our row of images
	 * @param blog The blog we are working with
	 * @param images The images we are adding
	 * @param pageWidth The total page width so the images can be scaled
	 * @return Element The image element
	 */
	private Element createImages(Blog blog, BlogPost post, List<BlogImage> images, float pageWidth) {
		try {
			logger.debug("Adding " + images.size() + " images to pdf output");
			
			PdfPTable table = new PdfPTable(imagesPerRow);
			table.setSpacingBefore(18f);

			// try and calculate our cell width - we do this so we can figure out how to
			// scale the images (the '50' was pulled out of my ass but seems to work
			// so am leaving it in)
			float cellWidth = (pageWidth/imagesPerRow) - (kCellPadding*2) - 50;
			
			// load in each image and add as a cell
			for (BlogImage image : images) {
				Image img = createImage(blog, post, image, Image.ALIGN_MIDDLE);
				if (img != null) {
					// now scale the image, the image is based on the page width
					// and the number of images on the page, each will be proportionally
					// sized

					// now scale our image to fit this width if we need to scale it - we scale
					// to a percent in order to maximize the images
					float imageWidth = img.getPlainWidth();
					if (cellWidth < imageWidth) {
						float scale = cellWidth / imageWidth;
						img.scalePercent((scale * 100));
					}

					// now create our cell
					PdfPCell cell = new PdfPCell(img);
					if (useImageBackground) {
						cell.setBackgroundColor(PdfColor.kImageBackgroundColor.getColor());
					}
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(kCellPadding);
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);
				}
			}
	
			// if we have an odd number of images add a blank cell
			// to the table
			if ((images.size() % imagesPerRow) != 0) {
			    PdfPCell cell = new PdfPCell();
			    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell.setPadding(kCellPadding);
			    cell.setBorder(PdfPCell.NO_BORDER);
			    table.addCell(cell);
			}
			
			return table;
		}
		catch (Exception e) {
			logger.error("Failed adding images to PDF", e);
			throw new RuntimeException("Failed adding images to PDF.\nCause: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the imagesPerRow
	 * @return int The imagesPerRow
	 */
	public int getImagesPerRow() {
		return imagesPerRow;
	}

	/**
	 * Sets the imagesPerRow
	 * @param imagesPerRow the imagesPerRow to set
	 */
	public void setImagesPerRow(int imagesPerRow) {
		this.imagesPerRow = imagesPerRow;
	}

}
