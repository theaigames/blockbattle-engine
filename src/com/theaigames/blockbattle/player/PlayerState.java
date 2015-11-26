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

package com.theaigames.blockbattle.player;

import com.theaigames.blockbattle.field.ShapeType;
import com.theaigames.blockbattle.moves.Move;

public class PlayerState {
	
	private final Move move;
	private final int combo;
	private final int skips;
	private final int points;
	private final ShapeType nextShape;
	private final String fieldString;
	private final int round;
	
	public PlayerState(int roundNumber, Move move, int combo, int skips, int points, ShapeType nextShape, String fieldString) {
		this.round = roundNumber;
		this.move = move;
		this.combo = combo;
		this.skips = skips;
		this.points = points;
		this.nextShape = nextShape;
		this.fieldString = fieldString;
	}
	
	public int getRound() {
		return this.round;
	}
	
	public Move getMove(){
		return this.move;
	}
	
	public String getMoveString() {
		if(this.move == null)
			return "";
		return this.move.toString();
	}
	
	public int getCombo() {
		return this.combo;
	}
	
	public int getSkips() {
		return this.skips;
	}
	
	public int getPoints() {
		return this.points;
	}
	
	public ShapeType getNextShape() {
		return this.nextShape;
	}
	
	public String getFieldString() {
		return this.fieldString;
	}
}