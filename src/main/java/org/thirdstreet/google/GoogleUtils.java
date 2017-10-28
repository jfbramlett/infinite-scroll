package org.thirdstreet.google;

import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Some utilities for dealing with the google api
 * 
 * @author John Bramlett
 */
public final class GoogleUtils {
	private static final String kBaseUrl = "https://www.googleapis.com/blogger/v3/blogs/";
	private static final String kApiKey = "key=AIzaSyAYcXPWLW5aY9tZ4NwxqGniAuJH-FldFp8";

	/**
	 * Gets the url of the call to get the details of a blog.
	 *
	 * @param blogUrl The blogs url
	 * @return String The url used to get the details
	 */
	public static String blogUrl(final String blogUrl) {
		return kBaseUrl + "byurl?url=" + blogUrl + "&" + kApiKey;
	}

	/**
	 * Gets the url used to retrieve a set of posts from the given blog.
	 *
	 * @param blogId The blog id
	 * @return String The url used to get the posts
	 */
	public static String posts(final String blogId) {
		return kBaseUrl + blogId + "/posts?" + kApiKey + "&fetchImages=true&fetchBodies=true&maxResults=50";
	}

	/**
	 * Gets the url used to retrieve a set of posts from the given blog.
	 *
	 * @param blogId The blog id
	 * @param fromDate The date from which to retrieve our blogs
	 * @return String The url used to get the posts
	 */
	public static String posts(final String blogId, final Calendar fromDate) {
		if (fromDate != null) {
			final DateTime dt = new DateTime(fromDate.getTime());
			return posts(blogId) + "&startDate=" + dt.toStringRfc3339();
		} else {
			return posts(blogId);
		}
	}

	/**
	 * Gets the url used to retrieve a set of posts from the given blog.
	 *
	 * @param blogId The blog id
	 * @param fromDate The date from which to retrieve our blogs
	 * @param pageToken The token for the paginated request
	 * @return String The url used to get the posts
	 */
	public static String posts(final String blogId, final Calendar fromDate, final String pageToken) {
		return posts(blogId, fromDate) + "&pageToken=" + pageToken;
	}

	/**
	 * Gets the url used to retrieve the comments for a blog post.
	 *
	 * @param blogId The blog id
	 * @param postId The post id
	 * @return String The url used to get the comments
	 */
	public static String comments(final String blogId, final String postId) {
		return kBaseUrl + blogId + "/posts/" + postId + "/comments?" + kApiKey + "&fetchBodies=true&status=live&maxResults=50";
	}

	/**
	 * Gets the url used to retrieve the comments for a blog post.
	 *
	 * @param blogId The blog id
	 * @param postId The post id
	 * @param pageToken The page token
	 * @return String The url used to get the comments
	 */
	public static String comments(final String blogId, final String postId, final String pageToken) {
		return comments(blogId, postId) + "&pageToken=" + pageToken;
	}


	/**
	 * Converts our google datetime to a regular java date
	 * 
	 * @param dt The date/time we are converting
	 * @return Date The java date object for this
	 */
	public static Calendar convertDate(final DateTime dt) {
		Calendar result = null;
		try {
			if (dt != null) {
				// now convert the google date time to a date
				result = Calendar.getInstance();
				result.setTimeInMillis(dt.getValue());
			}
		}
		catch (Exception e) {
			// do nothing
			;
		}
		
		return result;
	}

	/**
	 * Converts our calendar to a string.
	 * @param cal The date time
	 * @return String The converting date time
	 */
	public static String convertDateToString(final Calendar cal) {
		return new DateTime(cal.getTime()).toStringRfc3339();
	}

	/**
	 * Converts a date string in to a date
	 * @param dt The date string we are converting
	 * @return Date The converted date
	 */
	public static Calendar convertDate(final Object dt) {
		if (dt != null) {
			return convertDate(DateTime.parseRfc3339(dt.toString()));
		}
		return null;
	}

	/**
	 * Constructor - private as access is via static methods
	 */
	private GoogleUtils() {
		super();
	}

}
