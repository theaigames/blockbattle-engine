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

package com.theaigames.blockbattle;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.theaigames.blockbattle.field.Shape;
import com.theaigames.blockbattle.field.ShapeType;
import com.theaigames.blockbattle.moves.Move;
import com.theaigames.blockbattle.moves.MoveType;
import com.theaigames.blockbattle.player.Player;
import com.theaigames.game.GameHandler;
import com.theaigames.game.player.AbstractPlayer;

public class Processor implements GameHandler {
	
	private ArrayList<Player> players;
	private int roundNumber;
	private AbstractPlayer winner;
	private boolean gameOver;
	private ShapeType nextShape;
	private int fieldWidth;
	private int fieldHeight;
	
	private final int MAX_MOVES = 40;
	private final int ROUNDS_PER_SOLID = 15;
	
	// points
	private final int POINTS_PER_GARBAGE = 3;
	private final int SINGLE_CLEAR_SCORE = 0;
	private final int DOUBLE_CLEAR_SCORE = 3;
	private final int TRIPLE_CLEAR_SCORE = 6;
	private final int QUAD_CLEAR_SCORE = 10;
	private final int SINGLE_T_SCORE = 5;
	private final int DOUBLE_T_SCORE = 10;
	private final int PERFECT_CLEAR_SCORE = 18;
	
	public Processor(List<Player> players, int fieldWidth, int fieldHeight) {
		this.players = (ArrayList<Player>) players;
		this.roundNumber = 0;
		this.winner = null;
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		
		setNextShape();
		
		// store game start and set opponent for player
		for(Player player : this.players) {
			storePlayerState(player, null);
			player.setOpponent(this.players);
		}
	}

	@Override
	public void playRound(int roundNumber) {
		
		System.out.println("playing round " + roundNumber);
		
		this.roundNumber = roundNumber;
		ShapeType nextShape = this.nextShape;
		
		//set shape for next round
		setNextShape();
		
		// spawn current shape
		for(Player player : this.players) {
			
			//create current shape
			Shape shape = new Shape(nextShape, player.getField());
			
			if(!shape.spawnShape())
				setWinner(player.getOpponent());
			
			player.setCurrentShape(shape);
			
			//first store start of round state
			storePlayerState(player, null);
		}
		
		if(this.gameOver) // game could be over after spawning of shape
			return;
		
		// send updates and ask for moves
		for(Player player : this.players) {
			sendRoundUpdatesToPlayer(player);
			
			ArrayList<Move> moves = parseMoves(player.requestMove("moves"), player);
			player.setRoundMoves(moves);
		}
		
		// execute all moves
		for(Player player : this.players) {
			executeMovesForPlayer(player);
		}
		
		// remove rows and store the amount removed
		for(Player player : this.players) {
			player.setRowsRemoved(player.getField().processEndOfRoundField());
			player.setFieldCleared(player.getField().isFieldCleared());
		}
		
		// handle everything that changes after the pieces have been placed
		for(Player player : this.players) {
			
			processPointsForPlayer(player);
			
			if(this.roundNumber % ROUNDS_PER_SOLID == 0) // add solid line on certain round number
				if(player.getField().addSolidRows(1)) // set winner if player is out of bounds
					setWinner(player.getOpponent());
		}
	}

	@Override
	public int getRoundNumber() {
		return roundNumber;
	}

	@Override
	public AbstractPlayer getWinner() {
		return winner;
	}
	
	@Override
	public boolean isGameOver() {
		return (gameOver || winner != null);
	}

	@Override
	public String getPlayedGame() {

		return "";
	}

	/**
	 * Sets the next shape to be played randomly
	 */
	private void setNextShape() {
		this.nextShape = ShapeType.getRandom();
	}
	
	/**
	 * Sends all updates the player needs at the start of the round.
	 * @param player : player to send the updates to
	 */
	private void sendRoundUpdatesToPlayer(Player player) {
		
		// game updates
		player.sendUpdate("round", roundNumber);
		player.sendUpdate("this_piece_type", player.getCurrentShape().getType().toString());
		player.sendUpdate("next_piece_type", nextShape.toString());
		player.sendUpdate("this_piece_position", player.getCurrentShape().getPositionString());
		
		// player updates
		player.sendUpdate("row_points", player, player.getRowPoints());
		player.sendUpdate("combo", player, player.getCombo());
		player.sendUpdate("skips", player, player.getSkips());
		player.sendUpdate("field", player, player.getField().toString(false, false));
		
		// opponent updates
		Player opponent = player.getOpponent();
		player.sendUpdate("field", opponent, opponent.getField().toString(false, false));
		player.sendUpdate("row_points", opponent, opponent.getRowPoints());
		player.sendUpdate("combo", opponent, opponent.getCombo());
		player.sendUpdate("skips", opponent, opponent.getSkips());
	}
	
	private ArrayList<Move> parseMoves(String input, Player player) {
		ArrayList<Move> moves = new ArrayList<Move>();
		String[] parts = input.split(",");
		
		for(int i=0; i < parts.length; i++) {
			if(i > MAX_MOVES) {
				player.getBot().outputEngineWarning(String.format("Maximum number of moves reached, only the first %s will be executed.", MAX_MOVES));
				break;
			}
			if(parts[i].isEmpty())
				break;
			
			Move move = parseMove(parts[i], player);
			if(move != null)
				moves.add(move);
		}
		
		return moves;
	}
	
	private Move parseMove(String input, Player player) {
		MoveType moveType = MoveType.fromString(input);
		
		if(moveType == null) {
			player.getBot().outputEngineWarning(String.format("Cannot parse input: %s", input));
			return null;
		}

		return new Move(player, moveType);
	}
	
	private void executeMovesForPlayer(Player player) {
		Shape shape = player.getCurrentShape();
		Move lastMove1 = null;
		Move lastMove2 = null;
		Point lastLocation = new Point(-1, -1);
		player.setUsedSkip(false);
		
		for(Move move : player.getRoundMoves()) {
			
			lastLocation = new Point(shape.getLocation().x, shape.getLocation().y);
			
			if(shape.isFrozen()) {
				player.getBot().outputEngineWarning("Piece was frozen in place on the previous move. Skipping all next moves.");
				break;
			}
			switch(move.getType()) {
				case LEFT:
					move.setIllegalMove(shape.oneLeft());
					break;
				case RIGHT:
					move.setIllegalMove(shape.oneRight());
					break;
				case TURNLEFT:
					move.setIllegalMove(shape.turnLeft());
					break;
				case TURNRIGHT:
					move.setIllegalMove(shape.turnRight());
					break;
				case DOWN:
					move.setIllegalMove(shape.oneDown());
					break;
				case DROP:
					move.setIllegalMove(shape.drop());
					break;
				case SKIP:
					if (player.getSkips() > 0) {
						shape.skip();
						player.setSkips(player.getSkips() - 1);
						player.setUsedSkip(true);
					}
					break;
			}
			
			// add a moveResult to the player's playedGame
			storePlayerState(player, move);
			
			lastMove2 = lastMove1;
			lastMove1 = move;
			
			if (move.getType() == MoveType.SKIP)
				break;
		}
		
		// freeze shape and add extra drop move if the piece is still loose in the field
		if(!shape.isFrozen()) {
			int initialY = shape.getLocation().y;
			shape.drop();
			int finalY = shape.getLocation().y;
			
			if(initialY != finalY) {
				String error = "The piece is still loose in the field. Dropping it.";
				if (lastMove1 != null && lastMove1.getType() == MoveType.SKIP)
					error = "Can't perform 'skip'. There were no skips available.";
				Move move = new Move(player, MoveType.DROP);
				move.setIllegalMove(error);
				
				storePlayerState(player, move);
				player.getBot().outputEngineWarning(error);
				player.setTSpin(false);
			} else {
				player.setTSpin(shape.checkTSpin(lastMove1, lastMove2, lastLocation));
			}
		} else {
			player.setTSpin(shape.checkTSpin(lastMove1, lastMove2, lastLocation));
		}
		
		if(shape.isOverflowing()) {
			setWinner(player.getOpponent());
		}
	}
	
	private void processPointsForPlayer(Player player) {
		
		int unusedRowPoints = player.getRowPoints() % POINTS_PER_GARBAGE;
		int rowsRemoved = player.getRowsRemoved();
		
		// calculate row points for this round
		int rowPoints;
		if(player.getTSpin()) { // T-spin clears
			switch(rowsRemoved) {
				case 2:
					rowPoints = this.DOUBLE_T_SCORE;
					player.setSkips(player.getSkips() + 1);
					break;
				case 1:
					rowPoints = this.SINGLE_T_SCORE;
					break;
				default:
					rowPoints = 0;
					break;
			}
		}
		else {
			switch(rowsRemoved) { // Normal clears
				case 4:
					rowPoints = this.QUAD_CLEAR_SCORE;
					player.setSkips(player.getSkips() + 1);
					break;
				case 3:
					rowPoints = this.TRIPLE_CLEAR_SCORE;
					break;
				case 2:
					rowPoints = this.DOUBLE_CLEAR_SCORE;
					break;
				case 1:
					rowPoints = this.SINGLE_CLEAR_SCORE;
					break;
				default:
					rowPoints = 0;
					break;
			}
		}
		
		// set new combo
		if(rowsRemoved > 1 || (rowsRemoved == 1 && player.getTSpin())) {
			rowPoints += player.getCombo(); // add combo points of previous round
			player.setCombo(player.getCombo() + 1);
		} else if(rowsRemoved < 1 && !player.getUsedSkip()) {
			player.setCombo(0);
		} else if (!player.getUsedSkip()) {
			rowPoints += player.getCombo(); // add combo points of previous round
		}
			
		// check if the whole field is cleared and reward points
		if(player.getFieldCleared())
			rowPoints = this.PERFECT_CLEAR_SCORE;
		
		player.addRowPoints(rowPoints);
		
		// add unused row points too from previous rounds
		rowPoints += unusedRowPoints;
		
		// calculate whether the first garbage line has single or double holes
		int totalNrLines = player.getRowPoints() / POINTS_PER_GARBAGE;
		boolean firstIsSingle = false;
		if (totalNrLines % 2 == 0) {
			firstIsSingle = true;
		}
		
		// add the solid rows to opponent and check for gameover
		if(player.getOpponent().getField().addGarbageLines(rowPoints / POINTS_PER_GARBAGE, firstIsSingle)) {
			setWinner(player);
		}
	}
	
	// stores everything needed in a state for the visualizer for given player
	private void storePlayerState(Player player, Move move) {
		player.addPlayerState(this.roundNumber, move, this.nextShape);
	}
	
	// if there was a winner already, set winner to null, so we know it's a draw
	private void setWinner(Player player) {
		this.gameOver = true;
		if(this.winner == null || this.winner == player)
			this.winner = player;
		else
			this.winner = null;
	}
}
