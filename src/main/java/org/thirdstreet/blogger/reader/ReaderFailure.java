package org.thirdstreet.blogger.reader;

import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.blogger.blog.model.BlogImage;

/**
 * Value object used to hold failure information
 * @author bramlej
 */
public class ReaderFailure {

	protected final String message;
	protected final Throwable error;
	protected final Blog blog;
	protected final BlogPost post;
	protected final BlogImage image;

	/**
	 * Creates a new builder.
	 *
	 * @return ReaderFailureBuilder The builder
	 */
	public static ReaderFailureBuilder builder() {
		return new ReaderFailureBuilder();
	}

	/**
	 * Constructor.
	 *
	 * @param builder The builder holding our values
	 */
	private ReaderFailure(final ReaderFailureBuilder builder) {
		super();
		this.message = builder.message;
		this.error = builder.error;
		this.blog = builder.blog;
		this.post = builder.post;
		this.image = builder.image;
	}

	/**
	 * Gets the message
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the error
	 * @return the error
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Gets the blog
	 * @return the blog
	 */
	public Blog getBlog() {
		return blog;
	}

	/**
	 * Gets the post
	 * @return the post
	 */
	public BlogPost getPost() {
		return post;
	}

	/**
	 * Gets the image
	 * @return the image
	 */
	public BlogImage getImage() {
		return image;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer();
		
		msg.append(message);
		return msg.toString();
	}

	/**
	 * Builder used to create our instances.
	 */
	public static class ReaderFailureBuilder {
		private String message;
		private Throwable error;
		private Blog blog;
		private BlogPost post;
		private BlogImage image;

		/**
		 * Constructor.
		 */
		private ReaderFailureBuilder() {
		}

		/**
		 * Sets the message
		 *
		 * @param message The message
		 * @return ReadFailureBuilder The updated builder
		 */
		public ReaderFailureBuilder setMessage(final String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the error
		 *
		 * @param error The error
		 * @return ReadFailureBuilder The updated builder
		 */
		public ReaderFailureBuilder setError(final Throwable error) {
			this.error = error;
			return this;
		}

		/**
		 * Sets the blog
		 *
		 * @param blog The blog
		 * @return ReadFailureBuilder The updated builder
		 */
		public ReaderFailureBuilder setBlog(final Blog blog) {
			this.blog = blog;
			return this;
		}

		/**
		 * Sets the post
		 *
		 * @param post The post
		 * @return ReadFailureBuilder The updated builder
		 */
		public ReaderFailureBuilder setPost(final BlogPost post) {
			this.post = post;
			return this;
		}

		/**
		 * Sets the image
		 *
		 * @param image The image
		 * @return ReadFailureBuilder The updated builder
		 */
		public ReaderFailureBuilder setImage(final BlogImage image) {
			this.image = image;
			return this;
		}

		/**
		 * Creates our entity instance.
		 *
		 * @return ReaderFailure The entity instance
		 */
		public ReaderFailure build() {
			return new ReaderFailure(this);
		}
	}
}
