package example5;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.texts.TextBox;

public class Manager5 extends MenuManager{
	TextBox main, bottom;

	public Manager5(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		this.menu = Panel.createRoot(Color.WHITE);
		Font font = new Font("Arial", Font.PLAIN, 30);
		main = new TextBox("top","",menu,"0","0","7width/8","3height/4",font,Color.LIGHT_GRAY);
		main.setOutline(true);
		main.acceptEnter(true);
		bottom = new TextBox("bottom","",menu,"0","centery+height/4","7width/8","height/8",font,Color.LIGHT_GRAY);
		bottom.setOutline(true);
		main.setTextScroller(new ScrollBar(true,menu,"7width/8","0","width/8","3height/4",Color.GRAY));
		bottom.setTextScroller(new ScrollBar(false,menu,"0","7height/8","7width/8","height/8",Color.GRAY));
		new Button("virtuality","!",menu,"7width/8","3height/4","width/8","height/4",font,Color.RED);
	}

	@Override
	protected void clickableAction(Clickable c) {
		if(c.getId().equals("virtuality")) {
			//do stuff here
			Button b = (Button)c;
			if(b.getText().equals("!")) {
				b.setText("Ok");
				//disable virtuality
				main.allowVirtualSpace(false);
				bottom.allowVirtualSpace(false);
			}else {
				b.setText("!");
				main.allowVirtualSpace(true);
				bottom.allowVirtualSpace(true);
			}
		}
	}

	@Override
	protected void lostFocusAction(Clickable c) {}

}
