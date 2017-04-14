package org.jaya.scriptconverter;

public interface ScriptConverter {
	
	// Script type strings
	public static final String ITRANS_SCRIPT = "ITRANS";
	public static final String DEVANAGARI_SCRIPT = "Devanagari";
	public static final String KANNADA_SCRIPT = "Kannada";
	public static final String TELUGU_SCRIPT = "Telugu";
	
	public static final String LITERAL_MODE_START_STRING = "{#";
	public static final String LITERAL_MODE_END_STRING = "#}";
	
	public String getSourceScript();
	public String getDestinationScript();
	public String convert(String input);
}
