package org.thirdstreet.blogger.writer.pdf;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to hold the fonts used in our pdf output
 * @author bramlej
 */
@SuppressWarnings("unchecked")
public final class PdfFont {
	private static final Logger logger = LoggerFactory.getLogger(PdfFont.class);
	private static LinkedList<PdfFont> fonts = new LinkedList<>();
	
	//private static BaseFont kFont;
	private static String kFont = FontFactory.HELVETICA; //"arial";
	//private static String kFont = "Helvetica";
	

	public static PdfFont kDocumentTitleFont; 
	public static PdfFont kDocumentAuthorFont; 
	public static PdfFont kDocumentDateFont; 
		
	public static PdfFont kContentTitleFont;
	public static PdfFont kPostingDateFont;
	public static PdfFont kContentFont;
	public static PdfFont kCommentTitleFont;
	public static PdfFont kCommentFont;
	
	public static Set<String> kBuiltinFonts;
				
	static {
		try {
			// load our set of built in fonts - do this by getting the registered
			// fonts before loading in any external ones
			kBuiltinFonts = new HashSet<>();
			Set<String> registeredFonts = FontFactory.getRegisteredFamilies();
			kBuiltinFonts.addAll(registeredFonts);
			
			// now load in our external fonts
			logger.debug("Loading fonts...");
			String windowsDir = System.getenv("windir");
			if (windowsDir == null) {
				windowsDir = "c:\\windows";
			}
			FontFactory.registerDirectory(windowsDir + "\\fonts");
			logger.debug("Fonts loaded");
									
			kDocumentTitleFont = new PdfFont(kFont, 30f, Font.BOLD, PdfColor.kTitlePageFontColor);
			kDocumentAuthorFont = new PdfFont(kFont, 20f, Font.BOLDITALIC, PdfColor.kTitlePageFontColor); 
			kDocumentDateFont = new PdfFont(kFont, 14f, Font.BOLD, PdfColor.kTitlePageFontColor); 
				
			kContentTitleFont = new PdfFont(kFont,20f, Font.BOLDITALIC, PdfColor.kContentTitleFontColor);
			kPostingDateFont = new PdfFont(kFont, 10f, Font.BOLD, PdfColor.kContentFontColor);
			kContentFont = new PdfFont(kFont, 12f, Font.NORMAL, PdfColor.kContentFontColor);
			kCommentTitleFont = new PdfFont(kFont, 10f, Font.BOLD, PdfColor.kContentFontColor);
			kCommentFont = new PdfFont(kFont, 8f, Font.NORMAL, PdfColor.kContentFontColor);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize fonts!", e);
		}
	}	
	
	private Font myFont;

	/**
	 * Retrieves a list of available fonts in the system
	 * @param includeBuiltin Flag indicating whether to include the PDF
	 * built-in fonts (these cannot be embedded)
	 * @return List<String> The available fonts
	 */
	public static List<String> listAvailableFonts(boolean includeBuiltin) {
		// get the set of available fonts
		Set<String> registeredFonts = FontFactory.getRegisteredFamilies();
		
		List<String> rf = new ArrayList<String>();
		for (Iterator<String> it = registeredFonts.iterator(); it.hasNext();) {
			String fontName = it.next();
			
			// if we are including the built-ins then add all fonts
			if (includeBuiltin) {
				rf.add(fontName);
			}
			else {
				// we don't want built-ins so make sure this font
				// isn't in our set of built-ins
				if (!kBuiltinFonts.contains(fontName)) {
					rf.add(fontName);
				}
			}
		}
		// now make sure to sort
		Collections.sort(rf);

		return rf;
	}
	
	/**
	 * Constructor
	 * @param name The font family name
	 * @param size The size of the font
	 * @param style The fonts style
	 * @param color The font color
	 */
	private PdfFont(String name, float size, int style, PdfColor color) {		
		myFont = FontFactory.getFont(name, getEncoding(name), true, size, style);
		setColor(color);
	}
	
	/**
	 * Constructor - declared private as access is via static methods
	 * @param font The font
	 * @param color The font color
	 */
	private PdfFont(Font font, PdfColor color) {
		myFont = font;
		setColor(color);
		
		// now register the font
		fonts.add(this);
	}

	/**
	 * Gets our font
	 * @return Font The font
	 */
	public Font getFont() {
		return myFont;
	}
	
	/**
	 * Gets our base font
	 * @return The base font
	 */
	public BaseFont getBaseFont() {
		BaseFont result = myFont.getBaseFont();
		// if we don't find our base font the easy way get the
		// calculated one
		if (result == null) {
			result = myFont.getCalculatedBaseFont(false);
		}
		
		return result;
	}
	/**
	 * Updates our font color
	 * @param newColor The new font color
	 */
	public void setColor(PdfColor newColor) {
		myFont.setColor(newColor.getColor());
	}
	
	/**
	 * Updates our font to a new font family
	 * @param font The new font family
	 */
	public void setFont(Font font) {
		Color fontColor = myFont.getColor();
		myFont = font;
		myFont.setColor(fontColor);
	}
	
	/**
	 * Gets our font family name
	 * @return String the font name
	 */
	public String getFontName() {
		return myFont.getFamilyname();
	}
	
	/**
	 * Gets our font size
	 * @return float The font size
	 */
	public float getFontSize() {
		return myFont.getSize();
	}
	
	/**
	 * Gets our font style
	 * @return String The font style
	 */
	public String getStyle() {
		String result = "";
		if (myFont.isBold()) {
			result = "Bold";
		}
		if (myFont.isItalic()) {
			if (result.length() > 0 ) {
				result = result + " | ";
			}
			result = result + "Italics";
		}
		
		return result;
	}

	/**
	 * Returns a flag indicating if the font is bold
	 * @return boolean Returns true if the font is bold,
	 * false otherwise
	 */
	public boolean isBold() {
		return myFont.isBold();
	}
	/**
	 * Returns a flag indicating if the font is italic
	 * @return boolean Returns true if the font is italic,
	 * false otherwise
	 */
	public boolean isItalic() {
		return myFont.isItalic();
	}

	/**
	 * Gets the encoding for the given font
	 * @param fontName The name of the font we are getting the encoding for
	 * @return String The encoding
	 */
	public static String getEncoding(String fontName) {
		String result = null;
		
		// first lookup the font
		Font f = FontFactory.getFont(fontName);
		if (f != null) {
			// we found it so get the base font and from there the encoding
			BaseFont baseFont = f.getBaseFont();
			if (baseFont != null) {
				result = baseFont.getEncoding();
			}
			else {
				baseFont = f.getCalculatedBaseFont(false);
				if (baseFont != null) {
					result = baseFont.getEncoding();
				}
			}
		}
		
		return result;
	}
	
}
