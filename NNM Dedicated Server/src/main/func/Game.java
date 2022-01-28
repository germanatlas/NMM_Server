package main.func;

import java.util.ArrayList;
import java.util.Random;

import main.Manager;
import main.online.ClientManager;
import main.online.packs.DataPackage;
import main.state.GameState;
import main.state.Location;

public class Game {
	
	/*
	 * TODO
	 * 
	 */
	
	private Manager man;
	private ClientManager c[] = new ClientManager[2];
	private GameState STATE, tmpSTATE;
	private Random rdm;
	private DataPackage dp, dp2, dpdupe, dp2dupe;
	
	private int field[][];
	private int pc[];

	private ArrayList<String> repetitiveField;
	
	private int count,
				roundsWithoutMill,
				repetition;
	
	private boolean color, activeUser,
					lastMill,
					newboard;
	
	
	public Game(Manager manager, ClientManager c1, ClientManager c2) {
		
		this.man = manager;
		c[0] = c1;
		c[1] = c2;
		reset();
		while(true) {
			tick();
		}
		
	}

	public void tick() {
		
		try {
			
			if(STATE != GameState.WIN) {
				dp = (DataPackage) c[(activeUser?0:1)].receiveData();
			} else {
				dp = (DataPackage) c[0].receiveData();
				dp2 = (DataPackage) c[1].receiveData();
				
				if(dp != null) dpdupe = dp;
				if(dp2 != null) dp2dupe = dp2;
			}
			
		} catch(ClassCastException e) {
			
			return;
			
		}
		
		if(dp == null && dpdupe == null) {
			
			return;
		}
		
		if(STATE == GameState.WIN) {
			
			if(dp2dupe == null) {
				return;
			}
			
			if(dpdupe.getStatus() == 98 && dp2dupe.getStatus() == 98) {
				
				reset();
				return;
				
			} else if(dpdupe.getStatus() == 99 || dp2dupe.getStatus() == 99) {
				
				c[0].sendData(new DataPackage(99, 0, 0, 0, 0));
				c[1].sendData(new DataPackage(99, 0, 0, 0, 0));
				STATE = GameState.END;
				
			}
			return;
			
		}
		
		if(dp.getStatus() == 99) {
			
			c[0].setLocation(Location.LOBBY);
			c[1].setLocation(Location.LOBBY);
			STATE = GameState.END;
			return;
			
		}
		
		checkGameState();
		
		if(STATE == GameState.PLACE) {
			
			if(field[dp.getToX()][dp.getToY()] == 0) {
				
				field[dp.getToX()][dp.getToY()] = (activeUser?1:2);
				if(checkMill(dp.getToX(), dp.getToY())) {
					
					tmpSTATE = STATE;
					STATE = GameState.MILL;
					c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, 0, 0, dp.getToX(), dp.getToY()));
					c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, 0, 0, dp.getToX(), dp.getToY()));
					
					
				} else {

					count++;
					if(activeUser == lastMill) roundsWithoutMill++;
					checkGameState();
					c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, 0, 0, dp.getToX(), dp.getToY()));
					c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, 0, 0, dp.getToX(), dp.getToY()));
					activeUser = !activeUser;
					
					
				}
				
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
				newboard = false;
				
			}
			
		} else
		
		if(STATE == GameState.MOVE) {
			
			if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
				field[dp.getToX()][dp.getToY()] == 0) {
				
				if(isNeighbor(dp.getFromX(), dp.getFromY(), dp.getToX(),dp.getToY())) {
					
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
					
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;
						STATE = GameState.MILL;
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						
						
					} else {

						if(activeUser == lastMill) roundsWithoutMill++;
						checkGameState();
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
						
						
					}
					
				} else {
					
					c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
					newboard = false;
					
				}
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
				newboard = false;
				
			}
			
		} else
		
		if(STATE == GameState.JUMPONE || STATE == GameState.JUMPTWO || STATE == GameState.JUMPBOTH) {
			
			if(STATE == GameState.JUMPONE) {
				
				if(activeUser) { // User with 3 Stones jumps
					
					if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
						field[dp.getToX()][dp.getToY()] == 0) {
						
						field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
						field[dp.getFromX()][dp.getFromY()] = 0;
							
						if(checkMill(dp.getToX(), dp.getToY())) {

							tmpSTATE = STATE;	
							STATE = GameState.MILL;
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							
								
						} else {

							if(activeUser == lastMill) roundsWithoutMill++;
							checkGameState();
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							activeUser = !activeUser;
							
								
						}
					
					} else {
						
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
						newboard = false;
						
					}
						
				} else { //Other user moves normally
						
					if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
						field[dp.getToX()][dp.getToY()] == 0) {
							
						if(isNeighbor(dp.getFromX(), dp.getFromY(), dp.getToX(),dp.getToY())) {
								
							field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
							field[dp.getFromX()][dp.getFromY()] = 0;
								
							if(checkMill(dp.getToX(), dp.getToY())) {

								tmpSTATE = STATE;
								STATE = GameState.MILL;
								c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								
								
							} else {

								if(activeUser == lastMill) roundsWithoutMill++;
								checkGameState();
								c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								activeUser = !activeUser;
								
									
							}
								
						} else {
							
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
							newboard = false;
							
						}
							
					} else {
							
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
						newboard = false;
							
					}
						
				}
				
			} else if(STATE == GameState.JUMPTWO) {
				
				if(!activeUser) { //User with 3 Stones jumps
					
					if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
						field[dp.getToX()][dp.getToY()] == 0) {
						
						field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
						field[dp.getFromX()][dp.getFromY()] = 0;
							
						if(checkMill(dp.getToX(), dp.getToY())) {

							tmpSTATE = STATE;	
							STATE = GameState.MILL;
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							
								
						} else {

							if(activeUser == lastMill) roundsWithoutMill++;
							checkGameState();
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							activeUser = !activeUser;
							
								
						}
					
					} else {
						
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
						newboard = false;
						
					}
						
				} else { // Other user moves normally
						
					if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
						field[dp.getToX()][dp.getToY()] == 0) {
							
						if(isNeighbor(dp.getFromX(), dp.getFromY(), dp.getToX(),dp.getToY())) {
								
							field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
							field[dp.getFromX()][dp.getFromY()] = 0;
								
							if(checkMill(dp.getToX(), dp.getToY())) {

								tmpSTATE = STATE;
								STATE = GameState.MILL;
								c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								
								
							} else {
								
								checkGameState();
								c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								activeUser = !activeUser;
								
									
							}
								
						} else {
							
							c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
							newboard = false;
						}
							
					} else {

						c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
						newboard = false;
							
					}
						
				}
				
			} else { // Both players are in Jump phase
				
				if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
					field[dp.getToX()][dp.getToY()] == 0) {
							
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
							
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;		
						STATE = GameState.MILL;
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						
								
					} else {
								
						c[(activeUser?0:1)].sendData(new DataPackage(GameState.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
						
								
					}
							
				} else {
							
					c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
					newboard = false;
							
				}
				
			}
			
		} else
		
		if(STATE == GameState.MILL) {
			
			if(field[dp.getToX()][dp.getToY()] == (!activeUser?1:2)) {

				field[dp.getToX()][dp.getToY()] = 0;
				pc[(!activeUser?0:1)]--;
				lastMill = activeUser;
				roundsWithoutMill = 0;
				checkForWin();
				if(STATE != GameState.WIN) {
					
					c[(activeUser?0:1)].sendData(new DataPackage(GameState.MILL.id, 0, 0, dp.getToX(), dp.getToY()));
					c[(!activeUser?0:1)].sendData(new DataPackage(GameState.MILL.id, 0, 0, dp.getToX(), dp.getToY()));
					
					
				}
				
				count++;
				activeUser = !activeUser;
				STATE = tmpSTATE;
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(GameState.NOTALLOWED.id, 0, 0, 0, 0));
				newboard = false;
			}
			
		}
		
	}

	private boolean isNeighbor(int fromX, int fromY, int toX, int toY) {
		
		int[][] n = getNeighbors(fromX, fromY);
		
		for(int i = 0; i < n.length; i++) {
			
			if(n[i][0] == toX && n[i][1] == toY) {
				
				return true;
				
			}
			
		}
		
		return false;
	}

	private void checkForWin() {
		
		if(!checkForAllowedMoves()) {
			
			STATE = GameState.WIN;
			
		} else if(pc[(!activeUser?0:1)] < 3){
			
			c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			STATE = GameState.WIN;
			
		} else if(pc[(activeUser?0:1)] < 3) {

			c[(!activeUser?0:1)].sendData(new DataPackage(GameState.YOUWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(activeUser?0:1)].sendData(new DataPackage(GameState.NMYWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			STATE = GameState.WIN;
			
		}
		
	}

	private boolean checkForAllowedMoves() {
		
		if(count < 17 || (STATE.name().contains("JUMP") || STATE == GameState.MILL)) {
			
			return true;
			
		}
		boolean f1 = false, f2 = false;
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				
				if(field[i][j] != 0) {
					
					int[][] n = getNeighbors(i, j);
					
					for(int a = 0; a < n.length; a++) {
						
						int x = n[a][0], y = n[a][1];
						if(field[x][y] == 0) {
							
							if(field[i][j] == (activeUser?1:2)) {
								f1 = true;
							} else {
								f2 = true;
							}
							
						}
						
					}
					
				}
				
			}
		}
		
		if(f1 && f2) {
			return true;
		} else if(f1) {
			c[(!activeUser?0:1)].sendData(new DataPackage(GameState.NMYWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(activeUser?0:1)].sendData(new DataPackage(GameState.YOUWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			STATE = GameState.WIN;
		}else if(f2) {
			c[(activeUser?0:1)].sendData(new DataPackage(GameState.NMYWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(!activeUser?0:1)].sendData(new DataPackage(GameState.YOUWIN.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			STATE = GameState.WIN;
		}
		
		return false;
	}

	private void checkForStalemate() {
		
		if(newboard && (STATE != GameState.MILL)) {
			
			if(checkForRepetition()) {
				repetition++;
			} else {
				repetition = 0;
			}
			
		}
		newboard = true;
		
		if(roundsWithoutMill == 20) {
			
			STATE = GameState.STALEMATE;
			c[(activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
		
		}
			
		
		if(repetition/2 == 3) {
			
			STATE = GameState.STALEMATE;
			c[(activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			c[(activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
			
		}
		
	}

	private boolean checkForRepetition() {
		
		String s = "";
		
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				
				s += field[i][j];
				
			}
			
		}
		
		if(repetitiveField.contains(s)) {
			return true;
		}
		
		repetitiveField.add(s);
		return false;
	}

	//checks general game state
	private void checkGameState() {
		
		if(STATE == GameState.WIN) {
			return;
		}
		
		if(STATE != GameState.MILL) {
			
			if(pc[0] > 3 && pc[1] > 3) {
				
				if(count < 17) {
					
					STATE = GameState.PLACE;
					
				} else {
					
					STATE = GameState.MOVE;
					
				}
				
			} else if(pc[0] == 3 && pc[1] == 3) {
				
				STATE = GameState.JUMPBOTH;
				
			} else if(pc[0] == 3) {
				
				STATE = GameState.JUMPONE;
				
			} else if(pc[1] == 3) {
				
				STATE = GameState.JUMPTWO;
				
			}
			
		}
		
		checkForWin();
		checkForStalemate();
		
	}

	private void printField() {
		
		String msg = "\n";
		
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				
				msg += field[j][i] + " ";
				
			}
			
			msg += "\n";
		}
		
		man.print(msg);
		
	}

	private boolean checkMill(int x, int y) {
		
		int n[][] = getNeighbors(x, y);
		for(int i = 0; i < n.length; i++) {
			
			int nX = n[i][0], nY = n[i][1]; 
			if(field[nX][nY] == field[x][y]) {
				
				if(getThirdInRow(nX,nY,x,y) == field[x][y]) {
					
					return true;
					
				}
				
			}
			
		}
		
		return false;
		
	}

	private int getThirdInRow(int x1, int y1, int x2, int y2) {
		
		String[] lines = {	"00,03,06","00,30,60",
				"11,13,15","11,31,51",
				"22,23,24","22,32,42",
				"30,31,32","03,13,23",
				"34,35,36","43,53,63",
				"42,43,44","24,34,44",
				"51,53,55","15,35,55",
				"60,63,66","06,36,66"
		};
		
		for (int i = 0; i < lines.length; i++) {
		
			if (lines[i].contains(x1 + "" + y1) && lines[i].contains(x2 + "" + y2)) {
			
				int id1 = -1, id2 = -1;
				String [] tempS = lines[i].split(",");
				
				for (int j = 0; j < tempS.length; j++) {
				
					if(tempS[j].contains(x1 + "" +y1)) {
						id1 = j;
					}
					else if(tempS[j].contains(x2 + "" + y2)) { 
						id2 = j;
					}
				}
				
				for (int j = 0; j < tempS.length; j++) {
				
					if(!(j == id1 || j == id2)) {
	
						String[] tempSM = tempS[j].split("");
						return field[Integer.parseInt(tempSM[0])][Integer.parseInt(tempSM[1])];
					
					}
				}
			}
		}
		
		return 0;
		
	}
	

	private int[][] getNeighbors(int x, int y) {
		
		int[][] tmp = new int[4][2];
		int index = 0, n = -1;
		
		String[] lines = {	"00,03,06","00,30,60",
				"11,13,15","11,31,51",
				"22,23,24","22,32,42",
				"30,31,32","03,13,23",
				"34,35,36","43,53,63",
				"42,43,44","24,34,44",
				"51,53,55","15,35,55",
				"60,63,66","06,36,66"
		};
		
		for(int i = 0; i < lines.length; i++) {
			
			if(lines[i].contains(x + "" + y) && n != i) {
				n = i;
				String[] t = lines[i].split(",");
				
				for(int j = 0; j < t.length; j++) {
					
					if(t[j].contains(x + "" + y)) {
						
						
						if(j == 0 || j == 2) {
							
							tmp[index][0] = Integer.parseInt(t[1].split("")[0]);
							tmp[index][1] = Integer.parseInt(t[1].split("")[1]);
							index++;
							
						} else {
							
							tmp[index][0] = Integer.parseInt(t[0].split("")[0]);
							tmp[index][1] = Integer.parseInt(t[0].split("")[1]);
							index++;
							tmp[index][0] = Integer.parseInt(t[2].split("")[0]);
							tmp[index][1] = Integer.parseInt(t[2].split("")[1]);
							index++;
							
						}
						
						
					}
					
				}
				
				
			}
			
		}
		
		int[][] ret = new int[index][2];
		
		for(int i = 0; i < index; i++) {
			ret[i] = tmp[i];
		}
		
		return ret;
	}

	private void reset() {

		STATE = GameState.NEW;
		rdm = new Random();
		color = rdm.nextBoolean();
		activeUser = rdm.nextBoolean();
		/*
		 * Color:
		 *  0 - White
		 *  1 - Black
		 * activeUser
		 *  0 - enemy starts
		 *  1 - you start
		 * 
		 */
		c[0].sendData(new DataPackage(GameState.NEW.id, (color?0:1), (activeUser?0:1), 0, 0));
		c[1].sendData(new DataPackage(GameState.NEW.id, (!color?0:1), (!activeUser?0:1), 0, 0));
		pc = new int[2];
		pc[0] = 9;
		pc[1] = 9;
		count = -1;
		repetition = 0;
		field = emptyField();
		repetitiveField = new ArrayList<String>();
		roundsWithoutMill = 0;
		STATE = GameState.PLACE;
		
		
	}

	private int[][] emptyField() {
		int f[][] = new int[7][7];
		for(int i = 0; i < 7; i++)
			for(int j = 0; j < 7; j++)
				f[i][j] = 0;
		
		return f;
	}
	
	public GameState getState() {
		
		return STATE;
		
	}
	
	public ClientManager getClient(int i) {
		if(i >= 0 && i <= 1) {
			return c[i];
		}
		return null;
	}

}
