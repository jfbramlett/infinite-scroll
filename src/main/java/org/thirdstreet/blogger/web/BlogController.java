package org.thirdstreet.blogger.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.blog.BlogFactory;
import org.thirdstreet.blogger.blog.model.BlogJob;
import org.thirdstreet.blogger.reader.BlogReader;
import org.thirdstreet.blogger.reader.ReadStatusEvent;
import org.thirdstreet.blogger.security.AwsUserDetailsService;
import org.thirdstreet.blogger.security.CurrentUser;
import org.thirdstreet.blogger.web.dto.BlogDto;
import org.thirdstreet.blogger.web.dto.UserDto;
import org.thirdstreet.blogger.writer.BlogWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Web controller used to manage our REST endpoints.
 */
@Controller
public class BlogController {

	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

	private static final String kApplicationJson = "application/json";

	private final BlogFactory blogFactory;
	private final BlogReader blogReader;
	private final BlogWriter blogWriter;
	private final Executor executor;
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
	private final AwsUserDetailsService userDetailsService;

	/**
	 * Constructor.
	 *
	 * @param blogFactory The blog factory
	 * @param blogReader The blog reader
	 * @param blogWriter The blog writer
	 * @param executor The executor used to run async jobs
	 * @param userDetailsService The user details service
	 */
	@Autowired
	public BlogController(final BlogFactory blogFactory, final BlogReader blogReader,
						  final BlogWriter blogWriter, final Executor executor,
						  final AwsUserDetailsService userDetailsService) {
		this.blogFactory = blogFactory;
		this.blogReader = blogReader;
		this.blogWriter = blogWriter;
		this.executor = executor;
		this.userDetailsService = userDetailsService;
	}

	@RequestMapping(path = "/user", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public DeferredResult<UserDto> user(final OAuth2Authentication authentication) {
		validateUser(authentication);

		final DeferredResult<UserDto> deferredResult = new DeferredResult<>();

		executor.execute(() -> user(getUser(authentication), deferredResult));

		return deferredResult;
	}

	private void user(final String userName, final DeferredResult<UserDto> deferredResult) {
		final CurrentUser currentUser = userDetailsService.getCurrentUser(userName);

		deferredResult.setResult(new UserDto(currentUser.getFirstName(),
				currentUser.getLastName(), currentUser.getUsername()));
	}

	@RequestMapping(path = "/blogs", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public DeferredResult<List<BlogDto>> listBlogs(final OAuth2Authentication authentication) {

		validateUser(authentication);

	    final DeferredResult<List<BlogDto>> deferredResult = new DeferredResult<>();

	    executor.execute(() -> listBlogs(getUser(authentication), deferredResult));

	    return deferredResult;
	}

	private void listBlogs(final String user, final DeferredResult<List<BlogDto>> deferredResult) {
        try {
            final List<BlogDto> blogs = blogFactory.listBlogs(user)
                    .stream()
                    .map(this::toBlogDto)
                    .collect(Collectors.toList());
            deferredResult.setResult(blogs);
        } catch (Throwable t) {
            deferredResult.setErrorResult(t);
        }
    }

	@RequestMapping(path = "/blogs/{id}", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public DeferredResult<BlogDto> getBlog(@PathVariable  final String id, final OAuth2Authentication authentication) {
		validateUser(authentication);

        final DeferredResult<BlogDto> deferredResult = new DeferredResult<>();

        executor.execute(() -> getBlog(id, deferredResult));

        return deferredResult;
	}

	private void getBlog(final String id, final DeferredResult<BlogDto> deferredResult) {
        try {
            Blog blog = blogFactory.getBlog(id);
            deferredResult.setResult(toBlogDto(blog));
        } catch (Throwable t) {
            deferredResult.setErrorResult(t);
        }
    }

	@RequestMapping(path = "/addBlog", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public DeferredResult<List<BlogDto>> addBlog(@RequestParam("url") final String url, final OAuth2Authentication authentication) {
		validateUser(authentication);

        final DeferredResult<List<BlogDto>> deferredResult = new DeferredResult<>();

        final BlogDto blog = new BlogDto(null, url, null, null);

        executor.execute(() -> addBlog(getUser(authentication), blog, deferredResult));

        return deferredResult;
	}

	private void addBlog(final String user, final BlogDto blog, final DeferredResult<List<BlogDto>> deferredResult) {
        try {
            final MutableObject<Throwable> exception = new MutableObject<>();

            blogReader.getBlog(blog.getUrl(), user)
                    .subscribe(blogFactory::createBlog,
                            exception::setValue);
            if (exception.getValue() != null) {
                throw exception.getValue();
            }
            final List<BlogDto> blogs = blogFactory.listBlogs(user)
                    .stream()
                    .map(this::toBlogDto)
                    .collect(Collectors.toList());
            deferredResult.setResult(blogs);
        } catch (Throwable t) {
            deferredResult.setErrorResult(t);
        }
    }

	@RequestMapping(path = "/download/{blogId}", method = RequestMethod.GET)
	public SseEmitter downloadBlogs(@PathVariable final String blogId, final OAuth2Authentication authentication) {
		validateUser(authentication);

		SseEmitter emitter = new SseEmitter(TimeUnit.HOURS.toMillis(1));

		executor.execute(() -> downloadBlog(emitter, blogId));

		return emitter;
	}

	private void downloadBlog(final SseEmitter emitter, final String blogId) {
		Blog blog = blogFactory.getBlog(blogId);
		blog.setCurrentAction("Downloading");
		blogFactory.saveBlog(blog);

		final SSEEmitterWrapper emitterWrapper = new SSEEmitterWrapper(emitter);

		final BlogJob job = new BlogJob(blog.getGuid(), "download");
		blogFactory.saveBlogJob(job);

		try {
			final MutableObject<Throwable> exception = new MutableObject<>();
			final MutableLong postCount = new MutableLong(0);
			final MutableLong imageCount = new MutableLong(0);
			final MutableLong commentCount = new MutableLong(0);

			blogReader.getBlogPosts(blog)
					.subscribe(post -> {
								emitterWrapper.send(new ReadStatusEvent("Processing post " + post.getTitle()));

								blogFactory.saveBlogPost(blog, post);
								postCount.increment();

								blogReader.getBlogPostComments(blog, post)
										.subscribe(c -> {
											blogFactory.saveBlogComment(blog, post, c);
											commentCount.increment();
										});

								post.getImages().forEach(image -> {
									blogFactory.saveBlogImage(blog, post, image);
									imageCount.increment();
								});
							},
							exception::setValue);

			if (exception.getValue() != null) {
				emitterWrapper.completeWithError(exception.getValue());
			} else {
				job.setPostDownloaded(postCount.getValue());
				job.setCommentsDownloaded(commentCount.getValue());
				job.setImagesDownloaded(imageCount.getValue());
				job.setEndTime();
				blogFactory.saveBlogJob(job);

				emitterWrapper.send(new ReadStatusEvent("Completed blog download, retrieved " +
					postCount.getValue() + " posts, " + imageCount.getValue() + " images, and " +
					commentCount.getValue() + " comments."));
				emitterWrapper.complete();
			}
		} finally {
		    blog.setCurrentAction("");
		    blog.setLastDownloadDate(Calendar.getInstance());
		    blogFactory.saveBlog(blog);
        }
	}

	@RequestMapping(path = "/generate/{blogId}", method = RequestMethod.GET)
	public SseEmitter generatePdf(@PathVariable final String blogId,
								  @RequestParam(value="fromDate") final String fromDate,
								  @RequestParam(value="toDate") final String toDate,
								  final OAuth2Authentication authentication) throws ParseException {
		validateUser(authentication);

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		SseEmitter emitter = new SseEmitter(TimeUnit.HOURS.toMillis(1));

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(simpleDateFormat.parse(fromDate));
		final Calendar toCal = Calendar.getInstance();
		toCal.setTime(simpleDateFormat.parse(toDate));

		executor.execute(() -> generatePdf(emitter, blogId, fromCal, toCal));

		return emitter;
	}

	@RequestMapping(value = "/listdownloads/{blogId}", method = RequestMethod.GET, produces = kApplicationJson)
	@ResponseBody
	public List<String> listDownloads(@PathVariable("blogId") final String blogId, final OAuth2Authentication authentication) {
		validateUser(authentication);

		Blog blog = blogFactory.getBlog(blogId);
		return blogFactory.listBlogOutput(blog);
	}

	@RequestMapping(value = "/pdf/{blogId}", method = RequestMethod.GET)
	public void downloadPdf(@PathVariable("blogId") final String blogId,
							@RequestParam("filename") final String filename,
							final OAuth2Authentication authentication,
							final HttpServletResponse response) throws IOException {
		validateUser(authentication);

		Blog blog = blogFactory.getBlog(blogId);

		File pdf = blogFactory.getBlogOutput(blog, filename);

		// Get your file stream from wherever.
		InputStream myStream = new FileInputStream(pdf);

		// Set the content type and attachment header.
		response.addHeader("Content-disposition", "attachment;filename=" + filename);
		response.setContentType("application/pdf");

		// Copy the stream to the response's output stream.
		IOUtils.copy(myStream, response.getOutputStream());
		response.flushBuffer();
	}

	private void generatePdf(final SseEmitter emitter, final String blogId, final Calendar start, final Calendar end) {
		SSEEmitterWrapper emitterWrapper = new SSEEmitterWrapper(emitter);

		try {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyddMM");

			Blog blog = blogFactory.getBlog(blogId);

			final String fileName = blog.getName() + "-" + simpleDateFormat.format(start.getTime()) + "-" + simpleDateFormat.format(end.getTime()) + ".pdf";

			blogWriter.write(blog,start, end,  "./" + fileName, new ReadStatusConsumer(emitterWrapper));

			emitter.send(new ReadStatusEvent("Completed PDF generation"));
			emitterWrapper.complete();
		} catch (Exception e) {
			emitterWrapper.completeWithError(e);
		}
	}

	private BlogDto toBlogDto(final Blog blog) {
		return new BlogDto(blog.getGuid(),
				blog.getUrl(),
				blog.getName(),
				blog.getLastDownloadDate() == null ? "" : simpleDateFormat.format(blog.getLastDownloadDate().getTime()));
	}

	/**
	 * Validates the user as a valid user of the system
	 */
	private void validateUser(final OAuth2Authentication authentication) {

		if (userDetailsService.getCurrentUser(getUser(authentication)) == null) {
			throw new RuntimeException("User " + getUser(authentication) + " is not a valid user for this app");
		}
	}

	/**
	 * Gets the user name from our authentication details
	 *
	 * @param authentication The user details
	 * @return String The user name
	 */
//	private String getUser(final Authentication authentication) {
//		return authentication.getName();
//	}

	/**
	 * Gets the user name from our authentication details
	 *
	 * @param oAuth2Authentication The authentication object
	 * @return String The user name
	 */
	private String getUser(final OAuth2Authentication oAuth2Authentication) {
		final Authentication details = oAuth2Authentication.getUserAuthentication();
		Map<String, Object> detailsMap = (Map<String, Object>) details.getDetails();
		return detailsMap.get("email").toString();
		//return details.getPrincipal().toString();
	}

	public static class ReadStatusConsumer implements Consumer<ReadStatusEvent> {
		private final SSEEmitterWrapper emitterWrapper;

		ReadStatusConsumer(final SSEEmitterWrapper emitterWrapper) {
			this.emitterWrapper = emitterWrapper;
		}

		public void accept(final ReadStatusEvent rse) {
			emitterWrapper.send(rse);
		}
	}

	static class SSEEmitterWrapper {
		private final SseEmitter emitter;

		SSEEmitterWrapper(final SseEmitter emitter) {
			this.emitter = emitter;
		}

		void send(final ReadStatusEvent event) {
			try {
				emitter.send(event, MediaType.APPLICATION_JSON);
			} catch (Throwable t) {
				logger.error("Failed to send event", t);
			}
		}

		void complete() {
			try {
				emitter.complete();
			} catch (Throwable t) {
				logger.error("Failed to send complete event", t);
			}
		}

		void completeWithError(final Throwable ex) {
			try {
				emitter.completeWithError(ex);
			} catch (Throwable t) {
				logger.error("Failed to send complete event", t);
			}
		}

	}
}
