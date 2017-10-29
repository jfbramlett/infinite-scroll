package com.bramlettny.infinitescroll.web.dto;

/**
 * Value object holding a post.
 */
public class Post {

	private final String uuid;
	private final String content;

	/**
	 * Constructor.
	 *
	 * @param uuid The uuid for the post
	 * @param content The post content
	 */
	public Post(final String uuid, final String content) {
		this.uuid = uuid;
		this.content = content;
	}

	/**
	 * The unique id for the post.
	 *
	 * @return String The uuid of the post
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Gets the post content.
	 *
	 * @return String The post content
	 */
	public String content() {
		return content;
	}
}
