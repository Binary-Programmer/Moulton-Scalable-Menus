package example1;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;

public class Example1 extends JPanel implements Container, MouseListener, KeyListener, MouseMotionListener, MouseWheelListener{
	private MenuManager manager = null;
	private static final long serialVersionUID = 1L;
	
	public static void main(String args[]){
		new Example1();
	}
	
	public Example1(){
		JFrame frame = new JFrame("Example 1");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		manager = new Manager1(this);
		manager.createMenu();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		//we don't need a render loop since there is no animation in this example.
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(manager != null)
			manager.render(g);
		requestFocus();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500,500);
	}

	@Override
	public int getMenuWidth() {
		return getWidth();
	}

	@Override
	public int getMenuHeight() {
		return getHeight();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(manager != null){
			manager.mouseMoved(e.getX(), e.getY());
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		if(manager != null){
			manager.keyTyped(e.getKeyChar());
			repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(manager != null){
			manager.keyPressed(e.getKeyCode());
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if(manager != null){
			manager.mousePressed(e.getX(), e.getY());
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(manager != null){
			manager.mouseReleased(e.getX(), e.getY());
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(manager != null) {
			manager.mouseScrolled(e.getX(), e.getY(), e.getWheelRotation());
			repaint();
		}
	}

}
