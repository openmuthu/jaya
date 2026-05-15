package org.jaya.android;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Builds and parses the {@code https://openmuthu.github.io/bookmark?…} deep-link
 * URL used to share bookmarks.
 *
 * <p>Kept as pure Java (no Android imports) so it can be covered by plain JUnit tests.
 */
public class BookmarkDeepLink {

    static final String BASE_URL = "https://openmuthu.github.io/bookmark";

    private BookmarkDeepLink() {}

    /**
     * Builds the shareable deep-link URL for a document location.
     *
     * @param path    Lucene FIELD_PATH value
     * @param localId Lucene FIELD_DOC_LOCAL_ID value
     * @param fp      content fingerprint (may be empty)
     * @param name    bookmark name (may be empty)
     * @return fully-formed URL string, or empty string on encoding failure
     */
    public static String buildUrl(String path, String localId, String fp, String name) {
        try {
            return BASE_URL
                    + "?path=" + URLEncoder.encode(path  != null ? path    : "", "UTF-8")
                    + "&id="   + URLEncoder.encode(localId != null ? localId : "", "UTF-8")
                    + "&fp="   + URLEncoder.encode(fp    != null ? fp      : "", "UTF-8")
                    + "&name=" + URLEncoder.encode(name  != null ? name    : "", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
