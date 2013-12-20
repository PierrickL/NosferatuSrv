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
	private boolean turn;
	
	//public GameServerThread(GameSrv srv,Socket s)
	public GameServerThread(GameSrv srv,Player p)
	{
//		this.sock = s;
		this.p = p;
		p.setThread(this);
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
		this.turn = false;
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
						srv.waitForFirstTurn(p.getName());
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(msg.contains("PA_FIRSTPLAYER")) {
					srv.setTurn(msg.split(":")[1]);
					srv.manageTurn();
				}
				if(msg.contains("PA_CARD_RENFIELD")) {
					String cardForRenfield = msg.split(":")[1];
					if(cardForRenfield.equals("BITE")) {
						srv.addRenfieldCard(GameBoard.BITE, p.getName());
					}
					else if(cardForRenfield.equals("GOSSIP")) {
						srv.addRenfieldCard(GameBoard.RUMOR, p.getName());
					}
					else if(cardForRenfield.equals("COMPONENT")) {
						srv.addRenfieldCard(GameBoard.COMPONENT, p.getName());
					}
					else if(cardForRenfield.equals("NIGHT")) {
						srv.addRenfieldCard(GameBoard.NIGHT, p.getName());
					}
					try
					{
						this.dOut.writeUTF("ASK_CARD_DISCARD");
						System.out.println("ASK_CARD_DISCARD");
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(msg.contains("PA_CARD_DISCARD")) {
					String cardForRenfield = msg.split(":")[1];
					if(cardForRenfield.equals("BITE")) {
						srv.addDiscardCard(GameBoard.BITE, p.getName());
					}
					else if(cardForRenfield.equals("GOSSIP")) {
						srv.addDiscardCard(GameBoard.RUMOR, p.getName());
					}
					else if(cardForRenfield.equals("COMPONENT")) {
						srv.addDiscardCard(GameBoard.COMPONENT, p.getName());
					}
					else if(cardForRenfield.equals("NIGHT")) {
						srv.addDiscardCard(GameBoard.NIGHT, p.getName());
					}
					
				}
				if(msg.equals("PA_CONFIRM_NEXT_TURN")) {
					srv.waitForNextTurn(p.getName());
				}
				if(msg.contains(GameBoard.DRAW))
				{
					playerDraw();
				}
				if(msg.equals("END_DRAW")) {
					if(turn) {
						try
						{
							this.dOut.writeUTF("ASK_CARD_RENFIELD");
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				if(msg.contains("PA_MAGIC")) {
					String magic = msg.split(":")[1];
					srv.resolveMagic(magic);
				}
				
				if(msg.contains("PA_TARGET_BITTEN")) {
					String target = msg.split(":")[1];
					srv.resolveBite(target);
				}
				
				if(msg.contains("PA_CONFIRM_END_TURN")) {
					srv.waitForEndTurn(p.getName());
				}
				
				if(msg.contains("PA_CONFIRM_CLOCK")) {
					srv.waitForResolveCards(p.getName());
				}
				
				if(msg.contains("PA_TARGET_IDENTITY")) {
					String target = msg.split(":")[1];
					srv.reveal(target);
				}
				
				if(msg.contains("PA_TARGET_TRANSFUSION")) {
					String target = msg.split(":")[1];
					srv.transfuse(target);
				}
				
				if(msg.contains("PA_TARGET_KILL")) {
					String target = msg.split(":")[1];
					srv.kill(target);
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
	 * Executes game mechanism linked to the action of drawing a card & sends the cards to the player
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
	
	public void setTurn(boolean turn) {
		this.turn = turn;
	}
}
