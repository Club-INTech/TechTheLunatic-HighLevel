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

package enums;

/**
 * Enumération des différents services. Plus d'informations sur les services dans la classe Container.
 * @author pf
 *
 */
public enum ServiceNames
{
	 LOG,
	 CONFIG,
	 
	 TABLE,
	 
	 LOCOMOTION,
	 ROBOT,
	 HOOK_FACTORY,

	 SERIAL_WRAPPER,

	 GAME_STATE,
	 
	 SCRIPT_MANAGER,

	 THREAD_SERIAL,

	 THREAD_SENSOR,
	 THREAD_TIMER,
	 THREAD_INTERFACE,
	 THREAD_EYES,
	 THREAD_EVENTS,
	 THREAD_BALISES,
	 
	 STRATEGIE;

	 /**
	  * Constructeur vide
	  */
	 private ServiceNames()
	 {
		 
	 }
	 
}
