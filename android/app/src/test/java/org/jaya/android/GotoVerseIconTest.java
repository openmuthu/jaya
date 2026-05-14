package org.jaya.android;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the goto-verse action bar icon uses a white fill colour so
 * it remains visible against the dark action bar
 * ({@code Theme.Holo.Light.DarkActionBar}).
 *
 * Scenarios covered
 * -----------------
 * IC-1  ic_goto_verse_white_24dp.xml exists in the drawable directory
 * IC-2  Its &lt;path&gt; element's fillColor attribute is white (#FFFFFFFF)
 * IC-3  main.xml menu references ic_goto_verse_white_24dp for action_verse_nav
 */
public class GotoVerseIconTest {

    private static final String DRAWABLE_PATH =
            "src/main/res/drawable/ic_goto_verse_white_24dp.xml";
    private static final String MENU_PATH =
            "src/main/res/menu/main.xml";
    private static final String WHITE_FILL = "#FFFFFFFF";

    // ── IC-1 ─────────────────────────────────────────────────────────────────

    @Test
    public void gotoVerseIcon_fileExists() {
        File iconFile = new File(DRAWABLE_PATH);
        assertTrue("ic_goto_verse_white_24dp.xml must exist so the icon is " +
                "visible on the dark action bar", iconFile.exists());
    }

    // ── IC-2 ─────────────────────────────────────────────────────────────────

    @Test
    public void gotoVerseIcon_fillColorIsWhite() throws Exception {
        File iconFile = new File(DRAWABLE_PATH);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = db.parse(iconFile);

        NodeList paths = dom.getElementsByTagName("path");
        assertTrue("ic_goto_verse_white_24dp.xml must contain at least one <path>",
                paths.getLength() > 0);

        Element path = (Element) paths.item(0);
        String fillColor = path.getAttribute("android:fillColor");
        assertNotNull("fillColor attribute must be set", fillColor);
        assertEquals(
                "Icon fill must be white (" + WHITE_FILL + ") so it is visible " +
                "on the DarkActionBar background; was: " + fillColor,
                WHITE_FILL, fillColor.toUpperCase());
    }

    // ── IC-3 ─────────────────────────────────────────────────────────────────

    @Test
    public void menuXml_verseNavUsesWhiteIcon() throws Exception {
        File menuFile = new File(MENU_PATH);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = db.parse(menuFile);

        NodeList items = dom.getElementsByTagName("item");
        String verseNavIcon = null;
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String id = item.getAttribute("android:id");
            if ("@+id/action_verse_nav".equals(id)) {
                verseNavIcon = item.getAttribute("android:icon");
                break;
            }
        }

        assertNotNull("action_verse_nav menu item must be present", verseNavIcon);
        assertEquals(
                "action_verse_nav must reference ic_goto_verse_white_24dp so it " +
                "is visible on the dark action bar",
                "@drawable/ic_goto_verse_white_24dp", verseNavIcon);
    }
}
