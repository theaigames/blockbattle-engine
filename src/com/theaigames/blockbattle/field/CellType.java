package com.theaigames.blockbattle.field;

public enum CellType {
	EMPTY(0), 
	SHAPE(1),
	BLOCK(2),
	SOLID(3);
	
	private final int code;
	
	private CellType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}
}
