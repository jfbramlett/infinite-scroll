package org.thirdstreet.blogger.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base value object for our dtos.
 */
public abstract class BaseDto {

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException jpe) {
			return "Unable to convert to JSON";
		}
	}
}
