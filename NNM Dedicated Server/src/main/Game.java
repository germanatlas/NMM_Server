package main;

import java.util.ArrayList;
import java.util.Random;

import main.online.ClientManager;
import main.online.DataPackage;
import main.state.State;

public class Game {
	
	/*
	 * TODO
	 * FIX JUMP
	 * FIX PLACING MILL
	 * CHECK END STATE
	 * ASK FOR NEW GAME
	 * LEAVING
	 * 
	 * 
	 */
	
	private Manager man;
	private ClientManager c[] = new ClientManager[2];
	private State STATE, tmpSTATE;
	private Random rdm;
	
	private int field[][];
	private int pc[];

	private ArrayList<String> repetitiveField;
	
	private int count,
				roundsWithoutMill,
				repetition;
	
	private boolean color, activeUser,
					endStatusSent,
					equal;
	
	
	public Game(Manager manager) {
		
		this.man = manager;
		c[0] = man.getClient(0);
		c[1] = man.getClient(1);
		reset();
		
	}

	public void tick() {
		
		DataPackage dp = c[(activeUser?0:1)].receiveData();
		
		if(dp == null) {
			
			return;
			
		}
		
		checkGameState();
		
		if(STATE == State.STALEMATE) {
			
			
			
		} else
		
		if(STATE == State.PLACE) {
			
			if(field[dp.getToX()][dp.getToY()] == 0) {
				
				field[dp.getToX()][dp.getToY()] = (activeUser?1:2);
				if(checkMill(dp.getToX(), dp.getToY())) {
					
					tmpSTATE = STATE;
					STATE = State.MILL;
					c[(activeUser?0:1)].sendData(new DataPackage(State.YOUMILL.id, 0, 0, 0, 0));
					c[(!activeUser?0:1)].sendData(new DataPackage(State.NMYMILL.id, 0, 0, dp.getToX(), dp.getToY()));
					
				} else {

					count++; man.print(count + "");
					checkGameState();
					c[(activeUser?0:1)].sendData(new DataPackage(State.ALLOWED.id, 0, 0, dp.getToX(), dp.getToY()));
					c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, 0, 0, dp.getToX(), dp.getToY()));
					activeUser = !activeUser;
					
				}
				
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
				
			}
			
		} else
		
		if(STATE == State.MOVE) {
			
			if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
				field[dp.getToX()][dp.getToY()] == 0) {
				
				if(isNeighbor(dp.getFromX(), dp.getFromY(), dp.getToX(),dp.getToY())) {
					
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
					
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;
						STATE = State.MILL;
						c[(activeUser?0:1)].sendData(new DataPackage(State.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(State.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						
					} else {
						
						checkGameState();
						c[(activeUser?0:1)].sendData(new DataPackage(State.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
						
					}
					
				}
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
				
			}
			
		} else
		
		if(STATE == State.JUMPONE || STATE == State.JUMPTWO || STATE == State.JUMPBOTH) {
			
			if(STATE == State.JUMPONE) {
				
				if(	(!equal && activeUser) &&
					field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
					field[dp.getToX()][dp.getToY()] == 0) {
						
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
						
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;	
						STATE = State.MILL;
						c[(activeUser?0:1)].sendData(new DataPackage(State.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(State.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							
					} else {
							
						c[(activeUser?0:1)].sendData(new DataPackage(State.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
							
					}
						
				} else {
						
					c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
						
				}
				
			} else if(STATE == State.JUMPTWO) {
				
				if(	(equal && activeUser) &&
					field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
					field[dp.getToX()][dp.getToY()] == 0) {
						
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
						
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;	
						STATE = State.MILL;
						c[(activeUser?0:1)].sendData(new DataPackage(State.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(State.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
							
					} else {
							
						c[(activeUser?0:1)].sendData(new DataPackage(State.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
							
					}
						
				} else {
						
					c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
						
				}
				
			} else {
				
				if(	field[dp.getFromX()][dp.getFromY()] == (activeUser?1:2) &&
					field[dp.getToX()][dp.getToY()] == 0) {
							
					field[dp.getToX()][dp.getToY()] = field[dp.getFromX()][dp.getFromY()];
					field[dp.getFromX()][dp.getFromY()] = 0;
							
					if(checkMill(dp.getToX(), dp.getToY())) {

						tmpSTATE = STATE;		
						STATE = State.MILL;
						pc[(!activeUser?0:1)]--;
						c[(activeUser?0:1)].sendData(new DataPackage(State.YOUMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(State.NMYMILL.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
								
					} else {
								
						c[(activeUser?0:1)].sendData(new DataPackage(State.ALLOWED.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						c[(!activeUser?0:1)].sendData(new DataPackage(STATE.id, dp.getFromX(), dp.getFromY(), dp.getToX(), dp.getToY()));
						activeUser = !activeUser;
								
					}
							
				} else {
							
					c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
							
				}
				
			}
			
		} else
		
		if(STATE == State.MILL) {
			
			if(field[dp.getToX()][dp.getToY()] == (!activeUser?1:2)) {

				field[dp.getToX()][dp.getToY()] = 0;
				c[(activeUser?0:1)].sendData(new DataPackage(State.MILL.id, 0, 0, dp.getToX(), dp.getToY()));
				c[(!activeUser?0:1)].sendData(new DataPackage(State.MILL.id, 0, 0, dp.getToX(), dp.getToY()));
				activeUser = !activeUser;
				STATE = tmpSTATE;
				
			} else {
				
				c[(activeUser?0:1)].sendData(new DataPackage(State.NOTALLOWED.id, 0, 0, 0, 0));
				
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
		// TODO Auto-generated method stub
		
	}

	private void checkForStalemate() {
		// TODO Auto-generated method stub
		
	}

	private void checkGameState() {
		
		if(STATE != State.MILL) {
			
			if(pc[0] > 3 && pc[1] > 3) {
				
				if(count < 17) {
					
					STATE = State.PLACE;
					
				} else {
					
					STATE = State.MOVE;
					
				}
				
			} else if(pc[0] == 3 && pc[1] == 3) {
				
				STATE = State.JUMPBOTH;
				
			} else if(pc[0] == 3) {
				
				STATE = State.JUMPONE;
				
			} else if(pc[1] == 3) {
				
				STATE = State.JUMPTWO;
				
			}
			
		}
		
		printField();

		checkForStalemate();
		
	}

	private void printField() {
		
		String msg = "\n";
		
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 7; j++) {
				
				msg += field[i][j] + " ";
				
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

		STATE = State.NEW;
		rdm = new Random();
		endStatusSent = false;
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
		equal = activeUser;
		c[0].sendData(new DataPackage(State.NEW.id, (color?0:1), (activeUser?0:1), 0, 0));
		c[1].sendData(new DataPackage(State.NEW.id, (!color?0:1), (!activeUser?0:1), 0, 0));
		pc = new int[2];
		pc[0] = 9;
		pc[1] = 9;
		count = -1;
		repetition = 0;
		field = emptyField();
		repetitiveField = new ArrayList<String>();
		roundsWithoutMill = 0;
		STATE = State.PLACE;
		
		
	}

	private int[][] emptyField() {
		int f[][] = new int[7][7];
		for(int i = 0; i < 7; i++)
			for(int j = 0; j < 7; j++)
				f[i][j] = 0;
		
		return f;
	}
	
	public State getState() {
		
		return STATE;
		
	}

}
