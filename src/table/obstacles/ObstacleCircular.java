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

	/**
	 * Ajoute sur le graphe n noeud autour du cercle délimitant l'obstacle
	 * @param graphe
	 * @param n
	 * @param ecart
	 */
	public Noeud[] fabriqueNoeudRelie(Graphe graphe,int n,int ecart) //fabrique n noeuds et les ajoute au grahe et les renvoie
	{
		Noeud[] myList = new Noeud[n];
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
return myList;
	}
	public Noeud[] fabriqueNoeud(Graphe graphe,int n,int ecart) //fabrique n noeuds et les ajoute au grahe et les renvoie
	{
		Noeud[] myList = new Noeud[n];
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
		return myList;
	}


	/**
	 * relie deux obstacles circulaires après avoir fabriqués les noeuds et arrêtes autour de l'obstacle sur un graphe de manière optimale. Ne vérifie PAS qu'il n'y a pas d'objet au milieu
	 * @param obstacle
	 * @param g
	 * @param n
	 * @param ecart
	 */
	public void relieObstacle(ObstacleCircular obstacle, Graphe g,int n,int ecart)
	{
		int mindist1=this.position.minusNewVector(obstacle.position).squaredLength(); //distance de l'obstacle à l'autre
		int mindist2=this.position.minusNewVector(obstacle.position).squaredLength(); //distance de l'obstacle à l'autre
		Noeud[] l1=this.fabriqueNoeud(g,n,ecart);
		Noeud[] l2=obstacle.fabriqueNoeud(g,n,ecart);
		Noeud nmin1=null;
		for (Noeud x:l1)
		{
			int calc=x.position.minusNewVector(obstacle.position).squaredLength();
			if(calc<mindist1)
			{
				mindist1=calc;
				nmin1=x;

			}
		}
		Noeud nmin2=null;
		for (Noeud x:l2)
		{
			int calc=x.position.minusNewVector(this.position).squaredLength();
			if(calc<mindist2)
			{
				mindist2=calc;
				nmin2=x;

			}
		}
		nmin1.attachelien(nmin2);

	}
	/**
	 * relie un obstacle circulaire, un rectangulaire sur un graphe après avoir fabriqués les noeuds et arrêtes autour de l'obstacle de manière optimale. Ne vérifie PAS qu'il n'y a pas d'objet au milieu
	 * @param obstacle
	 * @param g
	 * @param n
	 * @param ecart
	 */
	public void relieObstacle(ObstacleRectangular obstacle, Graphe g,int n,int ecart)
	{
		int mindist=1000000000; //distance de l'obstacle à l'autre
		Noeud[] l1=this.fabriqueNoeudRelie(g,n,ecart);
		Noeud[] l2=obstacle.fabriqueNoeudRelie(g,ecart);
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
