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

import com.theaigames.blockbattle.moves.Move;
import com.theaigames.blockbattle.moves.MoveType;

public class Shape {

	public ShapeType type;
	private Cell[][] shape;
	private int size;
	private Point location;
	private Cell[] blocks;
	private Field field;
	private boolean isFrozen;
	
	public Shape(ShapeType type, Field field) {
		this.type = type;
		this.field = field;
		this.blocks = new Cell[4];
		this.isFrozen = false;
		
		setShape();
	}
	
	// used for cloning
	public Shape(ShapeType type, int size, Field field, Point location, Cell[][] shape, Cell[] blocks, boolean isFrozen) {
		this.type = type;
		this.size = size;
		this.field = field;
		this.location = location;
		this.shape = shape;
		this.blocks = blocks;
		this.isFrozen = isFrozen;
	}
	
	public Shape clone() {
		Shape clone;
		
		if(this.location == null)
			clone = new Shape(this.type, this.field);
		else {
			Cell[][] shapeClone = new Cell[size][size];
			Cell[] blocksClone = new Cell[4];
			int blockNr = 0;
			for(int y=0; y < size; y++) {
				for(int x=0; x < size; x++) {
					shapeClone[x][y] = shape[x][y].clone();
					if(shapeClone[x][y].isShape()) {
						blocksClone[blockNr] = shapeClone[x][y];
						blockNr++;
					}
				}
			}
			clone = new Shape(this.type, this.size, this.field, (Point) this.location.clone(), shapeClone, blocksClone, isFrozen);
		}
		
		return clone;
	}
	
	// spawns the shape
	public boolean spawnShape() {
		int x = (field.getWidth() - this.size) / 2;
		int y = -1;
		
		this.location = new Point(x, y);
		setBlockLocations();
		
		if(hasCollision())
			return false;
		
		setShapeInField();
		return true;
	}

	////// Turn actions /////
	
	public String turnLeft() {
		Shape clone = this.clone();
		
		Cell[][] temp = clone.transposeShape();
		for(int y=0; y < size; y++) {
			for(int x=0; x < size; x++) {
				clone.shape[x][y] = temp[x][size - y - 1];
			}
		}
		
		clone.setBlockLocations();
		String error = clone.checkForPositionErrors("turnleft");
		
		if(error.isEmpty())
			takePosition(clone);
		
		return error;
	}
	
	public String turnRight() {
		Shape clone = this.clone();
		
		Cell[][] temp = clone.transposeShape();
		for(int x=0; x < size; x++) {
			clone.shape[x] = temp[size - x - 1];
		}
		
		clone.setBlockLocations();
		String error = clone.checkForPositionErrors("turnright");
		
		if(error.isEmpty()) {
			takePosition(clone);
		}
		
		return error;
	}
	
	private Cell[][] transposeShape() {
		Cell[][] temp = new Cell[size][size];
		for(int y=0; y < size; y++) {
			for(int x=0; x < size; x++) {
				temp[y][x] = shape[x][y];
			}
		}
		return temp;
	}
	
	///////////////////////////
	
	///// Shift actions /////
	
	public String oneDown() {
		Shape clone = this.clone();
		
		clone.location.y++;
		clone.setBlockLocations();
		
		if(clone.isBelowBottom() || clone.hasCollision())
			freezeInField();
		else
			takePosition(clone);
		
		return ""; // can't return an error
	}
	
	public String oneLeft() {
		Shape clone = this.clone();
		
		clone.location.x--;
		clone.setBlockLocations();
		String error = clone.checkForPositionErrors("left");
		
		if(error.isEmpty())
			takePosition(clone);
		else
			oneDown();
		
		return error;
	}
	
	public String oneRight() {
		Shape clone = this.clone();
		
		clone.location.x++;
		clone.setBlockLocations();
		String error = clone.checkForPositionErrors("right");
		
		if(error.isEmpty())
			takePosition(clone);
		else
			oneDown();
		
		return error;
	}
	
	public String drop() {
		while(!isFrozen) {
			oneDown();
		}
		return ""; // can't return an error
	}
	
	//////////// skip action /////////
	
	public void skip() {
		
		for(int i=0; i < blocks.length; i++) {
			blocks[i].setEmpty();
			field.setEmpty(blocks[i].getLocation());
		}
		
		isFrozen = true;
	}
	
	////////////////////////////////////
	
	private void takePosition(Shape shape) {		
		this.shape = shape.shape;
		this.blocks = shape.blocks;
		this.location = shape.location;
		
		field.cleanField();
		setShapeInField();
	}
	
	//// Position checks /////
	
	public boolean hasCollision() {
		for(int i=0; i < blocks.length; i++) {
			if(blocks[i].hasCollision(field))
				return true;
		}
		return false;
	}
	
	public boolean isBelowBottom() {
		for(int i=0; i < blocks.length; i++) {
			if(blocks[i].isBelowBottom(field))
				return true;
		}
		return false;
	}
	
	public boolean isOutOfBoundaries() {
		for(int i=0; i < blocks.length; i++) {
			if(blocks[i].isOutOfBoundaries(field))
				return true;
		}
		return false;
	}
	
	public boolean isOverflowing() {
		for(int i=0; i < blocks.length; i++) {
			if(!blocks[i].isEmpty() && blocks[i].isOverFlowing())
				return true;
		}
		return false;
	}
	
	private String checkForPositionErrors(String move) {
		String error = String.format("Can't perform %s: ", move);
		
		if(move.equals("left") || move.equals("right")) {
			
			if(isOutOfBoundaries())
				return error += "Piece is on the field boundary. Action changed to 'down'.";
			if(hasCollision())
				return error += "Other blocks are in the way. Action changed to 'down'.";
			
		} else if(move.equals("turnright") || move.equals("turnleft")) {
			
			if(isOutOfBoundaries() || isBelowBottom())
				return error += "Piece would move out of bounds. Move skipped.";
			if(hasCollision())
				return error += "Other blocks are in the way. Move skipped.";
			
		}
		return "";
	}
	
	public boolean checkTSpin(Move lastMove1, Move lastMove2, Point lastLocation) {
		if(this.type != ShapeType.T)
			return false;

		if(lastMove1 == null || lastMove2 == null)
			return false;
		
		// last move is turn or second to last move is turn
		if(!(lastMove1.getType() == MoveType.TURNRIGHT || lastMove1.getType() == MoveType.TURNLEFT 
				|| ((lastMove1.getType() == MoveType.DOWN || lastMove1.getType() == MoveType.DROP)
				&& (lastMove2.getType() == MoveType.TURNLEFT || lastMove2.getType() == MoveType.TURNRIGHT)
				&& (lastLocation.equals(this.location)))))
			return false;

//		// check if the T is 'locked' i.e. it cannot shift out of position
//		Shape clone;
//		
//		// check right
//		clone = this.clone();
//		clone.location.x++;
//		clone.setBlockLocations();
//		if(!clone.isOutOfBoundaries() && !clone.hasCollision())
//			return false;
//		
//		// check left
//		clone = this.clone();
//		clone.location.x--;
//		clone.setBlockLocations();
//		if(!clone.isOutOfBoundaries() && !clone.hasCollision())
//			return false;
//		
//		// check up
//		clone = this.clone();
//		clone.location.y--;
//		clone.setBlockLocations();
//		if(!clone.hasCollision())
//			return false;
		
		// check if 3/4 corners of the matrix are Blocks in the field
		Cell[] corners = new Cell[4];
		corners[0] = this.field.getCell(new Point(this.location.x, this.location.y));
		corners[1] = this.field.getCell(new Point(this.location.x + 2, this.location.y));
		corners[2] = this.field.getCell(new Point(this.location.x, this.location.y + 2));
		corners[3] = this.field.getCell(new Point(this.location.x + 2, this.location.y + 2));
		
		int counter = 0;
		for(int i = 0; i < corners.length; i++)
			if(corners[i] != null && corners[i].isBlock())
				counter++;
		
		if(counter == 3)
			return true;
		
		return false;
	}
	
	/////////////////////////////

	private void setBlockLocations() {
		for(int y=0; y < size; y++) {
			for(int x=0; x < size; x++) {
				if(shape[x][y].isShape()) {
					shape[x][y].setLocation(location.x + x, location.y + y);
				}
			}
		}
	}
	
	private void freezeInField() {
		for(int i=0; i < blocks.length; i++) {
			field.setBlock(blocks[i].getLocation(), this.type);
		}
		isFrozen = true;
	}
	
	private void setShapeInField() {
		for(int i=0; i < blocks.length; i++) {
			field.setShape(blocks[i].getLocation(), this.type);
		}
	}
	
	// set shape in square box
	private void setShape() {
		switch(this.type) {
			case I:
				this.size = 4;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[0][1];
				this.blocks[1] = this.shape[1][1];
				this.blocks[2] = this.shape[2][1];
				this.blocks[3] = this.shape[3][1];
				break;
			case J:
				this.size = 3;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[0][0];
				this.blocks[1] = this.shape[0][1];
				this.blocks[2] = this.shape[1][1];
				this.blocks[3] = this.shape[2][1];
				break;
			case L:
				this.size = 3;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[2][0];
				this.blocks[1] = this.shape[0][1];
				this.blocks[2] = this.shape[1][1];
				this.blocks[3] = this.shape[2][1];
				break;
			case O:
				this.size = 2;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[0][0];
				this.blocks[1] = this.shape[1][0];
				this.blocks[2] = this.shape[0][1];
				this.blocks[3] = this.shape[1][1];
				break;
			case S:
				this.size = 3;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[1][0];
				this.blocks[1] = this.shape[2][0];
				this.blocks[2] = this.shape[0][1];
				this.blocks[3] = this.shape[1][1];
				break;
			case T:
				this.size = 3;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[1][0];
				this.blocks[1] = this.shape[0][1];
				this.blocks[2] = this.shape[1][1];
				this.blocks[3] = this.shape[2][1];
				break;
			case Z:
				this.size = 3;
				this.shape = initializeShape();
				this.blocks[0] = this.shape[0][0];
				this.blocks[1] = this.shape[1][0];
				this.blocks[2] = this.shape[1][1];
				this.blocks[3] = this.shape[2][1];
				break;
			default:
				break;
		}
		
		// set type to SHAPE
		for(int i=0; i < blocks.length; i++) {
			this.blocks[i].setShape();
		}
	}
	
	private Cell[][] initializeShape() {
		Cell[][] newShape = new Cell[size][size];
		for(int y=0; y < size; y++) {
			for(int x=0; x < size; x++) {
				newShape[x][y] = new Cell(this.type);
			}
		}
		return newShape;
	}
	
	public String getPositionString() {
		return location.x + "," + location.y;
	}
	
	public ShapeType getType() {
		return this.type;
	}
	
	public boolean isFrozen() {
		return this.isFrozen;
	}
	
	public Point getLocation() {
		return this.location;
	}
	
//	public String toString() {
//		StringBuffer output = new StringBuffer();
//		for(int i=0; i < blocks.length; i++) {
//			output.append(blocks[i].getLocation().x + "," + blocks[i].getLocation().y + " ");
//		}
//		return output.toString();
//	}
}
