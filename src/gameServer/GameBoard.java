package gameServer;
import java.util.ArrayList;
import java.util.Random;


public class GameBoard
{
	private ArrayList<String> clock; //horloge
	private ArrayList<String> discard; //défausse
	private ArrayList<String> library; //pioche
	
	/*
	 * Game constants
	 * The deck sizes are based on 5 players game (<=> minimal number);
	 */
	public static final int MAX_CLOCK_SIZE = 6;
	public static final int MAX_LIBRARY_SIZE = 54; 
	public static final String RENFILED = "nosferatu_renfield";
	public static final String VAMPIRE = "nosferatu_vampire";
	public static final String HUNTER = "nosferatu_hunter";	
	public static final String DAWN = "nosferatu_dawn"; // 1
	public static final String NIGHT = "nosferatu_night"; // 1 per player + (10 - #Players) in the library 
	public static final String RUMOR = "nosferatu_rumor"; // 18
	public static final String COMPONENT = "nosferatu_component"; //15
	public static final String BITE = "nosferatu_bite"; // 16
	
	/*
	 * Player Action Constants
	 */
	public static final String DRAW = "PA_DRAWCARDS";
	
	///TODO : Attribute roles => Player Array => Shuffle => #1 = Renfiled ; #2 = Vamp
	
	
	/**
	 * We're assuming we're only playing with 5 players
	 */
	public GameBoard()
	{
		this.clock = new ArrayList<String>();
		this.discard = new ArrayList<String>();
		this.library = new ArrayList<String>();
		
	}
	
	/**
	 * Inits the card stacks
	 */
	private void init()
	{
		this.clock.add(DAWN);
		for(int i = 0; i < MAX_CLOCK_SIZE-1; i++)
		{
			this.clock.add(NIGHT);
		}
		this.shuffle(this.clock);
		
		for(int i = 0; i < 5; i++)
		{
			this.library.add(NIGHT);
		}
		
		for(int i = 0; i < 18; i++)
		{
			this.library.add(RUMOR);
		}
		
		for(int i = 0; i < 15; i++)
		{
			this.library.add(COMPONENT);
		}
		
		for(int i = 0; i < 16; i++)
		{
			this.library.add(BITE);
		}
		
		this.shuffle(this.library);
	}
	
	/**
	 * Suffles the cards
	 */
	private void shuffle(ArrayList<String>array)
	{
		int n = array.size();
		Random r = new Random();
		int j;
		String temp;
		for(int i = 0; i < n; i++)
		{
			j = r.nextInt(n-1);
			temp = array.remove(i);
			array.add(i, array.remove(j));
			array.add(j,temp);
		}
	}
	
	/**
	 * Discarding a card
	 * @param s the string symbolizing the card to be discarded
	 */
	public void discard(String s)
	{
		this.discard.add(s);
	}
	
	/**
	 * Draws 2 cards from the library
	 * @return an array of 2 string symbolizing the 2 cards
	 */
	public String[] drawFromLibrary()
	{
		String cards[] = new String[2];
		if(this.library.size() < 2)
		{
			//not enough cards ; merge library & discard
			this.mergeDiscardAndLibrary();
		}
		/*
		 * Taking the two cards from the top of the library
		 */
		cards[0] = this.library.remove(this.library.size()-1);
		cards[1] = this.library.remove(this.library.size()-1);
		return cards;
	}
	
	/**
	 * Helper Method
	 * Puts the cards from the discard in the library and shuffles the new library
	 */
	private void mergeDiscardAndLibrary()
	{
		int n = this.discard.size();
		for(int i = 0; i < n; i++)
		{
			this.library.add(this.discard.remove(i));
		}
		this.shuffle(this.library);
	}
}
