package org.thirdstreet.blogger.blog;

import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogJob;
import org.thirdstreet.blogger.blog.model.BlogPost;

import java.io.File;
import java.util.Calendar;
import java.util.List;

/**
 * Factory used to create our blog value objects
 * 
 * @author John Bramlett
 * 
 */
public interface BlogFactory {

	/**
	 * Creates/saves a new blog
	 *
	 * @param blog The blog we are saving
	 */
	void createBlog(final Blog blog);

	/**
	 * Saves a new blog
	 *
	 * @param blog The blog we are saving
	 */
	void saveBlog(final Blog blog);

	/**
	 * Gets the list of configured blogs
	 *
	 * @param user The user we are listing the blogs for
	 * @return List<Blog> The list of configured blogs
	 */
	List<Blog> listBlogs(final String user);

	/**
	 * Gets the blog with the given name
	 * @param id The id of the blog
	 * @return Blog The blog
	 */
	Blog getBlog(final String id);

	/**
	 * Takes the blog output in the given file and persists it
	 *
	 * @param blog The blog we are writing the output for
	 * @param file The name of the file the output was written to
	 */
	void saveBlogOutput(final Blog blog, final File file);

	/**
	 * Gets the url for the blog output (the pdf).
	 *
	 * @param blog The blog we are working with
	 * @param filename The filename of the output we are getting
	 * @return String The url for the output
	 */
	File getBlogOutput(final Blog blog, final String filename);

	/**
	 * Lists the set of downloadable blogs.
	 *
	 * @param blog The blog we are getting the available downloads
	 * @return List<String> The set of download files
	 */
	List<String> listBlogOutput(final Blog blog);

	/**
	 * Gets the posts for the given blog - the posts are the blog contents
	 *
	 * @param blog The blog we are getting the post for
	 * @return List<BlogPost> The blog posts
	 */
	List<BlogPost> getBlogPosts(final Blog blog);

	/**
	 * Gets the posts for the given blog - the posts are the blog contents
	 *
	 * @param blog The blog we are getting the post for
	 * @param start The start time period for posts
	 * @param end The end time period for posts to retrieve
	 * @return List<BlogPost> The blog posts
	 */
	List<BlogPost> getBlogPosts(final Blog blog, final Calendar start, final Calendar end);

	/**
	 * Save the blog posts
	 * @param blog The blog
	 * @param post The blog post
	 */
	void saveBlogPost(final Blog blog, final BlogPost post);

	/**
	 * Save the blog post comments
	 * @param blog The blog
	 * @param post The post
	 * @param comments The blog comments
	 */
	void saveBlogComments(final Blog blog, final BlogPost post, final List<BlogComment> comments);

	/**
	 * Save the blog post comments
	 * @param blog The blog
	 * @param post The post
	 * @param comment The blog comment
	 */
	void saveBlogComment(final Blog blog, final BlogPost post, final BlogComment comment);

	/**
	 * Gets the comments for the given blog post
	 * @param blog The blog
	 * @param post The post
	 * @return List<BlogComment> The comments
	 */
	List<BlogComment> getBlogComments(final Blog blog, final BlogPost post);

	/**
	 * Saves the blog image.
	 * @param blog The blog we are downloading
	 * @param post The blog post
	 * @param image The image we are saving
	 */
	void saveBlogImage(final Blog blog, final BlogPost post, final BlogImage image);

	/**
	 * Gets the blog image on local file system
	 *
	 * @param blog The blog we are retrieving the image for
	 * @param post The post associated with this image
	 * @param image The image details
	 * @return File A local version of the image
	 */
	File getBlogImage(final Blog blog, final BlogPost post, final BlogImage image);

	/**
	 * Saves the job details.
	 *
	 * @param job The job details
	 */
	void saveBlogJob(final BlogJob job);

}
