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

package robot.serial;

import enums.ServiceNames;

/**
 * N'est utilise que par le SerialManager afin de connaitre les attributs des cartes
 * @author pierre
 */
class CardSpecification 
{
	/**
	 * Nom de la carte électronique.
	 * Les noms peuvent être parmis:
	 * ServiceNames.SERIE_ASSERVISSEMENT(0)
	 * ServiceNames.SERIE_CAPTEURS_ACTIONNEURS(1)
	 * ServiceNames.SERIE_LASER(2)
	 */
	ServiceNames name;
	
	/** identifiant de la carte */
	int id;
	
	/** baudrate que la connexion série doit avoir pour parler a cette carte */
	int baudrate;
	
	/**
	 * Construit les spécifications d'une carte électronique.
	 * @param name Nom de la carte électronique.
	 * @param id identifiant de la carte
	 * @param baudrate baudrate que la connexion série doit avoir pour parler a cette carte
	 */
	CardSpecification(ServiceNames name, int id, int baudrate)
	{
		this.name = name;
		this.id = id;
		this.baudrate = baudrate;
	}
}
