package debug;

import smartMath.Vec2;

/**
 * Tous les paramètres pour une hyperbole
 * @author pf
 *
 */

public class Hyperbola {

	private static Vec2[] balises;
    private final static double speedOfSound = 0.34; // in mm/µs

	static
	{
		balises = new Vec2[3];
		balises[0] = new Vec2(-1500, 0);
		balises[1] = new Vec2(-1500, 2000);
		balises[2] = new Vec2(1500, 1000);
	}
	
	public Vec2 p1 = null, p2 = null;
	public double delta; // en mm
	
	public Hyperbola(Vec2 p1, Vec2 p2, double delta)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.delta = delta;
	}
	
	/**
	 * 0 est le couple 12
	 * 1 est le couple 02
	 * 2 est le couple 01
	 * @param couple
	 * @param delta
	 */
	public Hyperbola(int couple, double delta)
	{
		for(int j = 0; j < 3; j++)
		{
			if(j != couple)
			{
				if(p1 == null)
					p1 = balises[j];
				else
				{
					p2 = balises[j];
					break;
				}
			}
		}
		this.delta = delta * speedOfSound;
	}
	
}
