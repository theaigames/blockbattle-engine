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
