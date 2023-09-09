package example2;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.FormButton;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.containers.VirtualPanel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.geometrics.PolygonalButton;
import moulton.scalable.geometrics.ShapeResources;
import moulton.scalable.popups.NotificationPopUp;
import moulton.scalable.texts.StaticTextBox;
import moulton.scalable.texts.TextBox;
import moulton.scalable.visuals.View;

public class Manager2 extends MenuManager{
	TextBox box;
	
	TextBox nameField, ageField, interestsField;

	public Manager2(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		this.menu = Panel.createRoot(Color.WHITE);
		VirtualPanel grid = new VirtualPanel(menu,"width/10","height/10","?width","?height",
				"800","600",Color.CYAN);
		Font font = new Font("Arial",Font.PLAIN,20);
		box = new TextBox("",grid,0,0,font,Color.LIGHT_GRAY);
		box.setMessage("Here is an unenabled text box. Click the button to enable it.");
		box.setEnabled(false);
		addTouchComponent(box);
		
		Panel here = new Panel(grid,1,0,Color.GREEN);
		addTouchComponent(new Button(null,here, 1, 0, font, Color.GREEN).setId("invisible"));
		addTouchComponent(new Button("Click Me",here, 0, 1, font, Color.ORANGE).setId("button"));
		addTouchComponent(new PolygonalButton(grid,0,1,
				ShapeResources.generateCircleXCoords("centerx", "width", 10),
				ShapeResources.generateCircleYCoords("centery", "height", 10), Color.YELLOW)
				.setId("circle"));
		ScrollBar horiz = new ScrollBar(false,menu,"width/10","0","?width","height/10",Color.GRAY);
		grid.setWidthScrollBar(horiz);
		addTouchComponent(horiz);
		ScrollBar vert = new ScrollBar(true,menu,"0","height/10","width/10","?height",Color.GRAY);
		grid.setHeightScrollBar(vert);
		addTouchComponent(vert);
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("examples/fire rose-small.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		new View(img,grid,2,0);
		new StaticTextBox("This is a test example. You cannot modify this text, but you"
				+ "can select and copy it.", grid, 1, 1, font, Color.CYAN);
		
		//Here we can show off text chaining
		Panel formPanel = new Panel(grid, 2, 1, Color.WHITE);
		nameField = new TextBox("", formPanel, 0, 0, font, Color.RED)
				.setHint("Enter your name... Hit tab to continue");
		ageField = new TextBox("", formPanel, 0, 1, font, Color.ORANGE)
				.setHint("Enter your age... Hit tab to continue");
		interestsField = new TextBox("", formPanel, 0, 2, font, Color.YELLOW)
				.setHint("Enter your interests... Hit tab then enter");
		FormButton send = new FormButton("Send", this, formPanel, 0, 3,
				font, Color.GREEN);
		send.setId("accept");
		nameField.setFormChain(ageField);
		ageField.setFormChain(interestsField);
		interestsField.setFormChain(send);
		
		// You can set the end of a form chain by the following line:
		//send.setFormChain(MenuManager.FORM_END);
		// If the formChain field in a clickable is null, then focus will not be lost
	}

	@Override
	public void clickableAction(Clickable c) {
		String id = c.getId();
		if (id == null)
			return;
		switch (id) {
		case "button":
			box.setEnabled(!box.isEnabled());
			break;
		case "invisible":
			setPopUp(new NotificationPopUp("You pressed the invisible button!", null,
					new Font("Arial",Font.PLAIN,12), "ok", this));
			break;
		case "ok":			
			setPopUp(null);
			break;
		case "accept":
			//the form was accepted
			String name = nameField.getMessage();
			String age = ageField.getMessage();
			String interests = interestsField.getMessage();
			if(name.isEmpty() || age.isEmpty() || interests.isEmpty())
				return;
			nameField.clearMessage();
			ageField.clearMessage();
			interestsField.clearMessage();
			System.out.println("Hello, "+name+"! You seem like a nice person with some "
					+ "cool interests.");
			break;
		}
	}

}
