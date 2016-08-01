/*
 * Copyright (c) 2016, INTech.
 *
 * This file is part of INTech's HighLevel.
 *
 *  INTech's HighLevel is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  INTech's HighLevel is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with it.  If not, see <http://www.gnu.org/licenses/>.
 */

package tests;

import enums.ActuatorOrder;
import enums.ServiceNames;
import enums.Speed;
import graphics.Window;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import robot.Robot;
import strategie.GameState;
import table.Table;
import threads.dataHandlers.ThreadBalises;

public class JUnit_BorneArcade extends JUnit_Test 
{
	GameState real_state;
	Window win;

	ThreadBalises balises;
	
	public static void main(String[] args) throws Exception
	{
	   JUnitCore.main("tests.JUnit_BorneArcade");
	}
	
	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		real_state = (GameState) container.getService(ServiceNames.GAME_STATE);
        
		win = new Window((Table)container.getService(ServiceNames.TABLE), (Robot)real_state.robot);
		
		container.getService(ServiceNames.THREAD_INTERFACE);
		//container.getService(ServiceNames.THREAD_SENSOR);

		real_state.robot.setPosition(Table.entryPosition);
		real_state.robot.setOrientation(Math.PI);
		real_state.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
		
		real_state.robot.updateConfig();
		real_state.robot.useActuator(ActuatorOrder.MONTLHERY, false);

		balises = (ThreadBalises) container.getService(ServiceNames.THREAD_BALISES);
		balises.showDebug();
		container.startInstanciedThreads();

	}

	@Test
	public void start()
	{
		while(true)
		{
			real_state.robot.getPosition();
		}
	}
}
