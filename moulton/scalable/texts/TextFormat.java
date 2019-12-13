package moulton.scalable.texts;

/**
 * A utility class to format text. Call {@link #parseText(String)} to format the text provided with the rules
 * defined in {@link #isValidChar(char)} and {@link #emptyText()}.
 * @author Matthew Moulton
 */
public abstract class TextFormat {
	
	/**
	 * Sequentially goes through each character in the string test. Appends any valid characters
	 * onto a returning string then returns the string once all characters have been processed.
	 * @param test the string to retrieve any data from
	 * @return a string comprised of characters in test that were valid characters
	 * @see TextFormat#isValidChar(char)
	 */
	public String parseText(String test){
		char cs[] = test.toCharArray();
		String text = "";
		for(char c: cs){
			if(isValidChar(c))
				text+=c;
		}
		return text;
	}
	
	/**
	 * Used for each letter in the {@link TextBox#message} of the TextBox to determine if that
	 * character is valid for this formatting scheme.
	 * @param c the character to check if valid
	 * @return whether the character c is valid for this formatting
	 */
	public abstract boolean isValidChar(char c);
	
	/**
	 * The text box is checked when it loses focus to see if it contains an empty {@link TextBox#message},
	 * where empty is as dictated by {@link String#isEmpty()}. If so, the string returned by this method
	 * will replace the empty string.
	 * @return the string that should replace the empty string
	 */
	public abstract String emptyText();
	
}
