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
