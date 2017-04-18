package org.jaya.scriptconverter;

public class KannadaToDevanagariConverter implements ScriptConverter {

	@Override
	public ScriptType getSourceScript() {
		return ScriptType.KANNADA;
	}

	@Override
	public ScriptType getDestinationScript() {
		return ScriptType.DEVANAGARI;
	}

	@Override
	public String convert(String input) {
		ScriptConverter kannadaToITRANSConverter = ScriptConverterFactory.getScriptConverter(ScriptType.KANNADA, ScriptType.ITRANS);
		ScriptConverter itransToDevanagariConverter = ScriptConverterFactory.getScriptConverter(ScriptType.ITRANS, ScriptType.DEVANAGARI);
		String itransString = kannadaToITRANSConverter.convert(input);		
		return itransToDevanagariConverter.convert(itransString);
	}

}
