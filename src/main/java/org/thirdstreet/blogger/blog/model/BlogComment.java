package org.thirdstreet.blogger.blog.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import com.bramlettny.common.util.JsonUtil;
import com.google.api.client.util.DateTime;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.thirdstreet.google.GoogleUtils;

/**
 * Value object used to represent a blogger comment
 * 
 * @author John Bramlett
 */
public class BlogComment {

	private final Map<String, Object> blogJson;

	/**
	 * Constructor
	 *
	 * @param blogJson The json map
	 */
	public BlogComment(final String blogJson) {
		super();

		this.blogJson = JsonUtil.toMap(blogJson);
		this.blogJson.put("postId", ((Map)this.blogJson.get("post")).get("id").toString());

		if (!this.blogJson.containsKey("publishedDate")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			this.blogJson.put("publishedDate", sdf.format(getPublished().getTime()));
		}
		if (!this.blogJson.containsKey("publishedMillis")) {
			this.blogJson.put("publishedMillis", getPublished().getTimeInMillis());
		}

	}

	/**
	 * Gets the underlying blog json
	 * @return String The json for the blog
	 */
	public String getBlogJson() {
		return JsonUtil.toJson(blogJson);
	}

	/**
	 * Gets the author
	 * 
	 * @return the author
	 */
	public String getAuthor() {
		return ((Map)blogJson.get("author")).get("displayName").toString();
	}

	/**
	 * Gets the id
	 * 
	 * @return the id
	 */
	public String getId() {
		return blogJson.get("id").toString();
	}

	/**
	 * Gets the published
	 * 
	 * @return the published
	 */
	public Calendar getPublished() {
		return GoogleUtils.convertDate(DateTime.parseRfc3339(blogJson.get("published").toString()));
	}

	/**
	 * Gets the title
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return blogJson.getOrDefault("title", StringUtils.EMPTY).toString();
	}

	/**
	 * Gets the updated
	 * 
	 * @return the updated
	 */
	public Calendar getUpdated() {
		return GoogleUtils.convertDate(DateTime.parseRfc3339(blogJson.get("updated").toString()));
	}

	/**
	 * Gets the text
	 * @return the text
	 */
	public String getText() {
		final String contentText = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeHtml4(blogJson.get("content").toString()));
		final Document doc = Jsoup.parse(contentText);

		return new HtmlToPlainText().getPlainText(doc);
	}
}
