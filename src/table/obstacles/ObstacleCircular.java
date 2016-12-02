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
import smartMath.Circle;
import smartMath.Segment;
import smartMath.Vec2;

import java.util.ArrayList;

/**
 * Obstacle de forme circulaire.
 *
 * @author pf, marsu
 */
public class ObstacleCircular extends Obstacle
{
	
	/** rayon en mm de cet obstacle */
	protected int radius=0;
	private ArrayList<Noeud> lNoeud;
	
	/**
	 * crée un nouvel obstacle de forme circulaire a la position et a la taille spécifiée.
	 *
	 * @param position position du centre de l'obstacle a créer
	 * @param radius rayon de l'obstacle a créer 
	 */
	public ObstacleCircular(Vec2 position, int radius)
	{
		super(position);
		this.radius = radius;
	}

	
	/* (non-Javadoc)
	 * @see table.obstacles.Obstacle#clone()
	 */
	public ObstacleCircular clone()
	{
		return new ObstacleCircular(position.clone(), radius);
	}
	
	/**
	 * Verifie si a == b pour des obstacles circulaires
	 * @param otherObstacle b
	 * @return true si a == b
	 */
	public boolean equals(ObstacleCircular otherObstacle) 
	{
		return (
				this.radius == otherObstacle.radius
			&&  this.position.equals(otherObstacle.position)	
				);
	}

	/**
	 * Copie this dans other, sans modifier this
	 *
	 * @param other l'obstacle circulaire a modifier
	 */
	public void clone(ObstacleCircular other)
	{
		other.position = position;
		other.radius = radius;
	}

	/**
	 * Peut faire null pointer exception si on vérifie pas que la liste des noeuds n'est pas vide
	 * @param pointInCentre
	 * @return
	 */
	public Noeud noeudProche(Vec2 pointInCentre)
	{
		float distmin=Integer.MAX_VALUE;
		Noeud noeudMin=null;
		for (Noeud x:this.lNoeud)
		{
			float a=pointInCentre.distance(x.position);
			if (a<distmin)
			{
				distmin=a;
				noeudMin=x;
			}
		}

		return noeudMin;
	}

	/**
	 * Vérifie si le point donné est dans l'obstacle
	 * @param point le point à tester
	 */
	public boolean isInObstacle(Vec2 point)
	{
		return ((Segment.squaredLength(point, position) < radius*radius));
	}

	/**
	 * Donne le rayon de cet obstacle circulaire.
	 *
	 * @return le rayon de cet obstacle circulaire.
	 */
	public int getRadius()
	{
		return radius;
	}

	public void setRadius(int radius)
	{
		this.radius = radius;
	}
	
	/**
	 * Convertit l'obstacle en cercle.
	 * 
	 * @return
	 */
	@SuppressWarnings("javadoc")
	public Circle toCircle()
	{
		return new Circle(position, radius);
	}
	
	/* (non-Javadoc)
	 * @see table.obstacles.Obstacle#toString()
	 */


	/**
	 * Créer les noeuds autour de l'obstacle en commençant à 0 sur un cercle trigonométrique usuel radians puis tous les 2kpi/n
	 * @param graphe le graphe sur lequel on doit créer les noeuds
	 * @param n le nombre de noeud à créer autour de l'obstacle circulaire
	 * @param ecart écart par rapport a la distance minimale
	 * @return liste des noeuds crées
	 */
	public ArrayList<Noeud> fabriqueNoeud(Graphe graphe,int n,int ecart) //fabrique n noeuds et les ajoute au grahe et les renvoie
	{
		ArrayList<Noeud> myList = new ArrayList<Noeud>();
		double h=(this.getRadius()+ecart)/Math.cos(Math.PI/n);
		for (int i=0;i<n;i++)
		{

			Vec2 spin=new Vec2((int)(h*Math.cos(2*Math.PI*i/n)), (int) (h*Math.sin(Math.PI*2*i/n)));
			Vec2 po=this.getPosition().plusNewVector(spin);
			if(Math.abs(po.x)<=1500 && po.y<=2000 && po.y>=0) {
				Noeud noeudact = new Noeud(graphe, po);

				myList.add(noeudact);

				graphe.getlNoeuds().add(noeudact);
			}
		}
		this.lNoeud=myList;

		return myList;
	}


	public String toString()
	{
		return "Obstacle circulaire de centre " + position + " et de rayon: "+radius;
	}
	
	public void printObstacleDeleted()
	{
		System.out.println("Obstacle enlevé");
	}
	
	public void printObstacleMemory()
	{
		System.out.println("Obstacle en memoire");
	}
}
