package moulton.scalable.texts;

public class LineBreak {
	public final String LINE;
	public final String RAW_LINE;
	public final String REMAINDER;
	public final boolean CHAR_CONSUMED;
	
	protected LineBreak(String line, String rem) {
		LINE = line;
		RAW_LINE = line;
		REMAINDER = rem;
		CHAR_CONSUMED = false;
	}
	protected LineBreak(String line, String rawLine, String rem) {
		LINE = line;
		RAW_LINE = rawLine;
		REMAINDER = rem;
		CHAR_CONSUMED = true;
	}
	
	public static LineBreak check(boolean wordSplit, String line, String rem) {
		boolean charConsumed = false;
		String rawLine = null;
		
		if(!wordSplit) {
			int ii=line.length()-1;
			for(; ii>-1; ii--) { //backtrack to find a suitable character to break on
				char c = line.charAt(ii);
				if(c == '\n' || c == ' ') { //these characters are just consumed in the break
					charConsumed = true;
					rem = line.substring(ii+1) + rem;
					rawLine = line.substring(0, ii+1);
					line = line.substring(0, ii);
					break;
				}else if(c == '-') {
					if(ii<line.length()-1) { //if the - was not the char too long
						//keep the - on this line
						rem = line.substring(ii+1) + rem;
						line = line.substring(0, ii+1);
					}
					break;
				}
			}
			if(ii == -1) //no break character found, split the word
				wordSplit = true;
		}if(wordSplit) {
			int length = line.length();
			//if that character was a new line, consume it
			if(line.charAt(length-1) == '\n') {
				charConsumed = true;
				rawLine = line;
				line = line.substring(0, length-1);
			}else {
				rem = line.charAt(length-1) + rem;
				line = line.substring(0, length-1);
			}
		}
		
		if(charConsumed)
			return new LineBreak(line, rawLine, rem);
		else
			return new LineBreak(line, rem);
	}

}
