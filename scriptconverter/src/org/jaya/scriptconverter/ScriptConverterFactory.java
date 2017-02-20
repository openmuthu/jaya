package org.jaya.scriptconverter;

import java.util.ArrayList;
import java.util.List;

public class ScriptConverterFactory {
	
	static{
		m_scriptConverterList = new ArrayList<ScriptConverter>();
		
		ScriptConverterFactory.registerScriptConverter(new ITRANSToDevaNagariConverter());		
	}
	
	private static List<ScriptConverter> m_scriptConverterList;

	public static ScriptConverter getScriptConverter(String sourceScriptType, String destScriptType){
		for(ScriptConverter sc : m_scriptConverterList)
		{
			if(sc.getSourceScript() == sourceScriptType && sc.getDestinationScript() == destScriptType)
				return sc;
		}
		return null;
	}
	
	public static void registerScriptConverter(ScriptConverter scriptConverter){
		m_scriptConverterList.add(scriptConverter);
	}
}
