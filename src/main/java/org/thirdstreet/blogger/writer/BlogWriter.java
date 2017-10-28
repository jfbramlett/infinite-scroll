package org.thirdstreet.blogger.writer;

import org.thirdstreet.blogger.blog.model.Blog;
import org.thirdstreet.blogger.reader.ReadStatusEvent;

import java.util.Calendar;
import java.util.function.Consumer;

/**
 * Interface for our blogger writer - used to write a blog to some output
 * system
 *
 * @author John Bramlett
 */
public interface BlogWriter
{

  /**
   * Writes our blog
   * @param blog The blog we are writing
   * @param start The start time period
   * @param end The end time period
   * @param filename The name of the file we are writing to
   * @param statusConsumer The consumer to send status update to
   * @throws Exception
   */
  void write(final Blog blog, final Calendar start, final Calendar end, final String filename,
             final Consumer<ReadStatusEvent> statusConsumer) throws Exception;
}