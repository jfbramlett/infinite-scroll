package org.thirdstreet.blogger.blog.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.bramlettny.common.util.JsonUtil;
import com.google.api.client.util.Lists;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.thirdstreet.google.GoogleUtils;

/**
 * Value object used to represent a blogger entry
 * 
 * @author John Bramlett
 */
public class BlogPost {

	private final List<BlogImage> images;
	private final Map<String, Object> blogJson;

	/**
	 * Constructor
	 */
	public BlogPost(final String blogPostJson) {
		super();

		this.blogJson = JsonUtil.toMap(blogPostJson);

		// find any anchors or images
		final String contentText = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeHtml4(blogJson.get("content").toString()));
		final Document doc = Jsoup.parse(contentText);
		this.images = Lists.newArrayList();
		Elements images = doc.select("img");
		if (images != null) {
			for (Element e : images) {
				final String src = e.attr("src");
				BlogImage image = new BlogImage(src, getId());
				this.images.add(image);
			}
		}

		if (!blogJson.containsKey("publishedDate")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			blogJson.put("publishedDate", sdf.format(getPublished().getTime()));
		}
		if (!blogJson.containsKey("publishedMillis")) {
			blogJson.put("publishedMillis", getPublished().getTimeInMillis());
		}
	}

	/**
	 * Gets our blog post json
	 * @return String The blog post as json
	 */
	public String getBlogPostJson() {
		return JsonUtil.toJson(blogJson);
	}

	/**
	 * Gets the number of expected comments
	 * @return long The number of expected comments
	 */
	public long getCommentCount() {
		return Long.valueOf(((Map)blogJson.get("replies")).get("totalItems").toString());
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
		return GoogleUtils.convertDate(blogJson.get("published"));
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
		return GoogleUtils.convertDate(blogJson.get("updated"));
	}

	/**
	 * Gets the text
	 * @return the text
	 */
	public String getText() {
		final String contentText = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeHtml4(blogJson.get("content").toString()));
		final Document doc = Jsoup.parse(contentText);

		return new HtmlToPlainText().getPlainText(doc).replaceAll("<http.*>(\n)?", "");
	}

	/**
	 * Gets the images
	 * @return the images
	 */
	public List<BlogImage> getImages() {
		return images;
	}

	/**
	 * Gets a list of images where we have a valid file
	 * @return List<BlogImage> The set of valid images
	 */
	public List<BlogImage> getValidImages() {
		List<BlogImage> result = new LinkedList<BlogImage>();
		
		if (images != null) {
			for (BlogImage i : images) {
				//if (i.exists()) {
					result.add(i);
				//}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a flag indicating if we have images
	 * @return Returns true if we have images, false otherwise
	 */
	public boolean hasImages() {
		return ((images != null) && (images.size() > 0));
	}
	
	/**
	 * Returns a flag indicating if we have comments
	 * @return boolean Returns true if there are comments, false otherwise
	 */
	public boolean hasComments() {
		return getCommentCount() > 0;
	}
}
