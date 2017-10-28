package org.thirdstreet.blogger.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A value object representation of our blog used to transfer data between it and the UI.
 */
public class BlogDto extends BaseDto {
	private final String id;
	private final String url;
	private final String name;
	private final String lastDownload;

	/**
	 * Constructor.
	 * @param url The url for the blog
	 * @param name The name of the blog
	 * @param lastDownload The last download date of the blog
	 */
	@JsonCreator
	public BlogDto(@JsonProperty(value = "id") final String id,
				   @JsonProperty(value = "url") final String url,
				   @JsonProperty(value = "name") final String name,
				   @JsonProperty(value = "lastDownload") final String lastDownload) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.lastDownload = lastDownload;
	}

	/**
	 * Gets the id of the blog
	 *
	 * @return String The id of the blog
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets our url
	 *
	 * @return String The url of the blog
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Gets the name for the blog
	 *
	 * @return String name The name of the blog
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the last download date of the blog
	 *
	 * @return String The last download date
	 */
	public String getLastDownload() {
		return lastDownload;
	}
}
