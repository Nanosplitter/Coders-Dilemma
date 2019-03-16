package com.codingame.game;
import java.util.List;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    // Uncomment the line below and comment the line under it to create a Solo Game
    // @Inject private SoloGameManager<Player> gameManager;
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;

    private Action player0LastAction;
    private Action player1LastAction;

    private int player0Score;
    private int player1Score;

    @Override
    public void init() {
        player0Score = 0;
        player1Score = 0;
    }
    
    
    private void drawHud(String action0, String action1) {
    	Text text0 = graphicEntityModule.createText(action0)
                .setX(0)
                .setY(120)
                .setZIndex(20)
                .setFontSize(40)
                .setFillColor(0xffffff)
                .setAnchor(0.5);
    	
    	Text text1 = graphicEntityModule.createText(action1)
                .setX(300)
                .setY(120)
                .setZIndex(20)
                .setFontSize(40)
                .setFillColor(0xffffff)
                .setAnchor(0.5);
    }

    private void endGame() {
        gameManager.endGame();
        System.out.println();
        Player p0 = gameManager.getPlayers().get(0);
        Player p1 = gameManager.getPlayers().get(1);
        if (p0.getScore() > p1.getScore()) {
            System.out.println("-----------------Player 0 Wins----------------");
            System.out.println("Score 0: " + p0.getScore());
            System.out.println("Score 1: " + p1.getScore());
        }
        if (p0.getScore() < p1.getScore()) {
        	System.out.println("-----------------Player 1 Wins----------------");
        	System.out.println("Score 0: " + p0.getScore());
            System.out.println("Score 1: " + p1.getScore());
            //p0.hud.setAlpha(0.3);
        }
        System.out.println();
    }

    @Override
    public void gameTurn(int turn) {
        Player player0 = gameManager.getPlayer(0);
        Player player1 = gameManager.getPlayer(1);
        
        player0.id = 0;
        player1.id = 1;

        if (turn != 0) {
            player0.sendInputLine(player1LastAction.action);
            player1.sendInputLine(player0LastAction.action);
        } else {
            player0.sendInputLine("-1");
            player1.sendInputLine("-1");
        }

        player0.execute();
        player1.execute();

        //Get Player 0's Action and validate it
        try {
            player0LastAction = player0.getAction();
            if (!player0LastAction.action.equals("SILENT") && !player0LastAction.action.equals("TALK")) {
            	System.out.println("Invalid Action");
                throw new InvalidAction("Invalid action");
            }
        } catch (TimeoutException e) {
        	System.out.println("Timeout");
            player0.deactivate(String.format("$%d timeout!", player0.getIndex()));
            player0.setScore(-1);
            endGame();
        } catch (InvalidAction e) {
            player0.deactivate(e.getMessage());
            player0.setScore(-1);
            endGame();
        }

        //Get Player 1's Action and validate it
        try {
            player1LastAction = player1.getAction();
            if (!player1LastAction.action.equals("SILENT") && !player1LastAction.action.equals("TALK")) {
            	System.out.println("Invalid Action");
                throw new InvalidAction("Invalid action");
            }
        } catch (TimeoutException e) {
        	System.out.println("Timeout");
            player1.deactivate(String.format("$%d timeout!", player1.getIndex()));
            player1.setScore(-1);
            endGame();
        } catch (InvalidAction e) {
            player1.deactivate(e.getMessage());
            player1.setScore(-1);
            endGame();
        }
        
        //Calculate Scores
        System.out.println();
        System.out.println("--------------------------------");
        if (player0LastAction.action.equals("SILENT") && player1LastAction.action.equals("SILENT")) {
        	System.out.println("Both Silent");
			player0Score += 4;
			player1Score += 4;
		} else if (player0LastAction.action.equals("SILENT") && player1LastAction.action.equals("TALK")) {
			System.out.println("0 Silent | 1 Talk");
			player1Score += 5;
		} else if (player0LastAction.action.equals("TALK") && player1LastAction.action.equals("SILENT")) {
			System.out.println("0 Talk | 1 Silent");
			player0Score += 5;
		} else if (player0LastAction.action.equals("TALK") && player1LastAction.action.equals("TALK")) {
			System.out.println("Both Talk");
			player0Score += 1;
			player1Score += 1;
        }
        System.out.println("Score 0: " + player0Score);
        System.out.println("Score 1: " + player1Score);
        
        System.out.println("--------------------------------");
        System.out.println();
        
        player0.setScore(player0Score);
        player1.setScore(player1Score);
        
        if (turn > 98) {
        	endGame();
        }
    }
}
