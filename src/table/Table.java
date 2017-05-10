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

package table;


import container.Service;
import enums.ColorModule;
import smartMath.Vec2;
import table.obstacles.ObstacleManager;
import utils.Config;
import utils.Log;

/* Positions :
 * 			_______________________________________________________
 * 			|-1500,2000         	0,2000		         1500,2000|
 * 			|           		      							  |
 * 			|           		     							  |
 * 			|           	 		  							  |
 * 			|           	 		  							  |
 * 			|           	 		  							  |
 * 			|-1500,0           		 0,0       				1500,0|
 *          -------------------------------------------------------
 *          
 * (0,0) = entre les deux zones de départ
 * (0,2000) = Base Lunaire
 */

/**
 * Stocke toutes les informations liées à la table (muables et immuables) au cours d'un match.
 * @author Discord
 */
public class Table implements Service
{
	/** Le gestionnaire d'obstacle. */
	private ObstacleManager mObstacleManager;
	
	/** système de log sur lequel écrire. */
	private Log log;

	/** endroit ou lire la configuration du robot */
	private Config config;
	
	//TODO : définir les éléments de jeu de la table
	public Balls cratereDepart;
	public Balls cratereBaseLunaire;
	public Fusee fuseeDepart;
	public Fusee fuseeBase;
	public Cylindre devantDepart;
	public Cylindre cratere;
	public Cylindre cratereBase;
	public Cylindre devantBase;
	public Cylindre pleinMilieu;

	// Au besoin, créer les classes nécessaires dans le package table

	/** point de départ du match à modifier a chaque base roulante */
	public static final Vec2 entryPosition = new Vec2(615,206);
	
	/**
	 * Instancie une nouvelle table
	 *
	 * @param log le système de log sur lequel écrire.
	 * @param config l'endroit ou lire la configuration du robot
	 */
	private Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;
		this.mObstacleManager = new ObstacleManager(log, config);
		initialise();
	}
	
	public void initialise() // initialise la table du debut du jeu
	{
		Balls cratereDepartB=new Balls(new Vec2(850, 540));
		 Balls cratereBaseLunaire=new Balls(new Vec2(500,1850 ));
		 Fusee fuseeDepart=new Fusee(new Vec2(350, 40), ColorModule.BLUE);
		 Fusee fuseeBase=new Fusee(new Vec2(1460, 1350),ColorModule.MULTI);
		 Cylindre devantDepart=new Cylindre(new Vec2(500,600),ColorModule.MULTI);
		 Cylindre cratereDepart=new Cylindre(new Vec2(1300,600),ColorModule.BLUE);
		 Cylindre cratereBase=new Cylindre(new Vec2(700,1850),ColorModule.BLUE);
		 Cylindre devantBase=new Cylindre(new Vec2(600,1400),ColorModule.MULTI);
		 Cylindre pleinMilieu=new Cylindre(new Vec2(1000,1100),ColorModule.MULTI);
		// TODO : initialiser les éléments de jeu définis plus haut
	}

	public ObstacleManager getObstacleManager()
	{
		return mObstacleManager;
	}

	/* (non-Javadoc)
	 * @see container.Service#updateConfig()
	 */
	@Override
	public void updateConfig()
	{
		// TODO update config
	}
}

