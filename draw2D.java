/* Stephen Trotter, V00198061
program basis: This is a vector based drawing program which will take mouse and keyboard
input from the user and produce 2D pictures, represented in JFrame.
Current goals: To draw single lines using the mouse and to show where the line will be drawn 
by tracking the current location of the mouse after click one, click two will set the line in place, 
increment the array and then be ready for the next line
Future intentions: different drawing modes and colour modes along with filled shapes 
file save support (as text files filled with integers, 1st number of each line refering to how 
many objects of each type and then subsequent numbers relating to the points in the arrays

Version history: 
1.0: Done
this version contains the layout for the finished program with buttons for 
add line (between two points)
pencil (free draw)
ellipse
fill ellipse
rectangle
fill rectangle
line thickness
line colours (black red green and blue)
clear image
undo (implements the stack to store several images) 
erase part of image (eraser, assumes white background)
animate draw and fill functions
polyline
rich-line
save image
JFILECHOOSER

bug -*fixed* cannot create a "dot" with the draw function, need to draw when only clicked 
bug -*fixed* regular line not working correctly, need to add similar functionality as poly line
bug -*fixed* drawing won't resume after an undo operation
bug - *fixed* out of memory after a large number of drawn pictures, problem in undo button

Acknowledgements: Some example code supplied by Prof Rich Little
*/
import java.util.LinkedList;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.JFileChooser;

///// for using textures?
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

// for file input and output
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;

import javax.imageio.ImageIO;
import java.net.URL;
import java.applet.Applet;
import java.io.File;
//import javax.imageio.IIOException;

public class draw2D {
	public static int HEIGHT = 800;
	public static int WIDTH = 1000;

	
/////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws InterruptedException{

		draw2D.paintWindow draw = new draw2D.paintWindow();
		draw.setSize(WIDTH, HEIGHT);
		draw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		draw.setVisible(true);
	}
//////////////////////////////////////////////////////////////////////////////
	
public static class paintWindow extends JFrame {

		public static int westX = 80;
		public static int westY = HEIGHT - 50;
		public static int southX = WIDTH;
		public static int southY = 50;	
		private JPanel panelWest;
		private JPanel panelSouth;
		private draw2D.paper PAPER;
		
		public paintWindow(){
			setTitle("Stephen's Drawing Program");
			setSize(WIDTH, HEIGHT);
		
			panelWest = new JPanel();
			panelSouth = new JPanel();
			PAPER = new draw2D.paper();
			//create two panels to hold the buttons
			
			panelWest.setPreferredSize(new Dimension(westX, westY));
			panelSouth.setPreferredSize(new Dimension(southX, southY));
			
			// without this block of code nothing happens
			// Creates the container for the buttons and 
			// the image and then sets the layout
			Container express = this.getContentPane();
			express.setLayout(new BorderLayout());
			express.add(panelWest, BorderLayout.WEST);
			express.add(panelSouth, BorderLayout.SOUTH);
			express.add(PAPER, BorderLayout.CENTER);
			
			// add colour buttons, probably add colours on top of the buttons later
			makeColorButton(Color.BLACK);
			makeColorButton(Color.lightGray);
			makeColorButton(Color.WHITE);			makeColorButton(Color.RED);
			makeColorButton(Color.YELLOW);
			makeColorButton(Color.GREEN);
			makeColorButton(Color.BLUE);
			makeColorButton(Color.MAGENTA);
			makeColorButton(Color.blue);
			makeColorButton(Color.yellow);
			makeColorButton(Color.CYAN);
			makeColorButton(Color.yellow);
			makeColorButton(Color.ORANGE);
			makeColorButton(new Color(156, 93, 82));// brown?
			
			makeShapeButton("Pencil");
			makeShapeButton("Line");
			makeShapeButton("PolyLine");
			makeShapeButton("Rect");
			makeShapeButton("Fill-Rect");
			makeShapeButton("ellipse");
			makeShapeButton("fill-ellipse");
			makeShapeButton("Rich-Line");
			makeEraseButton();
			
			makeClearButton();
			makeClearBlackButton();
			
			makeUndoButton();
			makeSaveButton();
			makeLoadButton();
			
			makeSizeButton(1);
			makeSizeButton(2);
			makeSizeButton(3);
			makeSizeButton(4);
			makeSizeButton(5);
			makeSizeButton(6);
			makeSizeButton(8);
			makeSizeButton(10);

		}
		
		public void makeShapeButton(final String shape){
			JButton tempButton = new JButton(shape);
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.nullSet();
					PAPER.changeMode(shape);
				}
			});
		}
		
		public void makeEraseButton(){
			JButton tempButton = new JButton("eraser");
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.changeColor(Color.WHITE);
					PAPER.changeMode("Pencil");
				}
			});
		}
		
		public void makeUndoButton(){
			JButton tempButton = new JButton("undo");
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.undo();
				}
			});
		}
		
		public void makeClearButton(){
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.clear();
				}
			});
			panelWest.add(clearButton);
		}
		
		public void makeClearBlackButton(){
			JButton clearBlackButton = new JButton("ClearBlack");
			clearBlackButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.clearBlack();
				}
			});
			panelWest.add(clearBlackButton);
		}
		
		public void makeSizeButton(final int lineSize){
			JButton tempButton = new JButton("Size "+lineSize);
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.changeSize(lineSize);
				}
			});
		}
		
		// saves the file as a png for the user
		public void makeSaveButton(){
			JButton tempButton = new JButton("Save");
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// want to open a box which will let you name the file
					BufferedImage out = PAPER.saveButtonFunction();
					File f = null;
					
					JFileChooser filechooser = new JFileChooser();
					if(filechooser.showSaveDialog(PAPER) == JFileChooser.APPROVE_OPTION) {
						f = filechooser.getSelectedFile();
						
						try {	
							ImageIO.write(out, "png", f);
						} catch (Exception e1) {
						}
						
					} else {
					}
				}
			});
		}
		
		// opens the requested file from the user and sets it to the top left corner
		public void makeLoadButton(){
			JButton tempButton = new JButton("Load");
			tempButton.setForeground(Color.BLACK);
			tempButton.setPreferredSize(new Dimension(westX, 15));
			panelWest.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				
					//Create a file chooser
					File f = null;
					JFileChooser filechooser = new JFileChooser();
					if(filechooser.showOpenDialog(PAPER) == JFileChooser.APPROVE_OPTION) {
						// filechooser.showOpenDialog(component parent)
						f = filechooser.getSelectedFile();
						try {	
							BufferedImage img = ImageIO.read(f);
							PAPER.loadButtonFunction(img);// want to open a box which will let you name the file
						} catch (Exception e1) {
						}
					} else {
					}
					
				}
			});
		}
		
		public void makeColorButton(final Color color) {
			JButton tempButton = new JButton();
			tempButton.setBorderPainted(false); // needed to show the background colour!!!!!!!!!
			tempButton.setBackground(color); // supposed to make the button the color of the it refers to, use 
			tempButton.setOpaque(true); // makes the setbackground happen.... nope
			tempButton.setPreferredSize(new Dimension(16, 16));
			panelSouth.add(tempButton);
			tempButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PAPER.changeColor(color);
				}
			});
		}
	}	

///////////////////////////////////////////////////
// this is the image class which holds the drawn image, this will eventually hold a
// stack of images to allow undo and perhaps redo
public static class paper extends JPanel {
	
		//this is gonna be your image that we draw on
		BufferedImage image;
		Graphics2D graphics2D;
		//these hold mouse coordinates
		int currentX, currentY, oldX, oldY;
		public static LinkedList<BufferedImage> undoButton;
		int mode;
		boolean poly;
		int polyX, polyY;
		FileInputStream in;
		FileOutputStream out;
		
	public paper(){
		LinkedList<BufferedImage> undo = new LinkedList<BufferedImage>();
		this.undoButton = undo;
		in = null;
		out = null;
		mode = 1;
		poly = false;
		setDoubleBuffered(false);
		addMouseListener(new MouseAdapter() {
					//if the mouse is pressed it sets the oldX & oldY
					//coordinates as the mouses x & y coordinates
					public void mousePressed(MouseEvent e) {
						// saves the current image to a stack
						saveImage();	
							oldX = e.getX();
							oldY = e.getY();
							switch (mode){
							case 1: // pencil function
								graphics2D.drawLine(oldX, oldY, oldX, oldY);
								repaint();
								break;
							case 2: // line
								graphics2D.drawLine(oldX, oldY, oldX, oldY);
								repaint();
								break;
							case 3: // poly-line
								if (e.BUTTON1 == e.getButton()){
									//graphics2D.drawLine(oldX, oldY, oldX, oldY);
									if (poly) {
										graphics2D.drawLine(oldX, oldY, polyX, polyY);
										polyX = oldX;
										polyY = oldY;
									} else {
										poly = true;
										polyX = oldX;
										polyY = oldY;
									}				
									repaint();
								} else if (e.BUTTON3 == e.getButton()) { // this is the right mouse button for my mouse
									poly = false;
								}
								
								break;
							case 4: // rect
								repaint();			
								break;
							case 5: // fill rect
								repaint();				
								break;
							case 6: // ellipse
								repaint();			
								break;
							case 7: // fill ellipse
								repaint();			
								break;
							case 8:
								break;
							}
					}
					public void mouseReleased(MouseEvent e) {
						
						currentX = e.getX();
						currentY = e.getY();
				switch (mode){
					case 1: // pencil function
						break;
					case 2: // line
						graphics2D.drawLine(oldX, oldY, currentX, currentY);
						repaint();
						break;
					case 3: // poly-line
						repaint();					
						break;
					case 4: // rect
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.drawRect(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.drawRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.drawRect(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.drawRect(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 5: // fill rect
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.fillRect(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.fillRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.fillRect(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.fillRect(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 6: // ellipse
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.drawOval(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.drawRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.drawOval(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.drawOval(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 7: // fill ellipse
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.fillOval(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.fillOval(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.fillOval(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.fillOval(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 8:
						break;
				}
			}
					
			});

		addMouseMotionListener(new MouseMotionAdapter() {
			//while the mouse is dragged it sets currentX & currentY as the mouses x and y
			//then it draws a line at the coordinates
			//it repaints it and sets oldX and oldY as currentX and currentY
			public void mouseDragged(MouseEvent e) {
				switch (mode){ // right clicking will allow you you do temporarily see what you would draw
					case 1: // pencil function
						currentX = e.getX();
						currentY = e.getY();
						graphics2D.drawLine(oldX, oldY, currentX, currentY);
						repaint();
						oldX = currentX;
						oldY = currentY;
						break;
					case 2: // line
						loadOriginal();
						currentX = e.getX();
						currentY = e.getY();
						graphics2D.drawLine(oldX, oldY, currentX, currentY);
						repaint();
						break;
					case 3: // poly-line
						currentX = e.getX();
						currentY = e.getY();
					
						break;
					case 4: // rect
						currentX = e.getX();
						currentY = e.getY();
						loadOriginal();						
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.drawRect(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.drawRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.drawRect(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.drawRect(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 5: // fill rect
						currentX = e.getX();
						currentY = e.getY();
						loadOriginal();
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.fillRect(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.fillRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.fillRect(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.fillRect(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 6: // ellipse
						currentX = e.getX();
						currentY = e.getY();
						loadOriginal();
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.drawOval(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.drawRect(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.drawOval(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.drawOval(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 7: // fill ellipse
						currentX = e.getX();
						currentY = e.getY();
						loadOriginal();
						if (oldX < currentX) {
							if (oldY < currentY) {
								graphics2D.fillOval(oldX, oldY, currentX-oldX, currentY-oldY);
							} else {
								graphics2D.fillOval(oldX, currentY, currentX-oldX, oldY-currentY);
							}
						} else {
							if (oldY< currentY) {
								graphics2D.fillOval(currentX, oldY, oldX-currentX, currentY-oldY);
							} else {
								graphics2D.fillOval(currentX, currentY, oldX-currentX, oldY-currentY);
							}
						}
						repaint();					
						break;
					case 8:
						currentX = e.getX();
						currentY = e.getY();
						graphics2D.drawLine(oldX, oldY, currentX, currentY);
						repaint();
						break;
				}	
			}
			});
	}

	//this repaints part of the image rather than repainting the whole thing
	//if it has nothing on it then
	//it creates an image the size of the window
	//sets the value of Graphics as the image
	//sets the rendering
	//runs the clear() method
	//then it draws the image
	public void paintComponent(Graphics g) {
		// creates image if no image yet
		// and initializes the graphics2D object
 		if(image == null) {
			image = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
			graphics2D = (Graphics2D)image.getGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
		}

		g.drawImage(image, 0, 0, null);
	}
	
	/// classes needed for the undo button functionality
	public void saveImage() {
		// prevents out of memory error 
		if (undoButton.size() > 10){
			undoButton.removeLast();
		}
		
		BufferedImage imageForStack = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    		Graphics2D g2d = imageForStack.createGraphics();
    		g2d.drawImage(image, 0, 0, null);
		undoButton.push(imageForStack);
	}
    	
	public void undo(){
		if (undoButton.size() > 0) {
			Image img = undoButton.pop();
			graphics2D.drawImage(img, 0, 0, null);
			repaint();
		}
		
		//graphics2D.
	}
	
	public void changeMode(String desired){
		if (desired == "Pencil") {
			mode = 1;
		} else if (desired == "Line") {
			mode = 2;
		} else if (desired == "PolyLine") {
			mode = 3;
		} else if (desired == "Rect") {
			mode = 4;
		} else if (desired == "Fill-Rect") {
			mode = 5;
		} else if (desired == "ellipse") {
			mode = 6;
		} else if (desired == "fill-ellipse") {
			mode = 7;
		} else if (desired == "Rich-Line") {
			mode = 8;
		} else {
			mode = 1;
		}
	}
	
	public void loadOriginal(){
		if (undoButton.size() > 0) {
			Image img = undoButton.peek();
			graphics2D.drawImage(img, 0, 0, null);
			repaint();
		}
		
		//graphics2D.
	}

	//this is the clear
	//it sets the colors as white
	//then it fills the window with white
	//then it sets the color back to black
	public void clear() {
		graphics2D.setPaint(Color.white);
		graphics2D.fillRect(0, 0, getSize().width, getSize().height);
		graphics2D.setPaint(Color.black);
		repaint();
	}
	public void clearBlack() {
		graphics2D.setPaint(Color.BLACK);
		graphics2D.fillRect(0, 0, getSize().width, getSize().height);
		graphics2D.setPaint(Color.black);
		repaint();
	}

	public void changeColor(Color cIn) {
		graphics2D.setPaint(cIn);
		repaint();
	}
	
	public void nullSet() {
		poly =false;
	}
	
	public void changeSize(int lineSize){
		graphics2D.setStroke(new BasicStroke(lineSize));
		repaint();
	}
	
	public void loadButtonFunction(BufferedImage img){
		
		graphics2D.drawImage(img, 0, 0, null);
		repaint();
	}
	
	public BufferedImage saveButtonFunction(){
		return image;
	}
	
	// this method is to select the drawing tool to be used when clicking
	public void changeShape(String shape){
		
		//graphics2D.
	}
}
}
