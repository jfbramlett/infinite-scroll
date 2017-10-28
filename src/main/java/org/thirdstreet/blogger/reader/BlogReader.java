package org.thirdstreet.blogger.reader;


import io.reactivex.Observable;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogPost;

/**
 * General interface for a component used to read our blog to an atom xml format
 * 
 * @author John Bramlett
 */
public interface BlogReader {

	/**
	 * Gets the blog summary
	 *
	 * @param blogUrl The blog url
	 * @param user The user who owns this blog
	 * @return Observable&lt;Blog&gt; The blog details for the given url
	 */
	Observable<Blog> getBlog(final String blogUrl, final String user);


	/**
	 * Gets our blog posts
	 *
	 * @param blog The blog we are retrieving
	 * @return Observable&lt;BlogPost&gt; The downloaded blog
	 */
	Observable<BlogPost> getBlogPosts(final Blog blog);

	/**
	 * Gets the comments associated with the given post
	 *
	 * @param blog The blog we are working with
	 * @param post The post we are getting the comments for
	 * @return Observable&lt;BlogComment&gt; The comments
	 */
	Observable<BlogComment> getBlogPostComments(final Blog blog, final BlogPost post);
}