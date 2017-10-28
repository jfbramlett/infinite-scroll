package org.thirdstreet.blogger.blog.model;

import com.bramlettny.common.util.JsonUtil;
import com.google.common.collect.Maps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a job being done against a given blog.
 */
public class BlogJob {
	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private final Map<String, Object> blogJson;

	/**
	 * Constructor
	 */
	public BlogJob(final String blogPostJson) {
		super();

		this.blogJson = JsonUtil.toMap(blogPostJson);
	}

	public BlogJob(final String blogId, final String action) {
		blogJson = Maps.newConcurrentMap();
		blogJson.put("id", UUID.randomUUID().toString());
		blogJson.put("blogId", blogId);
		blogJson.put("action", action);

		final Date startTime = new Date();
		blogJson.put("startTimeMillis", startTime.getTime());
		blogJson.put("startTime", dateFormat.format(startTime));
	}

	/**
	 * Gets our blog post json
	 * @return String The blog post as json
	 */
	public String getBlogPostJson() {
		return JsonUtil.toJson(blogJson);
	}

	public String getId() {
		return blogJson.get("id").toString();
	}

	public BlogJob setPostDownloaded(final long count) {
		blogJson.put("downloadedPost", count);
		return this;
	}

	public BlogJob setCommentsDownloaded(final long count) {
		blogJson.put("downloadedComments", count);
		return this;
	}

	public BlogJob setImagesDownloaded(final long count) {
		blogJson.put("downloadedImages", count);
		return this;
	}

	public BlogJob setEndTime() {
		final Date endTime = new Date();
		blogJson.put("endTimeMillis", endTime.getTime());
		blogJson.put("endTime", dateFormat.format(endTime));
		return this;
	}
}
