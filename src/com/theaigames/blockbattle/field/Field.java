// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package com.theaigames.blockbattle.field;

import java.awt.Point;
import java.security.SecureRandom;

public class Field {

	private int width;
	private int height;
	private int solidRows;
	public Cell grid[][];
	private static final SecureRandom RANDOM = new SecureRandom();
	
	public Field(int width, int height) {
		
		this.height = height;
		this.width = width;
		this.solidRows = 0;
		grid = new Cell[width][height];
		
		for(int y=0; y < height; y++) {
			for(int x=0; x < width; x++) {
				grid[x][y] = new Cell(x, y);
			}
		}
	}
	
	// moves the whole field upwards to make room for new
	// lines from the bottom, returns true if game over.
	// leaves the bottom rows untouched (are changed in the appropriate
	// methods).
	public boolean moveFieldUp(int amount) {
		if(amount <= 0)
			return false;
		
		boolean gameOver = false;
		
		for(int y = 0; y < height + amount - solidRows; y++) {
			for(int x = 0; x < width; x++) {

				int newY = y - amount;
				
				if(newY < 0 && !grid[x][y].isEmpty()) {
					gameOver = true;
				}
				
				if(newY >= 0 && y < height) { // move cells up
					grid[x][newY] = grid[x][y].clone();
					grid[x][newY].setLocation(x, newY);
				}
			}
		}
		
		return gameOver;
	}
	
	// adds solid rows to bottom, returns true if game over
	public boolean addSolidRows(int amount) {
		boolean gameOver = moveFieldUp(amount);
		
		// make the bottom rows into solid rows
		for(int y = height - solidRows - 1; y > height - solidRows - amount - 1; y--) {
			for(int x = 0; x < width; x++) {
				grid[x][y].setSolid();
			}
		}
		
		this.solidRows += amount;
		
		return gameOver;
	}
	
	public boolean addGarbageLines(int amount, boolean firstIsSingle) {
		boolean gameOver = moveFieldUp(amount);
//		ArrayList<Integer> exclude = new ArrayList<Integer>();
		int count = 0;
		
		// make the bottom rows into garbage lines
		for(int y = height - solidRows - 1; y > height - solidRows - amount - 1; y--) {

//			// add random hole, and make sure it is not on the same column
//			// for multiple garbage lines
//			int emptyCellIndex = RANDOM.nextInt(width - exclude.size());
//			for(int ex : exclude) {
//				if(emptyCellIndex < ex)
//					break;
//				emptyCellIndex++;
//			}
//			exclude.add(emptyCellIndex);
//			Collections.sort(exclude);
//
//			for(int x = 0; x < width; x++) {
//				if(x == emptyCellIndex)
//					grid[x][y].setEmpty();
//				else {
//					grid[x][y].setBlock();
//					grid[x][y].setShapeType(ShapeType.G);
//				}
//			}
			
			// switch between single and double holes in garbage lines
			count++;
			
			for(int x = 0; x < width; x++) {
				grid[x][y].setBlock();
				grid[x][y].setShapeType(ShapeType.G);
			}
			
			int index1 = RANDOM.nextInt(width);
			grid[index1][y].setEmpty();
			
			if ((count % 2 == 1 && !firstIsSingle) || (count % 2 == 0 && firstIsSingle)) { // double hole
				int rotate = 1 + RANDOM.nextInt(width - 1);
				int index2 = (index1 + rotate) % width;
				grid[index2][y].setEmpty();
			}
		}
		
		return gameOver;
	}
	
	// handles round end by clean up and checking for full rows
	// returns number of full rows
	public int processEndOfRoundField() {
		int rowsRemoved = 0;
		for(int y=0; y < height; y++) {
			boolean fullRow = true;
			
			for(int x=0; x < width; x++) { 
				if(grid[x][y].isShape())
					grid[x][y].setEmpty();
				if(!grid[x][y].isBlock()) // check if line contains only block cells
					fullRow = false;
			}
			
			if(fullRow) { // move cells down one line
				for(int oy = y - 1; oy >= 0; oy--) {
					for(int x=0; x < width; x++) {
						grid[x][oy + 1] = grid[x][oy].clone(); 
					}
				}
				for(int x=0; x < width; x++) {
					grid[x][0].setEmpty();
				}
				rowsRemoved++;
			}
		}
		return rowsRemoved;
	}
	
	// checks whether the whole field is empty
	public boolean isFieldCleared() {	
		for(int y=0; y < height; y++)
			for(int x=0; x < width; x++)
				if(grid[x][y].isBlock())
					return false;
		return true;
	}
	
	// removes shape cells from field
	public void cleanField() {
		for(int y=0; y < height; y++) {
			for(int x=0; x < width; x++) { 
				if(grid[x][y].isShape())
					grid[x][y].setEmpty();
			}
		}
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public Cell getCell(Point p) {
		if(p.x < 0 || p.x >= width || p.y < 0 || p.y >= height)
			return null;
		return grid[p.x][p.y];
	}
	
	public void setShape(Point p, ShapeType shape) {
		Cell cell = getCell(p);
		if(cell != null && cell.isEmpty()) {
			cell.setShape();
			cell.setShapeType(shape);
		}
	}
	
	public void setBlock(Point p, ShapeType shape) {
		Cell cell = getCell(p);
		if(cell != null) {
			if(!cell.isSolid() && !cell.isBlock()) {
				cell.setBlock();
				cell.setShapeType(shape);
			} else {
				System.err.printf("Can't set block here. (%s %s)\n", p.toString(), shape);
			}
		}
	}
	
	public void setEmpty(Point p) {
		Cell cell = getCell(p);
		if(cell != null)
			cell.setEmpty();
	}
	
	/**
	 * Returns string representation of the current field
	 * pretty for printing in console
	 */
	public String toString(boolean pretty, boolean forVisualizer) {
		StringBuffer output = new StringBuffer();
		
		String rowConnector = ",";
		String collumnConnector = ";";
		if(pretty) {
			rowConnector = " ";
			collumnConnector = "\n";
		}
		
		String collumnJoin = "";
		for(int y=0; y < height; y++) {
			
			output.append(collumnJoin);
			
			String rowJoin = "";
			for(int x=0; x < width; x++) {
				output.append(rowJoin);
				String cellState = grid[x][y].getState().getCode() + "";
				
				if(forVisualizer && grid[x][y].getShapeType() != ShapeType.NONE) {
					cellState = grid[x][y].getShapeType().toString();
				}
				
				output.append(cellState);
				rowJoin = rowConnector;
			}
			
			collumnJoin = collumnConnector;
		}
		
		return output.toString();
	}
}
