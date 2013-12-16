package gameServer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
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

	public GameSrv()
	{
		System.out.println("Starting Game server...");
		this.port = 27002;
//		this.outputs = new HashMap<Socket, DataOutputStream>();
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
					//GameServerThread sThread = new GameServerThread(this, newSocket);
					GameServerThread sThread = new GameServerThread(this, p);
					String msg = "ONLINE_PLAYER=";
					msg += getPlayerCount();
					this.sendToAll(msg);
					
					/*
					 * REMOVE THIS LINE
					 */
					this.definePlayerRoles();
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
			players[j] = players[i];
		}
		
//		Assign roles & send them to client
		DataOutputStream dOut = null;
		Player p = null;
		p = (Player)players[0];
		p.setRole(GameBoard.RENFIELD);
		dOut = this.outputs.get(p);
		try
		{
			dOut.writeUTF(GameBoard.RENFIELD);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		p = (Player)players[1];
		p.setRole(GameBoard.VAMPIRE);
		dOut = this.outputs.get(p);
		try
		{
			dOut.writeUTF(GameBoard.VAMPIRE);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 2; i < n; i++)
		{
			p = (Player)players[i];
			p.setRole(GameBoard.HUNTER);
			dOut = this.outputs.get(p);
			try
			{
				dOut.writeUTF(GameBoard.HUNTER);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Retrieves the role of a particular player
	 * @param playerName
	 * @return
	 */
	public String getRoleByName(String playerName)
	{
		Set<Player> pSet= outputs.keySet();
		String role = null;
		for(Player p : pSet)
		{
			if(p.getName().equals(playerName))
			{
				role = p.getRole();
				break;
			}
		}
		
		return role;
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
