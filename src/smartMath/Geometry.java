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

import table.obstacles.ObstacleRectangular;

/**
 * classe de calculs de géométrie
 * @author Etienne
 */
public class Geometry
{
	/**
	 * calcule le "vrai" modulo (entre 0 et module) contairement a % qui calcule entre -module et module
	 * @param number le nombre dont on veut calculer le modulo
	 * @param module le module pour le modulo
	 * @return number [module]
	 */
	public static double modulo(double number, double module)
	{
		double modulo = number%module;
		if (modulo<0)
			modulo += module;
		return modulo;
	}
	
	/**
	 * calcule la différence entre deux angles dans un cercle (prends en compte le fait que le cercle soit circulaire)
	 * @param angle1 le premier angle entre 0 et sizeOfCircle
	 * @param angle2 le deuxieme angle entre 0 et sizeOfCircle (on peut échanger angle1 et angle2 sans changer le retour)
	 * @param sizeOfCircle la taille du cercle (2Pi en Radiant 2000Pi en milliRadian ou 360 en Degré par exemple)
	 * @return angle1 - angle2 dans un espace circulaire, forcement < a sizeOfCircle
	 */
	public static double minusAngle(double angle1, double angle2, double sizeOfCircle)
	{
		double angleMin = Math.min(angle1, angle2);
		double angleMax = Math.max(angle1, angle2);
		return Math.min(angleMax-angleMin, sizeOfCircle-angleMax+angleMin);
	}
/**
	public static boolean intersects(Segment seg,ObstacleRectangular obstacle)
	{

		if(obstacle.getlNoeud().size() > 0)
		{
			Vec2 ab=seg.getA().minusNewVector(seg.getB());
			Vec2 ac=seg.getA().minusNewVector(obstacle.getlNoeud().get(0).position);
			Vec2 ad=seg.getA().minusNewVector(obstacle.getlNoeud().get(1).position);
			Vec2 ae=seg.getA().minusNewVector(obstacle.getlNoeud().get(2).position);
			Vec2 af=seg.getA().minusNewVector(obstacle.getlNoeud().get(3).position);
			int p1=ab.dot(ac);
			int p2=ab.dot(ad);
			int p3=ab.dot(ae);
			int p4=ab.dot(af);
			boolean b1= ab.dot(ac) * ab.dot(ad)>=0;
			boolean b2= (ab.dot(ad)* ab.dot(ae)>=0);
			boolean b3= (ab.dot(ae)* ab.dot(af)>=0);

			return (!(b1 && b2 && b3));

		}

		return false;
	}*/


// Compute the bit code for a point (x, y) using the clip rectangle
// bounded diagonally by (xmin, ymin), and (xmax, ymax)

// ASSUME THAT xmax, xmin, ymax and ymin are global constants.

	public static int ComputeOutCode(double x, double y,ObstacleRectangular obs)

	{

		int INSIDE = 0; // 0000
		int LEFT = 1;   // 0001
		int RIGHT = 2;  // 0010
		int BOTTOM = 4; // 0100
		int TOP = 8;    // 1000
		int code;
		int xmin=obs.getlNoeud().get(1).position.x;
		int xmax=obs.getlNoeud().get(0).position.x;
		int ymin=obs.getlNoeud().get(0).position.y;
		int ymax=obs.getlNoeud().get(1).position.y;
		code = INSIDE;          // initialised as being inside of [[clip window]]

		if (x < xmin) //         // to the left of clip window
		{code |= LEFT;}
		else if (x > xmax)      // to the right of clip window
		{code |= RIGHT;}
		if (y < ymin)           // below the clip window
		{code |= BOTTOM;}
		else if (y > ymax)      // above the clip window
		{code |= TOP;}

		return code;
	}

	// Cohen–Sutherland clipping algorithm clips a line from
// P0 = (x0, y0) to P1 = (x1, y1) against a rectangle with
// diagonal from (xmin, ymin) to (xmax, ymax).

	public static boolean CohenSutherlandLineClipAndDraw(double x0, double y0, double x1, double y1,ObstacleRectangular obs) {


		int INSIDE = 0; // 0000
		int LEFT = 1;   // 0001
		int RIGHT = 2;  // 0010
		int BOTTOM = 4; // 0100
		int TOP = 8;    // 1000
		int xmin = obs.getlNoeud().get(1).position.x;
		int xmax = obs.getlNoeud().get(0).position.x;
		int ymin = obs.getlNoeud().get(0).position.y;
		int ymax = obs.getlNoeud().get(1).position.y;

		// compute outcodes for P0, P1, and whatever point lies outside the clip rectangle
		int outcode0 = ComputeOutCode(x0, y0, obs);
		int outcode1 = ComputeOutCode(x1, y1, obs);
		boolean accept = false;

		while (true) {
			if ((outcode0 | outcode1) == 0) { // Bitwise OR is 0. Trivially accept and get out of loop
				accept = true;
				break;
			} else if ((outcode0 & outcode1) != 0) { // Bitwise AND is not 0. Trivially reject and get out of loop
				break;
			} else {
				// failed both tests, so calculate the line segment to clip
				// from an outside point to an intersection with clip edge
				double x = 0;
				double y = 0;

				// At least one endpoint is outside the clip rectangle; pick it.
				int outcodeOut = outcode0 > 0 ? outcode0 : outcode1;

				// Now find the intersection point;
				// use formulas y = y0 + slope * (x - x0), x = x0 + (1 / slope) * (y - y0)
				if ((outcodeOut & TOP) > 0) {           // point is above the clip rectangle
					x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
					y = ymax;
				} else if ((outcodeOut & BOTTOM) != 0) { // point is below the clip rectangle
					x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
					y = ymin;
				} else if ((outcodeOut & RIGHT) != 0) {  // point is to the right of clip rectangle
					y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
					x = xmax;
				} else if ((outcodeOut & LEFT) != 0) {   // point is to the left of clip rectangle
					y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
					x = xmin;
				}

				// Now we move outside point to intersection point to clip
				// and get ready for next pass.
				if (outcodeOut == outcode0) {
					x0 = x;
					y0 = y;
					outcode0 = ComputeOutCode(x0, y0, obs);
				} else {
					x1 = x;
					y1 = y;
					outcode1 = ComputeOutCode(x1, y1, obs);
				}
			}
		}
		return accept;
	}

	/**
	 * dit si l'angleEnd est plus loin que angleBegin sur le cercle trigo (prends en compte le fait que le cercle soit circulaire)
	 * @param angleBegin l'angle de position initiale (doit être en Radian pas milliRadian)
	 * @param angleEnd l'angle dont on veut savoir si il est plus loin sur le cercle trigo (doit être en Radian et pas en milliRadian)
	 * @return true si angleEnd est plus loin que angleBegin sur le cercle trigo
	 */
	public static boolean isFurtherInTrigoCircle(double angleBegin, double angleEnd) 
	{
		// si on ne passe pas par 2PI alors sera le plus petit (en abs)
		double possibility1 = modulo(angleEnd, 2*Math.PI)-modulo(angleBegin, 2*Math.PI);
		// si on passe par 2PI alors sera le plus petit (en abs)
		double possibility2 = modulo(angleEnd, 2*Math.PI)+2*Math.PI-modulo(angleBegin, 2*Math.PI);
		
		if (Math.abs(possibility1)<Math.abs(possibility2))
			//on ne passe pas par 2PI, on regarde si on tourne dans le sens trigo
			return possibility1>0;
		else
			//on passe par 2PI, on regarde si on tourne dans le sens trigo
			return possibility2>0;
	}
	
	/**
	 * 
	 * @param segment1
	 * @param segment2
	 * @return vrai si il y a intersection entre les deux segments, faux sinon (les extremités ne sont pas comptées comme intersection)
	 */
	public static boolean intersects(Segment segment1, Segment segment2)
	{
		// les points formant les segments 1 et 2 sont A1, B1, A2, B2
		// pour qu'il y ait intersection, il faut :
		// - les segments ne soient pas parallèles : (A1B1)^(A2B2) != 0
		// - le point d'intersection est entre A2 et B2 : (A1B1)^(A1B2) * (A1B1)^(A1A2) < 0
		// - le point d'intersection est entre A1 et B1 : (A2B2)^(A2B1) * (A2B2)^(A2A1) < 0
		// ^ = produit vectoriel
		return ((double)segment1.getB().x - (double)segment1.getA().x) * ((double)segment2.getB().y - (double)segment2.getA().y) - ((double)segment1.getB().y - (double)segment1.getA().y) * ((double)segment2.getB().x - (double)segment2.getA().x) != 0
				&& (((double)segment1.getB().x - (double)segment1.getA().x) * ((double)segment2.getB().y - (double)segment1.getA().y) - ((double)segment1.getB().y - (double)segment1.getA().y) * ((double)segment2.getB().x - (double)segment1.getA().x)) * (((double)segment1.getB().x - (double)segment1.getA().x) * ((double)segment2.getA().y - (double)segment1.getA().y) - ((double)segment1.getB().y - (double)segment1.getA().y) * ((double)segment2.getA().x - (double)segment1.getA().x)) < 0
				&& (((double)segment2.getB().x - (double)segment2.getA().x) * ((double)segment1.getB().y - (double)segment2.getA().y) - ((double)segment2.getB().y - (double)segment2.getA().y) * ((double)segment1.getB().x - (double)segment2.getA().x)) * (((double)segment2.getB().x - (double)segment2.getA().x) * ((double)segment1.getA().y - (double)segment2.getA().y) - ((double)segment2.getB().y - (double)segment2.getA().y) * ((double)segment1.getA().x - (double)segment2.getA().x)) < 0
				;
	}
	
	/**
	 * 
	 * @param segment
	 * @param circle
	 * @return vrai si il y a intersection entre le segment et le cercle, faux sinon
	 */
	public static boolean intersects(Segment segment, Circle circle)
	{
		// TODO : expliquer l'algo (TOO MANY CASTS EXCEPTION)
		double area = ((double)circle.center.x - (double)segment.getA().x)*((double)segment.getB().y - (double)segment.getA().y) - ((double)circle.center.y - (double)segment.getA().y)*((double)segment.getB().x - (double)segment.getA().x);
		double distA = ((double)segment.getA().x - (double)circle.center.x)*((double)segment.getA().x - (double)circle.center.x) + ((double)segment.getA().y - (double)circle.center.y)*((double)segment.getA().y - (double)circle.center.y);
		double distB = ((double)segment.getB().x - (double)circle.center.x)*((double)segment.getB().x - (double)circle.center.x) + ((double)segment.getB().y - (double)circle.center.y)*((double)segment.getB().y - (double)circle.center.y);
		if(distA >= circle.radius * circle.radius && distB < circle.radius * circle.radius || distA < circle.radius * circle.radius && distB >= circle.radius * circle.radius)
			return true;
		return distA >= circle.radius * circle.radius
			&& distB >= circle.radius * circle.radius
			&& area * area / (((double)segment.getB().x - (double)segment.getA().x)*((double)segment.getB().x - (double)segment.getA().x)+((double)segment.getB().y - (double)segment.getA().y)*((double)segment.getB().y - (double)segment.getA().y)) <= circle.radius * circle.radius
			&& ((double)segment.getB().x - (double)segment.getA().x)*((double)circle.center.x - (double)segment.getA().x) + ((double)segment.getB().y - (double)segment.getA().y)*((double)circle.center.y - (double)segment.getA().y) >= 0
			&& ((double)segment.getA().x - (double)segment.getB().x)*((double)circle.center.x - (double)segment.getB().x) + ((double)segment.getA().y - (double)segment.getB().y)*((double)circle.center.y - (double)segment.getB().y) >= 0;
	}
	
	
	
	/**
	 * 
	 * @param segment1
	 * @param segment2
	 * @return le point d'intersection des droites portées par les segments.
	 */
	public static Vec2 intersection(Segment segment1, Segment segment2)
	{
		// resolution du systeme associe aux deux segments
		double inter, k;
		
		if((segment2.getB().y - segment2.getA().y) != 0)
		{
			inter = (double)(segment2.getB().x - segment2.getA().x) / (double)(segment2.getB().y - segment2.getA().y);
			k = (segment1.getA().x - segment2.getA().x + inter * (double)(segment2.getA().y - segment1.getA().y)) / (segment1.getB().x - segment1.getA().x - inter * (segment1.getB().y - segment1.getA().y));
		}
		else
			k = -(double)(segment2.getA().y - segment1.getA().y) / (double)(segment1.getB().y - segment1.getA().y);
		
		return new Vec2((int)(segment1.getA().x - k * (segment1.getB().x - segment1.getA().x)), (int)(segment1.getA().y - k * (segment1.getB().y - segment1.getA().y)));
	}

	/**
	 * Vérifie si la valeur donnée est entre les bornes données (limites incluses), utilisé pour simplifier les if
	 * @param val la valeur à tester
	 * @param a borne inf
	 * @param b borne sup
     */
	public static boolean isBetween(double val, double a, double b)
	{
		if(a>b) //Si le singe a mie de pain inf à la place de sup
		{
			double temp=b;
			b=a;
			a=temp;
		}

		return val >= a && val <= b;
	}
}
