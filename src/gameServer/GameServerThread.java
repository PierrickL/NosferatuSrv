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
		this.p = p;
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
				
				//set the player name
				if(msg.contains("PLAYER_NAME"))
				{
					this.p.setName(msg.split("=")[1]);
					
				}
				// Examinating all the cases : Requests from client
				if(msg.contains("PA_ASKROLE")) {
					try
					{
						this.dOut.writeUTF(p.getRole());
						msg = input.readUTF();
						System.out.println(s.getInetAddress().toString() + " : " + msg);
						if(msg.equals("PA_CONFIRM")) {
							if(p.getRole().equals(GameBoard.RENFIELD)) {
								this.dOut.writeUTF("VAMPIRE_IS:" + srv.getVampire().getName());
								msg = input.readUTF();
							}
							else {
								playerDraw();
								msg = input.readUTF();
								playerDraw();
								msg = input.readUTF();
							}
						}
						srv.waitForFirstTurn();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(msg.contains(GameBoard.DRAW))
				{
					playerDraw();
				}
				
				if(msg.contains(GameBoard.KILL))
				{
					String toBeKilled = msg.split("=")[1];
					String role = this.srv.getPlayerByName(toBeKilled).getRole();
					
					if(role.equals(GameBoard.VAMPIRE))
					{
						///TODO : Hunters win
						String winMSG;
						winMSG = GameBoard.VICTORY + ";" + GameBoard.HUNTER;
						this.srv.sendToAll(winMSG);
					}
				}
				if(msg.contains("PA_BITE"))
				{
					GameBoard b = this.srv.getGameBorBoard();
					b.bite();
					if(b.getBiteCount() >= 5)
					{
						// Vampires win
						String winMSG;
						winMSG = GameBoard.VICTORY + ";" + GameBoard.VAMPIRE;
						this.srv.sendToAll(winMSG);
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
		String card = b.drawFromLibrary();
		System.out.println(card);
		try
		{
			this.dOut.writeUTF(card);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setPlayerRole(String role) {
		p.setRole(role);
	}
}
