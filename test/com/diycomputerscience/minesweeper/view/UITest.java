package com.diycomputerscience.minesweeper.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;

import static org.easymock.EasyMock.*;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.fest.swing.core.MouseButton;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.diycomputerscience.minesweeper.Board;
import com.diycomputerscience.minesweeper.MineInitializationStrategy;
import com.diycomputerscience.minesweeper.MockMineUtils;
import com.diycomputerscience.minesweeper.PersistenceStrategy;
import com.diycomputerscience.minesweeper.Point;
import com.diycomputerscience.minesweeper.Square;

public class UITest {
	
	private FrameFixture window;
	private Board board;
	private OptionPane optionPane;
	private MineInitializationStrategy mineInitializationStrategy;
	private PersistenceStrategy persistenceStrategy;
	
	@BeforeClass 
	public static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}
	
	@Before
	public void setUp() throws Exception {
		optionPane = EasyMock.createMock(OptionPane.class);
		
		this.mineInitializationStrategy = EasyMock.createMock(MineInitializationStrategy.class);
		Point boardSize = new Point(Board.MAX_ROWS, Board.MAX_ROWS);
		expect(this.mineInitializationStrategy.mines(boardSize)).andReturn(MockMineUtils.mines(boardSize));
		replay(this.mineInitializationStrategy);
		
		this.persistenceStrategy = EasyMock.createMock(PersistenceStrategy.class);
		replay(this.persistenceStrategy);
		
		UI ui = GuiActionRunner.execute(new GuiQuery<UI>() {

			@Override
			protected UI executeInEDT() throws Throwable {								
				board = new Board(mineInitializationStrategy);
				return UI.build(board, optionPane, persistenceStrategy);
			}
			
		});
		
		this.window = new FrameFixture(ui);
		this.window.show();
	}

	@After
	public void tearDown() throws Exception {
		this.window.cleanUp();
	}

	@Test
	public void testUIVisibility() {
		replay(optionPane);
		
		this.window.requireVisible();		
	}

	@Test
	public void testUIDefaultCloseOperation() {
		replay(optionPane);
		
		assertEquals(JFrame.DISPOSE_ON_CLOSE, ((JFrame)this.window.target).getDefaultCloseOperation());
	}
	
	@Test
	public void testUITitle() {
		replay(optionPane);
		
		assertEquals("Minesweeper", this.window.target.getTitle());
	}
	
	@Test
	public void testMainPanel() {
		replay(optionPane);
		
		JPanel mainPanel = this.window.panel("MainPanel").target;
		
		// verify that the contentPane contains a JPanel called "MainPanel"
		assertNotNull(mainPanel);
		
		// verify that the layoutManaget of the mainPanel is GridLayout
		assertEquals(GridLayout.class, mainPanel.getLayout().getClass());
		
		// verify the dimensions of the GridLayout
		assertEquals(Board.MAX_ROWS, ((GridLayout)mainPanel.getLayout()).getRows());
		assertEquals(Board.MAX_COLS, ((GridLayout)mainPanel.getLayout()).getColumns());	
	}
	
	@Test
	public void testSquares() {
		replay(optionPane);
		
		JPanel mainPanel = this.window.panel("MainPanel").target;
		
		Component components[] = mainPanel.getComponents();
		
		// verify that the mainPanel has Board.MAX_ROWS x Board.MAX_COLS components
		assertEquals(Board.MAX_ROWS*Board.MAX_COLS, components.length);
		
		// verify that each component in the mainPanel is a JButton
		for(Component component : components) {
			assertEquals(JButton.class, component.getClass());
		}
	}
	
	@Test
	public void testLeftClickCoveredSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON).equals(squares[point.row][point.col].getCount());
	}
	
	@Test
	public void testLeftClickUncoveredSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON).equals(squares[point.row][point.col].getCount());
	}
	
	@Test
	public void testLeftClickCoveredSquareWhichIsAMine() throws Exception {
		Capture<Component> captureOfComponent = new Capture<Component>();
		Capture<Object> captureOfObject = new Capture<Object>();
		Capture<String> captureOfString = new Capture<String>();
		Capture<Integer> captureOfInteger = new Capture<Integer>();
		
		expect(this.optionPane.userConfirmation(capture(captureOfComponent), 
												capture(captureOfObject), 
												capture(captureOfString), 
												capture(captureOfInteger))).andReturn(JOptionPane.NO_OPTION);
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON);
		verify(this.optionPane);
		assertEquals("Confirm quit", captureOfString.getValue());
		assertEquals("That was a mine. You have lost the game. Would you like to play again ?", captureOfObject.getValue());
		assertNotNull(captureOfComponent.getValue());
		assertEquals(new Integer(JOptionPane.YES_NO_OPTION), captureOfInteger.getValue());
	}
	
	@Test
	public void testRightClickCoveredSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON).background().requireEqualTo(ColorUIResource.MAGENTA);
	}
	
	@Test
	public void testRightClickCoveredSquareWhichIsAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON).background().requireEqualTo(ColorUIResource.MAGENTA);
	}
	
	@Test
	public void testRightClickMarkedSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON).background().requireEqualTo(new ColorUIResource(238, 238, 238));
	}
	
	@Test
	public void testRightClickMarkedSquareWhichIsAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON).background().requireEqualTo(new ColorUIResource(238, 238, 238));
	}
	
	@Test
	public void testLeftClickMarkedSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON).background().requireEqualTo(ColorUIResource.MAGENTA);
	}
	
	@Test
	public void testLeftClickMarkedSquareWhichIsAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON).background().requireEqualTo(ColorUIResource.MAGENTA);
	}
	
	@Test
	public void testRightClickUncoveredSquareWhichIsNotAMine() throws Exception {
		replay(this.optionPane);
		
		Square squares[][] = this.board.getSquares();
		Point point = getFirstCoveredSquareWhichIsNotAMine(squares);
		this.window.button(point.row+","+point.col).click(MouseButton.LEFT_BUTTON);
		this.window.button(point.row+","+point.col).click(MouseButton.RIGHT_BUTTON).text().equals(squares[point.row][point.col].getCount());
		this.window.button(point.row+","+point.col).background().requireEqualTo(new ColorUIResource(238, 238, 238));
	}		
	
	private Point getFirstCoveredSquareWhichIsAMine(Square[][] squares) {
		for(int row=0; row<squares.length; row++) {
			for(int col=0; col<squares[row].length; col++) {
				if(squares[row][col].getState().equals(Square.SquareState.COVERED)  && squares[row][col].isMine()) {
					return new Point(row, col);
				}
			}
		}
		return null;
	}
	
	private Point getFirstCoveredSquareWhichIsNotAMine(Square[][] squares) {
		for(int row=0; row<squares.length; row++) {
			for(int col=0; col<squares[row].length; col++) {
				if(squares[row][col].getState().equals(Square.SquareState.COVERED)  && !squares[row][col].isMine()) {
					return new Point(row, col);
				}
			}
		}
		return null;
	}
	
}
