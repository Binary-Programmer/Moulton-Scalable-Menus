package moulton.scalable.clickables;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.containers.Panel;
import moulton.scalable.texts.TextInputComponent;

public class FormButton extends Button implements TextInputComponent {
	
	public FormButton(String id, String text, Panel parent, int x, int y, Font font, Color color) {
		super(id, text, parent, x, y, font, color);
	}
	
	public FormButton(String id, String text, Panel parent, String x, String y, String width, String height,
			Font font, Color color) {
		super(id, text, parent, x, y, width, height, font, color);
	}
	
	@Override
	public boolean isDeselectedOnRelease() {
		return false;
	}

	@Override
	public void appendMessage(String string) {
		//we want to be able to handle the enter character
		if(string.equals("\n")) {
			this.clicked = true;
		}
	}

	@Override
	public void removeMessage(int chars) {}

}
