package reversi;

public class ReversiController implements IController{
	private IModel model;
	private IView view;
	
	@Override
	public void initialise(IModel model, IView view) {
		this.model = model;
		this.view = view;
	}
	
	
	@Override
	public void startup() {
		//clear board
		model.clear(IModel.PLAYER_NONE);
		//starting positions
		model.setBoardContents(3, 3, IModel.PLAYER_WHITE);
		model.setBoardContents(4, 4, IModel.PLAYER_WHITE);
		model.setBoardContents(3, 4, IModel.PLAYER_BLACK);
		model.setBoardContents(4, 3, IModel.PLAYER_BLACK);
		
		//finish flag=false
		model.setFinished(false);
		
		model.setPlayer(IModel.PLAYER_WHITE);
		
		//messages- white plays first
		view.feedbackToUser(IModel.PLAYER_WHITE, "White player - choose where to put your piece");
		view.feedbackToUser(IModel.PLAYER_BLACK, "Black player - not your turn");
		//call refresh view
		update();
	}
	
	
	@Override
	public void update() {
		int currentp = model.getPlayer();		//get current states
		boolean finished = model.hasFinished();
		
		if(finished) {
			int wCount = 0;
			int bCount = 0;
			
			for(int y = 0; y < model.getBoardHeight(); y++) {
				for(int x=0; x < model.getBoardWidth(); x++) {
					
					int piece = model.getBoardContents(x, y);
					if(piece == IModel.PLAYER_WHITE) wCount++;
					if(piece == IModel.PLAYER_BLACK) bCount++;
				}
			}
			String mssg;
			if(wCount > bCount) {
				mssg = "White won. White " + wCount + " to Black " + bCount + ". Restart to continue.";
			} else if (bCount > wCount){
				mssg = "Black won. Black " + bCount + " to White " + wCount + ". Restart to continue.";
			} else {
				mssg = "Draw. Both players ended with " + wCount + " pieces. Restart to continue.";
			}
			//send messages
			view.feedbackToUser(IModel.PLAYER_WHITE, mssg);
			view.feedbackToUser(IModel.PLAYER_BLACK, mssg);
			
			//end game
			view.refreshView();
			return;
		}
		
		if(!finished) {
			if (currentp == IModel.PLAYER_WHITE) {
				view.feedbackToUser(IModel.PLAYER_WHITE, "White player - choose where to put your piece");
				view.feedbackToUser(IModel.PLAYER_BLACK, "Black player - not your turn");
			} else if(currentp == IModel.PLAYER_BLACK) {
				view.feedbackToUser(IModel.PLAYER_BLACK, "Black player - choose where to put your piece");
				view.feedbackToUser(IModel.PLAYER_WHITE, "White player - not your turn");
			}
		}
		view.refreshView();
	}
	
	
	@Override
	public void squareSelected(int player, int x, int y) {
		if(model.hasFinished()) {
			return;
		}
		
		//check for correct player
		if (player != model.getPlayer()) {
			view.feedbackToUser(player, "It's not your turn.");
			return;
		}
		//check for valid move- if it captures >=1 opponents piece
		int captured = captureCount(player, x, y);
		if (captured == 0) {
			view.feedbackToUser(player, "Invalid location to play a piece.");
			return;
		}
		//apply the capture move- flip
		doCapture(player, x, y);
		
		//change players
		if(player == IModel.PLAYER_WHITE) {
			model.setPlayer(IModel.PLAYER_BLACK);
		} else {
			model.setPlayer(IModel.PLAYER_WHITE);
		}
		
		//check if valid moves for next player
		int nextPlayer = model.getPlayer();
		boolean nextPlayerMove = false;
		
		for(int row = 0; row < model.getBoardHeight(); row++) {
			for(int col=0; col < model.getBoardWidth(); col++) {
				if (captureCount(nextPlayer, col, row) > 0) {
					nextPlayerMove = true;
					break;
				}
			}
			if(nextPlayerMove) {
				break;
			}
		}
		
		//skip turn if player no moves
		if (!nextPlayerMove) {
			view.feedbackToUser(nextPlayer, "No valid location to play in. Turn skipped.");
			//change player back
			if(nextPlayer == IModel.PLAYER_WHITE) {
				model.setPlayer(IModel.PLAYER_BLACK);
			} else {
				model.setPlayer(IModel.PLAYER_WHITE);
			}
		
			// if neither players can move- game ends
			int cPlayer = model.getPlayer();
			boolean cPlayerMove = false;
			
			for(int row = 0; row < model.getBoardHeight(); row++) {
				for(int col=0; col < model.getBoardWidth(); col++) {
					if (captureCount(cPlayer, col, row) >0) {
						cPlayerMove = true;
						break;
					}
				}
				if(cPlayerMove) {
					break;
				}
			}
			if (!cPlayerMove) {
				view.feedbackToUser(IModel.PLAYER_WHITE, "Game over. No value moves for each.");
				view.feedbackToUser(IModel.PLAYER_BLACK, "Game over. No value moves for each.");
				model.setFinished(true);
			}
		}
		update(); //GUI update
	}
	
	//HELP FUNCTIONS
	private int captureCount(int player, int x, int y) {
		//square should be empty to play on it- checking validity
		if (model.getBoardContents(x, y) != IModel.PLAYER_NONE) {
			return 0;
		}
		int opponent;		//establishing other player
		if (player == IModel.PLAYER_WHITE) {
			opponent = IModel.PLAYER_BLACK;
		} else {
			opponent = IModel.PLAYER_WHITE;
		}
		int captured = 0;		//move is valid if capture >0
		
		for (int dx=-1; dx<=1; dx++) {		//8 directions
			for(int dy =-1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) {		//no movement skip
					continue;
				}
				int cx = x + dx;	//current x and y position moved by direction
				int cy = y + dy;
				
				if(!inBound(cx, cy)) {		// skip if direction is off the board or if its not a components piece
					continue;
				}
				if (model.getBoardContents(cx, cy) != opponent) {
					continue;
				}
				
				int countDirection = 1;
				
				while (true) {
					cx += dx;		//moving further
					cy += dy;
					if(!inBound(cx, cy)) { 	//off board- don't get capture
						countDirection = 0;
						break;
					}
					int piece = model.getBoardContents(cx, cy);	//at opposite piece
					if (piece == opponent) {
						countDirection++;
					} else if (piece == player) {		//at own piece
						captured += countDirection;
						break;
					} else {			//empty
						countDirection = 0;
						break;
					}
				}	
			}
		}
		return captured;	
	}
	private void doCapture(int player, int x, int y) {
		model.setBoardContents(x,y,player);
		
		int opponent;		//establishing other player
		if (player == IModel.PLAYER_WHITE) {
			opponent = IModel.PLAYER_BLACK;
		} else {
			opponent = IModel.PLAYER_WHITE;
		}
		
		for (int dx=-1; dx<=1; dx++) {		//8 directions
			for(int dy =-1; dy <= 1; dy++) {
				
				if (dx == 0 && dy == 0) {		//no movement skip
					continue;
				}
				int cx = x + dx;	//current x and y position moved by direction
				int cy = y + dy;
				
				if(!inBound(cx, cy)) {		// skip if direction is off the board or if its not a components piece
					continue;
				}
				if (model.getBoardContents(cx, cy) != opponent) {
					continue;
				}
				int countDirection = 1;		//got first opponent piece
				
				while (true) {
					cx += dx;		//moving further(finding 'sandwitch')
					cy += dy;
					if(!inBound(cx, cy)) { 	//off board- don't get capture
						countDirection = 0;
						break;
					}
					int piece = model.getBoardContents(cx, cy);	//at opposite piece
					if (piece == opponent) {
						countDirection++;
					} else if (piece == player) {		//at own piece
						break;			//valid capture
					} else {			//empty
						countDirection = 0;
						break;
					}
				}
				if (countDirection == 0) {		//no flipping this dir.
					continue;
				}
				
				int flipx = x + dx;
				int flipy = y + dy;
				//flip pieces in this direction
				for(int i = 0; i < countDirection; i++) {
					model.setBoardContents(flipx, flipy, player);
					flipx += dx;
					flipy += dy;
				}
			}
		}
	}
	private boolean inBound(int x, int y) {
		if (x < 0) return false;
		if (y < 0) return false;
		if (x>=model.getBoardWidth()) return false;
		if (y>=model.getBoardHeight()) return false;
		return true;
	}
	//end of helper functions
	
	@Override
	public void doAutomatedMove(int player) {
		if(model.hasFinished()) {
			return;
		}
		
		//check turn
		if (player != model.getPlayer()) {
			view.feedbackToUser(player, "It is not your turn.");
			return;
		}
		
		int maxX = -1;
		int maxY = -1;
		int maxCount = 0;
		
		for(int row = 0; row < model.getBoardHeight(); row++) {
			for(int col=0; col < model.getBoardWidth(); col++) {
				 int count = captureCount(player, col, row);
				 if(count > maxCount) {
					 maxCount = count;
					 maxX = col;
					 maxY = row;
				 }
			}
		}
		// no valid moves=skip
		if(maxCount == 0) {
			view.feedbackToUser(player, "No valid location to play in. Turn skipped.");
			
			//change player
			if(player == IModel.PLAYER_WHITE) {
				model.setPlayer(IModel.PLAYER_BLACK);
			} else {
				model.setPlayer(IModel.PLAYER_WHITE);
			}
			
			//check if valid moves for next player
			int nextPlayer = model.getPlayer();
			boolean nextPlayerMove = false;
			
			for(int row = 0; row < model.getBoardHeight(); row++) {
				for(int col=0; col < model.getBoardWidth(); col++) {
					if (captureCount(nextPlayer, col, row) > 0) {
						nextPlayerMove = true;
						break;
					}
				}
				if(nextPlayerMove) {
					break;
				}
			}
			if (!nextPlayerMove) {
				view.feedbackToUser(IModel.PLAYER_WHITE, "Game over. No value moves for each.");
				view.feedbackToUser(IModel.PLAYER_BLACK, "Game over. No value moves for each.");
				model.setFinished(true);
			}
			update();
			return;
		}
		//apply the best move
		doCapture(player, maxX, maxY);
		//switch
		if(player == IModel.PLAYER_WHITE) {
			model.setPlayer(IModel.PLAYER_BLACK);
		} else {
			model.setPlayer(IModel.PLAYER_WHITE);
		}
		update();
	}
}