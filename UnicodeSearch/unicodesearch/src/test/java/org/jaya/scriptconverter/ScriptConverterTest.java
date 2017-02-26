package org.jaya.scriptconverter;

public class ScriptConverterTest {
	public static void main(String[] args) {
		ScriptConverter dev2it = ScriptConverterFactory.getScriptConverter(ScriptConverter.DEVANAGARI_SCRIPT,
				ScriptConverter.ITRANS_SCRIPT);
		ScriptConverter it2dev = ScriptConverterFactory.getScriptConverter(ScriptConverter.ITRANS_SCRIPT,
				ScriptConverter.DEVANAGARI_SCRIPT);

		String[] inputs = new String[] { "bRhaddhaatukOSa", "nArAyaNaM guNaiH sarvairudIrNaM dOShavarjitam",
				"jnEyaM gamyaM gurUMshcaapi natvaa sUtraartha uchyatE", "rAyaNaM" };
		String input;
		for (int i = 0; i < inputs.length; i++) {
			input = inputs[i];
			String dev = it2dev.convert(input);
			String it = dev2it.convert(dev);
			String dev2 = it2dev.convert(it);
			System.out.println("Iteration START");
			System.out.println("input:" + input);
			System.out.println("it   :" + it);
			System.out.println("dev  :" + dev);
			System.out.println("dev2 :" + dev2);
			System.out.println("Iteration END");
		}
	}
}
