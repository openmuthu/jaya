package org.jaya.scriptconverter;

public class DevanagariToTeluguConverter implements ScriptConverter {

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.DEVANAGARI;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.TELUGU;
	}

	@Override
	public String convert(String input) {
		ScriptConverter devanagariToITRANSConverter = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
		ScriptConverter itransToKannadaConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.TELUGU);
		String itransString = devanagariToITRANSConverter.convert(input);		
		return itransToKannadaConverter.convert(itransString);
	}

}
