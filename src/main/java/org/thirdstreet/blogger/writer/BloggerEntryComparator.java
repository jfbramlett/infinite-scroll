package org.thirdstreet.blogger.writer;

import java.util.Comparator;

import org.thirdstreet.blogger.blog.model.BlogPost;

/**
 * Comparator used to order our blog entries by updated date
 * 
 * @author bramlej
 */
public class BloggerEntryComparator implements Comparator<BlogPost> {

	public static final int kAscending = 1;
	public static final int kDescending = 2;

	protected int direction;

	/**
	 * Constructor
	 */
	public BloggerEntryComparator(int direction_) {
		super();
		direction = direction_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(org.thirdstreet.blogger.BlogEntry, org.thirdstreet.blogger.BlogEntry)
	 */
	public int compare(BlogPost arg0, BlogPost arg1) {

		BlogPost b1 = null;
		BlogPost b2 = null;

		// depending on our direction set our local cast variables
		if (direction == kAscending) {
			b1 = (BlogPost) arg0;
			b2 = (BlogPost) arg1;
		}
		else {
			b1 = (BlogPost) arg1;
			b2 = (BlogPost) arg0;
		}

		if (b1.getPublished().before(b2.getPublished())) {
			return -1;
		}
		else if (b1.getPublished().equals(b2.getPublished())) {
			return 0;
		}
		else {
			return 1;
		}
	}

}
