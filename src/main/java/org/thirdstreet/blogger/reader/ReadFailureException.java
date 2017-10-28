package org.thirdstreet.blogger.reader;

import java.util.List;

/**
 * Thrown if there are any failures downloading the blog data.
 */
public class ReadFailureException extends Exception {
	private final List<ReaderFailure> failures;

	/**
	 * Constructor
	 *
	 * @param msg The error message
	 * @param failures The failures
	 */
	public ReadFailureException(final String msg, final List<ReaderFailure> failures) {
		super(msg);
		this.failures = failures;
	}

	/**
	 * Gets the failures
	 * @return List<ReadFailure> The failures
	 */
	public List<ReaderFailure> getFailures() {
		return failures;
	}
}
