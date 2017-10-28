package org.thirdstreet.blogger.writer.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.BlogFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.reader.ReadStatusEvent;
import org.thirdstreet.blogger.writer.BlogWriter;
import org.thirdstreet.blogger.writer.pdf.layouts.LayoutManager;

/**
 * Write used to write contents of our blog to a PDF file
 * 
 * @author John Bramlett
 */
public class PdfBlogWriter implements BlogWriter {
	private static final Logger logger = LoggerFactory.getLogger(PdfBlogWriter.class);
	private static final String kTitleDateFormat = "MMMM dd, yyyy";

	private final boolean trailingBlank = true;
	private final boolean insurePDFAConformance = false;
	private final boolean includeComments = true;
	private final LayoutManager layoutManager = LayoutManager.DEFAULT_LAYOUT;

	private final BlogFactory blogFactory;

	/**
	 * Constructor
	 *
	 * @param blogFactory The blog factory
	 */
	public PdfBlogWriter(final BlogFactory blogFactory) {
		super();
		this.blogFactory = blogFactory;
	}

	@Override
	public void write(final Blog blog, final Calendar start, final Calendar end, final String filename,
					  final Consumer<ReadStatusEvent> statusConsumer) throws Exception {
		logger.debug("Writing blog to " + filename);

		BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", BaseFont.EMBEDDED);
		BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1252", BaseFont.EMBEDDED);

		// start by opening our blog
		Document document = open(blog, filename);

		// write a title page
		logger.info("Writing title page");
		statusConsumer.accept(new ReadStatusEvent("Writing title page"));
		writeTitlePage(blog, document, start, end);
		
		// reset our page count so the first content page is page 1
		document.resetPageCount();
		
		// write our header/footer
		writeHeaderFooter(blog, document);
		
		// write our content
		logger.info("Writing content");
		statusConsumer.accept(new ReadStatusEvent("Writing content"));
		writeContent(blog, start, end, document, statusConsumer);
		
		// now close our file
		logger.info("Completing doc");
		statusConsumer.accept(new ReadStatusEvent("Completing doc"));
		close(document);

		logger.info("Uploading to S3");
		statusConsumer.accept(new ReadStatusEvent("Uploading to S3"));
		blogFactory.saveBlogOutput(blog, new File(filename));

		logger.debug("Successfully wrote pdf!");
		statusConsumer.accept(new ReadStatusEvent("Successful wrote pdf"));
	}

	/**
	 * Writes a title page for our blog
	 * 
	 * @param blog The blog info
	 * @param document The document we are writing
	 * @param startDate The start date for this report
	 * @param endDate The end date for this report
	 */
	private void writeTitlePage(final Blog blog, final Document document, final Calendar startDate, final Calendar endDate) {
		SimpleDateFormat formatter = new SimpleDateFormat(kTitleDateFormat);
		try {			
			// write out our title - we are playing with fonts to try and jazz
			// up the output

			// add some space
			Paragraph spacer = new Paragraph(" ", PdfFont.kDocumentTitleFont.getFont());
			document.add(spacer);
			document.add(spacer);
			document.add(spacer);
			document.add(spacer);
			document.add(spacer);

			// now add our blog title
			Paragraph title = new Paragraph(blog.getTitle(), PdfFont.kDocumentTitleFont.getFont());
			title.setAlignment(Paragraph.ALIGN_CENTER);
			
			document.add(title);

			document.add(spacer);
			
			Date firstPost = startDate.getTime();
			Date lastPost = endDate.getTime();

			Paragraph createdOn = new Paragraph(formatter.format(firstPost) + " to " + formatter.format(lastPost), PdfFont.kDocumentDateFont.getFont());
			createdOn.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(createdOn);

			// now add a blank page
			document.add(Chunk.NEXTPAGE);
		}
		catch (Exception e) {
			logger.error("Failed to add content to our PDF document", e);
			throw new RuntimeException("Failed to add content to our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}

	/**
	 * Writes our header and footer
	 * @param blog The blog we are writing
	 * @param document The document we are writing
	 */
	private void writeHeaderFooter(final Blog blog, final Document document) {
		// add a header
		Chunk headerChunk = new Chunk(blog.getTitle() + " Blog", PdfFont.kContentFont.getFont());
		HeaderFooter header = new HeaderFooter(new Phrase(headerChunk), false);
		header.setBackgroundColor(PdfColor.kHeaderFooterBackground.getColor());
		header.setAlignment(Element.ALIGN_CENTER);
		document.setHeader(header);

		// and footer
		HeaderFooter footer = new HeaderFooter(new Phrase(new Chunk("- Page ", PdfFont.kContentFont.getFont())), new Phrase(new Chunk(" -", PdfFont.kContentFont.getFont())));
		footer.setBackgroundColor(PdfColor.kHeaderFooterBackground.getColor());
		footer.setAlignment(Element.ALIGN_CENTER);
		document.setFooter(footer);
	}

	/**
	 * Writes the content of the blog to our document
	 * @param blog The blog we are writing
	 * @param start The start time period
	 * @param end The end time period
	 * @param document The document we are writing
	 * @param statusConsumer The consumer to send status updates to
	 * @throws Exception
	 */
	private void writeContent(final Blog blog, final Calendar start, final Calendar end,
							  final Document document, final Consumer<ReadStatusEvent> statusConsumer) throws Exception{
		// now write our entries
		int chapter = 1;
		List<BlogPost> posts = blogFactory.getBlogPosts(blog, start, end);
		for (BlogPost e : posts) {
			// download our images
			statusConsumer.accept(new ReadStatusEvent("Downloading images for post"));
			List<BlogImage> images = e.getImages();
			List<File> imageFiles = images.stream()
					.map(i -> blogFactory.getBlogImage(blog, e, i))
					.collect(Collectors.toList());

			// get our page writer for this post
			IPdfPageWriter writer = layoutManager.getPageWriter(e);
			
			// now generate our page
			statusConsumer.accept(new ReadStatusEvent("Writing post"));
			writer.writeChapter(document, blog, e, blogFactory.getBlogComments(blog, e),
					chapter, includeComments);
			
			chapter++;

			// now cleanup our images
			imageFiles.forEach(File::delete);
		}
		
		// now add a trailing page
		if (trailingBlank) {
			// reset our header and footer			
			//HeaderFooter header = new HeaderFooter(new Phrase(new Chunk("", PdfFont.kContentFont.getFont())), false);
			//header.setBackgroundColor(PdfColor.kDocumentBackground.getColor());
			//document.setHeader(null);
			//document.setFooter(null);

			document.addHeader(blog.getTitle(), "");

			// move to the next page
			document.add(Chunk.NEXTPAGE);
			
			int pageNum = document.getPageNumber();
			if ((pageNum % 2) == 1) {
				// finish off the current page
				document.add(Chunk.NEXTPAGE);
			}
			// now add our blank (for front and back)
			document.add(Chunk.NEXTPAGE);
			document.add(Chunk.NEXTPAGE);

		}
	}

	/**
	 * Opens our document
	 * @param blog The blog we are writing
	 * @param filename The file to write to
	 * @return Document The document we are writing
	 */
	private Document open(final Blog blog, final String filename) {
		try {
			final Document document = new Document();
			Rectangle size = document.getPageSize();
			Rectangle newSize = new Rectangle(size);
			newSize.setBackgroundColor(PdfColor.kDocumentBackground.getColor());
			document.setPageSize(newSize);

			PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(filename));

			// if we are insuring conformance then flag it for that 
			if (insurePDFAConformance) {
				pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
				pdfWriter.setPDFXConformance(PdfWriter.PDFA1A);
			}
			
			// set some of our document properties based
			// on the blog data
			//document.addAuthor(blog.getAuthor());
			document.addCreationDate();
			//document.addCreator(blog.getAuthor());
			document.addTitle(blog.getTitle());
			document.addSubject("Blog");
						
			// open the document
			document.open();

			return document;
		}
		catch (Exception e) {
			logger.error("Failed to initialize our PDF document", e);
			throw new RuntimeException("Failed to initialize our PDF Document!\nCause: " + e.getMessage(), e);
		}
	}

	/**
	 * Closes our document
	 */
	public void close(final Document document) {
		if (document != null) {
			document.close();
		}
	}
}
