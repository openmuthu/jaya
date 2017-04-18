package org.jaya.scriptconverter;

public class DevanagariToKannadaConverter implements ScriptConverter {

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.DEVANAGARI;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.KANNADA;
	}

	@Override
	public String convert(String input) {
		ScriptConverter devanagariToITRANSConverter = ScriptConverterFactory.getScriptConverter(ScriptType.DEVANAGARI, ScriptType.ITRANS);
		ScriptConverter itransToKannadaConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.KANNADA);
		String itransString = devanagariToITRANSConverter.convert(input);		
		return itransToKannadaConverter.convert(itransString);
	}

}
