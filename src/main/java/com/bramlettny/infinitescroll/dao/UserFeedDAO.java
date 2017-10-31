package com.bramlettny.infinitescroll.dao;

import com.bramlettny.infinitescroll.entity.Post;
import org.apache.commons.text.RandomStringGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Data access object used to retrieve our post data as part of
 * a users feed.
 */
public class UserFeedDAO {

	private final RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().build();

	private final ArrayList<Post> feed = new ArrayList<>(1000);

	/**
	 * Constructor.
	 */
	public UserFeedDAO() {
		for (int i = 0; i < 1000; i++) {
			feed.add(new Post(UUID.randomUUID().toString(), randomStringGenerator.generate(10)));
		}
	}

	/**
	 * Retrieve the latest posts returning up to size elements.
	 *
	 * @param size The number of posts to return
	 * @return List&lt;Post&gt; The set of posts
	 */
	public List<Post> retrievePosts(final int size) {
		if (size > feed.size()) {
			return constructResult(0, feed.size());
		} else {
			return constructResult(feed.size() - size, feed.size());
		}
	}

	/**
	 * Retrieve posts from a given sequence number returning up to size elements.
	 *
	 * @param fromSeq The from sequence number
	 * @param size The number of records to retrieve
	 * @return List&lt;Post&gt; The set of posts
	 */
	public List<Post> retrievePostsFrom(final int fromSeq, final int size) {

	}

	/**
	 * Retrieve posts since a given sequence number returning up to size elements.
	 *
	 * @param sinceSeq The since sequence number
	 * @param size The number of records to retrieve
	 * @return List&lt;Post&gt; The set of posts
	 */
	public List<Post> retrievePostsSince(final int sinceSeq, final int size) {

	}

	/**
	 * Construct the result by converting the sublist and reversing.
	 *
	 * @param fromIndex The starting point of our sublist
	 * @param toIndex The end point of our sublist
	 * @return List&lt;Post&gt; The set of posts
	 */
	private List<Post> constructResult(final int fromIndex, final int toIndex) {
		final List<Post> result = feed.subList(fromIndex, toIndex);
		Collections.reverse(result);
		return result;
	}
}
