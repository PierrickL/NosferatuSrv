package gameServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class GameBoard
{
	private ArrayList<String> clock; //horloge
	private ArrayList<String> discard; //défausse
	private ArrayList<String> library; //pioche
	private int biteCount;
	private int addNight;
	
	/*
	 * Game constants
	 * The deck sizes are based on 5 players game (<=> minimal number);
	 */
	public static final int MAX_CLOCK_SIZE = 6;
	public static final int MAX_LIBRARY_SIZE = 54; 
	public static final String RENFIELD = "nosferatu_renfield";
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
	public static final String KILL = "PA_KILL";
	public static final String PBITE = "PA_BITE";// whenever a player plays a bite card
	public static final String VICTORY = "nosferatu_win";
	public static final String DEFEAT = "nosferatu_defeat";
	
	
	/**
	 * We're assuming we're only playing with 5 players
	 */
	public GameBoard()
	{
		this.clock = new ArrayList<String>();
		this.discard = new ArrayList<String>();
		this.library = new ArrayList<String>();
		this.biteCount = 0;
		this.addNight = 0;
		init();
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
	 * Shuffles the cards
	 */
	public void shuffle(ArrayList<String>array)
	{
		Collections.shuffle(array);
		/*int n = array.size();
		Random r = new Random();
		int j;
		String temp;
		for(int i = 0; i < n; i++)
		{
			j = r.nextInt(n-1);
			temp = array.remove(i);
			array.add(i, array.remove(j));
			array.add(j,temp);
		}*/
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
	 * Draws a card from the library
	 * @return a string symbolizing the card
	 */
	public String drawFromLibrary()
	{
		String card;
		if(this.library.size() < 1)
		{
			//not enough cards ; merge library & discard
			this.mergeDiscardAndLibrary();
		}
		/*
		 * Taking the two cards from the top of the library
		 */
		card = this.library.remove(this.library.size()-1);
		return card;
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
	
	public String drawFromClock() {
		return this.clock.remove(clock.size()-1);
	}
	
	public void reinitClock() {
		clock = new ArrayList<String>();
		for(int i=0; i<MAX_CLOCK_SIZE-1+addNight;i++) {
			clock.add(NIGHT);
		}
		clock.add(DAWN);
		shuffle(clock);
	}
	
	public void addNightInClock(int n) {
		addNight += n;
		int k=0;
		while(n > 0) {
			for(int i=k; i<library.size(); i++) {
				if(library.get(i).equals(NIGHT)) {
					k=i;
					library.remove(k);
					n--;
				}
			} 
		}
	}
	
	public void removeNightInClock() {
		addNight--;
	}

	/**
	 * To be called whenever a Vampire plays a bite card
	 */
	public void bite(int n)
	{
		this.biteCount += n;
	}
	
	public int getBiteCount()
	{
		return this.biteCount;
	}
	
	public void addDiscard(String card) {
		discard.add(card);
	}
}
