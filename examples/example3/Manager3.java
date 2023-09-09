package example3;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.texts.TextBox;
import moulton.scalable.texts.TextHistory;

public class Manager3 extends MenuManager {
	TextHistory hist;
	TextBox entry;

	public Manager3(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		menu = Panel.createRoot(Color.CYAN);
		Panel main = new Panel(menu,"0","0","width","height-50",Color.ORANGE);
		Font font = new Font("Arial",Font.PLAIN,20);
		hist = new TextHistory(main,"0","0","?width-width/10","height",font,false,50)
				.setWordSplitting(false)
				.setTextDemarcation(true);
		hist.setScrollBar(new ScrollBar(true, main, "width-width/10", "0",
				"?width", "height", Color.LIGHT_GRAY));
		hist.addToList("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
		hist.addToList("Aenean sit amet libero interdum sem fringilla ornare quis vel diam.");
		hist.addToList("Sed quis sem sed felis tristique tincidunt.");
		hist.addToList("In semper diam nec quam sollicitudin tempor.");
		hist.addToList("Vivamus molestie ipsum ac turpis tempus, eu hendrerit nulla porttitor.");
		entry = new TextBox("",menu,"0","height-50","?width-width/10","50",font,Color.LIGHT_GRAY);
		entry.setHint("...");
		new Button("Ok",menu,"width-width/10","height-50","?width","50",font,Color.GREEN).setId("ok");
		hist.setWordSplitting(false);
		hist.setTextDemarcation(true);
	}
	
	private void okAction() {
		hist.addToList(entry.getMessage());
		entry.setMessage("");
	}

	@Override
	public void clickableAction(Clickable c) {
		if(c.getId() != null && c.getId().equals("ok") && !entry.getMessage().isEmpty())
			okAction();
	}
	
	@Override
	public void keyTyped(char key) {
		if(key == '\n') //remap enter to send the message
			okAction();
		else
			super.keyTyped(key);
	}

}
