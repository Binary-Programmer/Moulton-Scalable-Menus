package example2;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.containers.PanelPlus;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.geometrics.PolygonalButton;
import moulton.scalable.geometrics.ShapeResources;
import moulton.scalable.popups.NotificationPopup;
import moulton.scalable.texts.StaticTextBox;
import moulton.scalable.texts.TextBox;
import moulton.scalable.texts.TextHistory;
import moulton.scalable.visuals.View;

public class Manager2 extends MenuManager{
	TextBox box;

	public Manager2(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		this.menu = Panel.createRoot(Color.WHITE);
		PanelPlus grid = new PanelPlus(menu,"width/10","height/10","?width","?height","800","600",Color.CYAN);
		Font font = new Font("Arial",Font.PLAIN,20);
		box = new TextBox("box","",grid,0,0,font,Color.LIGHT_GRAY);
		box.setMessage("Here is some text so that this box won't be all empty and stuff. That would be bad.");
		box.setEnabled(false);
		addTouchResponsiveComponent(box);
		
		Panel here = new Panel(grid,1,0,Color.GREEN);
		addTouchResponsiveComponent(new Button("invisible",null,here, 0, 0, font, Color.GREEN));
		addTouchResponsiveComponent(new Button("button","Click Me",here, 1, 1, font, Color.ORANGE));
		addTouchResponsiveComponent(new PolygonalButton("circle",grid,0,1,ShapeResources.generateCircleXCoords("centerx", "width", 10),
				ShapeResources.generateCircleYCoords("centery", "height", 10), Color.YELLOW));
		ScrollBar horiz = new ScrollBar(false,menu,"width/10","0","?width","height/10",Color.GRAY);
		grid.setWidthScrollBar(horiz);
		addTouchResponsiveComponent(horiz);
		ScrollBar vert = new ScrollBar(true,menu,"0","height/10","width/10","?height",Color.GRAY);
		grid.setHeightScrollBar(vert);
		addTouchResponsiveComponent(vert);
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("fire rose-small.png"));
		} catch (IOException e) {}
		new View(img,grid,2,0);
		new StaticTextBox("display", "This is a test example. You cannot modify this text, but you can select and copy it.",
				grid, 1, 1, font, Color.CYAN);
		
		TextHistory hist = new TextHistory(grid,2,1,font, true, 10);
		hist.addToList("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z 1 2 3 4 5 6 7 8 9 0");
		hist.addToList("0 1 2 3 4 5 6 7 8 9 a b c d e f g h i j k l m n o p q r s t u v w x y z");
	}

	@Override
	protected void clickableAction(Clickable c) {
		if(c.getId().equals("button")) {
			box.setEnabled(!box.isEnabled());
		}else if(c.getId().equals("invisible")) {
			setPopup(new NotificationPopup("You pressed the invisible button!", "ok", null, new Font("Arial",Font.PLAIN,12), false));
		}else if(c.getId().equals("ok")) {
			setPopup(null);
		}
	}

	@Override
	protected void lostFocusAction(Clickable c) {
		
	}

}
