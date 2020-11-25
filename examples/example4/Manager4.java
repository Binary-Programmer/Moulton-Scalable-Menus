package example4;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.RadioGroup;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.texts.TextBox;
import moulton.scalable.visuals.AnimatedButton;
import moulton.scalable.visuals.Animation;
import moulton.scalable.visuals.ImageButton;

public class Manager4 extends MenuManager{
	TextBox box;

	public Manager4(Container cont) {
		super(cont);
	}

	@Override
	public void createMenu() {
		this.menu = Panel.createRoot(Color.WHITE);
		menu.setTextResize(true);
		Font font = new Font("Arial", Font.PLAIN, 12);
		this.addTouchResponsiveComponent(new Button("toggle","ON",menu,"0","0","width/3","height/3",font,Color.CYAN));
		Button xButton = new Button("X", "X", menu, "width-width/3", "0", "?width", "height/3", font, Color.WHITE);
		xButton.setTouchedColor(Color.RED);
		this.addTouchResponsiveComponent(xButton);
		
		Button[] radios = new Button[4];
		radios[0] = new Button("up","^",menu,"centerx-width/6","0","?2width/3","height/3",font,Color.YELLOW);
		radios[1] = new Button("down","v",menu,"centerx-width/6","centery+height/6","width/3","?height",font,Color.YELLOW);
		radios[2] = new Button("left","<",menu,"0","centery-height/6","width/3","height/3",font,Color.YELLOW);
		radios[3] = new Button("right",">",menu,"width-width/3","centery-height/6","?width","height/3",font,Color.YELLOW);
		new RadioGroup(radios);
		
		BufferedImage folder1 = null, folder2 = null;
		try {
		    folder1 = ImageIO.read(new File("folder1.png"));
		    folder2 = ImageIO.read(new File("folder2.png"));
		}catch(IOException e) {}
		ImageButton open = new ImageButton("open",folder1,menu,"0","height-height/3","width/3","?height",Color.CYAN);
		open.setTouchedImage(folder2);
		this.addTouchResponsiveComponent(open);
		
		BufferedImage spin[] = new BufferedImage[16];
		try {
			BufferedImage deg0 = ImageIO.read(new File("spinning_anim/spin_top.png"));
			BufferedImage deg22 = ImageIO.read(new File("spinning_anim/spin_topperright.png"));
			BufferedImage deg45 = ImageIO.read(new File("spinning_anim/spin_topright.png"));
			BufferedImage deg67 = ImageIO.read(new File("spinning_anim/spin_toprighter.png"));
			
			spin[0] = new BufferedImage(deg0.getWidth(), deg0.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g0 = spin[0].createGraphics();
			g0.drawImage(deg0, 0, 0, null);
			g0.dispose();
			
			spin[1] = new BufferedImage(deg22.getWidth(), deg22.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g1 = spin[1].createGraphics();
			g1.drawImage(deg22, 0, 0, null);
			g1.dispose();
			
			spin[2] = new BufferedImage(deg45.getWidth(), deg45.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g2 = spin[2].createGraphics();
			g2.drawImage(deg45, 0, 0, null);
			g2.dispose();
			
			spin[3] = new BufferedImage(deg67.getWidth(), deg67.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g3 = spin[3].createGraphics();
			g3.drawImage(deg67, 0, 0, null);
			g3.dispose();
			
			for(int i=4; i<16; i++) {
				spin[i] = rotateImage(spin[i%4], i/4);
			}
		}catch(IOException e) {}
		Animation spinning = new Animation(150, spin);
		spinning.setLoop(-1);
		spinning.startAnimation();
		new AnimatedButton("spin", spinning, menu, "centerx-width/6","centery-height/6","width/3","height/3",Color.WHITE);
	}

	@Override
	protected void clickableAction(Clickable c) {
		if(c.getId().equals("toggle")) {
			Button toggleButton = (Button)c;
			if(toggleButton.getText().equals("ON"))
				toggleButton.setText("OFF");
			else
				toggleButton.setText("ON");
		}
	}

	@Override
	protected void lostFocusAction(Clickable c) {}
	
	private BufferedImage rotateImage(BufferedImage image, int factorOf90Deg) {
		//somehow you have changed the contents of image...
		//also just doesn't work at all for the output image, regardless of factor
		
		//correct the angle
		while(factorOf90Deg>=4)
			factorOf90Deg-=4;
		while(factorOf90Deg<0)
			factorOf90Deg+=4;
		
		//make the new image
		int width = (factorOf90Deg%2==0)? image.getWidth():image.getHeight();
		int height= (factorOf90Deg%2==0)? image.getHeight():image.getWidth();
		BufferedImage newImage = new BufferedImage(width,height,image.getType());
		int[] img1 = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		int[] img2 = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();
		
		//perform transformation
		for(int i=0; i<img1.length; i++) {
			int x = i%image.getWidth();
			int y = i/image.getWidth();
			
			int index;
			switch(factorOf90Deg) {
			case 0:
				index = i;
				break;
			case 1:
				index = image.getHeight()-(y+1) + image.getWidth()*x;
				break;
			case 2:
				index = img2.length-i-1;
				break;
			default:
				index = img2.length- (image.getHeight()-(y+1) + image.getWidth()*x) -1;
			}
			img2[index] = img1[i];
		}
		
		return newImage;
	}
}