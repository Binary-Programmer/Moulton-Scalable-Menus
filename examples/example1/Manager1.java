package example1;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.popups.ConfirmationPopUp;
import moulton.scalable.popups.NotificationPopUp;
import moulton.scalable.popups.PopUp;
import moulton.scalable.utils.GridFormatter;

public class Manager1 extends MenuManager {

	public Manager1(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		menu = Panel.createRoot(Color.BLACK);
		GridFormatter format = menu.getGridFormatter();
		format.setFrame("width/50", "height/50");
		format.setMargin("width/100", "height/100");
		format.specifyColumnWeight(0, 1.5);
		format.specifyRowWeight(2, 2.0/3);
		
		new Button("red",null,menu,0,0,null,new Color(255,0,0));
		new Button("orange",null,menu,1,0,null,new Color(255,128,0));
		new Button("yellow",null,menu,2,0,null,new Color(255,255,0));
		new Button("green",null,menu,3,0,null,new Color(128,255,0));
		
		new Button(null,null,menu,0,1,null,new Color(255,64,64));
		new Button(null,null,menu,1,1,null,new Color(255,160,64));
		new Button(null,null,menu,2,1,null,new Color(255,255,64));
		new Button(null,null,menu,3,1,null,new Color(160,255,64));
		
		new Button(null,null,menu,0,2,null,new Color(255,128,128));
		new Button("secret",null,menu,1,2,null,new Color(255,192,128));
		new Button(null,null,menu,2,2,null,new Color(255,255,128));
		new Button(null,null,menu,3,2,null,new Color(192,255,128));
		
		new Button(null,null,menu,0,3,null,new Color(255,192,192));
		new Button(null,null,menu,1,3,null,new Color(255,224,192));
		new Button(null,null,menu,2,3,null,new Color(255,255,192));
		new Button("exit",null,menu,3,3,null,new Color(224,255,192));
	}

	@Override
	public void clickableAction(Clickable c) {
		String id = c.getId();
		if(id != null) {
			switch(id) {
			case "secret":
				PopUp pop = new NotificationPopUp("Here is some really long text that must be"
						+ "split! I don't know how many lines it will take, but I am guessing that"
						+ "it will be a few.", "Secret", new Font("Arial", Font.PLAIN, 12), "ok",
						this);
				this.setPopUp(pop);
				break;
			case "red":
			case "orange":
			case "yellow":
			case "green":
				pop = new NotificationPopUp("You pressed a(n) "+id+" button!", null,
						new Font("Arial", Font.PLAIN, 15), "ok", this);
				this.setPopUp(pop);
				break;
			case "ok":
				setPopUp(null);
				break;
			case "exit":
				pop = new ConfirmationPopUp("Are you sure you want to quit?", null,
						new Font("Times New Roman", Font.PLAIN, 13), "quit", "ok", this, true);
				setPopUp(pop);
				break;
			case "quit":
				System.exit(0);
				break;
			}
		}
	}

	@Override
	public void lostFocusAction(Clickable c) {}

}