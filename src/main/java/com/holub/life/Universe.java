package com.holub.life;

import java.io.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.holub.io.Files;
import com.holub.ui.MenuSite;

import com.holub.life.Cell;
import com.holub.life.Storable;
import com.holub.life.Clock;
import com.holub.life.Neighborhood;
import com.holub.life.Resident;

/**
 * The Universe is a mediator that sits between the Swing
 * event model and the Life classes. It is also a singleton,
 * accessed via Universe.instance(). It handles all
 * Swing events and translates them into requests to the
 * outermost Neighborhood. It also creates the Composite
 * Neighborhood.
 *
 * @include /etc/license.txt
 */

public class Universe extends JPanel
{	private 		final Cell  	outermostCell;
	private 		final RuleHolder ruleHolder = new RuleHolder();

	// 판과 규칙을 바꾸거나 읽는 곳은 모두 이 잠금을 잡는다 (ADR 005).
	// 세대 진행은 Clock의 스레드에서, 클릭과 메뉴는 Swing의 스레드에서 실행되기 때문이다.
	private final Object boardLock = new Object();
	private long generationNumber = 0;	// 지금 몇 세대째인가. boardLock을 잡고 쓴다
	private long changeNumber = 0;		// 클릭, Clear, Load, 규칙 변경 때 올린다. boardLock을 잡고 쓴다

	/** 결말을 예측할 때 최대 몇 세대까지 미리 돌려 볼지. */
	private static final int PREDICTION_LIMIT = 500;

	private final EndingPredictor predictor = new EndingPredictor(PREDICTION_LIMIT);

	private final StatusLine statusLine  = new StatusLine();	// 창 아래 두 줄의 글자를 만든다. Swing의 스레드에서만 쓴다
	private final JLabel	 stateLabel  = new FixedLabel();	// 윗줄: 세대 수, 세포 수, 규칙
	private final JLabel	 endingLabel = new FixedLabel();	// 아랫줄: 예측한 결말
	private final JPanel	 statusBar	 = new JPanel( new GridLayout(2,1) );	// 두 줄을 담는다. Life가 창에 붙인다

	/** 결말을 보여 주는 아랫줄의 글자 크기. */
	private static final float ENDING_FONT_SIZE = 16f;

	/**
	 * 글자가 바뀌어도 창의 배치를 다시 하라고 요청하지 않는 글자 칸.
	 * JLabel은 글자가 바뀔 때마다 그 요청(revalidate)을 한다. 그러면 창이 판을 가운데 칸 크기로 늘리고,
	 * 판은 곧바로 정사각형으로 다시 줄어든다(아래 componentResized). 창을 키워 놓으면 이것이 번쩍임으로 보였다.
	 * 이 글자 칸은 창 아래에 가로로 꽉 차고 높이가 글자 한 줄로 정해져 있어서, 글자가 바뀌어도 배치를 다시 할 필요가 없다.
	 */
	private static class FixedLabel extends JLabel
	{	FixedLabel()
		{	super(" ");
		}
		public void revalidate()
		{	// 일부러 아무것도 하지 않는다
		}
	}

	// 예측은 작업자 스레드가 계산한다. 끝나면 결과를 Swing의 스레드로 넘겨 창 아래 두 줄에 반영한다.
	private final PredictionWorker worker = new PredictionWorker
			(	request -> predictor.predict( request.start(), request.rule() ),
				(request, ending) -> SwingUtilities.invokeLater
				(	() ->
					{	statusLine.accept( request, ending );
						showStatusText();
					}
				)
			);

	private static	final Universe 	theInstance = new Universe();

	/** The default height and width of a Neighborhood in cells.
	 *  If it's too big, you'll run too slowly because
	 *  you have to update the entire block as a unit, so there's more
	 *  to do. If it's too small, you have too many blocks to check.
	 *  I've found that 8 is a good compromise.
	 */
	private static final int  DEFAULT_GRID_SIZE = 8;

	/** The size of the smallest "atomic" cell---a Resident object.
	 *  This size is extrinsic to a Resident (It's passed into the
	 *  Resident's "draw yourself" method.
	 */
	private static final int  DEFAULT_CELL_SIZE = 8;

	// The constructor is private so that the universe can be created
	// only by an outer-class method [Neighborhood.createUniverse()].

	private Universe()
	{	// Create the nested Cells that comprise the "universe." A bug
		// in the current implementation causes the program to fail
		// miserably if the overall size of the grid is too big to fit
		// on the screen.

		outermostCell = new Neighborhood
						(	DEFAULT_GRID_SIZE,
							new Neighborhood
							(	DEFAULT_GRID_SIZE,
								new Resident(ruleHolder)
							)
						);

		final Dimension PREFERRED_SIZE =
						new Dimension
						(  outermostCell.widthInCells() * DEFAULT_CELL_SIZE,
						   outermostCell.widthInCells() * DEFAULT_CELL_SIZE
						);

		addComponentListener
		(	new ComponentAdapter()
			{	public void componentResized(ComponentEvent e)
				{
					// Make sure that the cells fit evenly into the
					// total grid size so that each cell will be the
					// same size. For example, in a 64x64 grid, the
					// total size must be an even multiple of 63.

					Rectangle bounds = getBounds();
					bounds.height /= outermostCell.widthInCells();
					bounds.height *= outermostCell.widthInCells();
					bounds.width  =  bounds.height;
					setBounds( bounds );
				}
			}
		);

		setBackground	( Color.white	 );
		setPreferredSize( PREFERRED_SIZE );
		setMaximumSize	( PREFERRED_SIZE );
		setMinimumSize	( PREFERRED_SIZE );
		setOpaque		( true			 );

		addMouseListener					//{=Universe.mouse}
		(	new MouseAdapter()
			{	public void mousePressed(MouseEvent e)
				{	Rectangle bounds = getBounds();
					bounds.x = 0;
					bounds.y = 0;

					PredictionRequest request;
					synchronized( boardLock )
					{	outermostCell.userClicked(e.getPoint(),bounds);
						request = readBoardAfterChange();
					}
					repaint();
					predictAgain( request );
				}
			}
		);

		MenuSite.addLine( this, "Grid", "Clear",
			new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{	PredictionRequest request;
					synchronized( boardLock )
					{	outermostCell.clear();
						generationNumber = 0;
						request = readBoardAfterChange();
					}
					repaint();
					predictAgain( request );
				}
			}
		);

		MenuSite.addLine			// {=Universe.load.setup}
		(	this, "Grid", "Load",
			new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{	doLoad();
				}
			}
		);

		MenuSite.addLine
		(	this, "Grid", "Store",
			new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{	doStore();
				}
			}
		);

		MenuSite.addLine
		(	this, "Grid", "Exit",
			new ActionListener()
			{	public void actionPerformed(ActionEvent e)
		        {	System.exit(0);
		        }
			}
		);
		MenuSite.addLine
				(	this, "Rule", "Conway (B3/S23)",
						new ActionListener()
						{	public void actionPerformed(ActionEvent e)
						{	changeRule( new ConwayRule() );
						}
						}
				);

		MenuSite.addLine
				(	this, "Rule", "HighLife (B36/S23)",
						new ActionListener()
						{	public void actionPerformed(ActionEvent e)
						{	changeRule( new HighLifeRule() );
						}
						}
				);

		MenuSite.addLine
				(	this, "Rule", "Custom (B/S)...",
						new ActionListener()
						{	public void actionPerformed(ActionEvent e)
						{	doCustomRule();
						}
						}
				);

		Clock.instance().addClockListener //{=Universe.clock.subscribe}
		(	new Clock.Listener()
			{	public void tick()
				{	// 한 세대의 계산과 반영을 한 잠금 안에서 한다. 그 사이에 규칙이나 판이 바뀌지 않는다.
					final PredictionRequest now;
					synchronized( boardLock )
					{	if( outermostCell.figureNextState
							   ( Cell.DUMMY,Cell.DUMMY,Cell.DUMMY,Cell.DUMMY,
								 Cell.DUMMY,Cell.DUMMY,Cell.DUMMY,Cell.DUMMY
							   )
						  )
						{	if( outermostCell.transition() )
								refreshNow();
						}
						++generationNumber;
						now = readBoard();
					}
					// 세대가 넘어간 것은 예측한 길을 따라간 것이다. 다시 예측하지 않고 창 아래의 숫자만 고친다.
					SwingUtilities.invokeLater( () -> showStatus(now) );
				}
			}
		);

		endingLabel.setFont( endingLabel.getFont().deriveFont(ENDING_FONT_SIZE) );
		statusBar.setBorder( BorderFactory.createEmptyBorder(4,8,4,8) );
		statusBar.add( stateLabel  );
		statusBar.add( endingLabel );

		final PredictionRequest first;
		synchronized( boardLock )
		{	first = readBoard();
		}
		SwingUtilities.invokeLater( () -> predictAgain(first) );
	}

	/** 창 아래 두 줄. 세대 수, 세포 수, 규칙과 예측한 결말을 보여 준다. */
	public JComponent statusBar()
	{	return statusBar;
	}

	/** statusLine이 만든 글자를 두 글자 칸에 올린다. Swing의 스레드에서 부른다. */
	private void showStatusText()
	{	stateLabel.setText ( statusLine.stateText()  );
		endingLabel.setText( statusLine.endingText() );
		endingLabel.setForeground( statusLine.endingColor() );
	}

	/** 규칙을 바꾼다. 규칙을 바꾸는 곳은 모두 이 메서드를 거친다. 그래야 다시 예측하는 것이 빠지지 않는다 (ADR 004). */
	private void changeRule( Rule rule )
	{	PredictionRequest request;
		synchronized( boardLock )
		{	ruleHolder.change( rule );
			request = readBoardAfterChange();
		}
		predictAgain( request );
	}

	/** 지금의 판, 규칙, 세대 번호, 변경 번호를 함께 읽는다. boardLock을 잡은 채로 부른다. */
	private PredictionRequest readBoard()
	{	LiveCellCollector collector = new LiveCellCollector();
		outermostCell.transfer( collector, new Point(0,0), Cell.STORE );	// STORE: 판 → collector (읽기)

		return new PredictionRequest
				(	new Generation( collector.cells(), outermostCell.widthInCells() ),
					ruleHolder.current(), generationNumber, changeNumber
				);
	}

	/** 판이나 규칙을 바꾼 직후에 부른다. 변경 번호를 올리고 바뀐 상태를 읽는다. boardLock을 잡은 채로 부른다. */
	private PredictionRequest readBoardAfterChange()
	{	++changeNumber;
		return readBoard();
	}

	/** 창 아래 두 줄을 이 상태로 고치고, 이 상태로 결말을 다시 예측하게 한다. Swing의 스레드에서 부른다. */
	private void predictAgain( PredictionRequest request )
	{	showStatus( request );
		worker.request( request );		// 계산은 작업자 스레드가 한다. 여기서는 기다리지 않는다
	}

	/** 창 아래 두 줄을 이 상태로 고친다. Swing의 스레드에서 부른다. */
	private void showStatus( PredictionRequest state )
	{	statusLine.update( state.generation(), state.start().liveCount(),
							state.rule().toString(), state.change() );
		showStatusText();
	}

	/** Singleton Accessor. The Universe object itself is manufactured
	 *  in Neighborhood.createUniverse()
	 */

	public static Universe instance()
	{	return theInstance;
	}

	private void doLoad()
	{	PredictionRequest request = null;	// 판을 건드리지 않고 끝나면 null로 남는다
		try
		{
			FileInputStream in = new FileInputStream(
			   Files.userSelected(".",".life","Life File","Load"));

			Clock.instance().stop();		// stop the game and

			// stop()은 이미 돌고 있는 세대가 끝나기를 기다리지 않는다. 그래서 판을 건드릴 때는 잠금을 잡는다.
			// 파일을 읽는 동안에는 잠금을 잡지 않는다.
			Storable memento;
			synchronized( boardLock )
			{	outermostCell.clear();			// clear the board.
				memento = outermostCell.createMemento();
				generationNumber = 0;
				request = readBoardAfterChange();
			}

			memento.load( in );

			synchronized( boardLock )
			{	outermostCell.transfer( memento, new Point(0,0), Cell.LOAD );
				request = readBoardAfterChange();
			}

			in.close();
		}
		catch( IOException theException )
		{	JOptionPane.showMessageDialog( null, "Read Failed!",
					"The Game of Life", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
		if( request != null )
			predictAgain( request );
	}

	private void doStore()
	{	try
		{
			FileOutputStream out = new FileOutputStream(
				  Files.userSelected(".",".life","Life File","Write"));

			Clock.instance().stop();		// stop the game

			Storable memento;
			synchronized( boardLock )		// 세대가 넘어가는 도중의 판을 저장하지 않게 한다
			{	memento = outermostCell.createMemento();
				outermostCell.transfer( memento, new Point(0,0), Cell.STORE );
			}
			memento.flush(out);

			out.close();
		}
		catch( IOException theException )
		{	JOptionPane.showMessageDialog( null, "Write Failed!",
					"The Game of Life", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** B/S 표기를 입력받아 그 규칙으로 바꾼다. 표기가 잘못됐으면 알리고 지금 규칙을 그대로 둔다. */
	private void doCustomRule()
	{	String notation = JOptionPane.showInputDialog( null,
					"Rule in B/S notation (e.g. B36/S23)",
					"The Game of Life", JOptionPane.QUESTION_MESSAGE);

		if( notation == null )			// 취소를 눌렀다
			return;

		try
		{	changeRule( new BirthSurvivalRule(notation) );
		}
		catch( IllegalArgumentException theException )
		{	JOptionPane.showMessageDialog( null,
					"Not a valid rule: " + notation + "\nExample: B36/S23",
					"The Game of Life", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Override paint to ask the outermost Neighborhood
	 *  (and any subcells) to draw themselves recursively.
	 *  All knowledge of screen size is also encapsulated.
	 *  (The size is passed into the outermost <code>Cell</code>.)
	 */

	public void paint(Graphics g)
	{
		Rectangle panelBounds = getBounds();
		Rectangle clipBounds  = g.getClipBounds();

		// The panel bounds is relative to the upper-left
		// corner of the screen. Pretend that it's at (0,0)
		panelBounds.x = 0;
		panelBounds.y = 0;
		outermostCell.redraw(g, panelBounds, true);		//{=Universe.redraw1}
	}

	/** Force a screen refresh by queing a request on
	 *  the Swing event queue. This is an example of the
	 *  Active Object pattern (not covered by the Gang of Four).
	 *  This method is called on every clock tick. Note that
	 *  the redraw() method on a given <code>Cell</code>
	 *  does nothing if the <code>Cell</code> doesn't
	 *  have to be refreshed.
	 */

	private void refreshNow()
	{	SwingUtilities.invokeLater
		(	new Runnable()
			{	public void run()
				{	Graphics g = getGraphics();
					if( g == null )		// Universe not displayable
						return;
					try
					{
						Rectangle panelBounds = getBounds();
						panelBounds.x = 0;
						panelBounds.y = 0;
						outermostCell.redraw(g, panelBounds, false); //{=Universe.redraw2}
					}
					finally
					{	g.dispose();
					}
				}
			}
		);
	}
}
