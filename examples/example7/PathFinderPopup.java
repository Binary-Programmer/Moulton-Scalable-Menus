package example7;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import moulton.scalable.clickables.Button;
import moulton.scalable.containers.ListPanel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.popups.Popup;
import moulton.scalable.texts.Alignment;
import moulton.scalable.texts.Caption;
import moulton.scalable.texts.TextBox;

public class PathFinderPopup extends Popup {
	private TextBox pathDisplay;
	private TextBox fileName;
	private Button okButton;
	private ListPanel contents;
	private Font smallFont;
	private ScrollBar contentBar;

	public PathFinderPopup(boolean load, String width, String height) {
		super(width, height, Color.WHITE);
		
		blanketBackground = new Color(0x33dddddd, true);
		Font font = new Font("Arial", Font.PLAIN, 15);
		Button xButton = new Button("cancel","X",base,"width-20","0","20","20",font,Color.WHITE);
		xButton.setTouchedColor(Color.RED);
		addTouchComponent(xButton);
		new Caption(load?"Load":"Save", base, "5", "18", font, Alignment.LEFT_ALIGNMENT).setYCentered(false);
		
		smallFont = new Font("Arial", Font.PLAIN, 12);
		pathDisplay = new TextBox("path", "", base, "25", "30", "?width", "20", smallFont, Color.WHITE);
		pathDisplay.setOutline(true);
		addTouchComponent(pathDisplay);
		pathDisplay.setTouchedColor(Color.WHITE);
		
		Button pathUp = new Button("pathUp","^",base, "0","30","20","20",font,Color.LIGHT_GRAY);
		pathUp.setClickAction(() -> {
			goUpDirectory();
			return true;
		});
		addTouchResponsiveComponent(pathUp);
		fileName = new TextBox("fileName","",base,"5","height-25","width*.75-5","20",font,Color.WHITE);
		fileName.setOutline(true);
		fileName.setHint("file name");
		addTouchComponent(fileName);
		fileName.setTouchedColor(Color.WHITE);
		okButton = new Button(load?"doLoad":"doSave","Ok",base,"width*.75+5","height-25","width*.25-10","20",font,Color.LIGHT_GRAY);
		okButton.setEnabled(false);
		addTouchComponent(okButton);
		
		contents = new ListPanel("20",base,"0","55","width-20","height-85","width-20",Color.WHITE);
		contents.setOutline(true);
		contentBar = new ScrollBar(true, base, "width-20","55","20","height-85",Color.LIGHT_GRAY);
		contentBar.setScrollRate(2);
		contents.setHeightScrollBar(contentBar);
		addTouchComponent(contentBar);
		
		ClassLoader loader = PathFinderPopup.class.getClassLoader();
		File file = new File(loader.getResource("example7" + File.separator + "PathFinderPopup.class").toString());
		File folder = file.getParentFile().getParentFile(); //get the parent twice to get out of the package
		String path = folder.getPath().substring(6); //to get past "file:\" that Java does
		setPath(path);
		
	}
	
	public void goUpDirectory() {
		String path = pathDisplay.getMessage();
		if(contentBar != null)
			contentBar.setOffset(0);
		if(path.lastIndexOf(File.separator) != path.indexOf(File.separator)) //if there are at least two \s
			setPath(path.substring(0, path.lastIndexOf(File.separator)));
		else {
			//if there is one, do not delete it
			int index = path.indexOf(File.separator);
			if(index != -1) {
				setPath(path.substring(0, path.lastIndexOf(File.separator)+1));
			}else
				System.err.println("Cannot go up to the parent directory!");
		}
	}
	
	public void setPath(String path) {
		path = path.replaceAll("%20", " ");
		pathDisplay.setMessage(path);
		contents.clearComponents();
		
		File[] subdirs = new File(path).listFiles();
		if(subdirs == null)
			return;
		Font italic = new Font(smallFont.getName(), Font.ITALIC, smallFont.getSize());
		for(int i=0; i<subdirs.length; i++) {
			File file = subdirs[i];
			String pathName = file.getPath();
			int lastSlash = pathName.lastIndexOf(File.separator);
			if(lastSlash != -1)
				pathName = pathName.substring(lastSlash+1);
			Button dirButton;
			if(file.isDirectory())
				dirButton = new Button("directoryButton", "  "+pathName, contents, 0, i, italic, Color.YELLOW);
			else
				dirButton = new Button("directoryButton", "  "+pathName, contents, 0, i, smallFont, Color.WHITE);
			dirButton.setAlignment(Alignment.LEFT_ALIGNMENT);
			
			dirButton.setClickAction(() -> {
				select(dirButton.getText().substring(2));
				return true;
			});
		}
	}
	
	public String getPath() {
		return pathDisplay.getMessage()+File.separator+fileName.getMessage();
	}
	
	public void emptySelection(boolean empty) {
		okButton.setEnabled(!empty);
	}
	
	public void select(String name) {
		//if this leads to a directory, move to it. If a file, select it
		String fullPath = pathDisplay.getMessage();
		if(fullPath.charAt(fullPath.length()-1) != File.separator.charAt(0)) //bottom dirs already have the \\ preface
			fullPath += File.separator;
		fullPath += name;
		File file = new File(fullPath);
		
		if(file.isDirectory()) {
			setPath(fullPath);
			if(contentBar != null)
				contentBar.setOffset(0);
		}else if(file.isFile()) {
			fileName.setMessage(name);
			emptySelection(false);
		}
	}

}
