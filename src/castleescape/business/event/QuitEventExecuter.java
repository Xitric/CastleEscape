/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package castleescape.business.event;

import castleescape.business.ViewUtil;
import castleescape.business.framework.Game;

/**
 * Event executer for quitting the game.
 */
public class QuitEventExecuter implements EventExecuter {

	@Override
	public void execute(Game game, Event event) {
		//Get event description and print it, if it exists
		String description = event.getEventParam(Event.DESCRIPTION);
		if (description != null) {
			ViewUtil.println(description);
		}

		//Notify the game that it should end
		game.end();
	}

}
