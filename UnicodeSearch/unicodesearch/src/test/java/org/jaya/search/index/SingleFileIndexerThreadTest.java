package org.jaya.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.jaya.search.JayaIndexMetadata;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import static org.apache.lucene.util.Version.LUCENE_47;
import static org.junit.Assert.*;

/**
 * Tests for {@link SingleFileIndexerThread}.
 *
 * The class is package-private so the test lives in the same package.
 */
public class SingleFileIndexerThreadTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File createTextFile(File dir, String name, String content) throws Exception {
        File f = new File(dir, name);
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) {
            w.write(content);
        }
        return f;
    }

    // sft01 — thread indexes a UTF-8 text file and commits
    @Test
    public void sft01_indexesUtf8File() throws Exception {
        File indexDir = tmp.newFolder("idx");
        File srcFile = createTextFile(tmp.newFolder("src"), "rAma.txt",
                "rAma rAma rAma shrI\nrAma hari rAma\nhari hari hari hari\nkRShNa");

        Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config);

        SingleFileIndexerThread thread = new SingleFileIndexerThread(writer, srcFile);
        thread.start();
        thread.join(5000); // max 5s

        assertFalse("sft01: thread must not still be alive after join", thread.isAlive());
        // Commit and close
        writer.commit();
        writer.close();

        // Verify at least one document was added (max doc > 0)
        org.apache.lucene.index.DirectoryReader reader =
                org.apache.lucene.index.DirectoryReader.open(FSDirectory.open(indexDir));
        int numDocs = reader.maxDoc();
        reader.close();
        assertTrue("sft01b: at least one document indexed", numDocs > 0);
    }

    // sft02 — thread handles non-existent file without crashing the thread
    @Test
    public void sft02_nonExistentFile_noException() throws Exception {
        File indexDir = tmp.newFolder("idx2");
        File missing = new File(tmp.getRoot(), "does_not_exist.txt");

        Analyzer analyzer = new StandardAnalyzer(LUCENE_47);
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config);

        SingleFileIndexerThread thread = new SingleFileIndexerThread(writer, missing);
        thread.start();
        thread.join(5000);

        assertFalse("sft02: thread finishes even with missing file", thread.isAlive());
        writer.close();
    }
}
