package castleescape.business.event;

import castleescape.business.framework.Game;

/**
 * Interface for all classes capable of executing events.
 */
public interface EventExecuter {

	/**
	 * Interface description of execute.
	 *
	 * @param game  the Game instance to perform this event on
	 * @param event the Event object which has been used to execute this
	 */
	void execute(Game game, Event event);

}
