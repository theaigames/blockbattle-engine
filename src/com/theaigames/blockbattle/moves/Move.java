package com.theaigames.blockbattle.moves;

import com.theaigames.game.moves.AbstractMove;
import com.theaigames.game.player.AbstractPlayer;

public class Move extends AbstractMove {
	
	private MoveType type;

	public Move(AbstractPlayer player, MoveType type) {
		super(player);
		this.type = type;
	}

	public MoveType getType() {
		return this.type;
	}
	
	public String toString() {
		if(!super.getIllegalMove().isEmpty())
			return super.getIllegalMove();
		return type.toString();
	}
}
