package org.thirdstreet.blogger.writer.pdf.layouts;

import java.util.List;

import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.writer.pdf.IPdfPageWriter;

/**
 * Class used to obtain the page writer based on a blog post 
 *
 * @author John Bramlett
 */
public class LayoutManager {

	public static LayoutManager DEFAULT_LAYOUT = new LayoutManager(new SingleImageLeftWrapTextPageWriter(), new AlignImagesOnTopPageWriter());


	private final IPdfPageWriter noImageWriter;
	private final IPdfPageWriter singleImageWriter;
	private final IPdfPageWriter multiImageWriter;

	/**
	 * Constructor
	 *
	 * @param singleImageWriter The writer to use when there is a single image
	 * @param multiImageWriter The writer to use when writing multiple images
	 */
	public LayoutManager(final IPdfPageWriter singleImageWriter, final IPdfPageWriter multiImageWriter) {
		super();

		// now add our writers
		this.noImageWriter = new NoImagePageWriter();
		this.singleImageWriter = singleImageWriter;
		this.multiImageWriter = multiImageWriter;
	}
	
	/**
	 * Gets the page writer for the given post
	 * @param post The post we are writing
	 * @return IPdfPageWriter The component used to write the page out
	 */
	public IPdfPageWriter getPageWriter(BlogPost post) {
		IPdfPageWriter result = null;
		
		// based on the number of images we pick a writer
		List<BlogImage> images = post.getImages();
		
		// if there are no images then use our no image writer
		if ((images == null) || (images.size() == 0)) {
			result = new NoImagePageWriter();
		}
		// one image find our single image writer
		else if (images.size() == 1) {
			result = singleImageWriter;
		}
		// multiple images use our multi image writer
		else {
			result = multiImageWriter;
		}
		
		return result;
		
	}
}