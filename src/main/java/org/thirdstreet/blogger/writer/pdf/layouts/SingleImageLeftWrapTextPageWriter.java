package org.thirdstreet.blogger.writer.pdf.layouts;

import com.lowagie.text.Image;

/**
 * Base class for our single image layouts that wrap the text to the
 * left
 *
 * @author John Bramlett
 */
public class SingleImageLeftWrapTextPageWriter extends SingleImageWrapTextPageWriter  {
	
	/**
	 * Constructor
	 */
	public SingleImageLeftWrapTextPageWriter() {
		super(Image.ALIGN_LEFT);
	}

}
