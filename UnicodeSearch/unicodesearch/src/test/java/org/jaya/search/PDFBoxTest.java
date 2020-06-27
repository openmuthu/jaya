package org.jaya.search;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * This will extract all true type-fonts of a pdf.
 * 
 */
public final class PDFBoxTest
{
    private int fontCounter = 1;

    private static final String PASSWORD = "-password";
    private static final String PREFIX = "-prefix";
    private static final String ADDKEY = "-addkey";

    private PDFBoxTest()
    {
    }

    /**
     * This is the entry point for the application.
     * 
     * @param args The command-line arguments.
     * 
     * @throws IOException If there is an error decrypting the document.
     */
    public static void main(String[] args) throws IOException
    {
    	System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
    	String[] args1 = new String[1];
    	//args1[0] = PDFBoxTest.class.getName();
    	//args1[1] = Constatants.FILES_TO_INDEX_DIRECTORY + File.separator + "itaragraMthAH/svApnavRMdAvanAkhyAna.txt_k";
    	args1[0] = "/Users/labuser/Downloads/gv10.pdf";
    	PDFBoxTest extractor = new PDFBoxTest();
        extractor.extractFonts(args1);
    }

    private void extractFonts(String[] args) throws IOException
    {
        if (args.length < 1 || args.length > 4)
        {
            usage();
        }
        else
        {
            String pdfFile = null;
            String password = "";
            String prefix = null;
            boolean addKey = false;
            for (int i = 0; i < args.length; i++)
            {
                switch (args[i])
                {
                    case PASSWORD:
                        i++;
                        if (i >= args.length)
                        {
                            usage();
                        }
                        password = args[i];
                        break;
                    case PREFIX:
                        i++;
                        if (i >= args.length)
                        {
                            usage();
                        }
                        prefix = args[i];
                        break;
                    case ADDKEY:
                        addKey = true;
                        break;
                    default:
                        if (pdfFile == null)
                        {
                            pdfFile = args[i];
                        }
                        break;
                }
            }
            if (pdfFile == null)
            {
                usage();
            }
            else
            {
                if (prefix == null && pdfFile.length() > 4)
                {
                    prefix = pdfFile.substring(0, pdfFile.length() - 4);
                }
                try (PDDocument document = PDDocument.load(new File(pdfFile), password))
                {
                	PDFTextStripper pdfStripper = new PDFTextStripper();
                    pdfStripper.setStartPage(1);
                    pdfStripper.setEndPage(1);
                    String parsedText = pdfStripper.getText(document);
                    System.out.println("Page1 Text: " + parsedText);
                    
                    for (PDPage page : document.getPages())
                    {
                        PDResources resources = page.getResources();
                        // extract all fonts which are part of the page resources
                        processResources(resources, prefix, addKey);
                    }
                }
            }
        }
    }

    private void processResources(PDResources resources, String prefix, boolean addKey) throws IOException
    {
        if (resources == null)
        {
            return;
        }

        for (COSName key : resources.getFontNames())
        {
            PDFont font = resources.getFont(key);
            // write the font
            if (font instanceof PDTrueTypeFont)
            {
                String name;
                if (addKey)
                {
                    name = getUniqueFileName(prefix + "_" + key, "ttf");
                }
                else
                {
                    name = getUniqueFileName(prefix, "ttf");
                }
                writeFont(font.getFontDescriptor(), name);
            }
            else if (font instanceof PDType0Font)
            {
                PDCIDFont descendantFont = ((PDType0Font) font).getDescendantFont();
                if (descendantFont instanceof PDCIDFontType2)
                {
                    String name;
                    if (addKey)
                    {
                        name = getUniqueFileName(prefix + "_" + key, "ttf");
                    }
                    else
                    {
                        name = getUniqueFileName(prefix, "ttf");
                    }
                    writeFont(descendantFont.getFontDescriptor(), name);
                }
            }
            else
            {
            	PDFontDescriptor desc = font.getFontDescriptor();
            	System.out.println("Font Name: " + font.getName());
            	if( desc != null )
            	{
            		System.out.println("Charset: " + desc.getCharSet()); 
            	}
            }
        }

        for (COSName name : resources.getXObjectNames())
        {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                PDFormXObject xObjectForm = (PDFormXObject) xobject;
                PDResources formResources = xObjectForm.getResources();
                processResources(formResources, prefix, addKey);
            }
        }

    }

    private void writeFont(PDFontDescriptor fd, String name) throws IOException
    {
        if (fd != null)
        {
            PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                System.out.println("Writing font: " + name);
                try (OutputStream os = new FileOutputStream(new File(name + ".ttf"));
                     InputStream is = ff2Stream.createInputStream())
                {
                    IOUtils.copy(is, os);
                }
            }
        }
    }

    private String getUniqueFileName(String prefix, String suffix)
    {
        String uniqueName = null;
        File f = null;
        while (f == null || f.exists())
        {
            uniqueName = prefix + "-" + fontCounter;
            f = new File(uniqueName + "." + suffix);
            fontCounter++;
        }
        return uniqueName;
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + PDFBoxTest.class.getName() + " [OPTIONS] <PDF file>\n"
                + "  -password  <password>        Password to decrypt document\n"
                + "  -prefix  <font-prefix>       Font prefix(default to pdf name)\n"
                + "  -addkey                      add the internal font key to the file name\n"
                + "  <PDF file>                   The PDF document to use\n");
        System.exit(1);
    }

}