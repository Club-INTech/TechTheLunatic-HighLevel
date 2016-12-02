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
 * Protocole des actionneurs.
 * contient pour chaque actionneur le nom des consignes java, la chaine à transmetttre à la carte actionneurs et la durée que cette action prend
 * @author pf, marsu
 *
 */

public enum ActuatorOrder
{
	// Syntaxe: NOM_METHODE("protocole_serie") Cette syntaxe suppose que l'action prends une seconde pour s'exécuter
	// Syntaxe alternative: NOM_METHODE("protocole_serie", durée actions)
	// exemple : MOVE_FORWARD("av")
	
	// Consignes avancer / reculer
	MOVE_FORWARD("av"),
	MOVE_BACKWARD("rc"),
	TURN_RIGHT("td"),
	TURN_LEFT("tg"),
	SSTOP("sstop"),
	MONTLHERY("montlhery"),
	
	// TODO : rajouter les actionneurs (AX-12, moteurs...)

	// TODO : Ajouter des durées pour les actions qui prennent du temps(selon les tests qu'on fera)

					//		3 POSITIONS DES BRAS DE LA PELLETEUSE 		 //
	// Rangement de la pelleteuse
	REPLIER_PELLETEUSE("bpr"),

	// Déploiement de la pelleteuse
	DEPLOYER_PELLETEUSE("bpd"),

	// Position intermédiaire de la pelleteuse, pour rotations de la pelle
	MED_PELLETEUSE("bpi"),

					//		3 POSITIONS DE LA PELLE		  //
	// Position avant prise de boules
	PRET_PELLE("ppd"),

	// Position intermédiaire de la pelle, maintient les boules
	TIENT_PELLE("ppi"),

	//Position de livraison de boules de la pelle
	LIVRE_PELLE("ppf"),

					//		3 POSITIONS DES ATTRAPE-MODULES		//
	// Position repliée
	PRET_ATTRAPE_M("pam"),

	// Position Retour (pour laisser passer la calle)
	RETOUR_ATTRAPE_M("ram"),

	// Position Post-livraison
	LIVRE_ATTRAPE_M("lam"),

					//		2 POSITIONS DES CALLAGES-MODULES	//
	// Positions repliée
	REPLI_CALLE("rca"),

	// Positions calle
	LIVRE_CALLE("lca"),

					//		2 POSITIONS DE L'ASCENCEUR-MODULES   //
	// Position basse
	BAISSE_ASC("bas"),

	// Position haute
	LEVE_ASC("las"),

	STOP("stop");

	/**
	 *  chaine de caractère envoyée au travers de la liaison série
	 */
	private String serialOrder;

	/** duurée de l'action en millisecondes */
	private int duration;

	/**
	 * Construit un ordre pour un actionneur
	 * on suppose que son temps d'exécution est d'une seconde
	 * @param serialString la chaine de caractère à envoyer à la carte actionnneurs
	 */
    ActuatorOrder(String serialString)
	{
		this.serialOrder = serialString;
		this.duration = 700;	// valeur par défaut de la durée de mouvement d'un actionneur
	}

	/**
	 * Construit un ordre pour un actionneur avec le temps d'exécution spécifié 
	 * @param serialString la chaine de caractère à envoyer à la carte actionnneurs
	 */
    ActuatorOrder(String serialString, int duration)
	{
		this.serialOrder = serialString;
		this.duration = duration;
	}
	
	/**
	 * Retrouve la chaine de caractère a envoyer par la série a la carte actionneur pour qu'elle effectue cet ordre
	 * @return la chaine de caractère à envoyer par la série à la carte actionneur
	 */
	public String getSerialOrder()
	{
		return serialOrder;
	}

	/**
	 * Renvoie la durée de l'action
	 * @return durée d'exécution de l'action
	 */
	public int getDuration()
	{
		return duration;
	}
}
