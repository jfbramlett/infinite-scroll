package org.thirdstreet.blogger.reader.google;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.reader.BlogReader;
import org.thirdstreet.blogger.reader.ReadFailureException;
import org.thirdstreet.blogger.reader.ReaderFailure;
import org.thirdstreet.google.GoogleUtils;
import com.bramlettny.common.http.HttpCommand;
import com.bramlettny.common.util.JsonUtil;

/**
 * Class used to read our blog to an XML format from google
 * 
 * @author John Bramlett
 */
public class GoogleBlogReader implements BlogReader {

	private static final Log logger = LogFactory.getLog(GoogleBlogReader.class);

	/**
	 * Constructor
	 */
	public GoogleBlogReader() {
		super();
	}

	@Override
	public Observable<Blog> getBlog(final String blogUrl, final String user) {
		try {
			final String blogResult = HttpCommand.get(GoogleUtils.blogUrl(blogUrl)).asString();
			return Observable.just(new Blog(blogResult, user));
		} catch (Throwable t) {
			final ReaderFailure readerFailure = ReaderFailure.builder()
												.setError(t)
												.setMessage("Unable to get blog details")
												.build();
			return Observable.error(new ReadFailureException("Failed downloading blog " + blogUrl,
					Lists.newArrayList(readerFailure)));
		}
	}

	@Override
	public Observable<BlogPost> getBlogPosts(Blog blog) {

		return Observable.create(subscriber -> {
            try {
                String postJson = HttpCommand.get(GoogleUtils.posts(blog.getBlogId(), blog.getLastDownloadDate())).asString();
                Map<String, Object> json = JsonUtil.toMap(postJson);

                while (json.containsKey("nextPageToken")) {
                    processPost(json, subscriber);

                    String nextPageToken = json.get("nextPageToken").toString();
                    postJson = HttpCommand.get(GoogleUtils.posts(blog.getBlogId(), blog.getLastDownloadDate(), nextPageToken)).asString();

                    json = JsonUtil.toMap(postJson);
                }

                processPost(json, subscriber);
				subscriber.onComplete();
            }
            catch (Exception exception) {
                logger.error("Failed downloading basic blog data (including post)!", exception);
				subscriber.onError(new RuntimeException("Failed downloading basic blog data (including post)!\nCause: " + exception.getMessage(),
						exception));
            }
        });
	}

	/**
	 * Internal routine used to process the result of one of our Http calls to get blog post details
	 *
	 * @param json The josn response from our call
	 * @param subscriber The subscriber we are emitting to
	 */
	private void processPost(final Map<String, Object> json, final ObservableEmitter<BlogPost> subscriber) {
		if (json.containsKey("items") && json.get("items") instanceof Collection) {
			Collection<Object> items = (Collection<Object>) json.get("items");
			items.forEach(o -> {
				if (o instanceof Map) {
					subscriber.onNext(new BlogPost(JsonUtil.toJson(o)));
				}
			});
		}
	}

	@Override
	public Observable<BlogComment> getBlogPostComments(final Blog blog, final BlogPost post) {
		if (post.getCommentCount() > 0) {
			return Observable.create(subscriber -> {
				try {
					String postJson = HttpCommand.get(GoogleUtils.comments(blog.getBlogId(), post.getId())).asString();
					Map<String, Object> json = JsonUtil.toMap(postJson);

					while (json.containsKey("nextPageToken")) {
						processComments(json, subscriber);

						String nextPageToken = json.get("nextPageToken").toString();
						postJson = HttpCommand.get(GoogleUtils.comments(blog.getBlogId(), post.getId(), nextPageToken)).asString();

						json = JsonUtil.toMap(postJson);
					}

					processComments(json, subscriber);

					subscriber.onComplete();
				} catch (Exception exception) {
					logger.error("Failed downloading blog comment)!", exception);
					subscriber.onError(new RuntimeException("Failed downloading blog comments!\nCause: " + exception.getMessage(),
							exception));
				}
			});
		} else {
			return Observable.empty();
		}
	}

	/**
	 * Internal routine used to process the result of one of our Http calls to get blog post comment details
	 *
	 * @param json The josn response from our call
	 * @param subscriber The subscriber we are emitting to
	 * return int The number of comments processed
	 */
	private int processComments(final Map<String, Object> json, final ObservableEmitter<BlogComment> subscriber) {
		if (json.containsKey("items") && json.get("items") instanceof Collection) {
			Collection<Object> items = (Collection<Object>) json.get("items");
			items.forEach(o -> {
				if (o instanceof Map) {
					subscriber.onNext(new BlogComment(JsonUtil.toJson(o)));
				}
			});

			return items.size();
		}

		return 0;
	}
}
