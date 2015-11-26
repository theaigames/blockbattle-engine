package com.theaigames.blockbattle.field;

import java.awt.Point;

public class Cell {
	
	private Point location;
	private CellType state;
	private ShapeType shape;

	public Cell(int x, int y) {
		this.location = new Point(x, y);
		this.state = CellType.EMPTY;
		this.shape = ShapeType.NONE;
	}
	
	public Cell(ShapeType type) {
		this.location = null;
		this.state = CellType.EMPTY;
		this.shape = type;
	}
	
	public Cell(Point location, CellType state, ShapeType shape) {
		this.location = location;
		this.state = state;
		this.shape = shape;
	}
	
	public Cell clone() {
		if(location != null)
			return new Cell((Point) this.location.clone(), this.state, this.shape);
		return new Cell(null, this.state, this.shape);
	}
	
	public boolean isOutOfBoundaries(Field f) {
		if(this.location.x >= f.getWidth() || this.location.x < 0) 
			return true;
		return false;
	}
	
	public boolean isOverFlowing() {
		if(this.location.y < 0)
			return true;
		return false;
	}
	
	public boolean isBelowBottom(Field f) {
		if(this.location.y >= f.getHeight())
			return true;
		return false;
	}
	
	public boolean hasCollision(Field f) {
		Cell cell = f.getCell(this.location);
		if(cell == null)
			return false;
		return (this.state == CellType.SHAPE && (cell.isSolid() || cell.isBlock()));
	}
	
	public void setLocation(int x, int y) {
		if(this.location == null)
			this.location = new Point();
		
		this.location.setLocation(x, y);
	}
	
	public void setShapeType(ShapeType shape) {
		this.shape = shape;
	}
	
	public void setShape() {
		this.state = CellType.SHAPE;
	}
	
	public void setBlock() {
		this.state = CellType.BLOCK;
	}
	
	public void setSolid() {
		this.state = CellType.SOLID;
		this.shape = ShapeType.NONE;
	}
	
	public void setEmpty() {
		this.state = CellType.EMPTY;
		this.shape = ShapeType.NONE;
	}
	
	public boolean isShape() {
		return this.state == CellType.SHAPE;
	}
	
	public boolean isBlock() {
		return this.state == CellType.BLOCK;
	}
	
	public boolean isSolid() {
		return this.state == CellType.SOLID;
	}
	
	public boolean isEmpty() {
		return this.state == CellType.EMPTY;
	}
	
	public Point getLocation() {
		return this.location;
	}

	public CellType getState() {
		return this.state;
	}
	
	public ShapeType getShapeType() {
		return this.shape;
	}
}
