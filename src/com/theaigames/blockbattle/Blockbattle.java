package com.theaigames.blockbattle;

import java.util.ArrayList;
import java.util.List;

import com.theaigames.blockbattle.field.Field;
import com.theaigames.blockbattle.player.Player;
import com.theaigames.engine.io.IOPlayer;
import com.theaigames.game.AbstractGame;
import com.theaigames.game.player.AbstractPlayer;

public class Blockbattle extends AbstractGame {
	
	private final long TIMEBANK_MAX = 10000l;
	private final long TIME_PER_MOVE = 500l;
	private final int FIELD_WIDTH = 10;
	private final int FIELD_HEIGHT = 20;
	
	private List<Player> players;

	@Override
	public void setupGame(ArrayList<IOPlayer> ioPlayers) throws Exception {
		
		System.out.println("Setting up game...");
		
		// set the maximum number of rounds if necessary
		super.maxRounds = -1;
		
		// create all the players and everything they need
		this.players = new ArrayList<Player>();
		for(int i=0; i<ioPlayers.size(); i++) {
			
			// create the playing field
			Field field = new Field(FIELD_WIDTH, FIELD_HEIGHT);
			
			// create the player
			String playerName = String.format("player%d", i+1);
			Player player = new Player(playerName, ioPlayers.get(i), TIMEBANK_MAX, TIME_PER_MOVE, field);
			this.players.add(player);
		}
		
		// send the settings
		for(AbstractPlayer player : this.players)
			sendSettings(player);
		
		// create the processor
		super.processor = new Processor(this.players, FIELD_WIDTH, FIELD_HEIGHT);
	}

	@Override
	public void sendSettings(AbstractPlayer player) {
		
		// create player names string
		String playerNames = "";
		for(Player p : this.players) {
			playerNames += p.getName() + ",";
		}
		playerNames = playerNames.substring(0, playerNames.length()-1);
		
		// send the mandatory settings
		player.sendSetting("timebank",(int) TIMEBANK_MAX);
		player.sendSetting("time_per_move",(int) TIME_PER_MOVE);
		player.sendSetting("player_names", playerNames);
		player.sendSetting("your_bot", player.getName());
		
		// send the game specific settings
		player.sendSetting("field_width", FIELD_WIDTH);
		player.sendSetting("field_height", FIELD_HEIGHT);
	}

	@Override
	protected void runEngine() throws Exception {
		super.engine.setLogic(this);
		super.engine.start();
	}
	
	// DEV_MODE can be turned on to easily test the
	// engine from eclipse
	public static void main(String args[]) throws Exception
	{
		Blockbattle game = new Blockbattle();
		
		// DEV_MODE settings
		game.TEST_BOT = "java -cp /home/jim/workspace/jimBotTetris/bin/ bot.BotStarter";
		game.NUM_TEST_BOTS = 2;
		game.DEV_MODE = false;
		
		game.setupEngine(args);
		game.runEngine();
	}
}
