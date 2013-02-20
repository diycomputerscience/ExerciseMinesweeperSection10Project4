package com.diycomputerscience.minesweeper.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import com.diycomputerscience.minesweeper.Board;
import com.diycomputerscience.minesweeper.FilePersistenceStrategy;
import com.diycomputerscience.minesweeper.PersistenceException;
import com.diycomputerscience.minesweeper.PersistenceStrategy;
import com.diycomputerscience.minesweeper.RandomMineInitializationStrategy;
import com.diycomputerscience.minesweeper.Square;
import com.diycomputerscience.minesweeper.UncoveredMineException;

public class UI extends JFrame {
	
	private Board board;
	private OptionPane optionPane;
	private PersistenceStrategy peristenceStrategy;
	private JPanel panel;
	
	private ResourceBundle resourceBundle;
	
	private static final Logger logger = Logger.getLogger(UI.class);
	
	public UI(Board board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {
		try {
			this.resourceBundle = ResourceBundle.getBundle("MessageBundle");
		} catch(MissingResourceException mre) {
			logger.warn("Could not locate MessageBundle file", mre);
		}
		
		// set this.board to the injected Board
		this.board = board;
		this.optionPane = optionPane;
		this.peristenceStrategy = persistenceStrategy;
		
		// Set the title to "Minesweeper"
		this.setTitle(this.resourceBundle.getString("title"));
				
		this.panel = new JPanel();
		
		// Set the name of the panel to "MainPanel" 
		panel.setName("MainPanel");
		
		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
		panel.setLayout(new GridLayout(Board.MAX_ROWS, Board.MAX_COLS));
		
		// add squares to the panel
		this.layoutSquares(panel);
		// add panel to the content pane
		this.getContentPane().add(this.panel);
		
		// set the menu bar
		this.setJMenuBar(buildMenuBar());
		
		// validate components
		//this.validate();
	}
	
	public void load(Board board) {
		logger.info("Loading board into the UI");
		this.board = board;
		
		this.getContentPane().removeAll();
		this.invalidate();
		
		this.panel = new JPanel();		
		// Set the name of the panel to "MainPanel" 
		panel.setName("MainPanel");		
		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
		panel.setLayout(new GridLayout(Board.MAX_ROWS, Board.MAX_COLS));
		
		// add squares to the panel
		this.layoutSquares(panel);
		// add panel to the content pane
		
		this.getContentPane().add(this.panel);
		this.validate();		
	}
	
	private void layoutSquares(JPanel panel) {		
		final Square squares[][] = this.board.getSquares();
					
		for(int row=0; row<Board.MAX_ROWS; row++) {
			for(int col=0; col<Board.MAX_COLS; col++) {
				final JButton squareUI = new JButton();
				squareUI.setName(row+","+col);
				final int theRow = row;
				final int theCol = col;
				squareUI.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent me) {						
						// invoke the appropriate logic to affect this action (left or right mouse click)
						if(SwingUtilities.isLeftMouseButton(me)) {
							try {
								UI.this.board.uncover(new com.diycomputerscience.minesweeper.Point(theRow, theCol));												
							} catch(UncoveredMineException ume) {
								logger.info("Game lost");
								squareUI.setBackground(Color.RED);
								String gameOverTitle = resourceBundle.getString("gameover.dialogue.msg.title");
								String gameOverMsg = resourceBundle.getString("gameover.dialogue.msg");
								int answer = optionPane.userConfirmation(UI.this, gameOverMsg, gameOverTitle, JOptionPane.YES_NO_OPTION);
								if(answer == JOptionPane.YES_OPTION) {
									logger.debug("Creating new board");
									Board board = new  Board(new RandomMineInitializationStrategy());
									load(board);
								} else {
									logger.info("Exiting game...");
									UI.this.dispose();
								}
							}
						} else if(SwingUtilities.isRightMouseButton(me)) {
							UI.this.board.mark(new com.diycomputerscience.minesweeper.Point(theRow, theCol));
						}
						// display the new state of the square
						updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[theRow][theCol]);						
					}
				});
				updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[theRow][theCol]);
				panel.add(squareUI);
			}
		}
	}
	
	private void updateSquareUIDisplay(JButton squareUI, Square square) {
		if(square.getState().equals(Square.SquareState.UNCOVERED)) {
			if(square.isMine()) {
				squareUI.setBackground(ColorUIResource.RED);
			} else {
				squareUI.setText(String.valueOf(square.getCount()));
			}							
		} else if(square.getState().equals(Square.SquareState.MARKED)) {
			squareUI.setText("");
			squareUI.setBackground(ColorUIResource.MAGENTA);
		} else if(square.getState().equals(Square.SquareState.COVERED)) {
			squareUI.setText("");
			squareUI.setBackground(new ColorUIResource(238, 238, 238));
		}
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setName("menubar");
		
		JMenu file = new JMenu(resourceBundle.getString("menu.file"));
		file.setName("file");
		JMenuItem fileSave = new JMenuItem(resourceBundle.getString("menuitem.save"));
		fileSave.setName("file-save");
		fileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					UI.this.peristenceStrategy.save(UI.this.board);
				} catch(PersistenceException pe) {
					logger.error("Could not save the game", pe);
				}
			}		
		});
		JMenuItem fileLoad = new JMenuItem(resourceBundle.getString("menuitem.load"));
		fileLoad.setName("file-load");
		fileLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					UI.this.load(UI.this.board = UI.this.peristenceStrategy.load());
				} catch(PersistenceException pe) {
					//TODO: error dialogue
					//TODO: This button should be enabled only if a previously saved state exists
					logger.error("Could not load game from previously saved state", pe);
				}
			}			
		});
		JMenuItem close = new JMenuItem(resourceBundle.getString("menuitem.close"));
		close.setName("file-close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				System.exit(0);
			}		
		});
		file.add(fileSave);
		file.add(fileLoad);
		file.add(close);
		menuBar.add(file);
		
		JMenu help = new JMenu(resourceBundle.getString("menu.help"));
		help.setName("help");
		JMenuItem helpAbout = new JMenuItem(resourceBundle.getString("menuitem.about"));
		helpAbout.setName("help-about");
		help.add(helpAbout);
		menuBar.add(help);
		
		return menuBar;
	}

	
	public static UI build(Board board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {
		UI ui = new UI(board, optionPane, persistenceStrategy);
		ui.setSize(300, 400);
		ui.setVisible(true);
		ui.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		return ui;
	}

	public static void main(String[] args) {
		logger.info("Starting the game");
		InputStream configIS = null;
		try {
			configIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			final Properties configProperties = new Properties();
			configProperties.load(configIS);
			
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					build(new Board(new RandomMineInitializationStrategy()), 
						  new SwingOptionPane(), 
						  new FilePersistenceStrategy(configProperties.getProperty("persistence.filename")));
				}
						
			});			
		} catch(IOException ioe) {
			logger.error("Quitting: Could not load filename for persistence... ", ioe);
		}
	}
}
