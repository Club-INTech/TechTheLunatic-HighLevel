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

package smartMath;

/**
 * Classe des cercles, utile pour les points d'entrée des scripts et le Pathfinding
 * @author paul
 */
public class Circle {

	/** Position du centre du cercle*/
	private Vec2 center;
	
	/** Rayon du cercle*/
	private double radius;

	/** Etendu de l'arc de cercle (en absolue et dans le sens trigo)*/
	private double angleStart = 0;
	private double angleEnd = 2*Math.PI;
	
	/**
	 * construit un cercle à partir de son centre et rayon
	 * @param center le centre
	 * @param radius le rayon en mm
	 */
	public Circle(Vec2 center, double radius)
	{
		this.center=center;
		this.radius=radius;
	}

	/**
	 * construit un cercle de rayon nul, soit un point
	 * @param center le centre en mm, pas de virgule
	 */
	public Circle(Vec2 center)
	{
		this.center=center;
		this.radius=0;
	}

	/**
	 * construit un arc de cercle à partir de son rayon, son centre et son étendue
	 * @param center le centre
	 * @param radius le rayon en mm
	 * @param angleStart l'angle du début de l'arc
	 * @param angleEnd l'angle de fin de l'arc
	 */
	public Circle(Vec2 center, double radius, double angleStart, double angleEnd)
	{
		this.center=center;
		this.radius=radius;
		this.angleStart=angleStart;
		this.angleEnd=angleEnd;
	}

	/**
	 * Getter de center
	 * @return le centre du cercle (position en mm)
	 */
	public Vec2 getCenter()
	{
		return this.center;
	}

	public double getRadius() {
		return radius;
	}

	public double getAngleStart() {
		return angleStart;
	}

	public double getAngleEnd() {
		return angleEnd;
	}

	/**
	 * test si le Vec2 est dans le disque
	 * @param point un vec2 a tester
	 * @return vrai si le point est a l'interieur du cercle ou dessus
	 */
	public boolean containDisk(Vec2 point)
	{
		return (point.distance(this.center)<=this.radius);
	}
	
	/**
	 * test si le Vec2 appartient au cercle
	 * @param point un Vec2 a tester
	 * @return vrai si le point est sur le cercle
	 */
	public boolean containCircle(Vec2 point)
	{
		double dx=point.x-this.center.x;
		double dy=point.y-this.center.y;
		return (dx*dx+dy*dy)==(radius*radius);
	}
}
