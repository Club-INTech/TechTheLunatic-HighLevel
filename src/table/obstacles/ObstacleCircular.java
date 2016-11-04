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

/**
 * Obstacle de forme circulaire.
 *
 * @author pf, marsu
 */
public class ObstacleCircular extends Obstacle
{
	
	/** rayon en mm de cet obstacle */
	protected int radius=0;
	
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
	public void fabriqueNoeud(Graphe graphe,int n,int ecart) //fabrique n noeuds et les ajoute au grahe
	{
		Noeud[] myList = new Noeud[10];
		Noeud noeudact;
		for (int i=0;i<n;i++)
		{
			Vec2 spin=new Vec2((int)(this.getRadius()*Math.cos(2*Math.PI/n)/Math.cos(Math.PI/n))+ecart, (int) (this.getRadius()*Math.sin(Math.PI*2/n)/Math.cos(Math.PI/n))+ecart);
			noeudact=new Noeud(graphe,this.getPosition().plusNewVector(spin));
			myList[i]=noeudact;
			graphe.getlNoeuds().add(noeudact);

			//p=h/cos(pi/n)
			// on fait les liens
		}
		for (int i=0;i<n;i++)
		{ myList[i].attachelien(myList[i%n]);
		}

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
