package example7;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Container;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.texts.TextBox;
import moulton.scalable.utils.GridFormatter;

public class Manager7 extends MenuManager{
	private TextEditBox fileContents;
	private Button saveButton;
	
	private String filePath = null;
	private boolean controlOn = false;
	private boolean shiftOn = false;
	
	private final int UNDO_CAP = 20;
	private String[] undoHistory;
	private int histIndex = 0; //the index that holds what is current

	public Manager7(Container cont) {
		super(cont);
	}
	
	private void setUndo(String start) {
		undoHistory = new String[UNDO_CAP];
		undoHistory[0] = start;
		histIndex = 0;
	}
	
	private void addUndoEntry(String current) {
		histIndex++; //go to next index
		if(histIndex >= UNDO_CAP)
			histIndex = 0; //wrap around if necessary
		undoHistory[histIndex] = current;
		//delete the next in the circular array
		if(histIndex >= UNDO_CAP-1)
			undoHistory[0] = null;
		else
			undoHistory[histIndex+1] = null;
		
		//if it is possible to undo, then it should be possible to save (if the path is set)
		if(filePath != null)
			saveButton.setEnabled(true);
	}

	@Override
	public void createMenu() {
		menu = Panel.createRoot(Color.WHITE);
		Panel controlPanel = new Panel(menu, "0", "0", "width", "40", Color.GRAY);
		GridFormatter format = controlPanel.getGridFormatter();
		format.setMargin("10", "10");
		format.setFrame("10", "10");
		Font font = new Font("Arial", Font.PLAIN, 15);
		addTouchComponent(new Button("new", "New", controlPanel, 0, 0, font, Color.WHITE));
		addTouchComponent(new Button("load", "Load", controlPanel, 1, 0, font, Color.WHITE));
		saveButton = new Button("save", "Save", controlPanel, 2, 0, font, Color.WHITE);
		saveButton.setEnabled(false);
		addTouchComponent(saveButton);
		addTouchComponent(new Button("saveAs", "Save As", controlPanel, 3, 0, font, Color.WHITE));
		fileContents = new TextEditBox("fileContents","", menu, "0", "40", "width-20", "?height", font, new Color(0xe5e5e5));
		fileContents.setTextScroller(new ScrollBar(true, menu, "width-20", "40", "20", "?height",Color.LIGHT_GRAY));
		fileContents.acceptEnter(true);
		setUndo(fileContents.getMessage());
	}
	
	@Override
	public void setClicked(Clickable clicked, int x, int y) {
		if(this.clicked == fileContents && clicked == fileContents && shiftOn)
			fileContents.selectToClick(x, y);
		else
			super.setClicked(clicked, x, y);
	}

	@Override
	public void clickableAction(Clickable c) {
		if(c.getId() != null) {
			switch(c.getId()) {
			case "new":
				setPath(null);
				addUndoEntry(fileContents.getMessage()); //allow undo from new
				break;
			case "load":
				createPopup(true);
				break;
			case "saveAs":
				createPopup(false);
				break;
			case "save":
				FileWriter fw = null;
				try {
					fw = new FileWriter(new File(filePath));
					fw.write(fileContents.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(fw != null) {
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				saveButton.setEnabled(false);
				break;
				
			//Path Finder Pop up actions
			case "doSave":
				String toPath = ((PathFinderPopup)popup).getPath();
				fw = null;
				try {
					fw = new FileWriter(new File(toPath));
					fw.write(fileContents.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(fw != null) {
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				filePath = toPath; //just save it without loading stuff
				saveButton.setEnabled(false);
				setPopup(null);
				break;
			case "doLoad":
				setPath(((PathFinderPopup)popup).getPath());
				setUndo(fileContents.getMessage());
				//then fall through to cancel\quit
			case "cancel":
				setPopup(null);
				break;
			case "pathUp":
				((PathFinderPopup)popup).goUpDirectory();
				break;
			case "directoryButton":
				((PathFinderPopup)popup).select(((Button)c).getText().substring(2));
				break;
			case "fileName":
				((PathFinderPopup)popup).emptySelection(false);
				break;
			}
		}
	}
	
	private void createPopup(boolean shouldLoad) {
		PathFinderPopup pop = new PathFinderPopup(shouldLoad, "350", "200");
		setPopup(pop);
	}
	
	private void setPath(String filePath) {
		this.filePath = filePath;
		
		if(filePath == null) {
			fileContents.setMessage("");
			saveButton.setEnabled(false);
		}else {
			//actually load the file up too
			Scanner scan = null;
			try {
				String fullString = "";
				scan = new Scanner(new File(filePath));
				while(scan.hasNextLine()) {
					fullString += scan.nextLine() + '\n';
				}
				//now the full is the contents of the file
				fileContents.setMessage(fullString);
				//if it is a txt file, we will not allow word splitting
				if(filePath.substring(filePath.lastIndexOf('.')+1).equals("txt"))
					fileContents.setWordSplitting(false);
				else
					fileContents.setWordSplitting(true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(scan != null)
					scan.close();
			}
		}
	}

	@Override
	public void lostFocusAction(Clickable c) {
		String id = c.getId();
		if(id == null)
			return;
		if(id.equals("fileName")) {
			((PathFinderPopup)popup).emptySelection(((TextBox)c).getMessage().isEmpty());
		}else if(id.equals("path")) {
			((PathFinderPopup)popup).setPath(((TextBox)c).getMessage());
		}
	}
	
	@Override
	public void keyPressed(int key) {
		if(key == KeyEvent.VK_CONTROL) //Ctrl
			controlOn = true;
		if(key == KeyEvent.VK_SHIFT)
			shiftOn = true;
		if(key == KeyEvent.VK_ESCAPE) { //Esc
			//We will use the escape key to cancel any popups
			if(this.popup != null)
				clickableAction(new Button("cancel", null, null, 0, 0, null, null));
		}if(clicked instanceof TextEditBox) {
			TextEditBox box = (TextEditBox)clicked;
			if(!shiftOn && (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ||
					key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN))
				box.removeSelection();
			
			if(controlOn) {
				if(shiftOn) {
					if(key == KeyEvent.VK_LEFT) //selection left to break
						box.selectShift(true, true);
					else if(key == KeyEvent.VK_RIGHT) //selection right to break
						box.selectShift(false, true);
				}else {
					if(key == KeyEvent.VK_LEFT) //left shift to break
						box.moveToBreak(true);
					if(key == KeyEvent.VK_RIGHT) //right shift to break
						box.moveToBreak(false);
					if(key == KeyEvent.VK_BACK_SPACE) //backspace to break
						box.deleteToBreak(true);
					if(key == KeyEvent.VK_DELETE) //delete to break
						box.deleteToBreak(false);
				}
				return;
			}else {
				if(shiftOn) {
					if(key == KeyEvent.VK_LEFT) //begin selection left
						box.selectShift(true, false);
					else if(key == KeyEvent.VK_RIGHT) //begin selection right
						box.selectShift(false, false);
					if(key == KeyEvent.VK_UP)
						box.selectVertical(true);
					else if(key == KeyEvent.VK_DOWN)
						box.selectVertical(false);
					return;
				}else {
					if(key == KeyEvent.VK_UP)
						box.moveVertical(true);
					if(key == KeyEvent.VK_DOWN)
						box.moveVertical(false);
				}
			}
			super.keyPressed(key);
			if(key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE)
				addUndoEntry(fileContents.getMessage());
		}
		if(controlOn) {
			if(shiftOn) {
				if(key == KeyEvent.VK_S) //save as
					clickableAction(new Button("saveAs", null, null, 0, 0, null, null));
			}
			switch(key) {
			case KeyEvent.VK_O: //ctr-O
			case KeyEvent.VK_L: //ctr-L
				clickableAction(new Button("load", null, null, 0, 0, null, null));
				break;
			case KeyEvent.VK_N: //ctr-N
				clickableAction(new Button("new", null, null, 0, 0, null, null));
				break;
			case KeyEvent.VK_S: //ctr-S
				if(saveButton.isEnabled())
					clickableAction(new Button("save", null, null, 0, 0, null, null));
				else
					clickableAction(new Button("saveAs", null, null, 0, 0, null, null));
				break;
			case KeyEvent.VK_Y: //ctr-Y
				//redo
				int tempIndex = histIndex + 1;
				if(tempIndex >= UNDO_CAP)
					tempIndex = 0;
				if(undoHistory[tempIndex] != null) {
					histIndex = tempIndex;
					fileContents.setMessage(undoHistory[histIndex]);
				}
				break;
			case KeyEvent.VK_Z: //ctr-z
				//undo
				tempIndex = histIndex - 1;
				if(tempIndex < 0)
					tempIndex = UNDO_CAP-1;
				if(undoHistory[tempIndex] != null) {
					histIndex = tempIndex;
					fileContents.setMessage(undoHistory[histIndex]);
				}
				break;
			}
		}
		
	}
	
	public void keyReleased(int key) {
		if(key == KeyEvent.VK_CONTROL) //Ctrl
			controlOn = false;
		if(key == KeyEvent.VK_SHIFT)
			shiftOn = false;
	}
	
	@Override
	public void keyTyped(char key) {
		super.keyTyped(key);
		if(key >= 30)
			addUndoEntry(fileContents.getMessage());
	}
	
}
