package com.bramlettny.infinitescroll.web;

import com.bramlettny.infinitescroll.entity.Post;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Web controller used to manage our REST endpoints.
 */
@Controller
public class InfiniteScrollController {

	private static final Logger logger = LoggerFactory.getLogger(InfiniteScrollController.class);

	private static final String kApplicationJson = "application/json";

	private final Executor executor = Executors.newFixedThreadPool(100,
			new BasicThreadFactory.Builder()
				.daemon(true)
				.namingPattern("web")
				.build());

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

	/**
	 * Constructor.
	 */
	@Autowired
	public InfiniteScrollController() {
	}

	@RequestMapping(path = "/feed", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public DeferredResult<List<Post>> post(@RequestParam(value = "size", required = true) final int size,
										   @RequestParam(value = "since", required = false) final int seqSince,
										   @RequestParam(value = "from", required = false) final int seqFrom) {
		final DeferredResult<List<Post>> deferredResult = new DeferredResult<>();

		executor.execute(() -> post(deferredResult, size, seqSince, seqFrom));

		return deferredResult;
	}

	private void post(final DeferredResult<List<Post>> deferredResult,
					  final int size,
					  final int seqSince,
					  final int seqFrom) {
		List<Post> posts = new ArrayList<>(100);
		for (int i = 0; i < 100; i++) {
			posts.add(new Post(UUID.randomUUID().toString(), randomStringGenerator.generate(10)));
		}

		deferredResult.setResult(posts);
	}
}
