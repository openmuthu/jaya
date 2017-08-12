package org.jaya.scriptconverter;

public class TeluguToDevanagariConverter implements ScriptConverter {

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.TELUGU;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.DEVANAGARI;
	}

	@Override
	public String convert(String input) {
		ScriptConverter teluguToITRANSConverter = ScriptConverterFactory.getScriptConverter(ScriptType.TELUGU, ScriptType.ITRANS);
		ScriptConverter itransToDevanagariConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);
		String itransString = teluguToITRANSConverter.convert(input);		
		return itransToDevanagariConverter.convert(itransString);
	}

}
