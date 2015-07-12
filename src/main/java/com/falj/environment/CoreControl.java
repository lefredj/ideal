/**
* CoreControl.java
* Core control
* @version0.1
* @see Environment
* @author Frederic JEAN
* @copyright (C) Frederic JEAN 2015
* @date 01/07/2015
* @notes  you can redistribute it and/or modify it under the terms of the 
* Licence Creative Commons Attribution 4.0 International.
* http://creativecommons.org/licenses/by/4.0/
*/

package com.falj.environment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class CoreControl {

	public static class Grid extends JPanel {

		private static final long serialVersionUID = 1L;
		private List<Point> fillCells;
		private List<Point> fillCells2;

		private int[] xPoints;
		private int[] yPoints;

		private Environment environment;

		private int sizeOfCell=20;

		private int sizeX;
		private int sizeY;

		JTextArea text;
		
		public Grid() throws InterruptedException, ClassNotFoundException, IOException {
			environment = new Environment(this);
			fillCells = new ArrayList<Point>(25);
			fillCells2 = new ArrayList<Point>(25);
			sizeX = sizeOfCell * environment.grid.length;
			sizeY = sizeOfCell * environment.grid[0].length;
			
			for (int i = 0; i < environment.grid.length; i++) {
				for (int j = 0; j < environment.grid[i].length; j++) {
					if( environment.grid[i][j] ) {
						fillCell(i, j);
					}	
					if( environment.gridSeen[i][j] == -1 ) {
						fillCell2(i, j);
					}
				}
			}

			drawAgent(environment.agentPosition);

			text = new JTextArea("");
			text.setEditable(false);
			text.setBounds( 300, 300, 20, 20);

			this.add(text);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for (Point fillCell : fillCells) {
				int cellX = sizeOfCell + (fillCell.x * sizeOfCell);
				int cellY = sizeOfCell + (fillCell.y * sizeOfCell);
				g.setColor(Color.RED);
				g.fillRect(cellX, cellY, sizeOfCell, sizeOfCell);
			}
			fillCells2.clear();
			for (int i = 0; i < environment.grid.length; i++) {
				for (int j = 0; j < environment.grid[i].length; j++) {
					if( environment.gridSeen[i][j] == -1 ) {
						fillCell2(i, j);
					}
				}
			}
			for (Point fillCell : fillCells2) {
				int cellX = sizeOfCell + (fillCell.x * sizeOfCell);
				int cellY = sizeOfCell + (fillCell.y * sizeOfCell);
				g.setColor(Color.GRAY);
				g.fillRect(cellX, cellY, sizeOfCell, sizeOfCell);
			}
			fillCells2.clear();
			
			Point feelCell = environment.getFeelCell();
			if(feelCell != null) {
				int cellX = sizeOfCell + (feelCell.x * sizeOfCell);
				int cellY = sizeOfCell + (feelCell.y * sizeOfCell);
				g.setColor(Color.GREEN);
				g.fillRect(cellX, cellY, sizeOfCell, sizeOfCell);
			}
			
			
			drawAgent(environment.agentPosition);

			g.setColor(Color.GREEN);
			g.fillPolygon(xPoints, yPoints, 3);

			text.repaint();
		}

		public void fillCell(int x, int y) {
			fillCells.add(new Point(x, y));
			repaint();
		}
		
		public void fillCell2(int x, int y) {
			fillCells2.add(new Point(x, y));
			repaint();
		}

		public void drawAgent(Integer[] agentPosition) {
			xPoints = new int[3];
			yPoints = new int[3];
			if( agentPosition[2] == 2 ) {
				xPoints[0] = (int) ((agentPosition[0]+1) * sizeOfCell + 2);
				yPoints[0] = (int) ((agentPosition[1]+2) * (sizeOfCell) - 2);

				xPoints[1] = (int) ((agentPosition[0]+2) * (sizeOfCell) - 2);
				yPoints[1] = (int) ((agentPosition[1]+2) * (sizeOfCell) - 2);

				xPoints[2] = (int) ((agentPosition[0]+1.5) * (sizeOfCell));
				yPoints[2] = (int) ((agentPosition[1]+1) * (sizeOfCell) + 2);
			}

			if( agentPosition[2] == 3 ) {
				yPoints[0] = (int) ((agentPosition[1]+1) * sizeOfCell + 2);
				xPoints[0] = (int) ((agentPosition[0]+2) * (sizeOfCell) - 2);

				yPoints[1] = (int) ((agentPosition[1]+2) * (sizeOfCell) - 2);
				xPoints[1] = (int) ((agentPosition[0]+2) * (sizeOfCell) - 2);

				yPoints[2] = (int) ((agentPosition[1]+1.5) * (sizeOfCell));
				xPoints[2] = (int) ((agentPosition[0]+1) * (sizeOfCell) + 2);
			}

			if( agentPosition[2] == 0 ) {
				xPoints[0] = (int) ((agentPosition[0]+1) * sizeOfCell + 2);
				yPoints[0] = (int) ((agentPosition[1]+1) * sizeOfCell + 2);

				xPoints[1] = (int) ((agentPosition[0]+2) * (sizeOfCell) - 2);
				yPoints[1] = (int) ((agentPosition[1]+1) * sizeOfCell + 2);

				xPoints[2] = (int) ((agentPosition[0]+1.5) * (sizeOfCell));
				yPoints[2] = (int) ((agentPosition[1]+2) * (sizeOfCell) - 2);
			}
			if( agentPosition[2] == 1 ) {
				yPoints[0] = (int) ((agentPosition[1]+1) * sizeOfCell + 2);
				xPoints[0] = (int) ((agentPosition[0]+1) * sizeOfCell + 2);

				yPoints[1] = (int) ((agentPosition[1]+2) * (sizeOfCell) - 2);
				xPoints[1] = (int) ((agentPosition[0]+1) * sizeOfCell + 2);

				yPoints[2] = (int) ((agentPosition[1]+1.5) * (sizeOfCell));
				xPoints[2] = (int) ((agentPosition[0]+2) * (sizeOfCell) - 2);
			}
			        	repaint();
		}


	}

	public static void main(String[] a) throws ClassNotFoundException, IOException {

		Grid grid;
		try {
			grid = new Grid();
			JFrame window = new JFrame();
			window.setSize(840, 840);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.add(grid);
			
			window.setVisible(true);
			Thread.sleep(1000);

			while(true){
				grid.environment.step();
				Thread.sleep(100);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}