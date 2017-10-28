package org.thirdstreet.blogger.reader;

/**
 * Event used to signal an update to our read status
 */
public class ReadStatusEvent {

	private final String status;

	/**
	 * Constructor.
	 *
	 * @param status The event status
	 */
	public ReadStatusEvent(final String status) {
		this.status = status;
	}

	/**
	 * Gets our event status.
	 *
	 * @return String The event status
	 */
	public String getStatus() {
		return status;
	}
}
