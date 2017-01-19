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


// Compute the bit code for a point (x, y) using the clip rectangle
// bounded diagonally by (xmin, ymin), and (xmax, ymax)

// ASSUME THAT xmax, xmin, ymax and ymin are global constants.

	public static int ComputeOutCode(Vec2 pos, Vec2 hautGauche, Vec2 basDroite)

	{

		int INSIDE = 0; // 0000
		int LEFT = 1;   // 0001
		int RIGHT = 2;  // 0010
		int BOTTOM = 4; // 0100
		int TOP = 8;    // 1000
		int code;
		int xmin=hautGauche.getX();
		int xmax=basDroite.getX();
		int ymin=basDroite.getY();
		int ymax=hautGauche.getY();
		code = INSIDE;          // initialised as being inside of [[clip window]]

		if (pos.getX() < xmin) //         // to the left of clip window
		{code |= LEFT;}
		else if(pos.getX()>xmax)
		{
			code|=RIGHT;
		}
		if (pos.getY() < ymin)           // below the clip window
		{code |= BOTTOM;}
		else if (pos.getY() > ymax)      // above the clip window
		{code |= TOP;}

		return code;
	}

	// Cohen–Sutherland clipping algorithm clips a line from
// P0 = (x0, y0) to P1 = (x1, y1) against a rectangle with
// diagonal from (xmin, ymin) to (xmax, ymax).

	public static boolean CohenSutherlandLineClipAndDraw(Vec2 depart,Vec2 arrivee,Vec2 hautGauche,Vec2 basDroite)
	{


		int INSIDE = 0; // 0000
		int LEFT = 1;   // 0001
		int RIGHT = 2;  // 0010
		int BOTTOM = 4; // 0100
		int TOP = 8;    // 1000
		int xmin = hautGauche.getX();
		int xmax = basDroite.getX();
		int ymin = basDroite.getY();
		int ymax = hautGauche.getY();

		// compute outcodes for P0, P1, and whatever point lies outside the clip rectangle
		int outcode0 = ComputeOutCode(depart, hautGauche, basDroite);
		int outcode1 = ComputeOutCode(arrivee, hautGauche, basDroite);
		boolean traverse = false;

		while (true) {
			int or=(outcode0|outcode1);
			int and=(outcode0&outcode1);
			if ((outcode0|outcode1)==0 ) { // Bitwise OR is 0. Trivially accept and get out of loop
				traverse = true;
				break;
			} else if ((outcode0&outcode1)!=0 ) { // Bitwise AND is not 0. Trivially reject and get out of loop
				break;
			}

			 else {
				// failed both tests, so calculate the line segment to clip
				// from an outside point to an intersection with clip edge
				double x = 0;
				double y = 0;
				double x0=depart.getX();
				double y0=depart.getY();
				double y1=arrivee.getY();
				double x1=arrivee.getX();


				// At least one endpoint is outside the clip rectangle; pick it.
				int outcodeOut = outcode0 > 0 ? outcode0 : outcode1;

				// Now find the intersection point;
				// use formulas y = y0 + slope * (x - x0), x = x0 + (1 / slope) * (y - y0)
				if ((outcodeOut&TOP)>0 ) {           // point is above the clip rectangle
					x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
					y = ymax;
				} else if ((outcodeOut &BOTTOM)>0 ) { // point is below the clip rectangle
					x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
					y = ymin;
				} else if ((outcodeOut & RIGHT)>0) {  // point is to the right of clip rectangle
					y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
					x = xmax;
				} else if ((outcodeOut & LEFT) > 0) {   // point is to the left of clip rectangle
					y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
					x = xmin;
				}

				// Now we move outside point to intersection point to clip
				// and get ready for next pass.
				if (outcodeOut == outcode0) {
					x0 = x;
					y0 = y;
					outcode0 = ComputeOutCode(new Vec2((int)x0,(int)y0), hautGauche,basDroite);
				} else {
					x1 = x;
					y1 = y;
					outcode1 = ComputeOutCode(new Vec2((int)x1,(int)y1), hautGauche,basDroite);
				}
			}
		}
		return traverse;
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
		return ((double)segment1.getB().getX() - (double)segment1.getA().getX()) * ((double)segment2.getB().getY() - (double)segment2.getA().getY()) - ((double)segment1.getB().getY() - (double)segment1.getA().getY()) * ((double)segment2.getB().getX() - (double)segment2.getA().getX()) != 0
				&& (((double)segment1.getB().getX() - (double)segment1.getA().getX()) * ((double)segment2.getB().getY() - (double)segment1.getA().getY()) - ((double)segment1.getB().getY() - (double)segment1.getA().getY()) * ((double)segment2.getB().getX() - (double)segment1.getA().getX())) * (((double)segment1.getB().getX() - (double)segment1.getA().getX()) * ((double)segment2.getA().getY() - (double)segment1.getA().getY()) - ((double)segment1.getB().getY() - (double)segment1.getA().getY()) * ((double)segment2.getA().getX() - (double)segment1.getA().getX())) < 0
				&& (((double)segment2.getB().getX() - (double)segment2.getA().getX()) * ((double)segment1.getB().getY() - (double)segment2.getA().getY()) - ((double)segment2.getB().getY() - (double)segment2.getA().getY()) * ((double)segment1.getB().getX() - (double)segment2.getA().getX())) * (((double)segment2.getB().getX() - (double)segment2.getA().getX()) * ((double)segment1.getA().getY() - (double)segment2.getA().getY()) - ((double)segment2.getB().getY() - (double)segment2.getA().getY()) * ((double)segment1.getA().getX() - (double)segment2.getA().getX())) < 0
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
		double area = ((double)circle.getCenter().getX() - (double)segment.getA().getX())*((double)segment.getB().getY() - (double)segment.getA().getY()) - ((double)circle.getCenter().getY() - (double)segment.getA().getY())*((double)segment.getB().getX() - (double)segment.getA().getX());
		double distA = ((double)segment.getA().getX() - (double)circle.getCenter().getX())*((double)segment.getA().getX() - (double)circle.getCenter().getX()) + ((double)segment.getA().getY() - (double)circle.getCenter().getY())*((double)segment.getA().getY() - (double)circle.getCenter().getY());
		double distB = ((double)segment.getB().getX() - (double)circle.getCenter().getX())*((double)segment.getB().getX() - (double)circle.getCenter().getX()) + ((double)segment.getB().getY() - (double)circle.getCenter().getY())*((double)segment.getB().getY() - (double)circle.getCenter().getY());
		if(distA >= circle.getRadius() * circle.getRadius() && distB < circle.getRadius() * circle.getRadius() || distA < circle.getRadius() * circle.getRadius() && distB >= circle.getRadius() * circle.getRadius())
			return true;
		return distA >= circle.getRadius() * circle.getRadius()
			&& distB >= circle.getRadius() * circle.getRadius()
			&& area * area / (((double)segment.getB().getX() - (double)segment.getA().getX())*((double)segment.getB().getX() - (double)segment.getA().getX())+((double)segment.getB().getY() - (double)segment.getA().getY())*((double)segment.getB().getY() - (double)segment.getA().getY())) <= circle.getRadius() * circle.getRadius()
			&& ((double)segment.getB().getX() - (double)segment.getA().getX())*((double)circle.getCenter().getX() - (double)segment.getA().getX()) + ((double)segment.getB().getY() - (double)segment.getA().getY())*((double)circle.getCenter().getY() - (double)segment.getA().getY()) >= 0
			&& ((double)segment.getA().getX() - (double)segment.getB().getX())*((double)circle.getCenter().getX() - (double)segment.getB().getX()) + ((double)segment.getA().getY() - (double)segment.getB().getY())*((double)circle.getCenter().getY() - (double)segment.getB().getY()) >= 0;
	}

	/**
	 *
	 * @param pointHorsCercle
	 * @param circle
	 * @return le point de l'arc de cercle le plus proche de pointHorsCercle
	 */
	public static Vec2 pointProche(Vec2 pointHorsCercle, Circle circle) {
		if (circle.containCircle(pointHorsCercle)) {
			return (pointHorsCercle);
		}
		Vec2 vec = new Vec2(- circle.getCenter().getX() + pointHorsCercle.getX(),
				- circle.getCenter().getY() + pointHorsCercle.getY());

		// Si la direction donnée par le vecteur pointHorsCercle intersecte l'arc de cercle, on a le point avec un simple Thalès
		if (vec.getA() >= circle.getAngleStart() && vec.getA() <= circle.getAngleEnd())
		{
			return pointHorsCercle.minusNewVector(new Vec2((int) ((pointHorsCercle.distance(circle.getCenter()) - circle.getRadius()) * (pointHorsCercle.getX() - circle.getCenter().getX()) / pointHorsCercle.distance(circle.getCenter())), (int) ((pointHorsCercle.distance(circle.getCenter()) - circle.getRadius()) * (pointHorsCercle.getY() - circle.getCenter().getY()) / pointHorsCercle.distance(circle.getCenter()))));
		}

		// Sinon, on doit choisir entre le point du début de l'arc de cercle et celui de fin
		else {
			Vec2 circleCenterStart = new Vec2(circle.getRadius(), circle.getAngleStart());
			Vec2 circleCenterEnd = new Vec2(circle.getRadius(), circle.getAngleEnd());

			if (circle.getCenter().plusNewVector(circleCenterStart).distance(pointHorsCercle) >= circle.getCenter().plusNewVector(circleCenterEnd).distance(pointHorsCercle)){
				return circleCenterEnd.plusNewVector(circle.getCenter());
			}
			else{
				return circleCenterStart.plusNewVector(circle.getCenter());
			}
		}
	}
	/**
	 * Retourne le point a l'extérieur du cercle en continuant la ligne droite depuis le centre du cercle
	 * @param pointDansCercle le point dans le cercle (attention cela n'est pas vérifié)
	 * @param circle le cercle
	 * @return la nouvelle position du point
	 */
	public static Vec2 pointExterieurv1(Vec2 pointDansCercle, Circle circle)
	{
		if(pointDansCercle.getX()>circle.getCenter().getX())
		{
			double x2=circle.getRadius()/Math.sqrt(1+(circle.getCenter().getX()-pointDansCercle.getX())/(circle.getCenter().getY()-pointDansCercle.getY())*(circle.getCenter().getX()-pointDansCercle.getX())/(circle.getCenter().getY()-pointDansCercle.getY()));
			Vec2 aAjouter=new Vec2((int)x2,(int)x2*(circle.getCenter().getY()-pointDansCercle.getY())/(circle.getCenter().getX()-pointDansCercle.getX()));
			return circle.getCenter().plusNewVector(aAjouter);
		}
		else if(pointDansCercle.getX()<circle.getCenter().getX())
		{
			double x2=circle.getRadius()/Math.sqrt(1+(circle.getCenter().getX()-pointDansCercle.getX())/(circle.getCenter().getY()-pointDansCercle.getY())*(circle.getCenter().getX()-pointDansCercle.getX())/(circle.getCenter().getY()-pointDansCercle.getY()));
			Vec2 aAjouter=new Vec2((int)x2,(int)x2*(circle.getCenter().getY()-pointDansCercle.getY())/(circle.getCenter().getX()-pointDansCercle.getX()));
			return circle.getCenter().minusNewVector(aAjouter);
		}
	 else
		{
			return circle.getCenter().plusNewVector(new Vec2(0,(int)circle.getRadius()));
		}
	}


	/**
	 * Retourne le point a l'extérieur du cercle en continuant la ligne droite depuis le centre du cercle
	 * @param pointDansCercle le point dans le cercle
	 * @param circle le cercle
	 * @return la nouvelle position du point
	 */
	// TODO Traiter le cas des arcs de cercle

	public static Vec2 pointExterieur(Vec2 pointDansCercle, Circle circle) {
		if (!circle.containCircle(pointDansCercle))
		{
			return(pointDansCercle);
		}
		return circle.getCenter().plusNewVector(new Vec2((int) (circle.getRadius()*(pointDansCercle.getX()-circle.getCenter().getX())/pointDansCercle.distance(circle.getCenter())),(int) (circle.getRadius()*(pointDansCercle.getY()-circle.getCenter().getY())/pointDansCercle.distance(circle.getCenter()))));
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
		
		if((segment2.getB().getY() - segment2.getA().getY()) != 0)
		{
			inter = (double)(segment2.getB().getX() - segment2.getA().getX()) / (double)(segment2.getB().getY() - segment2.getA().getY());
			k = (segment1.getA().getX() - segment2.getA().getX() + inter * (double)(segment2.getA().getY() - segment1.getA().getY())) / (segment1.getB().getX() - segment1.getA().getX() - inter * (segment1.getB().getY() - segment1.getA().getY()));
		}
		else
			k = -(double)(segment2.getA().getY() - segment1.getA().getY()) / (double)(segment1.getB().getY() - segment1.getA().getY());
		
		return new Vec2((int)(segment1.getA().getX() - k * (segment1.getB().getX() - segment1.getA().getX())), (int)(segment1.getA().getY() - k * (segment1.getB().getY() - segment1.getA().getY())));
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
