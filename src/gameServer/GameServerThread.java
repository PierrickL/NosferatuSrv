package gameServer;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


public class GameServerThread extends Thread
{
	private GameSrv srv;
	private Socket sock;
	
	public GameServerThread(GameSrv srv,Socket s)
	{
		this.sock = s;
		this.srv = srv;
		
		this.start();
	}
	
	public void run()
	{
		String msg;
		try
		{
			DataInputStream input = new DataInputStream(this.sock.getInputStream());
			for(;;)
			{
				msg = input.readUTF();
				///TODO : add some log'ing function
				System.out.println(this.sock.getInetAddress().toString() + " : " + msg);
				if(!msg.contains("com.cs385.chatclient.HELLO_MSG"))
				{
					this.srv.sendToAll(msg);
				}
				
			}
		} catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println(this.sock.getInetAddress().toString() + " has disconnected.");
			this.srv.removeConnection(this.sock);
			return;
		}
	}
}
