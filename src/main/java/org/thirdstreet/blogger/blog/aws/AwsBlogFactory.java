package org.thirdstreet.blogger.blog.aws;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.bramlettny.common.util.JsonUtil;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.aws.DynamoDBUtils;
import org.thirdstreet.aws.S3Utils;
import org.thirdstreet.blogger.blog.BlogFactory;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.model.BlogComment;
import org.thirdstreet.blogger.blog.model.BlogImage;
import org.thirdstreet.blogger.blog.model.BlogJob;
import org.thirdstreet.blogger.blog.model.BlogPost;
import org.thirdstreet.utils.FileUtilities;

import java.io.File;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * BlogFactory using Amazon's DynamoDB as a backend store.
 */
public class AwsBlogFactory implements BlogFactory {
	private static final Logger logger = LoggerFactory.getLogger(AwsBlogFactory.class);

	private static final String BLOG_OUTPUT_FOLDER = "pdf";
	private static final String IMAGE_FOLDER = "images";

	private static final String kUserTableName = "users";
	private static final String kUserKeyField = "email";

	private static final String kTableName = "blogs";
	private static final String kBlogsKeyField = "guid";

	private static final String kPostKeyField = "id";
	private static final String kCommentKeyField = "id";

	private static final String kJobsTableName = "jobs";
	private static final String kJobsKeyField = "id";

	private final long readRateLimit;
	private final long writeRateLimit;
	private final RateLimiter readRateLimiter;
	private final RateLimiter writeRateLimiter;


	/**
	 * Constructor.
	 */
	public AwsBlogFactory() {
		readRateLimit = 1;
		writeRateLimit = 1;
		this.readRateLimiter = RateLimiter.create(readRateLimit);
		this.writeRateLimiter = RateLimiter.create(writeRateLimit);
	}

	@Override
	public void createBlog(Blog blog) {
		saveBlog(blog);

		// create our tables for posts and comments
		logger.info("Create table for posts");
		DynamoDBUtils.createTable(getPostTableName(blog), kPostKeyField, readRateLimit, writeRateLimit);
		logger.info("Creating table for comments");
		DynamoDBUtils.createTable(getCommentTableName(blog), kCommentKeyField, readRateLimit, writeRateLimit);
		logger.info("Creating s3bucket for images");

		S3Utils.createBucket(S3Utils.getBucketName(blog));
		S3Utils.createFolder(S3Utils.getBucketName(blog), IMAGE_FOLDER);
	}

	@Override
	public void saveBlog(Blog blog) {
		writeRateLimiter.acquire();

		try {
			DynamoDBUtils.writeEntry(kTableName, blog.getBlogJson(), kBlogsKeyField, blog.getName());
		} catch (Throwable t) {
			logger.error("Failed to save our blog", t);
			throw new RuntimeException("Failed to save our blog", t);
		}
	}

	@Override
	public List<Blog> listBlogs(final String user) {
		readRateLimiter.acquire();

		final ScanFilter scanFilter = new ScanFilter(Blog.kOwner).eq(user);
		return DynamoDBUtils.scanTable(kTableName, scanFilter, this::createBlog);
	}

	@Override
	public Blog getBlog(String id) {
		readRateLimiter.acquire();

		return DynamoDBUtils.getItem(kTableName, new PrimaryKey(new KeyAttribute("guid", id)), this::createBlog);
	}

	@Override
	public void saveBlogOutput(final Blog blog, final File file) {
		S3Utils.putItem(S3Utils.getBucketName(blog), BLOG_OUTPUT_FOLDER, file);
	}

	@Override
	public File getBlogOutput(Blog blog, final String filename) {
		return S3Utils.getItem(S3Utils.getBucketName(blog), BLOG_OUTPUT_FOLDER,
				System.getProperty("java.io.tmpdir"), filename);
	}

	@Override
	public List<String> listBlogOutput(Blog blog) {
		return S3Utils.listItems(S3Utils.getBucketName(blog), BLOG_OUTPUT_FOLDER + "/");
	}

	@Override
	public void saveBlogJob(BlogJob job) {
		writeRateLimiter.acquire();
		try {
			DynamoDBUtils.writeEntry(kJobsTableName, job.getBlogPostJson(), kJobsKeyField, job.getId());
		} catch (Throwable t) {
			logger.error("Failed to save our blog", t);
			throw new RuntimeException("Failed to save our blog", t);
		}
	}

	/**
	 * Creates a blog entry from the given item.
	 *
	 * @param item The item from the db
	 * @return Blog The new blog entry
	 */
	private Blog createBlog(final Item item) {
		return new Blog(JsonUtil.toJson(item.asMap()));
	}

	@Override
	public List<BlogPost> getBlogPosts(final Blog blog) {
		final Date minDate = new Date(0L);
		final Calendar minCal = Calendar.getInstance();
		minCal.setTime(minDate);

		return getBlogPosts(blog, minCal, Calendar.getInstance());
	}

	@Override
	public List<BlogPost> getBlogPosts(final Blog blog, final Calendar start, final Calendar end) {
		readRateLimiter.acquire();

		ScanFilter filter = new ScanFilter("publishedMillis").between(convertDate(start), convertDate(end));
		final List<BlogPost> results = DynamoDBUtils.scanTable(getPostTableName(blog), filter, this::createBlogPost);

		// now sort these by date descending
		results.sort(Comparator.comparing(BlogPost::getPublished).reversed());
		
		return results;
	}

	private long convertDate(final Calendar cal) {
		return cal.getTimeInMillis();
	}

	@Override
	public void saveBlogPost(final Blog blog, final BlogPost blogPost) {
		writeRateLimiter.acquire();
		DynamoDBUtils.writeEntry(getPostTableName(blog), blogPost.getBlogPostJson(), kPostKeyField, blogPost.getId());
	}

	/**
	 * Looks up the given post by id
	 * @param blog The blog we are looking up the value for
	 * @param id The id for the post we are looking up
	 * @return BlogPost The post if it exists, null if it doesn't
	 */
	private BlogPost getBlogPost(final Blog blog, final String id) {
		readRateLimiter.acquire();

		return DynamoDBUtils.getItem(getPostTableName(blog), new PrimaryKey(new KeyAttribute(kPostKeyField, id)), this::createBlogPost);
	}

	/**
	 * Create a blog post from the given item
	 *
	 * @param item The item we are creating the post from
	 * @return BlogPost The post
	 */
	private BlogPost createBlogPost(final Item item) {
		return new BlogPost(JsonUtil.toJson(item.asMap()));
	}

	private String getPostTableName(final Blog blog) {
		return blog.getGuid();
	}

	@Override
	public void saveBlogComments(Blog blog, BlogPost post, List<BlogComment> comments) {
		for (BlogComment comment : comments) {
			saveBlogComment(blog, post, comment);
		}
	}

	@Override
	public void saveBlogComment(Blog blog, BlogPost post, BlogComment comment) {
		writeRateLimiter.acquire();

		DynamoDBUtils.writeEntry(getCommentTableName(blog), comment.getBlogJson(), kCommentKeyField, comment.getId());
	}

	@Override
	public List<BlogComment> getBlogComments(Blog blog, BlogPost post) {
		readRateLimiter.acquire();

		if (post.hasComments()) {
			ScanFilter filter = new ScanFilter("postId").eq(post.getId());
			return DynamoDBUtils.scanTable(getCommentTableName(blog), filter, this::createBlogComment);
		} else {
			return Lists.newArrayList();
		}
	}

	/**
	 * Create a blog comment from the given item
	 *
	 * @param item The item we are creating the post from
	 * @return BlogComment The comment
	 */
	private BlogComment createBlogComment(final Item item) {
		return new BlogComment(JsonUtil.toJson(item.asMap()));
	}

	private String getCommentTableName(final Blog blog) {
		return blog.getGuid() + "-comments";
	}

	@Override
	public void saveBlogImage(Blog blog, BlogPost post, BlogImage image) {
		try {
			File localImageFile = FileUtilities.downloadImage(image.getImageUrl(), image.getTempImageDir(), image.getImageName());

			S3Utils.putItem(S3Utils.getBucketName(blog), IMAGE_FOLDER, localImageFile);
		} catch (Exception e) {
			logger.error("Failed to download/save image " + image.getImageName(), e);
		}
	}

	@Override
	public File getBlogImage(Blog blog, BlogPost post, BlogImage image) {
		try {
			return S3Utils.getItem(S3Utils.getBucketName(blog), IMAGE_FOLDER,
					image.getTempImageDir().getAbsolutePath(), image.getImageName());
		} catch (Exception e) {
			logger.error("Failed to download image " + image.getImageName() + " from S3", e);
			return new File(image.getTempImageDir() + File.separator + image.getImageName());
		}
	}
}
