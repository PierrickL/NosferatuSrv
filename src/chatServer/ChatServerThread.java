package chatServer;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


public class ChatServerThread extends Thread
{
	private ChatServer srv;
	private Socket sock;
	
	public ChatServerThread(ChatServer srv,Socket s)
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
				if(!msg.contains("PLAYER_NAME"))
				{
					this.srv.sendToAll(msg);
				}else
				{
					String temp;
					temp = msg.split("=")[1] + " has joined the conversation.";
					this.srv.sendToAll(temp);
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