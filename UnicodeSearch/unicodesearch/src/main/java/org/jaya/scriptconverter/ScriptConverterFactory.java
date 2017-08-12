package org.jaya.scriptconverter;

import java.util.ArrayList;
import java.util.List;

public class ScriptConverterFactory {
	
	static{
		m_scriptConverterList = new ArrayList<ScriptConverter>();
		
		ScriptConverterFactory.registerScriptConverter(new ITRANSToDevaNagariConverter());
		ScriptConverterFactory.registerScriptConverter(new DevaNagariToITRANSConverter());		
		ScriptConverterFactory.registerScriptConverter(new ITRANSToKannadaConverter());
		ScriptConverterFactory.registerScriptConverter(new ITRANSToTeluguConverter());
		ScriptConverterFactory.registerScriptConverter(new DevanagariToKannadaConverter());
		ScriptConverterFactory.registerScriptConverter(new KannadaToDevanagariConverter());
		ScriptConverterFactory.registerScriptConverter(new KannadaToITRANSConverter());
		ScriptConverterFactory.registerScriptConverter(new DevanagariToTeluguConverter());
		ScriptConverterFactory.registerScriptConverter(new TeluguToITRANSConverter());
		ScriptConverterFactory.registerScriptConverter(new TeluguToDevanagariConverter());
	}
	
	private static List<ScriptConverter> m_scriptConverterList;

	public static ScriptConverter getScriptConverter(ScriptType sourceScriptType, ScriptType destScriptType){
		for(ScriptConverter sc : m_scriptConverterList)
		{
			if(sc.getSourceScript() == sourceScriptType && sc.getDestinationScript() == destScriptType)
				return sc;
		}
		System.err.println("Failed to get script converter for sourceType: " + sourceScriptType.name() + " and desType: " + destScriptType.name());
		return null;
	}
	
	public static void registerScriptConverter(ScriptConverter scriptConverter){
		m_scriptConverterList.add(scriptConverter);
	}
}
