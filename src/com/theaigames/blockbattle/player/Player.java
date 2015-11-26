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

import java.util.ArrayList;
import java.util.HashMap;

import com.theaigames.blockbattle.field.Field;
import com.theaigames.blockbattle.field.Shape;
import com.theaigames.blockbattle.field.ShapeType;
import com.theaigames.blockbattle.moves.Move;
import com.theaigames.engine.io.IOPlayer;
import com.theaigames.game.player.AbstractPlayer;

public class Player extends AbstractPlayer {
	
	private Field field;
	private Shape currentShape;
	private ArrayList<Move> roundMoves;
	private boolean performedTSpin;
	private boolean fieldCleared;
	private boolean usedSkip;
	private int rowPoints;
	private int combo;
	private int rowsRemoved;
	private int skips;
	private HashMap<Integer, ArrayList<PlayerState>> playedGame;
	private Player opponent;

	public Player(String name, IOPlayer bot, long maxTimeBank, long timePerMove, Field field) {
		super(name, bot, maxTimeBank, timePerMove);
		this.field = field;
		this.rowPoints = 0;
		this.combo = 0;
		this.skips = 0;
		this.playedGame = new HashMap<Integer, ArrayList<PlayerState>>();
		this.performedTSpin = false;
		this.fieldCleared = false;
		this.usedSkip = false;
	}
	
	public void addPlayerState(int round, Move move, ShapeType nextShape) {
		PlayerState moveResult = new PlayerState(round, move, this.combo, this.skips, this.rowPoints, nextShape, this.field.toString(false, true));
		
		if(round >= this.playedGame.size()) 
			this.playedGame.put(round, new ArrayList<PlayerState>());
		
		this.playedGame.get(round).add(moveResult);
	}
	
	public void setOpponent(ArrayList<Player> players) {
		for(Player player : players) {
			if(!player.equals(this)) {
				this.opponent = player;
				break;
			}
		}
	}
	
	public void setTSpin(boolean performedTSpin) {
		this.performedTSpin = performedTSpin;
	}
	
	public void setFieldCleared(boolean isFieldCleared) {
		this.fieldCleared = isFieldCleared;
	}
	
	public Player getOpponent() {
		return this.opponent;
	}

	public Field getField() {
		return this.field;
	}
	
	public void setCurrentShape(Shape shape) {
		this.currentShape = shape;
	}
	
	public Shape getCurrentShape() {
		return this.currentShape;
	}
	
	public void setRoundMoves(ArrayList<Move> moves) {
		this.roundMoves = moves;
	}
	
	public ArrayList<Move> getRoundMoves() {
		return this.roundMoves;
	}
	
	public void addRowPoints(int points) {
		this.rowPoints += points;
	}
	
	public boolean getTSpin() {
		return this.performedTSpin;
	}
	
	public boolean getFieldCleared() {
		return this.fieldCleared;
	}
	public int getRowPoints() {
		return this.rowPoints;
	}
	
	public void setCombo(int combo) {
		this.combo = combo;
	}
	
	public int getCombo() {
		return this.combo;
	}
	
	public void setSkips(int skips) {
		this.skips = skips;
	}
	
	public int getSkips() {
		return this.skips;
	}
	
	public void setUsedSkip(boolean usedSkip) {
		this.usedSkip = usedSkip;
	}
	
	public boolean getUsedSkip() {
		return this.usedSkip;
	}
	
	public void setRowsRemoved(int rowsRemoved) {
		this.rowsRemoved = rowsRemoved;
	}
	
	public int getRowsRemoved() {
		return this.rowsRemoved;
	}
	
	public HashMap<Integer, ArrayList<PlayerState>> getPlayedGame() {
		return this.playedGame;
	}
}
