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
 * segment, coordonnees double
 * @author Etienne
 *
 */
public class Segment
{
	private Vec2 mPointA;
	private Vec2 mPointB;
	
	public Segment()
	{
		mPointA = new Vec2();
		mPointB = new Vec2();
	}
	
	public Segment(Vec2 pointA, Vec2 pointB)
	{
		mPointA = pointA;
		mPointB = pointB;
	}

	public Vec2 getA()
	{
		return mPointA;
	}
	
	public Vec2 getB()
	{
		return mPointB;
	}
	
	public void setA(Vec2 pointA)
	{
		mPointA = pointA;
	}
	
	public void setB(Vec2 pointB)
	{
		mPointB = pointB;
	}

	/**
	 * Renvoie la distance au carré entre deux points Vec2
	 * @param pointA point 1
	 * @param pointB point 2
     */
	public static double squaredLength(Vec2 pointA, Vec2 pointB)
	{
		return (pointB.x - pointA.x)*(pointB.x - pointA.x) + (pointB.y - pointA.y)*(pointB.y - pointA.y);

	}
}
