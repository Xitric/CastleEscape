package castleescape.business.framework;

import castleescape.business.event.EventExecuter;
import castleescape.business.event.AddExitEventExecuter;
import castleescape.business.event.EventWord;
import castleescape.business.event.AddRoomItemEventExecuter;
import castleescape.business.event.RemovePlayerItemEventExecuter;
import castleescape.business.event.SetDescriptionEventExecuter;
import castleescape.business.event.MakeNoiseEventExecuter;
import castleescape.business.event.RemoveRoomItemEventExecuter;
import castleescape.business.event.AddPlayerItemEventExecuter;
import castleescape.business.command.Command;
import castleescape.business.command.QuitCommandExecuter;
import castleescape.business.command.InventoryCommandExecuter;
import castleescape.business.command.TakeCommandExecuter;
import castleescape.business.command.HelpCommandExecuter;
import castleescape.business.command.CommandWord;
import castleescape.business.command.GoCommandExecuter;
import castleescape.business.command.UseCommandExecuter;
import castleescape.business.command.DropCommandExecuter;
import castleescape.business.command.PeekCommandExecuter;
import castleescape.business.command.InspectCommandExecuter;
import castleescape.business.command.CommandExecuter;
import castleescape.business.Configurations;
import castleescape.business.ViewUtil;
import util.XMLRoomExitBuilder;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Class defining instance behavior for setting up and running a game. This
 * includes creating the game {@link Room rooms}, processing commands, changing
 * rooms, initiating and running the game loop and quitting the game. It also
 * defines instance methods for priting a welcome message and help information.
 *
 * @see <a href="https://codeshare.io/vRDTN">Codeshare</a>
 */
public class Game {

	/**
	 * The parser object responsible for getting input from the user and
	 * converting it to {@link Command} objects.
	 */
	private final Parser parser;

	/**
	 * Map of command executers. The keys are CommandWord objects and the values
	 * are the CommandExecuters associated with these CommandWord objects.
	 */
	private final HashMap<CommandWord, CommandExecuter> commandExecuters;

	/**
	 * Map of event executers. The keys are EventWord objects and the values are
	 * the Event Executers associated with these EventWord objects.
	 */
	private final HashMap<EventWord, EventExecuter> eventExecuters;

	/**
	 * Map of rooms in the game. The keys are room names and the values are the
	 * rooms with these names.
	 */
	private final HashMap<String, Room> roomMap;

	/**
	 * The room that we are currently in.
	 */
	private Room currentRoom;

	/**
	 * The monster in the game.
	 */
	private final Monster monster;

	/**
	 * The player character in the game.
	 */
	private Character player;

	/**
	 * The score manager in the game.
	 */
	private ScoreManager scoreManager;

	/**
	 * Boolean keeping track of whether the game is running.
	 */
	private boolean running;

	/**
	 * Constructs a new game object. This constructor will call the method
	 * {@link XMLRoomExitBuilder#getRooms()} to initialize all rooms in the game
	 * and constructs a {@link Parser} for reading user input.
	 * <p>
	 * To start the game, call the {@link #play()} method after the game object
	 * has been successfully constructed.
	 */
	public Game() {
		//Initialize rooms HashMap
		roomMap = XMLRoomExitBuilder.getRooms();
		currentRoom = roomMap.get(Configurations.START_ROOM_NAME);

		//Create a player character
		createPlayerCharacter();

		//Add command executers and associate them with command words
		commandExecuters = new HashMap<>();
		commandExecuters.put(CommandWord.HELP, new HelpCommandExecuter());
		commandExecuters.put(CommandWord.GO, new GoCommandExecuter());
		commandExecuters.put(CommandWord.TAKE, new TakeCommandExecuter());
		commandExecuters.put(CommandWord.DROP, new DropCommandExecuter());
		commandExecuters.put(CommandWord.INSPECT, new InspectCommandExecuter());
		commandExecuters.put(CommandWord.INVENTORY, new InventoryCommandExecuter());
		commandExecuters.put(CommandWord.USE, new UseCommandExecuter());
		commandExecuters.put(CommandWord.QUIT, new QuitCommandExecuter());
		commandExecuters.put(CommandWord.PEEK, new PeekCommandExecuter());

		//Add event executers and associate them with event words
		eventExecuters = new HashMap<>();
		eventExecuters.put(EventWord.ADD_EXIT, new AddExitEventExecuter());
		eventExecuters.put(EventWord.ADD_PLAYER_ITEM, new AddPlayerItemEventExecuter());
		eventExecuters.put(EventWord.ADD_ROOM_ITEM, new AddRoomItemEventExecuter());
		eventExecuters.put(EventWord.MAKE_NOISE, new MakeNoiseEventExecuter());
		eventExecuters.put(EventWord.SET_DESCRIPTION, new SetDescriptionEventExecuter());
		eventExecuters.put(EventWord.REMOVE_PLAYER_ITEM, new RemovePlayerItemEventExecuter());
		eventExecuters.put(EventWord.REMOVE_ROOM_ITEM, new RemoveRoomItemEventExecuter());

		//Initialize remaining variables
		parser = new Parser();
		monster = new Monster(roomMap.get(Configurations.MONSTER_START_ROOM_NAME));
		scoreManager = new ScoreManager();
	}

	/**
	 * Get the monster in the game.
	 *
	 * @return the monster
	 */
	public Monster getMonster() {
		return monster;
	}

	/**
	 * Get the player character in the game.
	 *
	 * @return the player
	 */
	public Character getPlayer() {
		return player;
	}

	/**
	 * Construct a new player character by prompting the user to choose between
	 * four predetermined options.
	 */
	private void createPlayerCharacter() {
		//Present options to the player
		ViewUtil.println("Players you can choose:");
		ViewUtil.println("\tPlayer 1: Norman who is a normal ninja, that makes less noise but can't carry that much");
		ViewUtil.println("\tPlayer 2: Bob is a bodybuilder making him capable of carrying a lot if items but he also makes a lot of noise");
		ViewUtil.println("\tPlayer 3: Obi the obvious is a man that makes a lot of noise, but is capable to carry a medium amount of stuff");
		ViewUtil.println("\tPlayer 4: Tim is pretty generic, he does not make that much noise and can carry a reasonable number of items");
		ViewUtil.newLine();

		//Scanner name is "input" and this feature makes it possible for the
		//user to choose a player character
		Scanner input = new Scanner(System.in);

		//Declare choice variable to store the player choice
		int choice;

		//Get user input until the choice maps to a valid Character
		while (true) {
			//Prompt the user to make a decision
			ViewUtil.print("Choose by typing a number between 1 and 4: ");

			//Get user input and handle the chance of the user typing
			//non-integer input
			try {
				choice = input.nextInt();
			} catch (InputMismatchException e) {
				ViewUtil.println("Only numbers allowed! Try again.");

				//Clear the input, as it is invalid
				input.nextLine();
				continue; //Star over in this loop
			}

			//If the choice is outside of the valid range [1;4] then tell the
			//user that their input was invalid. After this, the loop will
			//iterate again, allowing for the user to make an extra attempt.
			//Otherwise, if the value is valid, break out of the loop.
			// || means or
			if (choice > 5 || choice < 1) {
				ViewUtil.println("The integer has to be between 1 and 4");
			} else {
				break;
			}
		}

		//Choice is now an integer in the range [1;4]. Use a switch statement to
		//construct the matching player character.
		switch (choice) {
			case 1:
				//Low noise, low carry capacity
				//Norman the normal ninja
				player = new Character(0.2, 2);
				break;
			case 2:
				//A lot of noise, high cary capacity
				//Bob the (body)builder
				player = new Character(0.8, 6);
				break;
			case 3:
				//A lot of noise, medium carry capacity
				//Obi the obvious
				player = new Character(0.7, 3);
				break;
			case 4:
				//Medium noise, medium carry capacity
				//Tim the generic person
				player = new Character(0.4, 4);
				break;
			case 5:
				ViewUtil.println("Activated debug character! GODMODE");
				player = new Character(0, 999);
				break;
		}
	}

	/**
	 * Get the score manager in the game. The score manager is responsible for
	 * keeping track of the player's current score.
	 *
	 * @return the score manager
	 */
	public ScoreManager getScoreManager() {
		return scoreManager;
	}

	/**
	 * Get the event executer associated with the specified event word.
	 *
	 * @param eventWord the event word that the requested event executer is
	 *                  associated with
	 * @return the event executer associated with the specified event word
	 */
	public EventExecuter getEventExecuter(EventWord eventWord) {
		return eventExecuters.get(eventWord);
	}

	/**
	 * Start playing the game. This method will print the welcome message to the
	 * console and initiate the main game loop. Thus, this method will not
	 * return until the user is done playing the game.
	 * <p>
	 * For every iteration in the game loop the user will be asked to enter a
	 * command using the {@link Parser}, and the command will be processed and
	 * its event (if any) is carried out.
	 */
	public void play() {
		//Set the game to running mode
		running = true;

		//Print the welcome message to the console
		printWelcome();

		//Keep looping as long as the running variable is true. Setting this
		//variable to false anywhere inside the while loop will make it stop
		//iterating and allow this method to return, and thus allow the game to
		//end.
		while (running) {
			//Fetch the next command from the user. This method call pauses
			//until the user has entered a command.
			Command command = parser.getCommand();

			//For aesthetic reasons only
			ViewUtil.newLine();

			//If the player is caught by the monster, game over
			if (monster.isPlayerCaught()) {
				ViewUtil.println("The monster caught you and shredded you to pieces!");
				ViewUtil.println("\t\tGAME OVER");

				//Game over, so we quit
				quit();
			} else {
				//The player is still alive
				//Process the entered command. The processCommand() method will
				//return a boolean that is true if the command was a "quit" command
				//and false otherwise. By setting the value of the finished variable
				//here we can control if the loop will iterate again, or if it will
				//stop
				processCommand(command);
			}
		}

		//Record player score
		scoreManager.recordCurrentGameScore();

		//Reset the score manager in case someone chooses to run the game again
		scoreManager.reset();

		//Print out thank you and good bye message
		ViewUtil.println("Thank you for playing. Good bye.");
	}

	/**
	 * Print the welcome message to the console. This will also print the
	 * description of the first room given by {@link Room#getLongDescription()}.
	 */
	private void printWelcome() {
		//Print out game details
		ViewUtil.newLine();
		ViewUtil.println("Welcome to the World of Zuul!");
		ViewUtil.println("World of Zuul is a new, incredibly boring adventure game.");

		//Printing out CommandWord.HELP will replace it with the return value of
		//its toString() method, which is the string representation of the
		//command word
		ViewUtil.println("Type '" + CommandWord.HELP + "' if you need help.");
		ViewUtil.newLine();

		//Print the long description of the current room, that is the starting
		//room
		ViewUtil.println(currentRoom.getLongDescription());
	}

	/**
	 * Process the specified {@link Command} to carry out the action associated
	 * with it.
	 *
	 * @param command the command to process
	 */
	private void processCommand(Command command) {
		//Notify the monster that a command has been entered.
		monster.notifyOfCommand(this);

		//Get the command executer associated with the specified CommandWord
		//object
		CommandExecuter executer = commandExecuters.get(command.getCommandWord());

		//If no such command executer was found that means the command word is
		//unknown
		if (executer == null) {
			ViewUtil.println("I don't know what you mean.");
			return;
		}

		//At this point executer is able to execute the specified command
		executer.execute(this, command);
	}

	/**
	 * Make the game stop playing. The current game loop will run to an end.
	 */
	public void quit() {
		running = false;
	}

	/**
	 * Add a room to the game.
	 *
	 * @param room the room to add
	 */
	public void addRoom(Room room) {
		roomMap.put(room.getRoomName(), room);
	}

	/**
	 * Get the room that the player is currently in.
	 *
	 * @return the room that the player is currently in
	 */
	public Room getCurrentRoom() {
		return currentRoom;
	}

	/**
	 * Set the room that the character is in.
	 *
	 * @param room the room to move the character to
	 */
	public void setRoom(Room room) {
		currentRoom = room;
	}

	/**
	 * Get the room with the specified name.
	 *
	 * @param name the name of the room
	 * @return the room with the specified name, or null if no such room exists
	 */
	public Room getRoom(String name) {
		return roomMap.get(name);
	}
}