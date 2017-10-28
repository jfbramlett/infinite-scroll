package org.thirdstreet.blogger.blog.model;

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import com.bramlettny.common.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.thirdstreet.google.GoogleUtils;
import org.thirdstreet.utils.FileUtilities;

/**
 * Value object representing our blog
 * @author John Bramlett
 *
 */
public class Blog {
	public static final String kOwner = "owner";

	private final String blogDirectory;
	private Map<String, Object> blogJson;

	/**
	 * Constructor - constructs an instance from the given xml
	 * @param json the blog data
	 */
	public Blog(final String json) {
		super();

		this.blogJson = JsonUtil.toMap(json);


		String path = getUrl().replace("http://", "");
		int index = path.indexOf(".");
		if (index > 0) {
			path = path.substring(0, index);
		}
		this.blogJson.put("name", path);

		if (!blogJson.containsKey("guid")) {
			this.blogJson.put("guid", UUID.randomUUID().toString());
		}
		blogDirectory = FileUtilities.kAppDir + path;
	}

	/**
	 * Constructor - constructs an instance from the given xml
	 * @param json the blog data
	 * @param user The user who owns this blog
	 */
	public Blog(final String json, final String user) {
		this(json);

		this.blogJson.put(kOwner, user);
	}

	/**
	 * Gets this blogs guid.
	 * @return The unique identifier for this blog
	 */
	public String getGuid() {
		return blogJson.get("guid").toString();
	}

	/**
	 * Gets the title
	 * @return the title
	 */
	public String getTitle() {
		return blogJson.getOrDefault("name", StringUtils.EMPTY).toString();
	}

	/**
	 * Gets our blog name
	 *
	 * @return String The blog name
	 */
	public String getName() {
		return blogJson.getOrDefault("name", StringUtils.EMPTY).toString();
	}

	/**
	 * Gets the blog url.
	 *
	 * @return String The url
	 */
	public String getUrl() {
		return blogJson.get("url").toString();
	}

	/**
	 * Gets date the blog was created
	 * @return Calendar The date the blog was created
	 */
	public Calendar getPublished() {
		return GoogleUtils.convertDate(blogJson.get("published"));
	}

	/**
	 * Gets date the blog was last updated
	 * @return Calendar The date the blog was last updated
	 */
	public Calendar getUpdated() {
		return GoogleUtils.convertDate(blogJson.get("updated"));
	}

	/**
	 * Sets the blogId
	 * @return the blogId
	 */
	public String getBlogId() {
		return blogJson.get("id").toString();
	}

	/**
	 * Gets the last download date for this blog
	 * @return Calendar The last download date
	 */
	public Calendar getLastDownloadDate() {
		return GoogleUtils.convertDate(blogJson.get("lastDownloadDate"));
	}

	/**
	 * Sets the last download date for this blog
	 * @param lastDownloadDate The last download date for this blog
	 */
	public void setLastDownloadDate(Calendar lastDownloadDate) {
		blogJson.remove("currentAction");
		blogJson.put("lastDownloadDate", GoogleUtils.convertDateToString(lastDownloadDate));
	}

	/**
	 * Gets the current action being done against the blog
	 * @return String The current action
	 */
	public String getCurrentAction() {
		return blogJson.getOrDefault("currentAction", "").toString();
	}

	/**
	 * Sets current action being done against the blog
	 * @param currentAction The current action
	 */
	public void setCurrentAction(final String currentAction) {
		blogJson.put("currentAction", currentAction);
	}

	/**
	 * Gets the blogJson
	 * @return the blogJson
	 */
	public String getBlogJson() {
		return JsonUtil.toJson(blogJson);
	}

	@Override
	public String toString() {
		return JsonUtil.toJson(this);
	}
}
