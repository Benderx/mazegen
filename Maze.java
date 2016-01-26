package maze;

import java.awt.*;

import java.awt.geom.Point2D;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
   
public class Maze extends JFrame // main class for the game as a Swing application
{     
	
	private GameCanvas canvas;
   
	// Constructor to initialize the UI components and game objects
	static enum State 
		{
			MENU, INITIALIZED, PLAYING, PAUSED, GAMEOVER, DESTROYED
		}
	static State state;
	
	// Define constants for the game
	static final int CANVAS_WIDTH = 800;    // width and height of the game screen
	static final int CANVAS_HEIGHT = 600;
	static final int UPDATE_RATE = 100;    // number of game update per second
	static final long UPDATE_PERIOD = 1000000000L / UPDATE_RATE;  // nanoseconds
	
	private ArrayList<Point> pathFollow, availLocations;
	private int[][] grid, linesVert, linesHori, solverGrid;
	private Point current, winPoint, pass;
	private int widthG, heightG, scaler;
	private boolean keyDown, leftKeyDown, rightKeyDown, upKeyDown, downKeyDown, wKeyDown, aKeyDown, sKeyDown, dKeyDown, enterKeyDown, escapeKeyDown, spaceKeyDown, leftClickDown, rightClickDown, scrollClickDown,  toggle, toggle2, leftclick, enterclick, mazeEnd, vertRestrict, horiRestrict, win, spcaeclick, pathFound, endFound;
	
	public Maze() 
	{	
		// Initialize the game objects + variables//
		gameInit();
		
		//UI Components//
		canvas = new GameCanvas();
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		this.setContentPane(canvas);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int xS = ((screenSize.width - frameSize.width) / 2) - CANVAS_WIDTH/2;
		int yS = ((screenSize.height - frameSize.height) / 2) - CANVAS_HEIGHT/2;
		this.setLocation(xS, yS); // set location of frame in center of screen
   
		// Other UI components such as button, score board, if any.
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("Mazes!");
		this.setVisible(true);
	}
	
	// Initialize all the game objects, run only once in the constructor of the main class.
	
	public void gameInit() 
	{
		widthG = 10;
		heightG = 10;
		scaler = 15;
		toggle = false;
		toggle2 = false;
		enterclick = false;
		leftclick = false;
		horiRestrict = false;
		vertRestrict = false;
		mazeEnd = false;
		win = false;
		spcaeclick = false;
		pathFound = false;
		endFound = false;
		pass = new Point();
		availLocations = new ArrayList<Point>();
		pathFollow = new ArrayList<Point>();
		grid = new int[widthG][heightG];
		linesVert = new int[widthG + 1][heightG + 1];
		linesHori = new int[heightG + 1][widthG + 1];
		solverGrid = new int[widthG][heightG];
		for(int i = 0; i < grid.length; i ++) for(int j = 0; j < grid[0].length; j ++) grid[i][j] = 0;
		for(int i = 0; i < linesVert.length; i ++) for(int j = 0; j < linesVert[0].length; j ++) linesVert[i][j] = 1;
		for(int i = 0; i < linesHori.length; i ++) for(int j = 0; j < linesHori[0].length; j ++) linesHori[i][j] = 1;
		current = new Point((int)(Math.random() * widthG), (int)(Math.random() * heightG));
		winPoint = new Point((int)(Math.random() * widthG), (int)(Math.random() * heightG));
		grid[current.x][current.y] = 1;
		state = State.INITIALIZED;
		repaint();
		gameStart();
	}
	
	// Shutdown the game, clean up code that runs only once.
	public void gameShutdown()
	{
		// 
	}
	
	// Run the game loop here.
	private void gameLoop() 
	{
		// Regenerate the game objects for a new game
		// ......
		//state = State.PLAYING;
		
		// Game loop
		state = State.PLAYING;
		
		long beginTime, timeTaken, timeLeft;
		while (true) 
		{
			
			beginTime = System.nanoTime();
			if (state == State.PLAYING)
			{
				gameUpdate();
			}
			repaint();
			
			
			// Delay timer to provide the necessary delay to meet the target rate
			timeTaken = System.nanoTime() - beginTime;
			timeLeft = (UPDATE_PERIOD - timeTaken) / 1000000L;  // in milliseconds
			if (timeLeft < 1) timeLeft = 1;   // set a minimum
			try 
			{
				// Provides the necessary delay and also yields control so that other thread can do work.
				Thread.sleep(timeLeft);
			}	 
			catch (InterruptedException ex)
			{ 
				//
			}
		}
   	}
	
	// Update the state and position of all the game objects,
	// detect collisions and provide responses.
	public void gameUpdate() 
	{ 
		if(escapeKeyDown == true)
		{
			reset();
		}
		if(leftClickDown == true && leftclick == false)
		{
			if(mazeEnd == false) step();
			leftclick = true;
		}
		if(leftClickDown == false)
		{
			leftclick = false;
		}
		if(enterKeyDown == true && enterclick == false)
		{
			if(toggle == true) toggle = false;
			else if(toggle == false) toggle = true;
			enterclick = true;
		}
		if(enterKeyDown == false)
		{
			enterclick = false;
		}
		
		if(toggle == true)
		{
			if(mazeEnd == false) step();
		}
		
		if(mazeEnd == true)
		{
			if(rightKeyDown == true && horiRestrict == false)
			{
				if(linesVert[current.x + 1][current.y] == 0)
				{
					current.x = current.x + 1;
				}
				horiRestrict = true;
			}
			else if(leftKeyDown == true && horiRestrict == false)
			{
				if(linesVert[current.x][current.y] == 0)
				{
					current.x = current.x - 1;
				}
				horiRestrict = true;
			}
			if(leftKeyDown == false && rightKeyDown == false)
			{
				horiRestrict = false;
			}
			
			if(upKeyDown == true && vertRestrict == false)
			{
				if(linesHori[current.y][current.x] == 0)
				{
					current.y = current.y - 1;
				}
				vertRestrict = true;
			}
			else if(downKeyDown == true && vertRestrict == false)
			{
				if(linesHori[current.y + 1][current.x] == 0)
				{
					current.y = current.y + 1;
				}
				vertRestrict = true;
			}
			if(downKeyDown == false && upKeyDown == false)
			{
				vertRestrict = false;
			}
			
			if(current.x == winPoint.x && current.y == winPoint.y)
			{
				win = true;
			}
			
			if(spaceKeyDown == true && spcaeclick == false)
			{
				spcaeclick = true;
				if(toggle2 == true) toggle2 = false;
				else if(toggle2 == false) toggle2 = true;
			}
			else
			{
				spcaeclick = false;
			}
			if(toggle2 == true)
			{
				pass = getPath(pass);
			}
		}
	}
	
	public Point getPath(Point currentTemp)
	{
		if(pathFound == false)
		{
			if(endFound == false)
			{
				availLocations.clear();
				if(barrierLegal(new Point(currentTemp.x + 1, currentTemp.y), 1) == true && solverGrid[currentTemp.x + 1][currentTemp.y] < 1)
				{
					availLocations.add(new Point(currentTemp.x + 1, currentTemp.y));
				}
				if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 1) == true && solverGrid[currentTemp.x - 1][currentTemp.y] < 1)
				{
					availLocations.add(new Point(currentTemp.x - 1, currentTemp.y));
				}
				if(barrierLegal(new Point(currentTemp.x, currentTemp.y + 1), 0) == true && solverGrid[currentTemp.x][currentTemp.y + 1] < 1)
				{
					availLocations.add(new Point(currentTemp.x, currentTemp.y + 1));
				}
				if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 0) == true && solverGrid[currentTemp.x][currentTemp.y - 1] < 1)
				{
					availLocations.add(new Point(currentTemp.x, currentTemp.y - 1));
				}
				
				if(availLocations.size() != 0)
				{
					int randomSide = (int)(Math.random() * availLocations.size());
					currentTemp.x = availLocations.get(randomSide).x;
					currentTemp.y = availLocations.get(randomSide).y;
					solverGrid[currentTemp.x][currentTemp.y] += 1;
				}
				else
				{
					Point backUpPoint = new Point(-1, -1);
					if(barrierLegal(new Point(currentTemp.x + 1, currentTemp.y), 1) == true && solverGrid[currentTemp.x + 1][currentTemp.y] < 2)
					{
						backUpPoint = new Point(currentTemp.x + 1, currentTemp.y);
					}
					else if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 1) == true && solverGrid[currentTemp.x - 1][currentTemp.y] < 2)
					{
						backUpPoint = new Point(currentTemp.x - 1, currentTemp.y);
					}
					else if(barrierLegal(new Point(currentTemp.x, currentTemp.y + 1), 0) == true && solverGrid[currentTemp.x][currentTemp.y + 1] < 2)
					{
						backUpPoint = new Point(currentTemp.x, currentTemp.y + 1);
					}
					else if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 0) == true && solverGrid[currentTemp.x][currentTemp.y - 1] < 2)
					{
						backUpPoint = new Point(currentTemp.x, currentTemp.y - 1);
					}
					if(!(backUpPoint.x < 0))
					{
						solverGrid[currentTemp.x][currentTemp.y] += 1;
						currentTemp.x = backUpPoint.x;
						currentTemp.y = backUpPoint.y;
					}
				}
				if(winPoint.x == currentTemp.x && winPoint.y == currentTemp.y)
				{
					endFound = true;
				}
				
			}
			else
			{
				Point backUpPoint = new Point(-1, -1);
				if(barrierLegal(new Point(currentTemp.x + 1, currentTemp.y), 1) == true && solverGrid[currentTemp.x + 1][currentTemp.y] == 1)
				{
					backUpPoint = new Point(currentTemp.x + 1, currentTemp.y);
				}
				else if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 1) == true && solverGrid[currentTemp.x - 1][currentTemp.y] == 1)
				{
					backUpPoint = new Point(currentTemp.x - 1, currentTemp.y);
				}
				else if(barrierLegal(new Point(currentTemp.x, currentTemp.y + 1), 0) == true && solverGrid[currentTemp.x][currentTemp.y + 1] == 1)
				{
					backUpPoint = new Point(currentTemp.x, currentTemp.y + 1);
				}
				else if(barrierLegal(new Point(currentTemp.x, currentTemp.y), 0) == true && solverGrid[currentTemp.x][currentTemp.y - 1] == 1)
				{
					backUpPoint = new Point(currentTemp.x, currentTemp.y - 1);
				}
				pathFollow.add(backUpPoint);
				solverGrid[currentTemp.x][currentTemp.y] += 2;
				currentTemp.x = backUpPoint.x;
				currentTemp.y = backUpPoint.y;
				if(backUpPoint.x < 0)
				{
					pathFound = true;
					for(int i = 0; i < solverGrid.length; i++)
					{
						for(int j = 0; j < solverGrid[0].length; j++)
						{
							if(solverGrid[i][j] != 3)
							{
								solverGrid[i][j] = 0;
							}
						}
					}
					System.out.println("done");
				}
				
			}	
		}
		return currentTemp;
	}
	
	public void step()
	{
		
		ArrayList<Point> pos = new ArrayList<Point>();
		
		if(pointLegal(new Point(current.x + 1, current.y)) == true)
		{
			pos.add(new Point(current.x + 1, current.y));
		}
		if(pointLegal(new Point(current.x - 1, current.y)) == true)
		{
			pos.add(new Point(current.x - 1, current.y));
		}
		if(pointLegal(new Point(current.x, current.y + 1)) == true)
		{
			pos.add(new Point(current.x, current.y + 1));
		}
		if(pointLegal(new Point(current.x, current.y - 1)) == true)
		{
			pos.add(new Point(current.x, current.y - 1));
		}
		
		int possiblePos = pos.size();
		if(possiblePos != 0)
		{
			int randomSide = (int)(Math.random() * possiblePos);
			if(current.x != pos.get(randomSide).x)
			{
				if(pos.get(randomSide).x < current.x)
				{
					linesVert[pos.get(randomSide).x + 1][current.y] = 0;
				}
				else
				{
					linesVert[pos.get(randomSide).x][current.y] = 0;
				}
			}
			else if(current.y != pos.get(randomSide).y)
			{
				if(pos.get(randomSide).y < current.y)
				{
					linesHori[current.y][pos.get(randomSide).x] = 0;
				}
				else
				{
					linesHori[current.y + 1][pos.get(randomSide).x] = 0;
				}
			}
			current.x = pos.get(randomSide).x;
			current.y = pos.get(randomSide).y;
			grid[current.x][current.y] += 1;
			
		}
		else
		{
			if(linesVert[current.x][current.y] == 0 && grid[current.x - 1][current.y] == 1)
			{
				grid[current.x][current.y] += 1;
				current.x = current.x - 1;
			}
			else if(linesVert[current.x + 1][current.y] == 0 && grid[current.x + 1][current.y] == 1)
			{
				grid[current.x][current.y] += 1;
				current.x = current.x + 1;
			}
			
			else if(linesHori[current.y][current.x] == 0 && grid[current.x][current.y - 1] == 1)
			{
				grid[current.x][current.y] += 1;
				current.y = current.y - 1;
			}
			else if(linesHori[current.y + 1][current.x] == 0)
			{
				grid[current.x][current.y] += 1;
				current.y = current.y + 1;
			}
			else
			{
				mazeEnd = true;
				pass.x = current.x;
				pass.y = current.y;
				grid[current.x][current.y]++;
			}
		}
	}
	
	public boolean pointLegal(Point point)
	{
		if(point.x < 0 || point.x >= widthG || point.y < 0 || point.y >= heightG)
		{
			return false;
		}
		if(grid[point.x][point.y] > 0)
		{
			return false;
		}
		return true;
	}
	
	public boolean barrierLegal(Point point, int side)
	{
		if(point.x < 0 || point.x >= widthG || point.y < 0 || point.y >= heightG)
		{
			return false;
		}
		if(side == 1)
		{
			if(linesVert[point.x][point.y] > 0)
			{
				return false;
			}
		}
		if(side == 0)
		{
			if(linesHori[point.y][point.x] > 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public void reset()
	{
		enterclick = false;
		leftclick = false;
		toggle = false;
		toggle2 = false;
		mazeEnd = false;
		horiRestrict = false;
		vertRestrict = false;
		win = false;
		spcaeclick = false;
		pathFound = false;
		endFound = false;
		pass = new Point();
		availLocations = new ArrayList<Point>();
		pathFollow = new ArrayList<Point>();
		grid = new int[widthG][heightG];
		linesVert = new int[widthG + 1][heightG + 1];
		linesHori = new int[heightG + 1][widthG + 1];
		solverGrid = new int[widthG][heightG];
		for(int i = 0; i < grid.length; i ++) for(int j = 0; j < grid[0].length; j ++) grid[i][j] = 0;
		for(int i = 0; i < linesVert.length; i ++) for(int j = 0; j < linesVert[0].length; j ++) linesVert[i][j] = 1;
		for(int i = 0; i < linesHori.length; i ++) for(int j = 0; j < linesHori[0].length; j ++) linesHori[i][j] = 1;
		current = new Point((int)(Math.random() * widthG), (int)(Math.random() * heightG));
		winPoint = new Point((int)(Math.random() * widthG), (int)(Math.random() * heightG));
		grid[current.x][current.y]++;
	}
	
	
	// Refresh the display. Called back via repaint(), which invoke the paintComponent().
   	private void gameDraw(Graphics2D g2d) 
   	{
   		switch (state) 
   		{
   			case MENU:
   		   		
   				break;
   			case INITIALIZED:
   		   		
   				break;
   			case PLAYING:
   				if(win == false)
   				{
   					for(int i = 0; i < grid[0].length; i++)
	   				{
		   				for(int j = 0; j < grid.length; j++)
		   				{
		   					if(grid[j][i] == 1)
		   					{
		   						g2d.setColor(Color.blue);
		   						g2d.fillRect(j * scaler, i * scaler, scaler, scaler);
		   					}
		   					else if(grid[j][i] > 1)
		   					{
		   						g2d.setColor(Color.white);
		   						g2d.fillRect(j * scaler, i * scaler, scaler, scaler);
		   					}
		   					if(solverGrid[j][i] == 1)
		   					{
		   						g2d.setColor(Color.orange);
		   						g2d.fillRect(j * scaler, i * scaler, scaler, scaler);
		   					}
		   					else if(solverGrid[j][i] == 2)
		   					{
		   						g2d.setColor(Color.cyan);
		   						g2d.fillRect(j * scaler, i * scaler, scaler, scaler);
		   					}
		   					else if(solverGrid[j][i] == 3)
		   					{
		   						g2d.setColor(Color.magenta);
		   						g2d.fillRect(j * scaler, i * scaler, scaler, scaler);
		   					}
		   				}
	   				}
	   				g2d.setColor(Color.red);
	   				g2d.fillRect(current.x * scaler, current.y * scaler, scaler, scaler);
	   				if(mazeEnd == true)
	   				{
	   					g2d.setColor(Color.green);
	   					g2d.fillRect(winPoint.x * scaler, winPoint.y * scaler, scaler, scaler);
	   				}
	   				g2d.setColor(Color.gray);
	   				for(int i = 0; i < linesHori.length; i++)
	   				{
	   					for(int j = 0; j < linesHori[0].length - 1; j++)
	   	   				{
	   						if(linesHori[i][j] == 1)
	   						{
	   							g2d.drawLine(j * scaler, i * scaler, (j + 1) * scaler, i * scaler);
	   						}
	   	   				}
	   				}
	   				for(int i = 0; i < linesVert.length; i++)
	   				{
	   					for(int j = 0; j < linesVert[0].length - 1; j++)
	   	   				{
	   						if(linesVert[i][j] == 1)
	   						{
	   							g2d.drawLine(i * scaler, j * scaler, i * scaler, (j + 1) * scaler);
	   						}
	   	   				}
	   				}
   				}
   				else
   				{
   					g2d.setColor(Color.white);
   					g2d.drawString("You Win", 85, 85);
   				}
   				break;
   			case PAUSED:
   				// ......
   				break;
   			case DESTROYED:
   				// ......
   				break;
   			case GAMEOVER:
   				// ......
   				break;
   		}
   	}
   	
   	public void gameMouseReleased(MouseEvent e) 
   	{
   		if (e.getButton() == MouseEvent.NOBUTTON)
        {
   			//
        }
   		else if (e.getButton() == MouseEvent.BUTTON1) 
   		{
   			leftClickDown = false;
        }
   		else if (e.getButton() == MouseEvent.BUTTON2) 
   		{
   			scrollClickDown = false;
        }
   		else if (e.getButton() == MouseEvent.BUTTON3) 
   		{
   			rightClickDown = false;
   		}
   	}
   	public void gameMousePressed(MouseEvent e) 
   	{
   		if (e.getButton() == MouseEvent.NOBUTTON)
        {
   			//
        }
   		else if (e.getButton() == MouseEvent.BUTTON1) 
   		{
   			leftClickDown = true;
        }
   		else if (e.getButton() == MouseEvent.BUTTON2) 
   		{
   			scrollClickDown = true;
        }
   		else if (e.getButton() == MouseEvent.BUTTON3)
   		{
   			rightClickDown = true;
   		}
   	}
   	public void gameMouseMoved(MouseEvent e) 
   	{
   		//
   	}
   	public void gameMouseDragged(MouseEvent e) 
   	{
   		//
   	}
   	
   	
   	
   	// Process a key-pressed event. Update the current state.
   	public void gameKeyPressed(int keyCode)
   	{
   		keyDown = true;
   		switch (keyCode)
   		{
   			case KeyEvent.VK_UP:
   				upKeyDown = true;
   				break;
   			case KeyEvent.VK_DOWN:
   				downKeyDown = true;
   				break;
   			case KeyEvent.VK_LEFT:
   				leftKeyDown = true;
   				break;
   			case KeyEvent.VK_RIGHT:
   				rightKeyDown = true;
   				break;
   			case KeyEvent.VK_W:
   				wKeyDown = true;
   				break;
   			case KeyEvent.VK_A:
   				aKeyDown = true;
   				break;
   			case KeyEvent.VK_S:
   				sKeyDown = true;
   				break;
   			case KeyEvent.VK_D:
   				dKeyDown = true;
   				break;
   			case KeyEvent.VK_SPACE:
   				spaceKeyDown = true;
   				break;
   			case KeyEvent.VK_ENTER:
   				enterKeyDown = true;
   				break;
   			case KeyEvent.VK_ESCAPE:
   				escapeKeyDown = true;
   				break;
   			case 45:      //minus
   				widthG--;
   				reset();
   				break;
   			case 61:      //plus
   				widthG++;
   				reset();
   				break;
   			case 91:      //[
   				heightG--;
   				reset();
   				break;
   			case 93:      //]
   				heightG++;
   				reset();
   				break;
   		}
   	}
   
   	// Process a key-released event.
   	public void gameKeyReleased(int keyCode) 
   	{
   		keyDown = false;
   		switch (keyCode)
   		{
   			case KeyEvent.VK_UP:
   				upKeyDown = false;
   				break;
   			case KeyEvent.VK_DOWN:
   				downKeyDown = false;
   				break;
   			case KeyEvent.VK_LEFT:
   				leftKeyDown = false;
   				break;
   			case KeyEvent.VK_RIGHT:
   				rightKeyDown = false;
   				break;
   			case KeyEvent.VK_W:
   				wKeyDown = false;
   				break;
   			case KeyEvent.VK_A:
   				aKeyDown = false;
   				break;
   			case KeyEvent.VK_S:
   				sKeyDown = false;
   				break;
   			case KeyEvent.VK_D:
   				dKeyDown = false;
   				break;
   			case KeyEvent.VK_SPACE:
   				spaceKeyDown = false;
   				break;
   			case KeyEvent.VK_ENTER:
   				enterKeyDown = false;
   				break;
   			case KeyEvent.VK_ESCAPE:
   				escapeKeyDown = false;
   				break;
   		}
   	}
   
   	// Process a key-typed event.
   	public void gameKeyTyped(char keyChar) 
   	{
	   
   	}
   	
   	// Custom drawing panel, written as an inner class.
   	class GameCanvas extends JPanel implements KeyListener, MouseListener, MouseMotionListener
   	{
		boolean paintAll;
   		int xPos, yPos;
   		Image img;
   		
   		// Constructor
   		public GameCanvas() 
   		{
   			setFocusable(true);  // so that can receive key-events
   			requestFocus();
   			addKeyListener(this);
   			addMouseListener(this);
   			addMouseMotionListener(this);
   		}
   
   		// Override paintComponent to do custom drawing.
   		// Called back by repaint().
   		@Override
   		public void paintComponent(Graphics g) 
   		{
   			Graphics2D g2d = (Graphics2D)g;
   			super.paintComponent(g2d);   // paint background
   	   		setBackground(Color.BLACK);  // may use an image for background
   	   
   	   		// Draw the game objects
   	   		gameDraw(g2d);
   		}

   		@Override
   		public void mouseDragged(MouseEvent e)
   		{
   			gameMouseDragged(e);
   		}
   		
   		@Override
   		public void mouseMoved(MouseEvent e)
   		{
   			gameMouseMoved(e);
   		}
   		
   		@Override
   		public void mouseEntered(MouseEvent e)
   		{
   			//gameMouseEntered(e);
   		}
   		
   		@Override
   		public void mouseExited(MouseEvent e)
   		{
   			//gameMouseExited(e);
   		}
   		
   		@Override
   		public void mouseClicked(MouseEvent e)
   		{
   			//gameMouseClicked(e);
   		}
   		
   		@Override
   		public void mousePressed(MouseEvent e)
   		{
   			gameMousePressed(e);
   		}
   		
   		@Override
   		public void mouseReleased(MouseEvent e)
   		{
   			gameMouseReleased(e);
   		}
   		
   		// KeyEvent handlers
   		@Override
   		public void keyPressed(KeyEvent e) 
   		{
   			gameKeyPressed(e.getKeyCode());
   		}
      
   		@Override
   		public void keyReleased(KeyEvent e) 
   		{
   			gameKeyReleased(e.getKeyCode());
   		}
   
   		@Override
   		public void keyTyped(KeyEvent e) 
   		{
   			gameKeyTyped(e.getKeyChar());
   		}
   	}
   	
    // To start and re-start the game.
	public void gameStart() 
	{ 
		// Create a new thread
		Thread gameThread =  new Thread() 
		{
			// Override run() to provide the running behavior of this thread.
			@Override
			public void run() 
			{
				gameLoop();
			}
		};
		// Start the thread. start() calls run(), which in turn calls gameLoop().
		gameThread.start();
	}
	
	
	
   	// main
   	public static void main(String[] args) 
   	{
   		// Use the event dispatch thread to build the UI for thread-safety.
   		SwingUtilities.invokeLater(new Runnable() 
   		{
   			@Override
   			public void run() 
   			{
   				new Maze();
   			}
   		});
   	}
}
