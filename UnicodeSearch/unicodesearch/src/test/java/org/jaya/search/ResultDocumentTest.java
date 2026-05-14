package org.jaya.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jaya.scriptconverter.ScriptType;
import org.jaya.util.Constatants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ResultDocument}.
 */
public class ResultDocumentTest {

    private Document makeDoc(String path, String contents, String docLocalId) {
        Document doc = new Document();
        if (path != null)
            doc.add(new StringField(Constatants.FIELD_PATH, path, Field.Store.YES));
        if (contents != null)
            doc.add(new TextField(Constatants.FIELD_CONTENTS, contents, Field.Store.YES));
        if (docLocalId != null)
            doc.add(new StringField(Constatants.FIELD_DOC_LOCAL_ID, docLocalId, Field.Store.YES));
        return doc;
    }

    // rd01 — getId returns the doc id supplied at construction
    @Test
    public void rd01_getId() {
        Document doc = makeDoc("/path/file.txt", "content", "3");
        ResultDocument rd = new ResultDocument(42, doc);
        assertEquals("rd01: id must be 42", 42, rd.getId());
    }

    // rd02 — getDoc returns the Lucene document
    @Test
    public void rd02_getDoc() {
        Document doc = makeDoc("/path/file.txt", "content", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        assertSame("rd02: same Lucene doc returned", doc, rd.getDoc());
    }

    // rd03 — getPathSansExtensionForScriptType with ITRANS returns path minus extension
    @Test
    public void rd03_getPathSansExtensionForScriptType_itrans() {
        Document doc = makeDoc("/dAsasAhitya/kIrtane/song.txt", "content", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        String path = rd.getPathSansExtensionForScriptType(ScriptType.ITRANS);
        assertEquals("rd03: ITRANS path sans extension",
                "/dAsasAhitya/kIrtane/song", path);
    }

    // rd04 — getPathSansExtensionForScriptType with DEVANAGARI produces Devanagari text
    @Test
    public void rd04_getPathSansExtensionForScriptType_devanagari() {
        Document doc = makeDoc("/rAma/file.txt", "content", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        String path = rd.getPathSansExtensionForScriptType(ScriptType.DEVANAGARI);
        assertNotNull("rd04a: not null", path);
        assertFalse("rd04b: not empty", path.isEmpty());
    }

    // rd05 — getPathSansExtensionForScriptType returns empty string when doc is null
    @Test
    public void rd05_getPathSansExtensionForScriptType_nullDoc() {
        ResultDocument rd = new ResultDocument(0, null);
        assertEquals("rd05: null doc returns empty", "", rd.getPathSansExtensionForScriptType(ScriptType.ITRANS));
    }

    // rd06 — getDocContentsForScriptType with ITRANS content returns it unchanged
    @Test
    public void rd06_getDocContentsForScriptType_itrans() {
        Document doc = makeDoc("/path/f.txt", "rAma hari shrI", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        String contents = rd.getDocContentsForScriptType(ScriptType.ITRANS);
        assertEquals("rd06: ITRANS content returned unchanged", "rAma hari shrI", contents);
    }

    // rd07 — getDocContentsForScriptType with DEVANAGARI destination converts
    @Test
    public void rd07_getDocContentsForScriptType_devanagari() {
        Document doc = makeDoc("/path/f.txt", "rAma", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        String contents = rd.getDocContentsForScriptType(ScriptType.DEVANAGARI);
        assertNotNull("rd07a: not null", contents);
        assertFalse("rd07b: not empty", contents.isEmpty());
    }

    // rd08 — getDocContentsForScriptType returns empty string when doc is null
    @Test
    public void rd08_getDocContentsForScriptType_nullDoc() {
        ResultDocument rd = new ResultDocument(0, null);
        assertEquals("rd08: null doc returns empty", "", rd.getDocContentsForScriptType(ScriptType.ITRANS));
    }

    // rd09 — getPathSansExtensionForScriptType with KANNADA destination
    @Test
    public void rd09_getPathSansExtensionForScriptType_kannada() {
        Document doc = makeDoc("/rAma/file.txt", "content", "0");
        ResultDocument rd = new ResultDocument(0, doc);
        String path = rd.getPathSansExtensionForScriptType(ScriptType.KANNADA);
        assertNotNull("rd09: not null", path);
    }
}
