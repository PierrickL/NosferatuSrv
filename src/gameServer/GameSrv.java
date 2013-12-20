package gameServer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class GameSrv
{
	private int port;
	private ServerSocket sSock;
	//private HashMap<Socket, DataOutputStream> outputs;
	private HashMap<Player, DataOutputStream> outputs;
	private GameBoard board;
	private int confirm;
	private Player renfield;
	private Player vampire;
	private Player firstPlayer;
	private ArrayList<Player> playerOrder;
	private int turn;
	private int turnOffset;
	private ArrayList<String> renfieldCards;
	private boolean throne;
	private boolean transfusion;
	private boolean identity;
	private boolean removeNight;
	private boolean night;
	private ArrayList<String> names;
	
	public GameSrv()
	{
		System.out.println("Starting Game server...");
		this.port = 27002;
		this.outputs = new HashMap<Player, DataOutputStream>();
		
		try
		{
			this.sSock = new ServerSocket(this.port);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(-1);
		}
		System.out.println("Now listening on port " + this.port + ".");
		this.board = new GameBoard();
		this.confirm = 0;
		this.playerOrder = new ArrayList<Player>();
		this.turn = 0;
		this.turnOffset = 0;
		this.throne = false;
		this.transfusion = false;
		this.identity = false;
		this.removeNight = false;
		this.names = new ArrayList<String>();
	}

	/**
	 * Endless Loop that accepts connection & creates threads
	 */
	public void listen()
	{
		for (;;)
		{
			try
			{
				Socket newSocket = this.sSock.accept();
				DataOutputStream outputStream = new DataOutputStream(
						newSocket.getOutputStream());
				System.out.println("Connection from "
						+ newSocket.getInetAddress().toString() + " !");
				
				//Max # of players = 5
				if(getPlayerCount() < 5)
				{
					//this.outputs.put(newSocket, outputStream);
					Player p = new Player(newSocket);
					this.outputs.put(p, outputStream);
					playerOrder.add(p);
					//GameServerThread sThread = new GameServerThread(this, newSocket);
					GameServerThread sThread = new GameServerThread(this, p);
					String msg = "ONLINE_PLAYER=";
					msg += getPlayerCount();
					this.sendToAll(msg);
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(getPlayerCount() == 5) {
						definePlayerRoles();
						sendToAll("START_GAME");
					}
				}else
				{
					System.out.println("Server is full -> rejecting.");
					outputStream.writeUTF("ERROR_SRV_IS_FULL");
					newSocket.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removing the socket s from the broadcasting list
	 * @param s
	 */
	//public void removeConnection(Socket s)
	public void removeConnection(Player p)
	{
		this.outputs.remove(p);
		try
		{
			Socket s = p.getSocket();
			s.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getPlayerCount()
	{
		return this.outputs.size();
	}
	
	public Player getVampire() {
		return vampire;
	}

	public void sendToAll(String message)
	{
		Collection<DataOutputStream> outputStreams = outputs.values();
		for (DataOutputStream currentStream : outputStreams)
		{
			try
			{
				currentStream.writeUTF(message);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("To All : " + message);
	}
	
	/**
	 * Assigning player roles ; should only be called when the server is full
	 */
	public void definePlayerRoles()
	{
//		Retrieve all players
		Set<Player> playerSet = outputs.keySet();
		Object players[];
		players = playerSet.toArray();
		
//		Shuffle the array
		int n = players.length;
		Random r = new Random();
		int j;
		Object temp;
		for(int i = 0; i < n; i++)
		{
			j = r.nextInt(n-1);
			temp = players[i];
			players[i] = players[j];
			players[j] = temp;
		}
		
//		Assign roles
		DataOutputStream dOut = null;
		Player p = null;
		p = (Player)players[0];
		p.setRole(GameBoard.RENFIELD);
		renfield = p;
		System.out.println(p);
		dOut = this.outputs.get(p);
		/*try
		{
			dOut.writeUTF(GameBoard.RENFIELD);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		p = (Player)players[1];
		p.setRole(GameBoard.VAMPIRE);
		vampire = p;
		dOut = this.outputs.get(p);
		/*try
		{
			dOut.writeUTF(GameBoard.VAMPIRE);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		for(int i = 2; i < n; i++)
		{
			p = (Player)players[i];
			p.setRole(GameBoard.HUNTER);
			dOut = this.outputs.get(p);
			/*try
			{
				dOut.writeUTF(GameBoard.HUNTER);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		/*
		 * tells Renfield Vampire Name
		 */
		Player pVampire = (Player)players[1];
		renfield.setVampireName(pVampire.getName());
		System.out.println("renfield : " + renfield.getName() + " | " + renfield);
		System.out.println("vampire : " + vampire.getName() + " | " + vampire);
	}
	
	/**
	 * Retrieves the role of a particular player
	 * @param playerName
	 * @return
	 */
	public Player getPlayerByName(String playerName)
	{
		Set<Player> pSet= outputs.keySet();
		for(Player p : pSet)
		{
			if(p.getName().equals(playerName))
			{
				return p;
			}
		}
		return null;
	}
	
	public synchronized void waitForFirstTurn(String name) {
		if(!names.contains("name")) {
			names.add(name);
			confirm++;
			if(confirm == getPlayerCount()) {
				confirm = 0;
				names = new ArrayList<String>();
				DataOutputStream dOut = this.outputs.get(renfield);
				String msg = "WHO_FIRST_PLAYER";
				Set<Player> pSet= outputs.keySet();
				for(Player p : pSet)
				{
					if(!p.getName().equals(renfield.getName()))
					{
						if(firstPlayer == null || firstPlayer.getName() != p.getName()) {
							msg += ";" + p.getName();
						}
					}
				}
				System.out.println(msg);
				try {
					dOut.writeUTF(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void waitForNextTurn(String name) {
		if(!names.contains(name)) {
			names.add(name);
			confirm++;
			if(confirm == getPlayerCount()) {
				confirm = 0;
				names = new ArrayList<String>();
				manageTurn();
			}
		}
	}
	
	public synchronized void waitForEndTurn(String name) {
		if(!names.contains(name)) {
			names.add(name);
			confirm++;
			if(confirm == getPlayerCount()) {
				confirm = 0;
				names = new ArrayList<String>();
				endTurn();
			}
		}
	}
	
	public synchronized void waitForResolveCards(String name) {
		if(!names.contains(name)) {
			names.add(name);
			confirm++;
			System.out.println(confirm);
			if(confirm == getPlayerCount()) {
				confirm = 0;
				names = new ArrayList<String>();
				resolveCards();
			}
		}
	}
	
	public void setTurn(String firstPlayer) {
		while(!playerOrder.get(0).getName().equals(firstPlayer)) {
			Collections.rotate(playerOrder,1);
		}
		this.firstPlayer = playerOrder.get(0);
		this.turn = 0;
		this.turnOffset = 0;
		board.reinitClock();
		renfieldCards = new ArrayList<String>();
	}

	public void manageTurn() {
		if(turn + turnOffset >= playerOrder.size()) {
			String clock = board.drawFromClock();
			if(clock.equals(GameBoard.NIGHT)) {
				night = true;
			}
			else {
				night = false;
			}
			sendToAll("END_TURN1:" + clock);
		}
		else {
			Player p = playerOrder.get(turn + turnOffset);
			System.out.println("turn = " + turn);
			System.out.println("offset = " + turnOffset);
			if(!p.getName().equals(renfield.getName())) {
				if(turn >= 2) {
					String clock = board.drawFromClock();
					if(clock.equals(GameBoard.NIGHT)) {
						sendToAll("CLOCK_NIGHT");
						p.getThread().setTurn(true);
						DataOutputStream dOut = this.outputs.get(p);
						try {
							dOut.writeUTF("CURRENT_PLAYER");
							System.out.println("CURRENT_PLAYER");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						turn++;
					}
					else {
						night = false;
						sendToAll("END_TURN2:" + clock);
						//resolveCards();
					}
				}
				else {
					p.getThread().setTurn(true);
					DataOutputStream dOut = this.outputs.get(p);
					try {
						dOut.writeUTF("CURRENT_PLAYER");
						System.out.println("CURRENT_PLAYER");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					turn++;
				}
			}
			else if(turn + turnOffset == playerOrder.size()) {
				String clock = board.drawFromClock();
				if(clock.equals(GameBoard.NIGHT)) {
					night = true;
				}
				else {
					night = false;
				}
				sendToAll("END_TURN3:" + clock);
			}
			else {
				System.out.println("turning offset to 1");
				turnOffset = 1;
				if(turn + turnOffset < playerOrder.size()) {
					manageTurn();
				}
				else {
					String clock = board.drawFromClock();
					if(clock.equals(GameBoard.NIGHT)) {
						night = true;
					}
					else {
						night = false;
					}
					sendToAll("END_TURN4:" + clock);
				}
			}
		}
	}
	
	public void resolveCards() {
		int components = 0;
		int bites = 0;
		int nights = 0;
		for(int i=0; i<renfieldCards.size(); i++) {
			if(renfieldCards.get(i).equals(GameBoard.BITE)) {
				bites++;
			}
			else if(renfieldCards.get(i).equals(GameBoard.COMPONENT)) {
				components++;
			}
			else if(renfieldCards.get(i).equals(GameBoard.NIGHT)) {
				nights++;
			}
		}
		System.out.println("cards analysed");
		board.addNightInClock(nights);
		System.out.println("nights added");
		if(components == renfieldCards.size()) {
			sendToAll("MAGIC_OCCURS");
			DataOutputStream dOut = this.outputs.get(this.firstPlayer);
			String msg = "WHICH_MAGIC";
			if(!identity) {
				msg += ";identity";
			}
			if(!transfusion && board.getBiteCount() > 0) {
				msg += ";transfusion";
			}
			if(!removeNight) {
				msg += ";removeNight";
			}
			try {
				dOut.writeUTF(msg);
				System.out.println(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (bites > 0){
			board.bite(bites);
			if(board.getBiteCount() >= 5) {
				sendToAll("VAMPIRE_WIN");
			}
			else {
				sendToAll("VAMPIRE_BITES:" + bites);
				DataOutputStream dOut = this.outputs.get(renfield);
				String msg = "WHICH_TARGET_BITTEN;" + bites;
				Set<Player> pSet= outputs.keySet();
				for(Player p : pSet)
				{
					if(!p.getName().equals(renfield.getName()))
					{
						msg += ";" + p.getName();
					}
				}
				System.out.println(msg);
				try {
					dOut.writeUTF(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			sendToAll("NOTHING_HAPPENS");
			//endTurn();
		}
	}
	
	public void resolveMagic(String magic) {
		if(magic.equals("identity")) {
			this.identity = true;
			DataOutputStream dOut = this.outputs.get(renfield);
			String msg = "WHICH_IDENTITY";
			Set<Player> pSet= outputs.keySet();
			for(Player p : pSet)
			{
				if(!p.getName().equals(renfield.getName()))
				{
					if(vampire.getName() != p.getName()) {
						msg += ";" + p.getName();
					}
				}
			}
			System.out.println(msg);
			try {
				dOut.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(magic.equals("transfusion")) {
			this.transfusion = true;
			DataOutputStream dOut = this.outputs.get(this.firstPlayer);
			String msg = "WHICH_TRANSFUSION";
			Set<Player> pSet= outputs.keySet();
			for(Player p : pSet)
			{
				if(!p.getName().equals(renfield.getName()))
				{
					if(p.getBites() > 0) {
						msg += ";" + p.getName();
					}
				}
			}
			System.out.println(msg);
			try {
				dOut.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(magic.equals("removeNight")) {
			this.removeNight = true;
			board.removeNightInClock();
			sendToAll("NIGHT_REMOVED");
		}
		if(identity && transfusion && removeNight) {
			identity = false;
			transfusion = false;
			removeNight = false;
		}
		//endTurn();
	}
	
	public void resolveBite(String target) {
		sendToAll("HAS_BEEN_BITTEN:" + target);
		getPlayerByName(target).addBite();
		//endTurn();
	}
	
	public void reveal(String target) {
		String msg = "TARGET_IDENTITY_IS:" + target;
		sendToAll(msg);
	}
	
	public void transfuse(String target) {
		String card = board.drawFromLibrary();
		String msg = "TARGET_TRANSFUSION_IS:" + target + ":" + card;
		sendToAll(msg);
	}
	
	public void endTurn() {
		if(!night) {
			DataOutputStream dOut = this.outputs.get(this.firstPlayer);
			String msg = "END_ALL_TURN";
			Set<Player> pSet= outputs.keySet();
			for(Player p : pSet)
			{
				if(!p.getName().equals(renfield.getName()))
				{
					if(firstPlayer.getName() != p.getName()) {
						msg += ";" + p.getName();
					}
				}
			}
			System.out.println(msg);
			try {
				dOut.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			DataOutputStream dOut = this.outputs.get(renfield);
			String msg = "WHO_FIRST_PLAYER";
			Set<Player> pSet= outputs.keySet();
			for(Player p : pSet)
			{
				if(!p.getName().equals(renfield.getName()))
				{
					if(firstPlayer.getName() != p.getName()) {
						msg += ";" + p.getName();
					}
				}
			}
			System.out.println(msg);
			try {
				dOut.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void kill(String target) {
		if(target.equals("No thx")) {
			DataOutputStream dOut = this.outputs.get(firstPlayer);
			String msg = "WHO_FIRST_PLAYER";
			Set<Player> pSet= outputs.keySet();
			for(Player p : pSet)
			{
				if(!p.getName().equals(renfield.getName()))
				{
					if(firstPlayer.getName() != p.getName()) {
						msg += ";" + p.getName();
					}
				}
			}
			System.out.println(msg);
			try {
				dOut.writeUTF(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			String role = getPlayerByName(target).getRole();
			if(role.equals(GameBoard.VAMPIRE)) {
				sendToAll("HUNTERS_WIN");
			}
			else {
				sendToAll("VAMPIRE_SECOND_WIN:" + target);
			}
		}
	}
	
	public void addRenfieldCard(String card, String name) {
		renfieldCards.add(card);
		DataOutputStream dOut = this.outputs.get(renfield);
		String msg = "RENFIELD_SHOW_CARD:" + card + ":" + name;
		try {
			dOut.writeUTF(msg);
			System.out.println(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addDiscardCard(String card, String name) {
		board.addDiscard(card);
		sendToAll("ALL_SHOW_CARD:" + card + ":" + name);
	}
	
	public GameBoard getGameBorBoard()
	{
		return this.board;
	}

	public static void main(String args[])
	{
		GameSrv srv = new GameSrv();
		srv.listen();
	}
}
