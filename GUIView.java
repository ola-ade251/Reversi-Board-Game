package reversi;

import java.awt.GridLayout;

public class GUIView implements IView{
	private IModel model;				//read board state
	private IController controller;		//user actions
	
	private CWFrame wpFrame;			// for the white player
	private CWFrame bpFrame;			// for the black player
	
	private CWLabel wpLabel;			//for the white player
	private CWLabel bpLabel;			//for the black player
	
	private ReversiSquare[][] wpBoard;
	private ReversiSquare[][] bpBoard;
	
	@Override
	public void initialise(IModel model, IController controller) {
		this.model = model;
		this.controller = controller;
		
		//creating the frames
		wpFrame = new CWFrame("Reversi - white player");
		bpFrame = new CWFrame("Reversi - black player");
		
		//creating the labels
		wpLabel = new CWLabel("");
		bpLabel = new CWLabel("");
		
		//creating the panels
		CWPanel wpPanel = new CWPanel();
		CWPanel bpPanel = new CWPanel();
		
		CWPanel wpBottom = new CWPanel();
		CWPanel bpBottom = new CWPanel();
		
		//creating the buttons
		wpBoard = new ReversiSquare[8][8];
		bpBoard = new ReversiSquare[8][8];
		
		CWButton wpAI = new CWButton("Greedy AI (play white)");
		CWButton bpAI = new CWButton("Greedy AI (play black)");
		
		CWButton wpRestart = new CWButton("Restart");
		CWButton bpRestart = new CWButton("Restart");
		
		// layout
		wpPanel.setLayout(new GridLayout(8,8));
		bpPanel.setLayout(new GridLayout(8,8));
		
		wpBottom.setLayout(new GridLayout(2, 1));
		bpBottom.setLayout(new GridLayout(2, 1));
		
		//listeners
		wpAI.addActionListener(e -> this.controller.doAutomatedMove(IModel.PLAYER_WHITE));
		bpAI.addActionListener(e -> this.controller.doAutomatedMove(IModel.PLAYER_BLACK));
		
		wpRestart.addActionListener(e -> controller.startup());
		bpRestart.addActionListener(e -> controller.startup());
		
		//adding buttons to panel
		wpBottom.add(wpAI);
		wpBottom.add(wpRestart);
		bpBottom.add(bpAI);
		bpBottom.add(bpRestart);
		
		//adding to the frame
		wpFrame.add(wpLabel, "North");
		bpFrame.add(bpLabel, "North");
		
		wpFrame.add(wpBottom, "South");
		bpFrame.add(bpBottom, "South");
		
		
		for(int row =0; row <8; row++) {
			for(int col = 0; col < 8; col++) {
				//make button arrays and fill up the grid
				
				int wx = col;
				int wy = row;
				//flip co-ords for black board
				int bx = 7-col;
				int by = 7-row;
				
				wpBoard[row][col] = new ReversiSquare(wx, wy, model);
				bpBoard[row][col] = new ReversiSquare(bx, by, model);
				
				
				//listeners
				int fwx = wx;		//final
				int fwy = wy;
				int fbx = bx;
				int fby = by;
				wpBoard[row][col].addActionListener(e -> {
					controller.squareSelected(IModel.PLAYER_WHITE, fwx, fwy);
				});
				bpBoard[row][col].addActionListener(e -> {
					controller.squareSelected(IModel.PLAYER_BLACK, fbx, fby);
				});
				
				wpPanel.add(wpBoard[row][col]);
				bpPanel.add(bpBoard[row][col]);
			}
		}
		//adding the board panels to the frame
		wpFrame.add(wpPanel, "Center");
		bpFrame.add(bpPanel, "Center");
		
		//show frames
		wpFrame.setSize(400,400);
		bpFrame.setSize(400,400);
		
		wpFrame.setLocation(100,100);
		bpFrame.setLocation(600,100);
		
		wpFrame.setVisible(true);
		bpFrame.setVisible(true);
		
	}
	
	@Override
	public void refreshView() {		//visit every row and column to update every button
		for(int row=0; row < model.getBoardHeight(); row++) {
			for(int col = 0; col < model.getBoardWidth(); col++) {
				
				//update the boards
				wpBoard[row][col].repaint();
				bpBoard[row][col].repaint();
			}
		}
	}
	
	@Override
	public void feedbackToUser(int player, String message) {
		if(player == IModel.PLAYER_WHITE) {
			wpLabel.setText(message);
		} else {
			bpLabel.setText(message);
		}
	}

}
