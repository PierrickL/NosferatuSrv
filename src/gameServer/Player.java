package gameServer;

import java.net.Socket;

public class Player
{
	private Socket socket;
	private String role;
	private String name;
	
	public Player(Socket s)
	{
		this.socket = s;
		this.name = null;
	}
	
	public void setRole(String role)
	{
		this.role = role;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
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
