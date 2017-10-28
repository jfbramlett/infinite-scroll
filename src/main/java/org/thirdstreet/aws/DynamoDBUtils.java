package org.thirdstreet.aws;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.bramlettny.common.util.JsonUtil;
import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdstreet.blogger.blog.model.Blog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Set of utilities for interacting with DynamoDB
 */
public final class DynamoDBUtils {

	private static final Logger logger = LoggerFactory.getLogger(DynamoDBUtils.class);

	/**
	 * Constructor.
	 */
	private DynamoDBUtils() {
	}


	/**
	 * Write an item to our table.
	 *
	 * @param tableName The table to write to
	 * @param json The json content
	 * @param keyField The key field
	 * @param key The key value
	 */
	public static void writeEntry(final String tableName, final String json, final String keyField, final String key) {
		Table table = getTable(tableName);

		try {
			Map<String, Object> jsonMap = JsonUtil.toMap(json);
			cleanMap(jsonMap);

			Item newItem = new Item();
			newItem.withPrimaryKey(keyField, key);
			for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
				newItem.with(entry.getKey(), entry.getValue());
			}
			table.putItem(newItem);
			System.out.println("PutItem succeeded: " + key);

		} catch (Throwable t) {
			logger.error("Failed to write blog", t);
		}
	}


	private static void cleanMap(final Map<String, Object> map) {
		final Set<String> keysToRemove = Sets.newHashSet();

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			final Object value = entry.getValue();

			if (value == null) {
				keysToRemove.add(entry.getKey());
			} else {
				if (value instanceof String && value.toString().length() == 0) {
					keysToRemove.add(entry.getKey());
				} else if (value instanceof Collection) {
					// todo
				} else if (value instanceof Map) {
					if (((Map)value).size() == 0) {
						keysToRemove.add(entry.getKey());
					} else {
						cleanMap((Map<String, Object>) value);
					}
				}
			}
		}

		if (keysToRemove.size() > 0) {
			keysToRemove.forEach(k -> map.remove(k));
		}
	}


	public static <T> List<T> scanTable(final String tablename, final ScanFilter scanFilter, final Function<Item, T> converter) {
		final List<T> results = Lists.newArrayList();
		final Table table = getTable(tablename);

		ItemCollection<ScanOutcome> items =  table.scan(scanFilter);
		for (Item item : items) {
			results.add(converter.apply(item));
		}

		return results;
	}

	public static <T> T getItem(final String tablename, final PrimaryKey primaryKey, final Function<Item, T> converter) {
		final Table table = getTable(tablename);

		Item item = table.getItem(primaryKey);
		if (item != null) {
			return converter.apply(item);
		}

		return null;
	}

	private static AmazonDynamoDB getDynamoDBClient() {
		return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration("https://dynamodb.us-east-1.amazonaws.com", "us-east-1"))
				.build();
	}

	private static DynamoDB getDynamoDB() {
		return new DynamoDB(getDynamoDBClient());
	}

	public static Table getTable(final String tableName) {
		return getDynamoDB().getTable(tableName);
	}

	public static void createTable(final String tableName, final String keyField, final long readRateLimit, final long writeRateLimit) {
		try {
			TableUtils.createTableIfNotExists(getDynamoDBClient(),
					new CreateTableRequest()
							.withTableName(tableName)
							.withKeySchema(new KeySchemaElement(keyField, KeyType.HASH))
							.withProvisionedThroughput(new ProvisionedThroughput(readRateLimit, writeRateLimit))
							.withAttributeDefinitions(new AttributeDefinition(keyField, ScalarAttributeType.S)));
		} catch (Throwable t) {
			logger.error("Failed to create table " + tableName, t);
			throw t;
		}
	}
}
