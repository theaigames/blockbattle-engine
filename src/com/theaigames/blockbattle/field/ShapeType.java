package com.theaigames.blockbattle.field;

import java.security.SecureRandom;

public enum ShapeType {
	I, J, L, O, S, T, Z, NONE, G; // G is garbage
	
	private static final ShapeType[] VALUES = ShapeType.values();
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final int SIZE_SHAPES = VALUES.length - 2;
	
	/**
	 * Gets a random ShapeType, NONE and G not included
	 * @return
	 */
	public static ShapeType getRandom() {
		return VALUES[RANDOM.nextInt(SIZE_SHAPES)];
	}
}
