/**
 * Environment.java
 * Structure of the environment
 * @version0.1
 * @see EnvironmentInterface
 * @see Agent
 * @author Frederic JEAN
 * @copyright (C) Frederic JEAN 2015
 * @date 01/07/2015
 * @notes  you can redistribute it and/or modify it under the terms of the 
 * Licence Creative Commons Attribution 4.0 International.
 * http://creativecommons.org/licenses/by/4.0/
 */

package com.falj.environment;

import java.awt.Point;
import java.io.IOException;
import java.util.HashSet;

import com.falj.agent.Agent;
import com.falj.agent.Interaction;
import com.falj.environment.CoreControl.Grid;


public class Environment extends EnvironmentInterface {

	public Grid guiGrid;

	public boolean[][] grid;

	public int[][] gridSeen;

	// x, y, orientation
	public Integer[] agentPosition;

	// bumps
	public int fwd = 0;
	public int bumps = 0;
	public int actions = 0;
	public int score = 0;

	// r,l,front
	public int[] neighbourStatus;

	private double scoreBump = -10L;
	private double scoreFeel = -1L;
	private double scoreTurn = -1L;
	private double scoreFwd = 10L;
	private double scoreFwdSeen = 5L;

	private int seen = -1;

	private Point feelCell;

	private Point bumpCell;

	private String description;

	public Environment(Grid gui) throws ClassNotFoundException, IOException {

		guiGrid = gui;

		// simple environment (no seen status for cells
		seen = 0;

		// simple loop environment
//		setSimpleEnvironment();

		//setAllSeen();

		// random environment
		setRandomEnvironment(10, 0.25);
		// empty environment
		//setEmptyEnvironment();

		setInitialInteractions();

		neighbourStatus = new int[3];

		agent = new Agent(this,"000");

	}

	private void setAllSeen() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				gridSeen[i][j] = grid[i][j]?1:seen;
			}
		}		
	}

	private void setSimpleEnvironment() {
		grid = new boolean[7][6];		
		gridSeen = new int[7][6];

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				grid[i][j] = true;
			}
		}
		grid[1][1] = false;
		grid[2][1] = false;
		grid[3][1] = false;
		grid[4][1] = false;
		grid[1][2] = false;
		grid[4][2] = false;
		grid[5][2] = false;
		grid[1][3] = false;
		grid[5][3] = false;
		grid[1][4] = false;
		grid[2][4] = false;
		grid[3][4] = false;
		grid[4][4] = false;
		grid[5][4] = false;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				gridSeen[i][j] = grid[i][j]?1:0;
			}
		}
		// x,y,orientation
		agentPosition = new Integer[3];

		agentPosition[0] = 1;
		agentPosition[1] = 4;
		agentPosition[2] = 0;
		gridSeen[agentPosition[0]][agentPosition[1]] = seen;
	}

	private void setRandomEnvironment(int size, double fillPct) {
		grid = new boolean[size][size];
		gridSeen = new int[size][size];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				if((( i == 0 ) || (j == 0)) || (( i == grid.length-1 ) || (j == grid[i].length-1))) {
					grid[i][j] = true;
				} else {
					grid[i][j] = Math.random() > (1-fillPct);
				}
				gridSeen[i][j] = grid[i][j]?1:0;
			}
		}

		// x,y,orientation
		agentPosition = new Integer[3];
		int center = (int) size/2;
		grid[center][center] = false;
		gridSeen[center][center] = seen;
		agentPosition[0] = center;
		agentPosition[1] = center;
		agentPosition[2] = 0;


	}
	private void setEmptyEnvironment() {
		grid = new boolean[25][25];
		gridSeen = new int[25][25];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				if((( i == 0 ) || (j == 0)) || (( i == grid.length-1 ) || (j == grid[i].length-1))) {
					grid[i][j] = true;
				} else {
					grid[i][j] = false;
				}
				gridSeen[i][j] = grid[i][j]?1:0;
			}
		}

		// x,y,orientation
		agentPosition = new Integer[3];
		grid[13][13] = false;
		gridSeen[13][13] = -1;
		agentPosition[0] = 13;
		agentPosition[1] = 13;
		agentPosition[2] = 0;
		setInitialInteractions();

	}


	public void setInitialInteractions() {

		interactions = new HashSet<Interaction>();
		// set the possible interaction = (observation,action) / reaction
		// set the valences of interaction
		// turn right
		interactions.add(new Interaction("TR",scoreTurn));
		// turn left
		interactions.add(new Interaction("TL",scoreTurn));
		// feel right empty
		interactions.add(new Interaction("FRe",scoreFeel));
		// feel left empty
		interactions.add(new Interaction("FLe",scoreFeel));
		// feel Fwd empty
		interactions.add(new Interaction("FFe",scoreFeel));
		// feel right full
		//interactions.add(new Interaction("FRf",scoreFeel));
		// feel left full
		//interactions.add(new Interaction("FLf",scoreFeel));
		// feel Fwd full
		//interactions.add(new Interaction("FFf",scoreFeel));

		// forward empty
		interactions.add(new Interaction("FwdOk",scoreFwd));
		// forward bump
		//interactions.add(new Interaction("FwdBump",scoreBump));

	}


	public Interaction enact(String a) {
		// returns the environment response to an action
		Interaction result = new Interaction();
		getNeighboursStatus();
		feelCell = null;
		bumpCell = null;
		
		// turns
		if( a.compareTo("TR") == 0 ) {
			agentPosition[2] = (agentPosition[2]-1);
			if( agentPosition[2] < 0 ) {
				agentPosition[2] += 4;
			}
			result.setHash("TR");
			result.setValence(scoreTurn);
			score += scoreTurn;

		}
		if( a.compareTo("TL") == 0 ) {
			agentPosition[2] = (agentPosition[2]+1);
			if( agentPosition[2] > 3 ) {
				agentPosition[2] -= 4;
			}
			result.setHash("TL");
			result.setValence(scoreTurn);
			score += scoreTurn;
		}


		// feels
		if((( a.compareTo("FRe")  == 0 ) || ( a.compareTo("FRf")  == 0 )) || ( a.compareTo("FRs")  == 0 )) {

			if( neighbourStatus[0] == 1) {
				result.setHash("FRf");
			} else {
				if( neighbourStatus[0] == -1) {
					result.setHash("FRs");
				} else {
					result.setHash("FRe");
				}
			}
			feelCell = setFeelOrBumpCell(0);
			result.setValence(scoreFeel);
			score += scoreFeel;
		}

		if((( a.compareTo("FLe")  == 0 ) || ( a.compareTo("FLf")  == 0 )) || ( a.compareTo("FLs")  == 0 )) {

			if( neighbourStatus[1] == 1 ) {
				result.setHash("FLf");
			} else {
				if( neighbourStatus[1] == -1) {
					result.setHash("FLs");
				} else {
					result.setHash("FLe");
				}
			}
			feelCell = setFeelOrBumpCell(1);
			result.setValence(scoreFeel);
			score += scoreFeel;
		}


		if((( a.compareTo("FFe")  == 0 ) || ( a.compareTo("FFf")  == 0 )) || ( a.compareTo("FFs")  == 0 )) {

			if( neighbourStatus[2] == 1 ) {
				result.setHash("FFf");
			} else {
				if( neighbourStatus[2] == -1) {
					result.setHash("FFs");
				} else {
					result.setHash("FFe");
				}
			}
			feelCell = setFeelOrBumpCell(2);
			result.setValence(scoreFeel);
			score += scoreFeel;
		}

		// Fwd
		if((( a.compareTo("FwdOk")  == 0 ) || ( a.compareTo("FwdBump")  == 0 )) || ( a.compareTo("FwdSeen")  == 0 )) {
			if( neighbourStatus[2] == 1 ) {
				bumpCell = setFeelOrBumpCell(2);
				result.setHash("FwdBump");
				result.setValence(scoreBump);
				score += scoreBump;
			} else {
				if(agentPosition[2] == 0) {
					agentPosition[1] ++;
				}
				if(agentPosition[2] == 1) {
					agentPosition[0] ++;
				}
				if(agentPosition[2] == 2) {
					agentPosition[1] --;
				}
				if(agentPosition[2] == 3) {
					agentPosition[0] --;
				}
				if( neighbourStatus[2] == -1 ) {
					result.setHash("FwdSeen");
					result.setValence(scoreFwdSeen);
					score += scoreFwdSeen ;

				} else {
					result.setHash("FwdOk");
					result.setValence(scoreFwd);
					gridSeen[agentPosition[0]][agentPosition[1]] = seen;
					score += scoreFwd ;
				}
			}
		}

		actions++;
		if(result.getHash().compareTo("FwdBump") == 0) {
			bumps ++;
		}
		if(result.getHash().compareTo("FwdOk") == 0) {
			fwd ++;
		}
		guiGrid.text.setText("actions : " + actions + "\n" +
				"bumps : " + bumps + " | " + ((float)bumps/(float)actions)*100 + "%\n" + 
				"fwdOk : " + fwd + " | " + ((float)fwd/(float)actions)*100 + "%\n"+ 
				"score : " + score  + " | " + ((float)score/(float)actions) + "\n"
				+ description);

		guiGrid.repaint();
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public void step() {

		description = agent.step();
		//System.out.println(agentPosition[0]+"," + agentPosition[1] + "," + agentPosition[2]);
	}

	public void getNeighboursStatus() {
		if(agentPosition[2] == 0) {
			neighbourStatus[0] = gridSeen[agentPosition[0]-1][agentPosition[1]];
			neighbourStatus[1] = gridSeen[agentPosition[0]+1][agentPosition[1]];
			neighbourStatus[2] = gridSeen[agentPosition[0]][agentPosition[1]+1];
		}

		if(agentPosition[2] == 1) {
			neighbourStatus[0] = gridSeen[agentPosition[0]][agentPosition[1]+1];
			neighbourStatus[1] = gridSeen[agentPosition[0]][agentPosition[1]-1];
			neighbourStatus[2] = gridSeen[agentPosition[0]+1][agentPosition[1]];
		}
		if(agentPosition[2] == 2) {
			neighbourStatus[0] = gridSeen[agentPosition[0]+1][agentPosition[1]];
			neighbourStatus[1] = gridSeen[agentPosition[0]-1][agentPosition[1]];
			neighbourStatus[2] = gridSeen[agentPosition[0]][agentPosition[1]-1];
		}
		if(agentPosition[2] == 3) {
			neighbourStatus[0] = gridSeen[agentPosition[0]][agentPosition[1]-1];
			neighbourStatus[1] = gridSeen[agentPosition[0]][agentPosition[1]+1];
			neighbourStatus[2] = gridSeen[agentPosition[0]-1][agentPosition[1]];
		}

	}

	public Point setFeelOrBumpCell(int direction ) {
		Point cell = new Point();
		if(agentPosition[2] == 0) {
			if( direction == 0 ) {
				cell.x = agentPosition[0]-1;
				cell.y = agentPosition[1];
			}
			if( direction == 1 ) {
				cell.x = agentPosition[0]+1;
				cell.y = agentPosition[1];
			}
			if( direction == 2 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]+1;
			}
		}

		if(agentPosition[2] == 1) {
			if( direction == 0 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]+1;
			}
			if( direction == 1 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]-1;
			}
			if( direction == 2 ) {
				cell.x = agentPosition[0]+1;
				cell.y = agentPosition[1];
			}
		}
		if(agentPosition[2] == 2) {
			if( direction == 0 ) {
				cell.x = agentPosition[0]+1;
				cell.y = agentPosition[1];
			}
			if( direction == 1 ) {
				cell.x = agentPosition[0]-1;
				cell.y = agentPosition[1];
			}
			if( direction == 2 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]-1;
			}
		}
		if(agentPosition[2] == 3) {
			if( direction == 0 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]-1;
			}
			if( direction == 1 ) {
				cell.x = agentPosition[0];
				cell.y = agentPosition[1]+1;
			}
			if( direction == 2 ) {
				cell.x = agentPosition[0]-1;
				cell.y = agentPosition[1];
			}
		}
		return cell;
	}

	
	public Point getFeelCell() {
		return feelCell;
	}

	public Point getBumpCell() {
		return bumpCell;
	}
}