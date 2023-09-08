package moulton.scalable.texts;

/**
 * A utility superclass for ensuring text follows a specified text format.
 * <p>
 * When clients append to the pre-existing text, each character in the append should be considered
 * valid in a call to {@link #isValidChar(char)} to be included, and each should be appended (if
 * valid) before the next is considered (this is a slower method of processing, but is necessary
 * if character validity depends on the state of the text.
 * <p>
 * When clients replace/set the text, the text should be set to empty string, then each character
 * in the replacement should be appended in the method described above.
 * <p>
 * When any part of the text is deleted, {@link #deleteAction(String)} should be called with the
 * new intended text value. The return is what should be used as the new text value.
 * <p>
 * If after a text replacement no characters were added, or if a deletion removed all characters,
 * {@link #emptyText()} is called to fetch the intended default value.
 * @author Matthew Moulton
 */
public abstract class TextFormat {
	
	/**
	 * Used for each letter in the {@link TextBox#message} of the TextBox to determine if that
	 * character is valid for this formatting scheme.
	 * @param c the character to check if valid
	 * @return whether the character c is valid for this formatting
	 */
	public abstract boolean isValidChar(char c);
	
	/**
	 * Should be called when any amount of the text is deleted to guarantee that the format is
	 * maintained.
	 * <p>
	 * Default behavior returns the call of {@link #emptyText()} if newText.isEmpty(). If this
	 * method is overridden, it is the <i>responsibility of the implementor</i> to guarantee
	 * {@link #emptyText()} is called when necessary!
	 * @param newText the intended new text value
	 * @return the text value which should be used (may or may not be the same as newText)
	 */
	public String deleteAction(String newText) {
		if (newText.isEmpty())
			return emptyText();
		return newText;
	}
	
	/**
	 * The default string which should replace "" if it is ever the intended text value.
	 * @return the string which should replace the empty string. By default, the return is "",
	 * which indicates that no substantive replacement is necessary.
	 */
	public String emptyText() {
		return "";
	}
	
}
