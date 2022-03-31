import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class Main {
	public static void main(String[] args) {
		
		MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
		gameRunner.setSeed(-7347867073429989282L);
		gameRunner.addAgent(Player1.class,
			"Tororo",
			"https://static.codingame.com/servlet/fileservlet?id=61910307869345"
		);
		gameRunner.addAgent(Player2.class,
			"Ghilbib",
			"https://static.codingame.com/servlet/fileservlet?id=61910289640958"
		);
		
		// gameRunner.addAgent("python3 /home/user/player.py");
		
		// The first league is classic tic-tac-toe
		gameRunner.setLeagueLevel(1);
		// The second league is ultimate tic-tac-toe
		// gameRunner.setLeagueLevel(2);
		
		gameRunner.start();
	}
}
