package example8;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.containers.VirtualPanel;
import moulton.scalable.draggables.ScrollBar;

public class Manager8 extends MenuManager {

	public Manager8(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		menu = Panel.createRoot(Color.WHITE);
		VirtualPanel vp = new VirtualPanel(menu, "width/20", "height/20", "?width", "?height", "800", "800", Color.RED);
		ScrollBar horizBar = new ScrollBar(false, menu, "width/20", "0", "?width", "?height/20", Color.LIGHT_GRAY);
		ScrollBar vertBar = new ScrollBar(true, menu, "0", "height/20", "?width/20", "?height", Color.LIGHT_GRAY);
		vp.setWidthScrollBar(horizBar);
		vp.setHeightScrollBar(vertBar);
		Font font = new Font("Arial", Font.PLAIN, 12);
		Button b = new Button("", "HELLO", vp, "width/2", "height/2", "?width", "?height", font, Color.ORANGE);
		addTouchComponent(b);
		VirtualPanel vp2 = new VirtualPanel(vp, "0", "0", "width/2", "height/2", "500", "500", Color.CYAN);
		horizBar = new ScrollBar(false, vp, "0", "height/2", "width/2", "?height/2+height/30", Color.LIGHT_GRAY);
		vertBar = new ScrollBar(true, vp, "width/2", "0", "?width/2+width/30", "height/2", Color.LIGHT_GRAY);
		vp2.setWidthScrollBar(horizBar);
		vp2.setHeightScrollBar(vertBar);
		addTouchComponent(new Button("", "A", vp2, 0, 0, font, Color.CYAN));
		TouchPanel B = new TouchPanel(vp2, 1, 0, Color.BLUE);
		B.setTouchAction(() -> { B.setOutline(B.isTouched());  return false;});
		addTouchComponent(B);
		TouchPanel C = new TouchPanel(vp2, 0, 1, Color.MAGENTA);
		C.setTouchAction(() -> { C.setOutline(C.isTouched());  return false;});
		addTouchComponent(C);
		addTouchComponent(new Button("", "D", vp2, 1, 1, font, Color.PINK));
		/*
		addTouchComponent(new Button("", "B", vp2, 1, 0, font, Color.BLUE));
		addTouchComponent(new Button("", "C", vp2, 0, 1, font, Color.MAGENTA));
		addTouchComponent(new Button("", "D", vp2, 1, 1, font, Color.PINK));
		*/
		
		//add a centered button in b
		addTouchComponent(new Button("centered", "Centered!", B, "CENTERX", "CENTERY", "width/3", "height/3", font, Color.YELLOW));
	}

	@Override
	public void clickableAction(Clickable c) {}

	@Override
	public void lostFocusAction(Clickable c) {}

}
