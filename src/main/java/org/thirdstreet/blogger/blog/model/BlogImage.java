package org.thirdstreet.blogger.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

/**
 * Value object used to represent an image in our blog
 * 
 * @author John Bramlett
 */
public class BlogImage {

	private final String imageUrl;
	private final String postId;

	/**
	 * Constructor
	 * @param imageUrl The url for the image
	 * @param postId The id for the post
	 */
	public BlogImage(final String imageUrl, final String postId) {
		super();
		this.imageUrl = imageUrl;
		this.postId = postId;
	}

	/**
	 * Gets the imageUrl
	 * 
	 * @return the imageUrl
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * Gets the image name
	 * 
	 * @return the imageName
	 */
	@JsonIgnore
	public String getImageName() {
		int index = imageUrl.lastIndexOf("/");
		return postId + "-" + imageUrl.substring(index + 1, imageUrl.length());
	}

	/**
	 * Gets a local directory where to store images temporarily.
	 *
	 * @return File The local directory to temporarily store images
	 */
	public File getTempImageDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
}
