package org.thirdstreet.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Collection of utilities for dealing with S3
 */
public class S3Utils {

	private static final Logger logger = LoggerFactory.getLogger(S3Utils.class);

	private static final String SUFFIX = "/";

	/**
	 * Saves the given file to an S3 bucket
	 *
	 * @param bucketName The bucket name
	 * @param folderName The folder name
	 * @param file The file we are writing
	 */
	public static void putItem(final String bucketName, final String folderName, final File file) {
		AmazonS3 s3client = getS3Client();
		createBucket(bucketName);
		createFolder(bucketName, folderName);

		try {
			logger.info("Uploading a new object to S3 from a file\n");
			s3client.putObject(new PutObjectRequest(bucketName, folderName + SUFFIX + file.getName(), file));

			// once our file is uploaded remove the old file
			file.delete();
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason. {}", ase.getMessage(), ase);
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network. {}", ace.getMessage(), ace);
		}

	}

	/**
	 * Creates a virtual folder in S3
	 * @param bucketName The bucket name
	 * @param folderName The folder name
	 */
	public static void createFolder(final String bucketName, final String folderName) {
		AmazonS3 s3client = getS3Client();

		if (!s3client.doesObjectExist(bucketName, folderName)) {
			// create meta-data for your folder and set content-length to 0
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(0);

			// create empty content
			InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

			// create a PutObjectRequest passing the folder name suffixed by /
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
					folderName + SUFFIX, emptyContent, metadata);

			// send request to S3 to create folder
			s3client.putObject(putObjectRequest);
		}
	}

	/**
	 * Create our s3 bucket with the given name.
	 *
	 * @param bucket The bucket name
	 */
	public static void createBucket(final String bucket) {
		try {
			AmazonS3 s3client = getS3Client();
			if (!s3client.doesBucketExist(bucket)) {
				s3client.createBucket(bucket);
			}
		} catch (Throwable t) {
			logger.error("Failed to create bucket " + bucket, t);
		}
	}

	/**
	 * Gets the buket name for the given blog.
	 *
	 * @param blog The blog we are getting the bucket name for
	 * @return String The bucket name
	 */
	public static String getBucketName(final Blog blog) {
		return blog.getGuid() + ".blog-backup.3street.net";
	}

	/**
	 * Creates our S3 client.
	 *
	 * @return AmazonS3 The s3 client
	 */
	private static AmazonS3 getS3Client() {
		return AmazonS3ClientBuilder.standard()
				.withRegion("us-east-1")
				.build();
	}

	/**
	 * Gets an item from an S3 bucket
	 *
	 * @param bucketName The bucket name
	 * @param folder The folder
	 * @param localFolder The local folder to write the item to
	 * @param itemName The item we are getting
	 * @return InputStream The item
	 */
	public static File getItem(final String bucketName, final String folder, final String localFolder, final String itemName) {
		try {
			File localItem = new File(localFolder + File.separator + itemName);

			FileOutputStream fos = new FileOutputStream(localItem);
			InputStream s3Item = getItemAsStream(bucketName, folder, itemName);

			IOUtils.copy(s3Item, fos);

			s3Item.close();
			fos.close();

			return localItem;
		}catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason. {}", ase.getMessage(), ase);
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network. {}", ace.getMessage(), ace);
		} catch (IOException ioe) {
			logger.error("Failed to write temp file", ioe);
		}

		return null;
	}

	/**
	 * List items from an S3 bucket
	 *
	 * @param bucketName The bucket name
	 * @param folder The folder
	 * @return List<String> The bucket items
	 */
	public static List<String> listItems(final String bucketName, final String folder) {
		final List<String> results = Lists.newArrayList();

		try {
			ListObjectsV2Request req = new ListObjectsV2Request()
					.withBucketName(bucketName)
					.withPrefix(folder)
					.withDelimiter(SUFFIX);

			ListObjectsV2Result listing = getS3Client().listObjectsV2(req);
			for (S3ObjectSummary summary: listing.getObjectSummaries()) {
				final String filename = summary.getKey().replace(folder, "");
				if (filename.length() > 0) {
					results.add(filename);
				}
			}

			return results;
		}catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason. {}", ase.getMessage(), ase);
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network. {}", ace.getMessage(), ace);
		}

		return null;
	}



	/**
	 * Gets an item from an S3 bucket
	 *
	 * @param bucketName The bucket name
	 * @param folder The folder
	 * @param itemName The item we are getting
	 * @return InputStream The item
	 */
	public static InputStream getItemAsStream(final String bucketName, final String folder, final String itemName) {
		AmazonS3 s3client = getS3Client();

		try {
			S3Object object = s3client.getObject(
					new GetObjectRequest(bucketName, folder + SUFFIX + itemName));
			return object.getObjectContent();
		}catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason. {}", ase.getMessage(), ase);
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network. {}", ace.getMessage(), ace);
		}

		return null;
	}
}
