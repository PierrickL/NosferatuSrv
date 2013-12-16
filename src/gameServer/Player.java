package gameServer;

import java.net.Socket;

public class Player
{
	private Socket socket;
	private String role;
	
	public Player(Socket s)
	{
		this.socket = s;
	}
	
	public void setRole(String role)
	{
		this.role = role;
	}
	
	public String getRole()
	{
		return this.role;
		
	}
	
	public Socket getSocket()
	{
		return this.socket;
	}
	
}
