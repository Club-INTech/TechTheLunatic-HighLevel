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

import exceptions.ConfigPropertyNotFoundException;
import smartMath.Circle;
import smartMath.Geometry;
import smartMath.Segment;
import smartMath.Vec2;
import utils.Config;
import utils.Log;

import java.util.ArrayList;

/**
 * Traite tout ce qui concerne la gestion des obstacles sur la table.
 * Les obstacles peuvent être fixes (bordures de la table par exemple) ou bien mobile (et alors considérés temporaires).
 * Un robot ennemi est une obstacle mobile par exemple. 
 * 
 * @author pf, marsu
 */

public class ObstacleManager
{

	/** système de log sur lequel écrire. */
    private Log log;

	/** endroit ou lire la configuration du robot */
	private Config config;

    /** Ensemble des obstacles mobiles/temporaires se trouvant sur la table */
    private ArrayList<ObstacleProximity> mMobileObstacles;

    /** Ensemble des obstacles circulaires */
    private ArrayList<ObstacleCircular> mCircularObstacle;

    /**
     * Ensemble des obstacles mobiles/temporaires a tester pour les placer sur la table
     */
	private ArrayList<ObstacleProximity> mUntestedMobileObstacles;

    
    //les bords de la table auxquels on ajoute le rayon du robot. Utilisé par le pathfinding.
    private ArrayList<Segment> mLines;
    //les obstacles rectangulaires de la table
	private ArrayList<ObstacleRectangular> mRectangles;

	private int defaultObstacleRadius;
	//le rayon de notre robot
	public int mRobotRadius;
	//les dimensions réels de notre robot
	private int mRobotLenght;
	private int mRobotWidth;
	
	// TODO virer : juste du debugg / interface graphique
	private int radiusDetectionDisc=0;
	private Vec2 positionDetectionDisc=new Vec2(0,0);

	/**	le temps donné aux obstacles pour qu'ils soit vérifiés */
	private final int timeToTestObstacle = 1000;

	/** Temps de vie d'un robot ennemi */
	private final int defaultLifetime = 1000;

	/**
     * Instancie un nouveau gestionnaire d'obstacle.
     *
     * @param log le système de log sur lequel écrire.
     * @param config l'endroit ou lire la configuration du robot
     */
    public ObstacleManager(Log log, Config config)
    {
        this.log = log;
        this.config = config;
        
        //creation des listes qui contiendrons les differents types d'obstacles
        mMobileObstacles = new ArrayList<ObstacleProximity>();
        mCircularObstacle = new ArrayList<ObstacleCircular>();
        mLines = new ArrayList<Segment>();
		mRectangles = new ArrayList<ObstacleRectangular>();
		
		mUntestedMobileObstacles= new ArrayList<ObstacleProximity>();


		
		updateConfig();
       
        //par defaut
        //mEnnemyRobot1 = new ObstacleCircular(new Vec2(0, 0), 200 + robotRadius);
      	//mEnnemyRobot2 = new ObstacleCircular(new Vec2(0, 0), 200 + robotRadius);
		

      		
      	//bords de la table
      	mLines.add(new Segment(new Vec2(-1500 + mRobotRadius, 0 + mRobotRadius), new Vec2(1500 - mRobotRadius, 0 + mRobotRadius)));
      	mLines.add(new Segment(new Vec2(1500 - mRobotRadius, 0 + mRobotRadius), new Vec2(1500 - mRobotRadius, 2000 - mRobotRadius)));
      	mLines.add(new Segment(new Vec2(1500 - mRobotRadius, 2000 - mRobotRadius), new Vec2(-1500 + mRobotRadius, 2000 - mRobotRadius)));
      	mLines.add(new Segment(new Vec2(-1500 + mRobotRadius, 2000 - mRobotRadius), new Vec2(-1500 + mRobotRadius, 0 + mRobotRadius)));

      	//Les différents obstacles fixés sur la table

		// zones de départ
		mRectangles.add(new ObstacleRectangular(new Vec2(-1145, 370), 710 + 2*mRobotRadius, 20 + 2*mRobotRadius));
		mRectangles.add(new ObstacleRectangular(new Vec2(1145, 370), 710 + 2*mRobotRadius, 20 + 2*mRobotRadius)); // x 350, y 360 => centre 965, 300
		//mRectangles.add(new ObstacleRectangular(new Vec2(-1145, 371), 710 + 2*mRobotRadius, 22 + 2*mRobotRadius));
		//mRectangles.add(new ObstacleRectangular(new Vec2(1145, 371), 710 + 2*mRobotRadius, 22 + 2*mRobotRadius));


		//fusées
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(-350, 40), 40 + mRobotRadius))); // 350, 40, 40 / 1460, 1350, 40
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(350, 40), 40 + mRobotRadius)));
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(-1460, 1350), 40 + mRobotRadius)));
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(1460, 1350), 40 + mRobotRadius)));

		//cratères
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(-850, 540), 120 + mRobotRadius, 0, 2*Math.PI/3)));
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(850, 540), 120 + mRobotRadius,Math.PI/3, Math.PI)));
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(-1500, 2000), 510 + mRobotRadius, -Math.PI/2, -Math.PI/5)));
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(1500, 2000), 510 + mRobotRadius, -4*Math.PI/5, -Math.PI/2)));

		//pose module côté
		mRectangles.add(new ObstacleRectangular(new Vec2(-1446, 950), 108 + 2*mRobotRadius, 500 + 2*mRobotRadius)); //-1446, 678, 108, 472
		mRectangles.add(new ObstacleRectangular(new Vec2(1446, 950), 108 + 2*mRobotRadius, 500 + 2*mRobotRadius));
		//base lunaire
		mCircularObstacle.add(new ObstacleCircular(new Circle(new Vec2(0, 2000), 800 + mRobotRadius)));


	}


    /**
     * Rend le gestionnaire d'obstacle fourni en argument explicite égal a ce gestionnaire.
     *
     * @param other les gestionnaire a modifier
     */
    public void copy(ObstacleManager other)
    {
    	//TODO innutilise
    }

    /**
     *  Cette instance est elle dans le même état que celle fournie en argument explicite ?
     *
     * @param other l'autre instance a comparer
     * @return true, si les deux instances sont dans le meme etat
     */
    public boolean equals(ObstacleManager other)
    {
    	//TODO inutilise
    	boolean IDontKnow = false;
        return IDontKnow;
    }
    
    /**
     * Utilis� par le pathfinding.
     * Retourne tout les les obstacles temporaires/mobiles. (détectés par la balise laser, les capteurs de distance, etc.)
     *
     * @return la liste des obstacles temporaires/mobiles de la table
     */
    public ArrayList<ObstacleProximity> getMobileObstacles()
    {
        return mMobileObstacles;
    }
    
    /**
     * Utilis� par le pathfinding.
     * Retourne tout les les obstacles fixes de la table.
     *
     * @return la liste des obstacles fixes de la table
     */
    
    public ArrayList<ObstacleCircular> getmCircularObstacle()
    {
        return mCircularObstacle;
    }
    
    /**
     * 
     * @return la liste des lignes formant les bords des obstacles sous forme de segments
     */
	public ArrayList<Segment> getLines()
	{
		return mLines;
	}
	
	/**
	 * @return la liste des rectangles formant les obstacles rectangulaires
	 */
	public ArrayList<ObstacleRectangular> getRectangles()
	{
		return mRectangles;
	}
	
	/**
	 * 
	 * @return le rayon de notre robot
	 */
	public int getRobotRadius()
	{
		return mRobotRadius;
	}
    
    /**
     * Ajoute un obstacle sur la table a la position spécifiée (de type obstacleProximity)
     *
     * @param position position ou ajouter l'obstacle
     */
    public synchronized void addObstacle(final Vec2 position)
    {
    	addObstacle(position,defaultObstacleRadius, defaultLifetime);
    }

	public int getmRobotLenght() {
		return mRobotLenght;
	}

	public int getmRobotWidth() {
		return mRobotWidth;
	}

	/**
     * Ajoute un obstacle sur la table a la position spécifiée, du rayon specifie (de type obstacleProximity)
     *
     * @param position position ou ajouter l'obstacle
     * @param radius rayon de l'obstacle a ajouter    
     * @param lifetime durée de vie (en ms) de l'obstace a ajouter
     * TODO A réadapter à l'année en cours
      */
    public synchronized void addObstacle(final Vec2 position, final int radius, final int lifetime)
    {
    	//vérification que l'on ne détecte pas un obstacle "normal"
    	if (position.getX()>-1500+mRobotRadius+100 && position.getX()<1500-mRobotRadius-100 && position.getY()>mRobotRadius+100 && position.getY()<2000-mRobotRadius-100 //hors de la table
                && !( Geometry.isBetween(position.getX(), -250, 250) && Geometry.isBetween(position.getY(), 600, 1500)) //C'est la vitre
                && !( Geometry.isBetween(position.getX(), -800, 800) && Geometry.isBetween(position.getY(), 1650, 2000)) //château de sable
				&& !( Geometry.isBetween(position.getX(), 700, 1000) && Geometry.isBetween(position.getY(), 950, 1250)) //château de sable tapis
				&& !( Geometry.isBetween(position.getX(), 0, 600) && Geometry.isBetween(position.getY(), 800, 1300)) //Notre zone de depose
				&& !( Geometry.isBetween(position.getX(), -1000, -700) && Geometry.isBetween(position.getY(), 950, 1250)) //château de sable tapis adv
				&& !( Geometry.isBetween(position.getX(), 800, 1500) && Geometry.isBetween(position.getY(), 500, 1800)) //tapis
				)
    	{
    		boolean isThereAnObstacleIntersecting=false;
    		for (int i = 0; i<mUntestedMobileObstacles.size(); i++)
    		{
    			ObstacleProximity obstacle = mUntestedMobileObstacles.get(i);
    			
    			//si l'obstacle est deja dans la liste des obstacles non-testés on l'ajoute dans la liste des obstacles
	    		if(obstacle.position.distance(position)<(obstacle.getRadius()+radius)/2)
	    		{
    				mUntestedMobileObstacles.get(i).numberOfTimeDetected++;
    				mUntestedMobileObstacles.get(i).position.set(position);
    				mUntestedMobileObstacles.get(i).setRadius(radius);
    				mUntestedMobileObstacles.get(i).setLifeTime(lifetime);
    				
    				// si on l'a deja vu plein de fois
    				if(mUntestedMobileObstacles.get(i).numberOfTimeDetected >= mUntestedMobileObstacles.get(i).getMaxNumberOfTimeDetected())
    					mUntestedMobileObstacles.get(i).numberOfTimeDetected = mUntestedMobileObstacles.get(i).getMaxNumberOfTimeDetected();

	    			// si on valide sa vision 
	    			if(mUntestedMobileObstacles.get(i).numberOfTimeDetected >= mUntestedMobileObstacles.get(i).getThresholdConfirmedOrUnconfirmed())
	    			{
	    				isThereAnObstacleIntersecting=true;
	    				mUntestedMobileObstacles.get(i).setLifeTime(lifetime);
	    				
	    				mMobileObstacles.add(mUntestedMobileObstacles.get(i));
	    				mUntestedMobileObstacles.remove(i);
	    			}
	    		}
    		}
    		for(int i = 0; i<mMobileObstacles.size(); i++)
    		{
    			ObstacleProximity obstacle = mMobileObstacles.get(i);
    			if(obstacle.position.distance(position)<obstacle.getRadius()+radius)
    			{
    				isThereAnObstacleIntersecting=true;
    				
    				mMobileObstacles.get(i).numberOfTimeDetected++;
    				mMobileObstacles.get(i).position.set(position);
    				mMobileObstacles.get(i).setRadius(radius);
    				mMobileObstacles.get(i).setLifeTime(lifetime);
    				
    				// si on l'a deja vu plein de fois
    				if(mMobileObstacles.get(i).numberOfTimeDetected >= mMobileObstacles.get(i).getMaxNumberOfTimeDetected())
    					mMobileObstacles.get(i).numberOfTimeDetected = mMobileObstacles.get(i).getMaxNumberOfTimeDetected();
    			}
    		}
    		if (!isThereAnObstacleIntersecting)
    			mUntestedMobileObstacles.add(new ObstacleProximity(new Circle(position, radius), timeToTestObstacle));

    			
    		/*on ne test pas si la position est dans un obstacle deja existant 
    		 *on ne detecte pas les plots ni les gobelets (et si on les detectes on prefere ne pas prendre le risque et on les evites)
    		 * et si on detecte une deuxieme fois l'ennemi on rajoute un obstacle sur lui
    		 */
    	}
    	else
    	{
    		//log.debug("Obstacle hors de la table");
		}
    }

	/**
	 * Ajoute un obstacle rectangulaire
	 * @param obs le ObstacleRectangular en question
     */
	public synchronized void addObstacle(ObstacleRectangular obs)
	{
		mRectangles.add(obs);
	}

	/**
	 * Ajoute un obstacle circulaire
	 * @param obs le ObstacleCircular en question
     */
	public synchronized void addObstacle(ObstacleCircular obs)
	{
		mCircularObstacle.add(obs);
	}

	/**
	 * Supprime la première occurence de cet obstacle
	 * @param obs l'obstacle
     */
	public synchronized void removeObstacle(ObstacleCircular obs)
	{
		mCircularObstacle.remove(obs);
	}

	/**
	 * Supprime la première occurence de cet obstacle
	 * @param obs l'obstacle
     */
	public synchronized void removeObstacle(ObstacleRectangular obs)
	{
		mRectangles.remove(obs);
	}

    /**
	 * Supprime du gestionnaire tout les obstacles dont la date de péremption est antérieure à la date fournie
     *
     */
    public synchronized void removeOutdatedObstacles()
    {
    	// enlève les obstacles confirmés s'ils sont périmés
    	for(int i = 0; i < mMobileObstacles.size(); i++)
    		if(mMobileObstacles.get(i).getOutDatedTime() < System.currentTimeMillis())
    		{
    			mMobileObstacles.remove(i--);
    		}
    	
    	// enlève les obstacles en attente s'ils sont périmés
    	for(int i = 0; i < mUntestedMobileObstacles.size(); i++)
    		if(mUntestedMobileObstacles.get(i).getOutDatedTime() < System.currentTimeMillis())
    		{
    			mUntestedMobileObstacles.remove(i--);
    		}
    }

    /**
     * Renvoie true si un obstacle chevauche un disque. (uniquement un obstacle detecte par les capteurs)
     *
     * @param discCenter le centre du disque a vérifier
     * @param radius le rayon du disque
     * @return true, si au moins un obstacle chevauche le disque
     */
    public synchronized boolean isDiscObstructed(final Vec2 discCenter, int radius)
    {
    	radiusDetectionDisc=radius;
    	positionDetectionDisc=discCenter;
    	
    	for(int i=0; i<mMobileObstacles.size(); i++)
    	{
    		if ((radius+mMobileObstacles.get(i).getRadius())*(radius+mMobileObstacles.get(i).getRadius())
    			 > (discCenter.getX()-mMobileObstacles.get(i).getPosition().getX())*(discCenter.getX()-mMobileObstacles.get(i).getPosition().getX())
    			 + (discCenter.getY()-mMobileObstacles.get(i).getPosition().getY())*(discCenter.getY()-mMobileObstacles.get(i).getPosition().getY()))
    		{
    			log.debug("Disque obstructed avec l'obstacle "+mMobileObstacles.get(i).getPosition()+"de rayon"+mMobileObstacles.get(i).getRadius());
    			log.debug("Disque en "+discCenter+" de rayon "+radius);
    			return true;
    		}
    	}
    	return false;
    }  

    /**
     * retourne la distance à l'ennemi le plus proche (en mm)
     * Les ennemis ne sont pris en compte que si ils sont dans la direstion donnée, a 90° près
     * si l'ennemi le plus proche est tangent à notre robot, ou plus proche, on retourne 0
     * @param position la position a laquelle on doit mesurer la proximité des ennemis
     * @param direction direction selon laquelle on doit prendre en compte les ennemis
     * @return la distance à l'ennemi le plus proche (>= 0)
     */
    public synchronized int distanceToClosestEnemy(Vec2 position, Vec2 direction)
    {
    	try
    	{
	    	//si aucun ennemi n'est détecté, on suppose que l'ennemi le plus proche est à 1m)
	    	
	    	int squaredDistanceToClosestEnemy = 10000000;
	    	
	    	
	    	int squaredDistanceToEnemyUntested=10000000;
	    	int squaredDistanceToEnemyTested=10000000 ;
	    	
	    	ObstacleCircular closestEnnemy = null;
	
	     	if(mMobileObstacles.size() == 0 && mUntestedMobileObstacles.size()==0)
	    		return 1000;
	     	
	     	
	     	//trouve l'ennemi le plus proche parmis les obstacles confirmés
	    	for(int i=0; i<mMobileObstacles.size(); i++)
	    	{
	    		Vec2 ennemyRelativeCoords = new Vec2((mMobileObstacles.get(i).position.getX() - position.getX()), 
	    											  mMobileObstacles.get(i).position.getY() - position.getY());
	    		if(direction.dot(ennemyRelativeCoords) > 0)
	    		{
		    		squaredDistanceToEnemyTested = ennemyRelativeCoords.squaredLength(); 
		    		if(squaredDistanceToEnemyTested < squaredDistanceToClosestEnemy)
		    		{
		    			squaredDistanceToClosestEnemy = squaredDistanceToEnemyTested;
		    			closestEnnemy = mMobileObstacles.get(i);
		    		}
	    		}
	    	}
	      	
	     	//trouve l'ennemi non confirmé le plus proche parmis les obstacles 
	    	// (et remplace la distance a l'ennemi le plus proche d'un ennemi confirmé par une distance a un ennemi non confirmé s'il est plus proche)
	    	for(int i=0; i<mUntestedMobileObstacles.size(); i++)
	    	{
	    		Vec2 ennemyRelativeCoords = new Vec2((mUntestedMobileObstacles.get(i).position.getX() - position.getX()), 
	    											  mUntestedMobileObstacles.get(i).position.getY() - position.getY());
	    		if(direction.dot(ennemyRelativeCoords) > 0)
	    		{
		    		squaredDistanceToEnemyUntested = ennemyRelativeCoords.squaredLength(); 
		    		if(squaredDistanceToEnemyUntested < squaredDistanceToClosestEnemy)
		    		{
		    			squaredDistanceToClosestEnemy = squaredDistanceToEnemyUntested;
		    			closestEnnemy = mUntestedMobileObstacles.get(i);
		    		}
	    		}
	    	}
	    	
	    	if(squaredDistanceToClosestEnemy <= 0)
	    		return 0;

	    	if(closestEnnemy != null)
	    	{
	    		//log.debug("Position de l'ennemi le plus proche, non testé, d'après distanceToClosestEnnemy: "+mUntestedMobileObstacles.get(indexOfClosestEnnemy).getPosition(), this);
		    	return (int)Math.sqrt((double)squaredDistanceToClosestEnemy) - mRobotRadius - closestEnnemy.getRadius();
	    	}
	    	else
	    		return 10000;
    	}
    	catch(IndexOutOfBoundsException e)
    	{
    		log.critical("Ah bah oui, out of bound");
    		throw e;
    	}
    }

    /**
     * Change le position d'un robot adverse.
     *
     * @param ennemyID numéro du robot
     * @param position nouvelle position du robot
     */
    public synchronized void setEnnemyNewLocation(int ennemyID, final Vec2 position)
    {
    	//TODO innutilise
    	//changer la position de l'ennemi demandé
    	//cela sera utilise par la strategie, la methode sera ecrite si besoin
    	mMobileObstacles.get(ennemyID).setPosition(position);
    }
    
    /**
     * Utilis� par le thread de stratégie. (pas implemente : NE PAS UTILISER!!!)
     * renvoie la position du robot ennemi voulu sur la table.
     * @param ennemyID l'ennemi dont on veut la position
     *
     * @return la position de l'ennemi spécifié
     */
    public Vec2 getEnnemyLocation(int ennemyID)
    {
    	//TODO innutilise
    	//donner la position de l'ennemi demandé
    	//cela sera utilise par la strategie, la methode sera ecrite si besoin
        return  mMobileObstacles.get(ennemyID).position;
    }
    
    
    /**
     * Utilisé pour les tests.
     * Renvois le nombre d'obstacles mobiles actuellement en mémoire
     *
     * @return le nombre d'obstacles mobiles actuellement en mémoire
     */
    public int getMobileObstaclesCount()
    {
        return mMobileObstacles.size();
    }
    
    /**
     * Vérifie si le position spécifié est dans l'obstacle spécifié ou non
     * Attention : l'obstacle doit etre issu des classes ObstacleCircular ou ObstacleRectangular sous peine d'exception
     * Attention : verifie si le point (et non le robot) est dans l'obstacle.
     *
     * @param pos la position a vérifier
     * @param obstacle l'obstacle a considérer
     * @return true, si la position est dans l'obstacle
     */
    public synchronized boolean isPositionInObstacle(Vec2 pos, Obstacle obstacle)
    {
    	if(obstacle instanceof ObstacleCircular)
    	{
    		ObstacleCircular obstacleCircular = (ObstacleCircular)obstacle;
    		return (pos.getX()-obstacleCircular.position.getX())*(pos.getX()-obstacleCircular.position.getX())+(pos.getY()-obstacleCircular.position.getY())*(pos.getY()-obstacleCircular.position.getY())<obstacleCircular.getRadius()*obstacleCircular.getRadius();
    	}
    	if(obstacle instanceof ObstacleRectangular)
    	{
    		ObstacleRectangular obstacleRectangular = (ObstacleRectangular)obstacle;
	    	return pos.getX()<(obstacleRectangular.position.getX()-(obstacleRectangular.sizeX/2)) || pos.getX()>(obstacleRectangular.position.getX()+(obstacleRectangular.sizeX/2)) || pos.getY()<(obstacleRectangular.position.getY()-(obstacleRectangular.sizeY/2)) || pos.getY()>(obstacleRectangular.position.getY()+(obstacleRectangular.sizeY/2));
    	}
    	else
    		throw new IllegalArgumentException();
    }
    
    /**
	 * Vérifie si la position donnée est dégagée ou si elle est dans l'un des obstacles sur la table (tous les obstacles)
     *
     * @param position la position a vérifier
     * @return true, si la position est dans un obstacle
     */
    public synchronized boolean isObstructed(Vec2 position)
    {
    	boolean isObstructed = false;
    	for(int i=0; i<mMobileObstacles.size(); i++)
    		isObstructed=isPositionInObstacle(position, mMobileObstacles.get(i));
    	for(int i = 0; i< mCircularObstacle.size(); i++)
    		isObstructed=isPositionInObstacle(position, mCircularObstacle.get(i));
    	for(int i=0; i<mRectangles.size(); i++)
    		isObstructed=isPositionInObstacle(position, mRectangles.get(i));
        return isObstructed;
    }
    
    /**
     *  On enlève les obstacles présents sur la table virtuelle mais non detectés
     * @param position 
     * @param orientation 
     * @param detectionRadius 
     * @param detectionAngle 
     *  @return true si on a enlevé un obstacle, false sinon
     */
    public synchronized boolean removeNonDetectedObstacles(Vec2 position, double orientation, double detectionRadius, double detectionAngle)
    {
		boolean obstacleDeleted=false;
		//check non testés ;--;et si <=0 remove
		// check testés ; -- ; et si <maxnon goto nonteste  remove de testés
		
		
    	//parcours des obstacles
		for(int i = 0; i < mUntestedMobileObstacles.size(); i++)
    	{
    		Vec2 positionEnnemy = mUntestedMobileObstacles.get(i).position;
    		int ennemyRay = mUntestedMobileObstacles.get(i).getRadius();
    		// On verifie que l'ennemi est dans le cercle de detection actuel
    		if((positionEnnemy.distance(position) < (detectionRadius+ennemyRay)*(detectionRadius+ennemyRay)))
    		{
    			if(isEnnemyInCone(positionEnnemy, position, detectionRadius, orientation,  detectionAngle, ennemyRay) )
    			{
    				mUntestedMobileObstacles.get(i).numberOfTimeDetected--;
    			
    				if(mUntestedMobileObstacles.get(i).numberOfTimeDetected <= 0)
    				{
    					mUntestedMobileObstacles.remove(i--);
	    				obstacleDeleted=true;
	    				log.debug("Ennemi untested en "+positionEnnemy+" enlevé !");
    				}
    			}
    		}
    	}
    	for(int i = 0; i < mMobileObstacles.size(); i++)
    	{
    		Vec2 positionEnnemy = mMobileObstacles.get(i).position;
    		int ennemyRay = mMobileObstacles.get(i).getRadius();
    		// On verifie que l'ennemi est dans le cercle de detection actuel
    		if((positionEnnemy.distance(position) < (detectionRadius+ennemyRay)*(detectionRadius+ennemyRay)))
    		{
    			if(isEnnemyInCone(positionEnnemy, position, detectionRadius, orientation,  detectionAngle, ennemyRay) )
    			{
    				mMobileObstacles.get(i).numberOfTimeDetected--;
    			
    				if(mMobileObstacles.get(i).numberOfTimeDetected < mMobileObstacles.get(i).getThresholdConfirmedOrUnconfirmed())
    				{
    					mMobileObstacles.get(i).setLifeTime(2000);
        				mUntestedMobileObstacles.add(mMobileObstacles.get(i));
	    				mMobileObstacles.remove(i--);
	    				
	    				obstacleDeleted=true;
	    				log.debug("Ennemi en "+positionEnnemy+" enlevé !");
    				}
    			}
    		}
    	}
    	return obstacleDeleted;
    }
    
    
    public boolean isEnnemyInCone(Vec2 positionEnnemy, Vec2 position, double detectionRadius, double orientation, double detectionAngle, int ennemyRay) 
    {
    	double ennemyAngle = Math.atan2(positionEnnemy.getX() - position.getX(), positionEnnemy.getY() - position.getY());
		
		// si le centre de l'obstacle est dans le cone 
		// ou 
		// si on intersecte avec le coté gauche 
		// ou
		// si on interesecte avec le coté droit
		Segment coteGaucheCone = new Segment(position, 
				new Vec2( position.getX() + (int)(detectionRadius*Math.cos(orientation + detectionAngle/2)), 
						  position.getY() + (int)(detectionRadius*Math.sin(orientation + detectionAngle/2)) ) );
		Segment coteDroitCone = new Segment(position, 
				new Vec2( position.getX() + (int)(detectionRadius*Math.cos(orientation - detectionAngle/2)), 
						  position.getY() + (int)(detectionRadius*Math.sin(orientation - detectionAngle/2)) ) );
		
		return (ennemyAngle < (orientation + detectionAngle/2)
	    && ennemyAngle > (orientation - detectionAngle/2)
	    || ( ( Geometry.intersects( coteGaucheCone , 
	    						   new Circle(positionEnnemy, ennemyRay)) )
	    || ( Geometry.intersects(	coteDroitCone, 
	    						   new Circle(positionEnnemy, ennemyRay))) )  );
	}
    
    /**
     * Debug / interface graphique
     * @return 
     */
    @SuppressWarnings("javadoc")
	public int getDiscRadius()
    {
    	return radiusDetectionDisc;
    }
    public Vec2 getDiscPosition()
    {
    	return positionDetectionDisc;
    }
    
    /**
     *  On enleve les obstacles qui sont en confrontation avec nous :
     *  Cela evite de se retrouver dans un obstacle
     * @param position 
     */
    public void removeObstacleInUs(Vec2 position)
    {
    	for(int i=0; i<mMobileObstacles.size(); i++)
    	{ 
    		if( (   (position.getX()-mMobileObstacles.get(i).getPosition().getX())*(position.getX()-mMobileObstacles.get(i).getPosition().getX())
    	    	+   (position.getY()-mMobileObstacles.get(i).getPosition().getY())*(position.getY()-mMobileObstacles.get(i).getPosition().getY()) )
    	    	<=( (mRobotRadius+mMobileObstacles.get(i).getRadius())*(mRobotRadius+mMobileObstacles.get(i).getRadius())) )
    			mMobileObstacles.remove(mMobileObstacles.get(i));
    	}
    }
    
    /**
     * supprime les obstacles fixes dans le disque
     * 
     * @param position
     * @param radius
     */
    public void removeFixedObstaclesInDisc(Vec2 position, int radius)
    {
    	for(int i = 0; i< mCircularObstacle.size(); i++)
    		if((position.getX()- mCircularObstacle.get(i).getPosition().getX())*(position.getX()- mCircularObstacle.get(i).getPosition().getX())
    		 + (position.getY()- mCircularObstacle.get(i).getPosition().getY())*(position.getY()- mCircularObstacle.get(i).getPosition().getY())
    		 <= mRobotRadius*mRobotRadius)
    			mCircularObstacle.remove(mCircularObstacle.get(i));
    }

    public void printObstacleFixedList()
    {
    	for(int i = 0; i< mCircularObstacle.size(); i++)
    		mCircularObstacle.get(i).printObstacleMemory();
    }
    
    public void updateConfig()
	{
		try 
		{
			mRobotRadius = Integer.parseInt(config.getProperty("rayon_robot"));
			mRobotLenght = Integer.parseInt(config.getProperty("longueur_robot"));
			mRobotWidth = Integer.parseInt(config.getProperty("largeur_robot"));
		    defaultObstacleRadius = Integer.parseInt(config.getProperty("rayon_robot_adverse"));
		}
	    catch (ConfigPropertyNotFoundException e)
    	{
    		log.debug("Revoir le code : impossible de trouver la propriété "+e.getPropertyNotFound());
		}
	}
    
    public ArrayList<ObstacleProximity> getUntestedArrayList()
    {
    	return mUntestedMobileObstacles;
    }

	/**
	 * Permet de update les obstacles avec un nouveau rayon de robot
	 * @param newRobotRadius le nouveau rayon
     */
	public void updateObstacles(int newRobotRadius)
	{
		if(this.mRobotRadius == newRobotRadius)
			return;

		for(ObstacleRectangular i : mRectangles)
		{
			i.changeDim(i.getSizeX()-2*mRobotRadius+2*newRobotRadius, i.getSizeY()-2*mRobotRadius+2*newRobotRadius);
		}

		for(ObstacleCircular i : mCircularObstacle)
		{
			i.setRadius(i.getRadius()-mRobotRadius+newRobotRadius);
		}

		for(ObstacleProximity i : mUntestedMobileObstacles)
		{
			i.setRadius(i.getRadius()-mRobotRadius+newRobotRadius);
		}

		for(ObstacleProximity i : mMobileObstacles)
		{
			i.setRadius(i.getRadius()-mRobotRadius+newRobotRadius);
		}

		this.mRobotRadius = newRobotRadius;
     /*   while (!ThreadWorker.isGraphReady())
        {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		ThreadWorker.recalculateGraph();*/
	}

    /**
     * Supprime tous les obstacles fixes qui superposent le point donné
     * Utile pour forcer le passage si les obstacles vont subir un changement
     * @param point le point à dégager
     * @return les obstacles supprimés
     */
	public ArrayList<Obstacle> freePoint(Vec2 point)
    {
        ArrayList<Obstacle> deleted = new ArrayList<>();

        for (int i = 0; i< mCircularObstacle.size(); i++)
        {
            if(mCircularObstacle.get(i).isInObstacle(point))
            {
                deleted.add(mCircularObstacle.get(i));
                removeObstacle(mCircularObstacle.get(i));
            }
        }

        for (int i=0;i< mRectangles.size();i++)
        {
            if(mRectangles.get(i).isInObstacle(point))
            {
                deleted.add(mRectangles.get(i));
                removeObstacle(mRectangles.get(i));
            }
        }
        return deleted;
    }

	/**
	 * Supprime TOUS les obstacles fixes de la table
     * http://cdn.meme.am/instances/500x/21541512.jpg
	 */
	public void destroyEverything()
	{
		mRectangles.clear();
		mLines.clear();
		mCircularObstacle.clear();
		mMobileObstacles.clear();
		mUntestedMobileObstacles.clear();
	}

	public ArrayList<ObstacleRectangular> getmRectangles() {
		return mRectangles;
	}
}
