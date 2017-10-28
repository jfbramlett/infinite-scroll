package org.thirdstreet.blogger.writer.pdf;

import java.awt.*;

/**
 * Class used to contain the colors for the pdf output
 * 
 * @author bramlej
 */
public final class PdfColor {

	//public static Color kDocumentBackground = new Color(17, 34, 51);
	public static PdfColor kDocumentBackground = new PdfColor(Color.white);
	
	//public static Color kHeaderFooterBackground = new Color(51,85,119);
	public static PdfColor kHeaderFooterBackground = new PdfColor(Color.white);
	
	//public static Color kImageBackgroundColor = new Color(245,245,220);
	public static PdfColor kImageBackgroundColor = new PdfColor(Color.black);

	//public static Color kImageBorderColor = new Color(51,85,119);
	public static PdfColor kImageBorderColor = new PdfColor(Color.lightGray);

	//public static Color kTitlePageFontColor = Color.white;
	public static PdfColor kTitlePageFontColor = new PdfColor(Color.black);
	
	// table colors
	public static PdfColor kContentTitleFontColor = new PdfColor(Color.black);
	public static PdfColor kContentFontColor = new PdfColor(Color.black);

	
	private Color myColor;

	/**
	 * Constructor - declared private as all access is via static variables
	 */
	private PdfColor(Color color) {
		super();
		myColor = color;
	}
	
	/**
	 * Returns our color
	 * @return Color The color
	 */
	public Color getColor() {
		return myColor;
	}
}
