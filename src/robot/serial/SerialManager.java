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

import exceptions.serial.SerialManagerException;
import gnu.io.CommPortIdentifier;
import threads.dataHandlers.ThreadSerial;
import utils.Log;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * 
 * Classe instanciatrice de la série
 * @author pierre
 * @author pf
 */
public class SerialManager 
{
	/**
	 * Sortie de log a utiliser pour informer l'utilisateur
	 */
	private Log log;

	/** Thread série à lancer */
	public ThreadSerial threadSerial = null;

	/** Liste pour stocker les series qui sont connectees au système, afin de trouver la bonne */
	private ArrayList<String> connectedSerial = new ArrayList<String>();

	/** Baudrate de la liaison série */
	public static final int baudrate = 115200;



	public SerialManager(Log log) throws SerialManagerException
	{
		this.log = log;

		this.threadSerial = new ThreadSerial(log, "STM32");

		checkSerial();
		createSerial();
	}

	/**
	 * Regarde toutes les series qui sont branchees (sur /dev/ttyUSB* et /dev/ttyACM*)
	 */
	public  void checkSerial()
	{
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			this.connectedSerial.add(port.getName());
		}
	}

	/**
	 * Création de la serie (il faut au prealable faire un checkSerial()).
	 *
	 * Instancie un thread pour chaque série, vérifie que tout fonctionne (ou non) et valide
	 * le tout une fois la bonne trouvée
	 *
	 * @throws SerialManagerException
	 */
	public void createSerial() throws SerialManagerException
	{
		int id;

		for (String connectedSerial : this.connectedSerial)
		{
			if(connectedSerial.contains("ACM"))
				continue;

			ThreadSerial ser = new ThreadSerial(log,"test");
			ser.initialize(connectedSerial, baudrate);

			if (ser.ping() != null)
				id = Integer.parseInt(ser.ping());
			else {
				ser.close();
				continue;
			}

			if (id != 0) {
				ser.close();
				continue;
			}

			ser.close();
			System.out.println("Carte sur: " + connectedSerial);

			threadSerial.initialize(connectedSerial, baudrate);
			threadSerial.start();
			return;
		}

		log.critical("La carte STM32 n'est pas détectée");
		throw new SerialManagerException();
	}

	/**
	 * Permet d'obtenir une série au préalable instanciée dans le constructeur.
	 * @return L'instance de la série
	 * @throws SerialManagerException 
	 */
	public ThreadSerial getSerial()	throws SerialManagerException
	{
		return threadSerial;
	}
}
