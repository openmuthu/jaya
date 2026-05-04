package org.jaya.annotation;

import org.jaya.util.TimestampUtils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class BookmarkGroup {

    /** Monotonic counter so rapid successive createNew() calls get distinct ids. */
    private static final AtomicLong sIdCounter = new AtomicLong(0);

    private String mId;
    private String mName;
    private Date mCreatedDate;

    public BookmarkGroup(String id, String name, Date createdDate) {
        mId = id;
        mName = name;
        mCreatedDate = createdDate;
    }

    /**
     * Create a new group with a guaranteed-unique id.
     * The id encodes the current timestamp plus a monotonic counter to avoid
     * collisions when groups are created in rapid succession (e.g. in tests).
     */
    public static BookmarkGroup createNew(String name) {
        String id = TimestampUtils.nowAsString() + "-" + sIdCounter.incrementAndGet();
        return new BookmarkGroup(id, name, new Date());
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getCreatedDate() {
        return mCreatedDate;
    }
}
