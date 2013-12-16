package gameServer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class GameServerThread extends Thread
{
	private GameSrv srv;
	//private Socket sock;
	private Player p;
	private DataOutputStream dOut;

	
	//public GameServerThread(GameSrv srv,Socket s)
	public GameServerThread(GameSrv srv,Player p)
	{
//		this.sock = s;
		this.p= p;
		this.srv = srv;
		Socket sock;
		sock = p.getSocket();
		try
		{
			this.dOut = new DataOutputStream(sock.getOutputStream());
		} catch (IOException e)
		{
			System.err.println("Could not open Socket. Closing current connection.");
				//srv.removeConnection(s);
				srv.removeConnection(p);
				return;
			
		}
		this.start();
	}
	
	public void run()
	{
		String msg;
		try
		{
//			DataInputStream input = new DataInputStream(this.sock.getInputStream());
			Socket s = p.getSocket();
			DataInputStream input = new DataInputStream(s.getInputStream());
			for(;;)
			{
				msg = input.readUTF();
				///TODO : add some log'ing function
//				System.out.println(this.sock.getInetAddress().toString() + " : " + msg);
				System.out.println(s.getInetAddress().toString() + " : " + msg);
				if(!msg.contains("com.cs385.chatclient.HELLO_MSG"))
				{
					// Examinating all the cases : Requests from client
					if(msg.contains(GameBoard.DRAW))
					{
						playerDraw();
					}
				}
				
			}
		} catch (IOException e)
		{
//			System.out.println(this.sock.getInetAddress().toString() + " has disconnected.");
//			this.srv.removeConnection(this.sock);
			Socket sock = p.getSocket();
			System.out.println(sock.getInetAddress().toString() + " has disconnected.");
			this.srv.removeConnection(p);
			
			return;
		}
	}
	
	/**
	 * Executes game mechanism linked to the action of drawing 2 cards & sends the cards to the player
	 */
	private void playerDraw()
	{
		GameBoard b = srv.getGameBorBoard();
		String cards[] = b.drawFromLibrary();
		try
		{
			this.dOut.writeUTF(cards[0]+";"+cards[1]);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
