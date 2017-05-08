var fs = require("fs");

function gretil_translit(gretilInput)
{
	newInput = gretilInput;
	prevInput = '';

	//var whiteSpacePat = /[\f\n\r\t\v]+/g;
	var whiteSpacePat = /[\r][\n]+/g;
	//var prevInputWords = prevInput.split(whiteSpacePat);
	var newInputWords = newInput.split(whiteSpacePat);
	//var prevOutputWords = prevOutput.split(whiteSpacePat);
	var outStr = '';

	for( var i = 0; i < newInputWords.length; i++)
	{
		// if( prevInputWords[i] != newInputWords[i] )
		// {
			outStr = outStr + translit_Devanagari_String(newInputWords[i]);
		// }
		// else
		// {
		// 	outStr = outStr + prevOutputWords[i];
		// }
	}
	return outStr;

	// prevInput = newInput;
	// translitOutput.value = outStr;
	// prevOutput = translitOutput.value;
}

function onLoad()
{
	// translitInput = document.getElementById("translitInput");
	// translitOutput = document.getElementById("translitOutput");
	// translitOutput.value = "";
	// prevInput = translitInput.value;
	// prevOutput = translitOutput.value;
	//terminalChars = ['a', 'A', 'aa', 'i', 'I', 'u', 'U', 'R', 'RR', 'lR', 'lRR', 'e', 'ai', 'o', 'au', 'M', 'H', '|', '||', '^', '_', '$', '@'];
	terminalChars = ['a', 'ā', 'Ā', 'A', 'aa', 'i', 'ī', 'Ī', 'I', 'u', 'ū', 'Ū', 'U', 'R', 'ṛ', 'Ṛ', 'RR', 'lR', 'ḷ', 'lRR', 'ḹ', 'e', 'ai', 'o', 'au', 'M', 'ṃ', 'H', 'ḥ', '|', '||', '^', '_', '$', '@', '\''];
	//twoCharSymbols = ['aa', 'ai', 'au', 'RR', 'lR', 'kh', 'gh', 'ch', 'jh', 'Th', 'Dh', 'th', 'dh', 'ph', 'bh', 'sh', '|', '||'];
	twoCharSymbols = ['ai', 'au', 'kh', 'gh', 'ch', 'jh', 'Th', 'ṭh', 'Dh', 'ḍh', 'th', 'dh', 'ph', 'bh', 'sh', '|', '||'];

	translitMap = [];
	translitMap['a'] = String.fromCharCode(0x0905);
	translitMap['ā'] = String.fromCharCode(0x0906, 0x093E);
	translitMap['Ā'] = String.fromCharCode(0x0906, 0x093E);
	translitMap['A'] = String.fromCharCode(0x0906, 0x093E);
	translitMap['aa'] = String.fromCharCode(0x0906, 0x093E);
	translitMap['i'] = String.fromCharCode(0x0907, 0x093F);
	translitMap['ī'] = String.fromCharCode(0x0908, 0x0940);
	translitMap['Ī'] = String.fromCharCode(0x0908, 0x0940);
	translitMap['I'] = String.fromCharCode(0x0908, 0x0940);
	translitMap['u'] = String.fromCharCode(0x0909, 0x0941);
	translitMap['ū'] = String.fromCharCode(0x090A, 0x0942);
	translitMap['Ū'] = String.fromCharCode(0x090A, 0x0942);
	translitMap['U'] = String.fromCharCode(0x090A, 0x0942);
	translitMap['R'] = String.fromCharCode(0x090B, 0x0943);
	translitMap['ṛ'] = String.fromCharCode(0x090B, 0x0943);
	translitMap['Ṛ'] = String.fromCharCode(0x090B, 0x0943);
	translitMap['RR'] = String.fromCharCode(0x0960, 0x0944);
	translitMap['lR'] = String.fromCharCode(0x090C, 0x0962);
	translitMap['ḷ'] = String.fromCharCode(0x090C, 0x0962);
	translitMap['lRR'] = String.fromCharCode(0x0961, 0x0963);
	translitMap['ḹ'] = String.fromCharCode(0x0961, 0x0963);
	translitMap['e'] = String.fromCharCode(0x090F, 0x0947);
	translitMap['ai'] = String.fromCharCode(0x0910, 0x0948);
	translitMap['o'] = String.fromCharCode(0x0913, 0x094B);
	translitMap['au'] = String.fromCharCode(0x0914, 0x094C);
	translitMap['M'] = String.fromCharCode(0x0902, 0x0902);
	translitMap['ṃ'] = String.fromCharCode(0x0902, 0x0902);
	translitMap['H'] = String.fromCharCode(0x0903, 0x0903);
	translitMap['ḥ'] = String.fromCharCode(0x0903, 0x0903);
	translitMap['|'] = String.fromCharCode(0x0964, 0x0964);
	translitMap['||'] = String.fromCharCode(0x0965, 0x0965);
	translitMap['^'] = String.fromCharCode(0x0951, 0x0951);
	translitMap['_'] = String.fromCharCode(0x0952, 0x0952);
	translitMap['$'] = String.fromCharCode(0x093D, 0x093D);
	translitMap['\''] = String.fromCharCode(0x093D, 0x093D);
	translitMap['@'] = String.fromCharCode(0x0950, 0x0950);
	//translitMap['\n'] = '\n';
	//translitMap['\r'] = '\r';

	translitMap['k'] = String.fromCharCode(0x0915);
	translitMap['kh'] = String.fromCharCode(0x0916);
	translitMap['g'] = String.fromCharCode(0x0917);
	translitMap['gh'] = String.fromCharCode(0x0918);
	translitMap['G'] = String.fromCharCode(0x0919);
	translitMap['ṅ'] = String.fromCharCode(0x0919);
	translitMap['c'] = String.fromCharCode(0x091A);
	translitMap['ch'] = String.fromCharCode(0x091B);
	translitMap['j'] = String.fromCharCode(0x091C);
	translitMap['jh'] = String.fromCharCode(0x091D);
	translitMap['J'] = String.fromCharCode(0x091E);
	translitMap['ñ'] = String.fromCharCode(0x091E);
	translitMap['T'] = String.fromCharCode(0x091F);
	translitMap['ṭ'] = String.fromCharCode(0x091F);
	translitMap['Th'] = String.fromCharCode(0x0920);
	translitMap['ṭh'] = String.fromCharCode(0x0920);
	translitMap['D'] = String.fromCharCode(0x0921);
	translitMap['ḍ'] = String.fromCharCode(0x0921);
	translitMap['Dh'] = String.fromCharCode(0x0922);
	translitMap['ḍh'] = String.fromCharCode(0x0922);
	translitMap['N'] = String.fromCharCode(0x0923);
	translitMap['ṇ'] = String.fromCharCode(0x0923);
	translitMap['t'] = String.fromCharCode(0x0924);
	translitMap['th'] = String.fromCharCode(0x0925);
	translitMap['d'] = String.fromCharCode(0x0926);
	translitMap['dh'] = String.fromCharCode(0x0927);
	translitMap['n'] = String.fromCharCode(0x0928);
	translitMap['p'] = String.fromCharCode(0x092A);
	translitMap['ph'] = String.fromCharCode(0x092B);
	translitMap['b'] = String.fromCharCode(0x092C);
	translitMap['bh'] = String.fromCharCode(0x092D);
	translitMap['m'] = String.fromCharCode(0x092E);
	translitMap['y'] = String.fromCharCode(0x092F);
	translitMap['r'] = String.fromCharCode(0x0930);
	translitMap['l'] = String.fromCharCode(0x0932);
	translitMap['v'] = String.fromCharCode(0x0935);
	translitMap['ś'] = String.fromCharCode(0x0936);
	translitMap['sh'] = String.fromCharCode(0x0936);
	translitMap['S'] = String.fromCharCode(0x0937);
	translitMap['ṣ'] = String.fromCharCode(0x0937);
	translitMap['s'] = String.fromCharCode(0x0938);
	translitMap['h'] = String.fromCharCode(0x0939);
	translitMap['L'] = String.fromCharCode(0x0933);
	//alert("Form loaded");
}

function isTerminal(inputChar)
{
	for( var ch in terminalChars)
	{
		if( inputChar == terminalChars[ch] )
			return true;
	}
	return false;
}

function isNonTerminal(inputChar)
{
	return !isTerminal(inputChar);
}

function translit_Devanagari_String(engStr)
{
	var outStr = "";
	var words = engStr.split(' ');
	for( var w in words )
	{
		outStr = outStr + translit_Devanagari_Word(words[w]) + ' ';
	}
	outStr = outStr.slice(0, outStr.length - 1);
	return outStr;
}

function translit_Devanagari_Word(inputWord)
{
	var outStr = "";
	var letterList = [];
	var charIndex = 0;
	var letter = '';
	var ch = inputWord.charAt(charIndex);
	var vowelPassed = false;
	while (ch != '' )
	{
		//document.write(ch);
		if( isNonTerminal(ch) )
		{			
			if( vowelPassed )
			{
				letterList.push(letter);
				letter = '';
				vowelPassed = false;
			}
		}
		else
			vowelPassed = true;
		letter += ch;
		charIndex++;
		ch = inputWord.charAt(charIndex);
	}
	letterList.push(letter);

	for( var l in letterList )
	{
		//alert(letterList[l]);
		outStr += translit_Devanagari_Letter(letterList[l]);
	}
	return outStr;
}

function translit_Devanagari_Letter(inputLetter)
{
	var outStr = '';
	var index = 0;
	var ch = inputLetter.charAt(0);
	var firstNonTermIndex = -1; //isTerminal(inputLetter.charAt(0))?false:true;
	for( var i = 0 ; ch != ''; )
	{		
		var twoCharLookAhead = inputLetter.substr(i, 2);
		unknownChar = false;

		for( var symIndex in twoCharSymbols )
		{
			if( twoCharSymbols[symIndex] == twoCharLookAhead )
			{
				++i;
				ch = twoCharLookAhead;
				break;
			}
		}
		var threeCharLookAhead = inputLetter.substr(i, 3);
		if( threeCharLookAhead == 'lRR' )
		{
			i += 2;
			ch = 'lRR';
		}

		var translitChar = translitMap[ch];
		
		if( translitChar == undefined )
		{
			outStr += ch;
			index += ch.length;
			//return outStr;
		}
		else
		{
			var isTerminalChar = isTerminal(ch);
			firstNonTermIndex = (isTerminalChar) ? firstNonTermIndex : i;


			if( isTerminalChar ) 
			{
				if( firstNonTermIndex >= 0 )
				{
					outStr = outStr.slice(0, index-1);
					if( ch != 'a' )
					{
						outStr += translitChar.charAt(1);
					}
				}
				else
					outStr += translitChar.charAt(0);
				index++;
			}
			else
			{
				outStr = outStr + translitChar + String.fromCharCode(0x094D);
				index += 2;
			}
		}
		ch = inputLetter.charAt(++i);
	}
	return outStr;
}

if( process.argv.length < 3 ){
	console.error("Usage Error: Expected gretil input file.");
	process.exit(1);
}

onLoad();
let outFile = process.argv[2]+"_converted";
let converted = "";
try{
	fs.unlinkSync(outFile);
}catch(e){}
let outStream = fs.createWriteStream(outFile, {defaultEncoding: 'ucs2',});
outStream.write("\uFEFF");
var lineReader = require('readline').createInterface({
  input: fs.createReadStream(process.argv[2])
});

lineReader.on('line', function (line) {
  //console.log(line);
//   converted += line;
//   converted += "\n";
	converted = gretil_translit(line);
	outStream.write(converted+"\n");
});

// fs.appendFile(outFile, converted, function (err) {
// if (err) throw err;
// });