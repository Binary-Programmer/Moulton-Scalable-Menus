package moulton.scalable.texts;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * The Line Break class serves as a utility for text based classes, such as the {@link TextHistory}
 * and the {@link TextBox}. A LineBreak instance can be generated through
 * {@link #check(boolean, String, String)}. The instance will have data about how the text should be
 * broken, such as the remaining line ({@link #LINE}), the remaining text after the break
 * ({@link #REMAINDER}), and whether any characters were consumed ({@link #CHAR_CONSUMED}). These
 * fields are public for easy access, but final to prevent data corruption.
 * @author Matthew Moulton
 */
public class LineBreak {
	/**
	 * The text that is recommended to remain on the line. This field does not include any characters
	 * that were consumed. If those characters are needed for further calculations, use {@link #RAW_LINE}.
	 */
	public final String LINE;
	/**
	 * The text that is recommended to remain on the line in addition to any characters that were
	 * considered to be consumed by the line break. If no characters were consumed, this will be
	 * equal to {@link #LINE}.
	 */
	public final String RAW_LINE;
	/**
	 * Text that was broken from the first line and is recommended to be placed on a successive line.
	 * {@link #check(boolean, String, String)} only computes one break at a time, so this text may need
	 * to be broken further.
	 */
	public final String REMAINDER;
	/**
	 * Whether a character was consumed in the line break. If a character was consumed, {@link #LINE}
	 * and {@link #REMAINDER} will exclude it. 
	 */
	public final boolean CHAR_CONSUMED;
	
	/**
	 * Creates a line break instance as initializes the constant values. Sets {@link #CHAR_CONSUMED}
	 * to false, and {@link #RAW_LINE} to {@link #LINE}.
	 * @param line the text for the line
	 * @param rem the text left over from line
	 */
	protected LineBreak(String line, String rem) {
		LINE = line;
		RAW_LINE = line;
		REMAINDER = rem;
		CHAR_CONSUMED = false;
	}
	/**
	 * Creates a line break instance as initializes the constant values. Sets {@link #CHAR_CONSUMED}
	 * to true.
	 * @param line the text for the line, excluding the consumed character
	 * @param rawLine the text for the line, including the consumed character
	 * @param rem the text left over from line
	 */
	protected LineBreak(String line, String rawLine, String rem) {
		LINE = line;
		RAW_LINE = rawLine;
		REMAINDER = rem;
		CHAR_CONSUMED = true;
	}
	
	/**
	 * Splits the line of text and returns an instance holding the results of the split.
	 * <p>
	 * It is assumed that prior to this method call, the contents of line have been found
	 * to be one character too long. Therefore, a split must occur for the line to fit properly.
	 * If wordSplit is true, then the algorithm does not need to worry about splitting words,
	 * and the last character from line will be placed at the beginning of rem. However, if
	 * wordSplit is false, an appropriate splitting place (such as a space, hyphen, or new line
	 * character) will be searched for. Yet, if no appropriate splitting place is found in the
	 * contents of line, a split will be performed as if wordSplit was true.
	 * <p>
	 * White space characters (such as a space or new line) will be consumed if they are split
	 * on. Consumed characters will not be included in the line result or the remainder, but
	 * can be accessed as {@link #RAW_LINE}.
	 * @param wordSplit whether words may be split midway when not necessary (this is useful if
	 * spaces have no inherent meaning as dividers in the text (for example, in encrypted text).
	 * For most cases, wordSplit = true is recommended.
	 * @param line the offending line that is one character too long. Once line has been trimmed
	 * by this method, the result will be available as {@link #LINE}.
	 * @param rem the remainder text from this line. Text that does not fit in {@link #LINE} will
	 * be placed at the front of this string, called {@link #REMAINDER} in the result.
	 * @return the line break instance, containing the result data of the split.
	 */
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
	
	/**
	 * Splits the given text into lines that are at most the width of <code>maxWidth</code>,
	 * by using {@link #check(boolean, String, String)} and widths determined by
	 * {@link FontMetrics#stringWidth(String)}.
	 * @param text the text to be split. All salient characters will end up in the return.
	 * @param maxWidth the max width that each line may be.
	 * @param metrics the font metrics used to determine the width of each line in processing.
	 * @param wordSplit whether words may be split at ends of lines
	 * @return an array of strings that contains all the salient characters from the original
	 * text, split such that each line is no longer than the given width.
	 */
	public static String[] lines(String text, int maxWidth, FontMetrics metrics, boolean wordSplit) {
		List<String> lines = new ArrayList<>();
		
		String rem = text; //could strip trailing here
		while(metrics.stringWidth(rem) > maxWidth) {
			String line = ""; //add characters to line until it exceeds the width
			while(metrics.stringWidth(line) <= maxWidth) {
				line += rem.charAt(0);
				rem = rem.substring(1);
			}
			LineBreak lb = check(wordSplit, line, rem);
			lines.add(lb.LINE);
			rem = lb.REMAINDER;
		}
		lines.add(rem);
		
		return lines.toArray(new String[lines.size()]);
	}

}
