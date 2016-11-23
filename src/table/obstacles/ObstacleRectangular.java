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

package table.obstacles;

import pathfinder.Graphe;
import pathfinder.Noeud;
import smartMath.Segment;
import smartMath.Vec2;

import java.util.ArrayList;

/**
 * Obstacle rectangulaire sont les bords sont alignés avec les axes X et Y (pas de possibilité de faire un rectangle en biais).
 * 
 * @author pf, marsu
 */
public class ObstacleRectangular extends Obstacle
{
	/** Positon du centre du rectangle représentant l'obstacle (intersection des 2 diagonales)*/
	protected Vec2 positon;
	
	/** taille du rectangle en mm selon l'axe X */
	protected int sizeX;
	
	/** taille du rectangle en mm selon l'axe Y */
	protected int sizeY;

	private Noeud[] lNoeud={null,null,null,null};

    public Noeud[] getlNoeud() {
        return lNoeud;
    }

    /**
	 *	crée un nouvel obstacle rectangulaire sur la table a la position désirée.
	 *
	 * @param position Positon désirée du centre du rectangle représentant l'obstacle (intersection des 2 diagonales)
	 * @param sizeX taille voulue du rectangle représentant l'obstacle en mm selon l'axe X
	 * @param sizeY taille voulue du rectangle représentant l'obstacle en mm selon l'axe Y
	 */
	public ObstacleRectangular(Vec2 position, int sizeX, int sizeY)
	{
		super(position);
		this.sizeY = sizeY;
		this.sizeX = sizeX;
	}

	/* (non-Javadoc)
	 * @see table.obstacles.Obstacle#clone()
	 */
	public ObstacleRectangular clone()
	{
		return new ObstacleRectangular(position.clone(), sizeX, sizeY);
	}
	
	/* (non-Javadoc)
	 * @see table.obstacles.Obstacle#toString()
	 */
	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	
	/**
	 * Renvoit la taille du rectangle en mm selon l'axe Y
	 *
	 * @return the size y
	 */
	public int getSizeY()
	{
		return this.sizeY;
	}
	
	/**
	 *  Renvoit la taille du rectangle en mm selon l'axe X
	 *
	 * @return the size x
	 */
	public int getSizeX()
	{
		return this.sizeX;
	}
	
	/**
	 * Renvoie les Segments des diagonales du rectangle
	 */
	public ArrayList<Segment> getSegments()
	{
		ArrayList<Segment> segments = new ArrayList<Segment>();
		segments.add(new Segment(new Vec2(position.x + sizeX/2 , position.y + sizeY/2), new Vec2(position.x - sizeX/2 , position.y - sizeY/2)));
		segments.add(new Segment(new Vec2(position.x + sizeX/2 , position.y - sizeY/2), new Vec2(position.x - sizeX/2 , position.y + sizeY/2)));

		return segments;
	}
	
	/**
	 * Vérifie si le point donné est dans l'obstacle
	 * @param point le point à tester
	 */
	public boolean isInObstacle(Vec2 point)
	{
		return point.x <= position.x + (sizeX / 2)
				&& point.x >= position.x - (sizeX / 2)
				&& point.y <= position.y + (sizeY / 2)
				&& point.y >= position.y - (sizeY / 2);
	}
	
	/**
	 * Fourni la plus petite distance entre le point fourni et l'obstacle.
	 *
	 * @param point point a considérer
	 * @return la plus petite distance entre le point fourni et l'obstacle.
	 */
	public float distance(Vec2 point)
	{
		return (float) Math.sqrt(SquaredDistance(point));
	}
	
	/**
	 * Fourni la plus petite distance au carré entre le point fourni et l'obstacle.
	 *
	 * @param in  point a considérer
	 * @return la plus petite distance au carré entre le point fourni et l'obstacle
	 */
	public float SquaredDistance(Vec2 in)
	{
		
		/*		
		 *  Schéma de la situation :
		 *
		 * 		 												  y
		 * 			4	|		3		|		2					    ^
		 * 				|				|								|
		 * 		____________________________________				    |
		 * 				|				|								-----> x
		 * 				|				|
		 * 			5	|	obstacle	|		1
		 * 				|				|
		 * 		____________________________________
		 * 		
		 * 			6	|		7		|		8
		 * 				|				|
		 */		
		
		// calcul des positions des coins
		Vec2 coinBasGauche = position.plusNewVector((new Vec2(0,-sizeY)));
		Vec2 coinHautGauche = position.plusNewVector((new Vec2(0,0)));
		Vec2 coinBasDroite = position.plusNewVector((new Vec2(sizeX,-sizeY)));
		Vec2 coinHautDroite = position.plusNewVector((new Vec2(sizeX,0)));
		
		// si le point fourni est dans les quarts-de-plans n°2,4,6 ou 8
		if(in.x < coinBasGauche.x && in.y < coinBasGauche.y)
			return in.squaredDistance(coinBasGauche);
		
		else if(in.x < coinHautGauche.x && in.y > coinHautGauche.y)
			return in.squaredDistance(coinHautGauche);
		
		else if(in.x > coinBasDroite.x && in.y < coinBasDroite.y)
			return in.squaredDistance(coinBasDroite);

		else if(in.x > coinHautDroite.x && in.y > coinHautDroite.y)
			return in.squaredDistance(coinHautDroite);

		// Si le point fourni est dans les demi-bandes n°1,3,5,ou 7
		if(in.x > coinHautDroite.x)
			return (in.x - coinHautDroite.x)*(in.x - coinHautDroite.x);
		
		else if(in.x < coinBasGauche.x)
			return (in.x - coinBasGauche.x)*(in.x - coinBasGauche.x);

		else if(in.y > coinHautDroite.y)
			return (in.y - coinHautDroite.y)*(in.y - coinHautDroite.y);
		
		else if(in.y < coinBasGauche.y)
			return (in.y - coinBasGauche.y)*(in.y - coinBasGauche.y);

		// Sinon, on est dans l'obstacle
		return 0f;
	}

	public void changeDim(int sizeX, int sizeY)
	{
		this.sizeX=sizeX;
		this.sizeY=sizeY;
	}

	/**
	 * ajoute les noeuds liés à l'obstacle sur le graphe et les relie entre eux
	 * @param graphe
	 * @param ecart écart minimal par rapport à l'obstacle
	 *
	 */
	public ArrayList<Noeud> fabriqueNoeudRelie(Graphe graphe,int ecart) //fabrique n noeuds et les ajoute au grahe
	{
		ArrayList<Noeud> lN=fabriqueNoeud(graphe,ecart);
		lN.get(0).attachelien(lN.get(1));
		lN.get(1).attachelien(lN.get(0));
		lN.get(0).attachelien(lN.get(2));
		lN.get(2).attachelien(lN.get(0));

				// et on relie les noeuds
		lN.get(2).attachelien(lN.get(3));
		lN.get(3).attachelien(lN.get(2));
		lN.get(1).attachelien(lN.get(3));
		lN.get(3).attachelien(lN.get(1));

		return lN;
	}

	/**
	 *  Fabrique dans graphe les 4 noeuds aux angles d'un obstacle rectangulaire et retourne la liste de ces noeuds
	 * @param graphe là où on ajoute les noeuds
	 * @param ecart ecart par rapport à l'angle
	 * @return tableau des 4 noeuds
	 */
	public ArrayList<Noeud> fabriqueNoeud(Graphe graphe,int ecart)
	{
		Vec2 coinBasGauche = position.plusNewVector((new Vec2(0-ecart,-sizeY-ecart)));
		Vec2 coinHautGauche = position.plusNewVector((new Vec2(0-ecart,ecart)));
		Vec2 coinBasDroite = position.plusNewVector((new Vec2(sizeX+ecart,-sizeY -ecart )));
		Vec2 coinHautDroite = position.plusNewVector((new Vec2(sizeX+ecart,0-ecart)));

		ArrayList<Noeud> li=new ArrayList<Noeud>();
		li.add(new Noeud(graphe,coinBasDroite));
		li.add(new Noeud(graphe,coinBasGauche));
		li.add(new Noeud(graphe,coinHautDroite));
		li.add(new Noeud(graphe,coinHautGauche));
		// et on relie les noeuds



		return li;
	}

	/**
	 * Fabrique et relie les noeuds de obstacle deux obstacles entre eux via leur noeud le plus proche l'un de l'autre
	 * @param obstacle Obstacle à relier
	 * @param g graphe sur lequelon travaille
	 * @param ecart ecart
	 */
	public void relieObstacle(ObstacleRectangular obstacle, Graphe g,int ecart)
	{
		int mindist=1000000000; //distance de l'obstacle à l'autre
		ArrayList<Noeud> l1=this.fabriqueNoeudRelie(g,ecart);
		ArrayList<Noeud> l2=obstacle.fabriqueNoeudRelie(g,ecart);
		Noeud nmin1=null;
		Noeud nmin2=null;
		for (Noeud n2:l2) {
			for (Noeud x : l1) {
				int calc = x.position.minusNewVector(n2.position).squaredLength();
				if (calc < mindist) {
					mindist = calc;
					nmin1 = x;
					nmin2=n2;

				}
			}
		}
		nmin1.attachelien(nmin2);

	}
	
}
