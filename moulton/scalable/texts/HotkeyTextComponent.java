package moulton.scalable.texts;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.utils.MenuComponent;

/**
 * Subclasses of HotkeyTextComponent are instances of {@link MenuComponent} that can react to the hotkey commands
 * of "copy", "cut", and "paste". The {@link MenuManager} associated with this component, is in charge of
 * detecting when these hotkey commands are performed by the user, and to call correlating events. Moreover, the
 * manager will copy the text returned by {@link #copy()} and {@link #cut()} to the system clipboard, and will
 * give the contents of the system clipboard (when it can be represented as a string) in the calling of
 * {@link #paste(String)}.
 * @author Matthew Moulton
 */
public interface HotkeyTextComponent extends TextInputComponent{
	
	/**Called by the {@link MenuManager} when it registers a copy request from the user.
	 * @return the text from this component that should be copied to the clipboard
	 */
	public abstract String copy();
	
	/**Called by the {@link MenuManager} when it registers a cut request from the user.
	 * @return the text from this component that should be copied to the clipboard
	 */
	public abstract String cut();
	
	/**Called by the {@link MenuManager} when it registers a paste command from the user.
	 * @param pasteText the text that was pasted into this component from the clipboard
	 */
	public abstract void paste(String pasteText);
}
