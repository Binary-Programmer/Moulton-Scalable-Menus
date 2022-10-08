package example6;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.containers.PartitionPanel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.texts.TextBox;
import moulton.scalable.visuals.AnimatedButton;
import moulton.scalable.visuals.Animation;
import moulton.scalable.visuals.AnimationListener;

public class Manager6 extends MenuManager implements AnimationListener {
	private Button [] scrollButtons = new Button[2];
	private ScrollBar bar;
	private PartitionPanel part;
	private Animation flower;
	private AnimatedButton flowerButton;
	private TextBox box;

	public Manager6(Container cont) {
		super(cont);
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		if(scrollButtons[0].isEnabled() || scrollButtons[1].isEnabled())
			part.setVerticalPartition("width/1.1");
		else
			part.setVerticalPartition("width");
		super.render(g);
	}

	@Override
	public void createMenu() {
		this.menu = Panel.createRoot(Color.WHITE);
		menu.setTextResize(true);
		part = new PartitionPanel(menu,"0","0","width","height/1.2",Color.GREEN);
		Font font = new Font("Arial", Font.PLAIN, 40);
		box = new TextBox("box","",part,0,0,font,Color.LIGHT_GRAY);
		box.setHint("Type here, or consider pressing the buttons.");
		part.setLeft(box);
		Panel rightPanel = new Panel(part,0,0,Color.RED);
		part.setRight(rightPanel);
		bar = new ScrollBar(true, rightPanel, "0", "height/8","width","3height/4",
				Color.GRAY);
		box.setTextScroller(bar);
		scrollButtons[0] = new Button("up","^",rightPanel,"0","0","width","height/8",
				font,Color.GRAY);
		scrollButtons[1] = new Button("down","v",rightPanel,"0","height-height/8",
				"width","?height",font,Color.GRAY);
		bar.setScrollButtons(scrollButtons[0],scrollButtons[1]);
		
		Panel bottom = new Panel(menu, "0", "height/1.2","width","?height", Color.MAGENTA);
		BufferedImage[] flowers = new BufferedImage[9];
		for(int i=0; i<flowers.length; i++) {
			try {
				flowers[i] = ImageIO.read(new File("examples/flower_anim/flower"+i+".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		flower = new Animation(300,flowers);
		flower.setAnimationListener(this);
		flowerButton = new AnimatedButton("flower", flower, bottom, 0, 0, Color.WHITE);
		addTouchComponent(flowerButton);
		addTouchComponent(new Button("reappear","Redo",bottom, 1,0, font, Color.RED));
		addTouchComponent(new Button("clear","CE",bottom, 2,0, font, Color.GREEN));
		addTouchComponent(new Button("add","+",bottom, 3,0, font, Color.BLUE));
	}

	@Override
	public void clickableAction(Clickable c) {
		if(c.getId().equals("flower")) {
			flower.setAnimationPlay(true);
		}else if(c.getId().equals("reappear")) {
			flowerButton.setVisible(true);
			flowerButton.setEnabled(true);
			flower.startAnimation();
			flower.setAnimationPlay(false);
		}else if(c.getId().equals("clear")) {
			box.setMessage("");
		}else if(c.getId().equals("add")) {
			//add some sample text to show off the partition
			box.setMessage("Alphabet: Aa Bb Cc Dd Ee Ff Gg Hh Ii Jj Kk Ll Mm Nn Oo Pp Qq Rr Ss Tt"
					+ "Uu Vv Ww Xx Yy Zz.\nRoman: I II III IV V VI VII VIII IX X");
		}
		//to get the connected scroll bar buttons to work properly
		else if(c.getId().equals("up")) {
			bar.setOffset(bar.getOffset()-1);
		}else if(c.getId().equals("down")) {
			bar.setOffset(bar.getOffset()+1);
		}
	}

	@Override
	public void lostFocusAction(Clickable c) {}

	@Override
	public void animationEndEvent(Animation animation) {
		flowerButton.setVisible(!flowerButton.isVisible());
	}

	@Override
	public void animationLoopEvent(Animation animation) {}
}