package gameServer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;

public class GameSrv
{
	private int port;
	private ServerSocket sSock;
	private HashMap<Socket, DataOutputStream> outputs;
	private GameBoard board;

	public GameSrv()
	{
		System.out.println("Starting Game server...");
		this.port = 27002;
		this.outputs = new HashMap<Socket, DataOutputStream>();
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
					this.outputs.put(newSocket, outputStream);
					GameServerThread sThread = new GameServerThread(this, newSocket);
					String msg = "ONLINE_PLAYER=";
					msg += getPlayerCount();
					this.sendToAll(msg);
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
	public void removeConnection(Socket s)
	{
		this.outputs.remove(s);
		try
		{
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
