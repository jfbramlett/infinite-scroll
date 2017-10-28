package org.thirdstreet.blogger.config;

import com.bramlettny.common.concurrent.DaemonThreadFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.cloud.sleuth.DefaultSpanNamer;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.thirdstreet.blogger.blog.BlogFactory;
import org.thirdstreet.blogger.blog.aws.AwsBlogFactory;
import org.thirdstreet.blogger.reader.BlogReader;
import org.thirdstreet.blogger.reader.google.GoogleBlogReader;
import org.thirdstreet.blogger.security.AwsUserDetailsService;
import org.thirdstreet.blogger.writer.BlogWriter;
import org.thirdstreet.blogger.writer.pdf.PdfBlogWriter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configures our blog settings.
 */
@Configuration
public class BlogConfiguration {

	/**
	 * Gets the executor
	 *
	 * @param tracer The tracer
	 * @return Executor The executor
	 */
	@Bean
	public Executor getExecutor(final Tracer tracer) {
		return new TraceableExecutorService(Executors.newFixedThreadPool(100, new DaemonThreadFactory("web executor")),
				tracer, new TraceKeys(), new DefaultSpanNamer());
	}

	/**
	 * Gets the factory used to get our blog downloaded blogs details
	 *
	 * @return BlogFactory The blog factory
	 */
	@Bean
	public BlogFactory getAwsBlogFactory() {
		return new AwsBlogFactory();
	}

	/**
	 * Gets our blog reader
	 * @return BlogReader The blog reader
	 */
	@Bean
	public BlogReader getBlogReader() {
		return new GoogleBlogReader();
	}

	/**
	 * Gets our blog wroter
	 * @param blogFactory The blog factory
	 * @return BlogWriter The blog writer
	 */
	@Bean
	public BlogWriter getBlogWriter(final BlogFactory blogFactory) {
		return new PdfBlogWriter(blogFactory);
	}

	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder() {
		Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
		b.modules(new ParameterNamesModule(), new GuavaModule());
		b.serializationInclusion(JsonInclude.Include.NON_NULL);
		return b;
	}

	@Bean
	public AwsUserDetailsService userDetailService() {
		return new AwsUserDetailsService();
	}
}
