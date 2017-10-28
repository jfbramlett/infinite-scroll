package org.thirdstreet.blogger.writer.pdf.layouts;

import com.lowagie.text.Image;

/**
 * Class for our single image layouts that wrap the text to the
 * right
 *
 * @author John Bramlett
 */
public class SingleImageRightWrapTextPageWriter extends SingleImageWrapTextPageWriter  {
	
	/**
	 * Constructor
	 */
	public SingleImageRightWrapTextPageWriter() {
		super(Image.ALIGN_RIGHT);
	}
}
