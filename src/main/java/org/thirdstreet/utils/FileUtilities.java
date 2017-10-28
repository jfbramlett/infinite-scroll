package org.thirdstreet.utils;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;


/**
 * General file utilities for our blogger tool
 * @author bramlej
 */
public final class FileUtilities {
	private static final Logger logger = LoggerFactory.getLogger(FileUtilities.class);

	public static final String kAppDir = System.getProperty("user.home") + File.separator + ".bloggerarchiver" + File.separator;
	private static final int kImageRetry = 5;	// flag for number of times to retry downloading an image

	static {
		FileUtilities.verifyDirectory(kAppDir);
	}

	/**
	 * File name filter that filters our results to json files.
	 */
	private static class JsonFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File file, String s) {
			return s.endsWith(".json");
		}
	}

	public static FilenameFilter JSON_FILENAME_FILTER = new JsonFilenameFilter();


	/**
	 * Constructor - declared private as all access is via static methods
	 */
	private FileUtilities() {
		super();
	}

	/**
	 * Writes the given content to the given file
	 *
	 * @param filename The name of the file we are writing
	 * @param content  The content for the file
	 */
	public static void writeFile(final String filename, final String content) {
		writeFile(new File(filename), content);
	}

	/**
	 * Writes the given content to the given file
	 *
	 * @param file    The file we are writing
	 * @param content The content for the file
	 */
	public static void writeFile(final File file, final String content) {
		try {
			// now write our file
			FileUtils.writeStringToFile(file, content, CharEncoding.UTF_8);
		} catch (Exception e) {
			logger.error("Failed writing file " + file.getName(), e);
			throw new RuntimeException("Failed writing file " + file.getName(), e);
		}
	}

	/**
	 * Reads the given file to a string
	 *
	 * @param file The file we are reading
	 * @return String The file content
	 */
	public static String readFile(final File file) {
		String result = null;

		try {
			if (!file.exists()) {
				logger.warn("File " + file.getName() + " does not exist!");
			} else {
				result = FileUtils.readFileToString(file, CharEncoding.UTF_8);
			}
		} catch (Exception e) {
			logger.error("Failed to read file " + file.getName(), e);
			throw new RuntimeException("Failed to read file " + file.getName(), e);
		}

		return result;
	}

	/**
	 * Reads the given file to a string
	 *
	 * @param filename The name of the file we are reading
	 * @return String The file contents
	 */
	public static String readFile(String filename) {
		return readFile(new File(filename));
	}

	/**
	 * Verifies the directory exists, if it doesn't it will create it
	 *
	 * @param directory The directory we are checking
	 * @return File The verified directory
	 */
	public static File verifyDirectory(final String directory) {
		// now make sure the directory exists
		File f = new File(directory);
		if (!f.exists()) {
			// make the entire path if need be
			f.mkdirs();
		}

		return f;
	}

	/**
	 * Downloads an image
	 * @param url The url for the image
	 * @param directory The directory we are writing to
	 * @param filename The filename we are writing to
	 * @return File The file
	 */
	public static File downloadImage(final String url, final File directory, final String filename) {
		if (!directory.exists()) {
			final boolean created = directory.mkdirs();
			if (!created) {
				logger.error("Failed to create tmp directory for images " + directory.getAbsolutePath());
				throw new RuntimeException("Failed to create tmp directory for images " + directory.getAbsolutePath());
			}
		}

		// try and download our image - we'll retry a couple
		// of times if it fails
		boolean downloaded = false;
		int retry = 0;
		while ((!downloaded) && (retry < kImageRetry)) {
			File imageFile = new File(directory.getAbsolutePath() + File.separator + filename);
			downloaded = FileUtilities.downloadImage(url, imageFile);
			retry++;
		}

		// if we didn't download then record the error
		if (!downloaded) {
			throw new RuntimeException("Failed to download image " + url);
		}

		return new File(directory.getAbsolutePath() + File.separator + filename);

	}



	/**
	 * Downloads an image to a file
	 *
	 * @param imageUrl  The image url
	 * @param imageFile The filename to use for the image
	 * @return boolean Returns true if the file was downloaded
	 * false otherwise
	 */
	public static boolean downloadImage(String imageUrl, File imageFile) {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;

		// flag indicating if the file should be delete
		// because of a partial download
		boolean deleteFile = false;

		try {
			logger.debug("Attempting to download image from url " + imageUrl);

			URL url = new URL(imageUrl);

			out = new BufferedOutputStream(new FileOutputStream(imageFile));

			conn = url.openConnection();

			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}

			logger.debug("Downloaded image to file " + imageFile.getName());

		} catch (Exception e) {
			// set our flag to delete the file since our
			// download failed
			deleteFile = true;
			logger.error("Failed to download image " + imageUrl, e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				// do nothing in this event
				;
			}

			// now see if we need to delete the image file or
			if (deleteFile) {
				logger.debug("Failed downloading image - removing file");
				//we have a partially downloaded file so remove it
				if (imageFile.exists()) {
					imageFile.delete();
					logger.debug("Image file " + imageFile.getName() + " deleted");
				}
			}
		}

		// return true if we have a good file, false otherwise
		// (i.e. the opposite of the deleteFile flag)
		return !deleteFile;
	}
}