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

package robot;

import container.Service;
import enums.DirectionStrategy;
import enums.Speed;
import enums.TurningStrategy;
import enums.UnableToMoveReason;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.Locomotion.BlockedException;
import exceptions.Locomotion.EnnemyCrashedException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.Locomotion.UnexpectedObstacleOnPathException;
import exceptions.serial.SerialConnexionException;
import hook.Hook;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import smartMath.Vec2;
import table.Table;
import utils.Config;
import utils.Log;
import utils.Sleep;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Entre Deplacement (appels à la série) et RobotVrai (déplacements haut niveau), Locomotion
 * s'occupe de la position, de la symétrie, des hooks, des trajectoires courbes et des blocages.
 * Structure, du bas au haut niveau: symétrie, hook, trajectoire courbe et blocage.
 * Les méthodes "non-bloquantes" se finissent alors que le robot roule encore.
 * (les méthodes non-bloquantes s'exécutent très rapidement)
 * Les méthodes "bloquantes" se finissent alors que le robot est arrêté.
 * @author pf
 *
 * TODO OPTIONNEL faire une gestion complète des trajectoires courbes
 * https://goo.gl/7HU589
 *
 */

public class Locomotion implements Service
{

	/** le log si on a des erreurs */
    private Log log;
    
    /**la config ...*/
    private Config config;
    
    /** la table sur laquelle evolue le robot*/
    private Table table;
    
    /** le bas-niveau*/
    private SerialWrapper serialWrapper;
    
    /** la longueur du robot (taille dans la direction où le robot avance)*/
    private int robotLength;
    
    /**
     * rayon du cercle place devant le robot qui sert pour la detection
     * 
     *la zone de détection d'obstacle est un disque comme suit:
     *     			          o  o
     *    			+----+ o        o		 Sens de déplacement du robot: ====>
     *    robot ->	|    |o          o
     *    			|    |o          o  <- Zone de vérification (ce disque est tangent au robot)
     *    			+----+ o        o 
     *   			          o  o
     */
    private int detectionRay;

    /** Distance de detection (sans disque de detection) */
    private int detectionDistance;
    
    /**
     * Position "bas niveau" du robot, celle du robot
     * La vraie.
     */
    private Vec2 lowLevelPosition = new Vec2();
    
    /**
     * Position "haut niveau" du robot, celle du robot
     * Celle qui commence toujours en vert
     */
    private Vec2 highLevelPosition = new Vec2();
    
    /**
     * la position visee au final par le deplacement
     */
    private Vec2 finalAim = new Vec2();
    
    /**
     * orientation réelle du robot (symetrisee)
     * non connue par les classes de plus haut niveau
     */
    private double lowLevelOrientation;
    
    /**
     * orientation réelle du robot non symetrisée
     */
    private double highLevelOrientation;
    
    /** Indique si la symétrie est activée (si le robot démarre du côté x<0)
     * La symétrie s'applique sur les déplacements et les actionneurs*/
    private boolean symetry;
    
    /**temps d'attente entre deux boucles d'acquitement
     * Override par la config */
    private int feedbackLoopDelay = 50;
    
    /**la distance dont le robot va avancer pour se dégager en cas de bloquage mécanique
     * Override par la config */
    private int distanceToDisengage = 50;
    
    /**l'angle dont le robot va tourner pour se dégager en cas de bloquage mécanique
     * Override par la config */
    private double angleToDisengage;
    
    /**
     * 	La distance maximale pour une correction translationnelle 
     * 	La correction ne sera effectuée que si le robot est loin de son point d'arrivée.
     */
    private final int maxLengthCorrectionThreeshold = 20;
    
    /**
     * 	L'orientation maximale pour une correction rotationelle 
     * 	La correction ne sera effectuée que si le robot est assez eloigné de son orientation souhaitée.
     */
    private final double maxRotationCorrectionThreeshold = 0.03;

    /**
     * L'orientation maximale pour ignorer le sens obligatoire de rotation
     * Si l'angle y est inférieur, le robot tourne en FASTEST
     */
    private final double maxRotationTurningStrategyIgnore = Math.PI/3;

    /**Booléen explicitant si le robot est prêt à tourner, utile pour le cercle de détection */
	public boolean isRobotTurning=false;	
	
	/** nombre d'essais maximum après une BlockedException*/
    private int maxRetriesIfBlocked=1;
    
    /** nombre d'essais en cours après un BlockedException*/
    private int actualRetriesIfBlocked=0;

    /** temps d'attente lorsqu'il y a un ennemie devant */
    private int timeToWaitIfEnnemy;

    /** temps d'attente que l'ennemie se bouge avant de décider de faire autre chose*/
    private int timeOutEnnemyMove;

    /** Indique si le robot est en marche avant, utile pour les capteurs*/
    public boolean isRobotMovingForward;
    
    /** Indique si le robot est en marche arrière, utile pour les capteurs*/
    public boolean isRobotMovingBackward;

    /** Donne la stratégie de rotation */
    private TurningStrategy turningStrategy = TurningStrategy.FASTEST;
    
    /**Donne la stratégie de translation */
    private DirectionStrategy directionStrategy = DirectionStrategy.FASTEST;

    /** Si le robot est censé forcer le mouvement */
    private boolean isForcing= false;

    /** Temps prévu de fin de mouvement */
    private long timeExpected = 0;

    /** Vitesse de translation */
    private double transSpeed = Speed.MEDIUM_ALL.translationSpeed;

    /** Vitesse de rotation */
    private double rotSpeed = Speed.MEDIUM_ALL.rotationSpeed;

    /** Valeurs des ultrasons filtrés par le LL pour la détection basique */
    private ArrayList<Integer> USvalues;

    /**
     * Valeur limite de détection pour le mode basique
     * Override par la config
     */
    private int basicDetectDistance;

    /** Si la détection basique est activée ou non
     * Override par la config
     */
    private boolean basicDetection;

    /** Seulement pour les arcs, empêche de symétriser deux fois */
    private boolean symetrised = false;

    /**Constructeur de Locomotion
     * @param log le fichier de log
     * @param config le fichier de config
     * @param table la table de jeu
     * @param serialWrapper protocole de communication série
     */
    private Locomotion(Log log, Config config, Table table, SerialWrapper serialWrapper)
    {
        this.log = log;
        this.config = config;
        this.serialWrapper = serialWrapper;
        this.table = table;
        USvalues = new ArrayList<Integer>(){{for(int i=0;i<4;i++)add(0);}};
        updateConfig();
    }

    
    /**
     * Fait tourner le robot (méthode bloquante)
     * Une manière de tourner qui réutilise le reste du code, car tourner
     * n'en devient plus qu'un cas particulier (celui où... on n'avance pas)
     * @param angle l'angle vise (en absolut)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void turn(double angle, ArrayList<Hook> hooks) throws UnableToMoveException
    {
    	turn(angle, hooks, true);
    } 
    	    	
    /**
     * Fait tourner le robot (méthode bloquante)
     * Une manière de tourner qui réutilise le reste du code, car tourner
     * n'en devient plus qu'un cas particulier (celui où... on n'avance pas)
     * @param angle l'angle vise (en absolut)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void turn(double angle, ArrayList<Hook> hooks, boolean mustDetect) throws UnableToMoveException
    {
        log.debug("Tourner de "+Double.toString(angle));

    	actualRetriesIfBlocked=0;
		updateCurrentPositionAndOrientation();

    	/**
    	 * calcul de la position visee du haut niveau
    	 *   on vise une position eloignee mais on ne s'y deplacera pas, le robot ne fera que tourner
    	 */
    	Vec2 aim = highLevelPosition.plusNewVector(new Vec2(1000.0,angle));
    	/*Vec2 aim = new Vec2(

        (int) (highLevelPosition.getX() + 1000*Math.cos(angle)),
        (int) (highLevelPosition.getY() + 1000*Math.sin(angle))
        );*/
    	finalAim = aim;

        isRobotMovingForward=true;
		//là il faut calculer ça donne le sens


        moveToPointException(aim, hooks, true, false,1 , mustDetect);
        isRobotMovingForward=false;

    	actualRetriesIfBlocked=0;
    }
    
  
    /**
     * Fait avancer le robot de "distance" (en mm).
     * @param distance la distance dont le robot doit se deplacer
     * @param hooks les potetniels hooks a prendre en compte (ne pas mettre null !)
     * @param wall vrai si on supppose qu'on vas se cogner dans un mur (et qu'il ne faut pas pousser dessus)
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void moveLengthwise(int distance, ArrayList<Hook> hooks, boolean wall) throws UnableToMoveException, EnnemyCrashedException
    {
    	moveLengthwise(distance, hooks, wall, true);
    }
    
    /**
     * Fait avancer le robot de "distance" (en mm).
     * @param distance la distance dont le robot doit se deplacer
     * @param hooks les potetniels hooks a prendre en compte (ne pas mettre null !)
     * @param wall vrai si on supppose qu'on vas se cogner dans un mur (et qu'il ne faut pas pousser dessus)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void moveLengthwise(int distance, ArrayList<Hook> hooks, boolean wall, boolean mustDetect) throws UnableToMoveException
    {    

    	actualRetriesIfBlocked=0;
    	
		updateCurrentPositionAndOrientation();

        log.debug("Avancer de "+Integer.toString(distance));
        
        /**
         * aim est la visée du haut niveau, qui commence toujours à droite
         * TODO; trouver ce que veut dire ce commentaire
         */
        Double dist = (double) distance;
        Vec2 aim = highLevelPosition.plusNewVector(new Vec2(dist, highLevelOrientation));

        finalAim = aim;
        // l'appel à cette méthode sous-entend que le robot ne tourne pas
        // il va donc en avant si la distance est positive, en arrière si elle est négative
        // si on est à 90°, on privilégie la marche avant
        
        // Pour les capteurs
        if(distance>=0)
        	isRobotMovingForward=true;
        else 
        	isRobotMovingBackward=true;
		moveToPointException(aim, hooks, distance >= 0, wall, 0, mustDetect);
		isRobotMovingForward=false;
    	isRobotMovingBackward=false;

		actualRetriesIfBlocked=0;// on reinitialise
    }

    /**
     * Vérifie qu'il n'y a pas d'ennemi AVANT d'avancer tous les timeToWaitEnnemy, puis lance un moveLengthwise classique
     * Si l'attente est très longue, on considère que l'ennemi a crash et on ajoute un obstacle permanent sur la table
     * On laisse l'IA gérer la suite
     * @param distance
     * @param hooks
     * @param expectedWallImpact
     * @param isDetect
     * @throws UnableToMoveException
     */
    public void moveLengthwiseAndWaitIfEnnemy(int distance, ArrayList<Hook> hooks, boolean expectedWallImpact, boolean isDetect) throws UnableToMoveException
    {
        Double dist = (double) distance;
        Vec2 aim = new Vec2(dist, highLevelOrientation);
        int totalTime = 0;
        int closest = table.getObstacleManager().distanceToClosestEnemy(highLevelPosition, aim);
        log.debug("Distance à l'ennemi le plus proche dans le sens de la marche :" + closest);

        while (closest <= detectionDistance && closest > -150 && totalTime < timeOutEnnemyMove)
        {
            Sleep.sleep(timeToWaitIfEnnemy);
            totalTime += timeToWaitIfEnnemy;
            log.debug ("Ennemi détecté dans le sens de marche, on attend (" + closest + " mm)");
            closest = table.getObstacleManager().distanceToClosestEnemy(highLevelPosition, aim);
        }

        if(totalTime >= timeOutEnnemyMove){

            table.getObstacleManager().crashEnnemyAdd(highLevelPosition, aim);
            log.debug("L'ennemie ne bouge plus, on ajoute un obstacle permanant, et on laisse l'IA gérer");
            throw new EnnemyCrashedException(aim, UnableToMoveReason.OBSTACLE_DETECTED);
        }

        else{
            moveLengthwise(distance, hooks, expectedWallImpact, isDetect);
        }
    }

    /**
     * Suit un chemin en ligne brisee
     * @param path le chemin a suivre (un arraylist de Vec2 qui sont les point de rotation du robot)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param directionstrategy ce que la strategie choisit comme optimal (en avant, en arriere, au plus rapide)
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void followPath(ArrayList<Vec2> path, ArrayList<Hook> hooks, DirectionStrategy directionstrategy) throws UnableToMoveException
    {
        followPath(path, hooks, directionstrategy, true);// par defaut, on detecte
    }
    
    /**
     * Suit un chemin en ligne brisee
     * @param path le chemin a suivre (un arraylist de Vec2 qui sont les point de rotation du robot)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param directionstrategy ce que la strategie choisit comme optimal (en avant, en arriere, au plus rapide)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    public void followPath(ArrayList<Vec2> path, ArrayList<Hook> hooks, DirectionStrategy directionstrategy, boolean mustDetect) throws UnableToMoveException
    {
		updateCurrentPositionAndOrientation();

    	//si un singe a mie de pain null pour les hooks on le gere
    	if(hooks == null)
    		hooks = new ArrayList<Hook>();

    	//un simple for (on vas au point 0 puis au point 1 etc.)
    	finalAim = path.get(path.size()-1);
  
    	path.remove(0);//On enleve le premier point, notre propre position
    	
    	for(int i = 0; i < path.size(); i++)
        {
            Vec2 aim = path.get(i);
			moveToPointForwardBackward(aim, hooks,
													/*on suppose qu'on ne se prends pas de mur (sinon la pathDingDing est a revoir)*/
													false, directionstrategy,
																			/*on veut avancer*/
																			0, mustDetect);
        }		
    }

    /**
     * Bloquant. Gère la marche arrière automatique selon la stratégie demandée.
     * @param aim le point visé sur la table (consigne donné par plus haut niveau donc non symetrise)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param mur vrai si on suppose qu'on vas se cogner dans un mur (et qu'on veut s'arreter des qu'on cogne)
     * @param strategy ce que la strategie choisit comme optimal (en avant, en arriere, au plus rapide)
     * @param turnOnly vrai si on veut uniquement tourner (et pas avancer)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    private void moveToPointForwardBackward(Vec2 aim, ArrayList<Hook> hooks, boolean mur, DirectionStrategy strategy, int turnOnly, boolean mustDetect) throws UnableToMoveException
    {
		actualRetriesIfBlocked=0;// on reinitialise

		updateCurrentPositionAndOrientation();

    	// on avance en fonction de ce que nous dit la strategie
    	if(strategy == DirectionStrategy.FORCE_BACK_MOTION)
    	{
            isRobotMovingBackward=true;
            moveToPointException(aim, hooks, false, mur, turnOnly, mustDetect);
            isRobotMovingBackward=false;
    	}
    	else if(strategy == DirectionStrategy.FORCE_FORWARD_MOTION)
    	{ 
            isRobotMovingForward=true;
            moveToPointException(aim, hooks, true, mur, turnOnly, mustDetect);
            isRobotMovingForward=false;
    	}
    	else if(strategy == DirectionStrategy.FASTEST)
    	{
    		// Calcul du moyen le plus rapide (on se sert d'un calcul de produit scalaire)
	        Vec2 delta = aim.clone();
	        delta.minus(lowLevelPosition);
	        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
	        Vec2 orientationVec = new Vec2(1000.0, lowLevelOrientation);
	     
	        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
	        boolean isFastestDirectionForward = delta.dot(orientationVec) >= 0;
	        
	        isRobotMovingForward = isFastestDirectionForward;
	        isRobotMovingBackward = !isFastestDirectionForward;
	        moveToPointException(aim, hooks, isFastestDirectionForward, mur, turnOnly, mustDetect);
	        isRobotMovingForward = false;
	        isRobotMovingBackward = false;
    	}

    	log.debug("Arrivés en "+aim+" vraie position : "+lowLevelPosition);
    	
		actualRetriesIfBlocked=0;// on reinitialise

    }

    /**
     * bloquant
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * @param aim la position visee sur la table (consigne donné par plus haut niveau donc non symetrise)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param isMovementForward vrai si on va en avant et faux si on va en arriere
     * @param headingToWall vrai si on suppose qu'on vas se cogner dans un mur (et qu'on veut s'arreter des qu'on cogne)
     * @param turnOnly vrai si on veut uniquement tourner (et pas avancer)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws UnableToMoveException si le robot a un bloquage mecanique
     */
    private void moveToPointException(Vec2 aim, ArrayList<Hook> hooks, boolean isMovementForward, boolean headingToWall, int turnOnly, boolean mustDetect) throws UnableToMoveException
    {
        if(isMovementForward)
            isRobotMovingForward=true;
        else
            isRobotMovingBackward=true;

        //int maxTimeToWaitForEnemyToLeave = 600; // combien de temps attendre que l'ennemi parte avant d'abandonner
        int unexpectedWallImpactCounter = 1; // combien de fois on réessayer si on se prend un mur (si wall est a true alors les impacts sont attendus donc on s'en fout)
        boolean doItAgain;
        int maxRetriesIfDisengage = 0;

        do
        {
            //si on a pas d'erreur on ne recommence pas
            doItAgain = false;
            try
            {
                moveToPointCorrectAngleAndDetectEnnemy(aim, hooks, isMovementForward, turnOnly!=0, mustDetect);
                isRobotMovingForward=false;
                isRobotMovingBackward=false;
            }
            catch (BlockedException e)
            {
                log.critical( e.logStack());
                log.critical("Haut : Catch de "+e+" dans moveToPointException");

                try {
                    serialWrapper.setTranslationnalSpeed(Speed.SLOW_ALL.translationSpeed);
                    serialWrapper.setRotationnalSpeed(Speed.SLOW_ALL.rotationSpeed);
                }catch (SerialConnexionException e0){
                    log.critical( e0.logStack());
                    log.debug("On ne fait rien après ceci: Catch de "+e0+" dans moveToPointException");
                }

                // si on s'y attendait, on ne fais rien.

                if (!headingToWall && !isForcing) //ici on ne s'y attendait pas donc on reagit
                {
                    if(maxRetriesIfBlocked > actualRetriesIfBlocked)
                    {
                        actualRetriesIfBlocked++;
                        log.debug("Tentative "+actualRetriesIfBlocked+" de deplacement ");
                        if(isMovementForward)
                            isRobotMovingForward=true;
                        else
                            isRobotMovingBackward=true;
                        moveToPointException(aim, hooks, isMovementForward, headingToWall, turnOnly, mustDetect); // on rentente s'il a y eu un probleme
                        isRobotMovingForward=false;
                        isRobotMovingBackward=false;
                    }

	                else if (actualRetriesIfBlocked == maxRetriesIfBlocked)
	                {
		                unexpectedWallImpactCounter--;
		                immobilise();
		                maxRetriesIfDisengage++;

		                /*
		                 * En cas de blocage, on recule (si on allait tout droit) ou on avance.
		                 */

                        try
                        {
                            // log.warning("On n'arrive plus à avancer. On se dégage");
                            log.warning("On n'arrive plus à bouger. On se dégage");
                            if(turnOnly!=0)
                            {
                                log.debug("On était en train de tourner!");

                                isRobotTurning=true;
                                Vec2 NEmi=table.getObstacleManager().getClosestObstacle(highLevelPosition,aim).getPosition();
                                Vec2 centreObstacleRobot=NEmi.minusNewVector(highLevelPosition);
                                int dot=centreObstacleRobot.dot(new Vec2(1000,highLevelOrientation));

                                // on alterne rotation à gauche et à droite
                                //      	if((unexpectedWallImpactCounter & 1) == 0)
                                //    		serialWrapper.turn(lowLevelOrientation+angleToDisengage);
                                //  	else
                                //		serialWrapper.turn(lowLevelOrientation-angleToDisengage);
                                serialWrapper.moveLengthwise((dot/Math.abs(dot))* distanceToDisengage);// On avance ou on recule conformément a ce qui devrait nous dégager
                                serialWrapper.turn(lowLevelOrientation+angleToDisengage*turnOnly);
                            }

                            else if(isMovementForward) {
                                serialWrapper.moveLengthwise(-distanceToDisengage);
                            }
                            else {
                                serialWrapper.moveLengthwise(distanceToDisengage);
                            }

                            while (!isMotionEnded());
                            if(maxRetriesIfDisengage < 2) {
                                doItAgain = true; // si on est arrivé ici c'est qu'aucune exception n'a été levée
                            }
                            else{
                                doItAgain = false;
                                serialWrapper.setTranslationnalSpeed((float)transSpeed);
                                serialWrapper.setRotationnalSpeed((float)rotSpeed);
                            }
	                    }
	                    catch (SerialConnexionException e1)
	                    {
	            			log.critical( e1.logStack());
	                        log.debug("On ne fait rien après ceci: Catch de "+e1+" dans moveToPointException");
	                    }
	                    catch (BlockedException e1)
	                    {
	            			log.critical( e1.logStack());
	                        log.debug("Catch de "+e1+" dans moveToPointException");
	                    	immobilise();

	                    	doItAgain = (maxRetriesIfDisengage < 2);

		                    if(!doItAgain)
		                    {
		                        log.critical("Lancement de UnableToMoveException dans MoveToPointException, visant "+finalAim.getX()+" :: "+finalAim.getY()+" cause physique");
		                        throw new UnableToMoveException(finalAim, UnableToMoveReason.PHYSICALLY_BLOCKED);
		                    }
						}
	                }
                }
                else if(!headingToWall)
                {
                    log.critical("Lancement de UnableToMoveException dans MoveToPointException, visant "+finalAim.getX()+" :: "+finalAim.getY()+" cause physique");
                    throw new UnableToMoveException(finalAim, UnableToMoveReason.PHYSICALLY_BLOCKED);
                }
            }

            catch (UnexpectedObstacleOnPathException unexpectedObstacle)
            {
                log.warning("Ennemi detecté : Catch de "+unexpectedObstacle);
    			log.warning( unexpectedObstacle.logStack());

                int sens = -1;
                if(isRobotMovingForward) {
                    sens = 1;
                }

                moveLengthwiseAndWaitIfEnnemy((int) finalAim.minusNewVector(highLevelPosition).length()*sens, hooks, headingToWall, mustDetect);
            }
            catch(SerialConnexionException e)
            {
                log.critical( e.logStack());

                // TODO : gérer cette exception
            }

        }
        while(doItAgain)
                ;
        // on recommence tant qu'il le faut


        // Tout s'est bien passé

    }


    /**
     * Bloquant. 
     * Gère les hooks, la correction de trajectoire et la détection.
     * @param aim la position visee sur la table (consigne donné par plus haut niveau donc non symetrise)
     * @param hooks les potentiels hooks a prendre en compte (ne pas mettre null !)
     * @param isMovementForward vrai si on va en avant et faux si on va en arriere
     * @param turnOnly vrai si on veut uniquement tourner (et pas avancer)
     * @param mustDetect true si on veut detecter, false sinon.
     * @throws BlockedException si le robot a un bloquage mecanique
     * @throws UnexpectedObstacleOnPathException si le robot rencontre un obstacle inattendu sur son chemin (par les capteurs)
     */
    private void moveToPointCorrectAngleAndDetectEnnemy(Vec2 aim, ArrayList<Hook> hooks, boolean isMovementForward, boolean turnOnly, boolean mustDetect) throws UnexpectedObstacleOnPathException, BlockedException, SerialConnexionException
    {         	
    	//double time=System.currentTimeMillis();
        moveToPointSymmetry(aim, isMovementForward, mustDetect, turnOnly, false);
        do 
        { 	
            updateCurrentPositionAndOrientation();

        	// en cas de détection d'ennemi, une exception est levée
        	if(mustDetect)
        	{
        		if(!basicDetection)
                    detectEnemyAtDistance2(aim.minusNewVector(highLevelPosition.clone()));	// 85 mm est une bonne distance pour être safe.
                else
                {
                    basicDetect(isMovementForward, false);
                }
        		
        		// si un ennemi est détecté à moins de 200, on diminue au minimum la vitesse
/*
        		try
            	{
            		if(mustDetect)
            			detectEnemyAtDistance(150, aim);
            	}
            	catch(UnexpectedObstacleOnPathException e)
            	{
            		//setTranslationnalSpeed(3);
            		
            		//debug
            		log.debug("diminution de la vitesse suite à la détection d'un ennemi proche", this);
            	}
            	*/
        	}
//        	else 
//        		log.debug("Pas de detection demandée", this); 

        	

            //on evalue les hooks (non null !)
            if(hooks != null)
	            for(Hook hook : hooks)
	                hook.evaluate();
                        
            // le fait de faire de nombreux appels permet de corriger la trajectoire
            //if(false && !isCurve && !isForcing)
              //  correctAngle(aim, isMovementForward, mustDetect);
            
            //log.critical("Temps pour finir la boucle d'asservissement "+(System.currentTimeMillis()-time), this);
            //time=System.currentTimeMillis();
            
            // On sleep pour eviter le spam de la serie
            try {
                Thread.sleep(feedbackLoopDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } 
        while(!isMotionEnded())
        	;
        
    }

    private void basicDetect(boolean isMovementForward, boolean turning) throws UnexpectedObstacleOnPathException
    {
        if(isMovementForward || turning)
        {
            if((USvalues.get(0) < basicDetectDistance && USvalues.get(0) != 0) || ((USvalues.get(1) < basicDetectDistance && USvalues.get(1) != 0)))
            {
                log.warning("Lancement de UnexpectedObstacleOnPathException dans basicDetect : Capteurs avant");
                throw new UnexpectedObstacleOnPathException();
            }
        }
        if(!isMovementForward || turning)
        {
            if((USvalues.get(2) < basicDetectDistance && USvalues.get(2) != 0) || ((USvalues.get(3) < basicDetectDistance && USvalues.get(3) != 0)))
            {
                log.warning("Lancement de UnexpectedObstacleOnPathException dans basicDetect : Capteurs arrière");
                throw new UnexpectedObstacleOnPathException();
            }
        }
    }

    /**
     * donne une consigne d'un nouvel angle a atteindre (pour corriger la trajectoire en cours de mouvement)
     * N'EST PAS LANCE EN TRAJECTOIRE COURBE ET EN MOUVEMENT FORCE
     * @param aim la point vise (non symetrisee)
     * @param isMovementForward vrai si on va en avant et faux si on va en arriere
     * @throws BlockedException si le robot a un bloquage mecanique
     * @throws UnexpectedObstacleOnPathException 
     */
    private void correctAngle(Vec2 aim, boolean isMovementForward, boolean mustDetect) throws BlockedException, UnexpectedObstacleOnPathException
    {
    	//envoi de la consigne avec turnOnly a false et a isCorrection a true (c'est bien une correction)
    	//la correction est toujours un turnOnly, on evite les doublons d'où le turnOnly à false.
    	moveToPointSymmetry(aim, isMovementForward, mustDetect, false, true);
    }
   
    /**
     * Non bloquant. 
     * Gère la symétrie et la marche arrière. (si on est en marche arriere le aim doit etre modifié pour que la consigne vers le bas niveau soit bonne)
     * @param aim la position visee sur la tab le (consigne donné par plus haut niveau donc non symetrise)
     * @param isMovementForward vrai si on vas en avant et faux si on vas en arriere
     * @param mustDetect si on autorise la detection pendant ce deplacement
     * @param turnOnly vrai si on veut uniquement tourner (et pas avancer)
     * @param isCorrection vrai si la consigne est une correction et pas un ordre de deplacement
     * @throws BlockedException si le robot rencontre un obstacle innatendu sur son chemin (par les capteurs)
     * @throws UnexpectedObstacleOnPathException 
     */
    private void moveToPointSymmetry(Vec2 aim, boolean isMovementForward, boolean mustDetect, boolean turnOnly,boolean isCorrection) throws BlockedException, UnexpectedObstacleOnPathException
    {
        updateCurrentPositionAndOrientation();

        // position donnée par le bas niveau avec un traitement dans UpdateCurrentPositionAndOrientation
        Vec2 givenPosition = highLevelPosition.clone();

        double distance;
        double angle;
        Vec2 aimSymmetrized = null;

        // Le point qu'on vise, donné par le haut niveau donc comme si on etais vert
        aimSymmetrized = aim.clone();

        if (symetry) // miroir des positions
        {
            givenPosition.setX(-givenPosition.getX());
            aimSymmetrized.setX(-aimSymmetrized.getX());
        }
        Vec2 delta = aimSymmetrized.clone();
        delta.minus(givenPosition);

        //calcul de la nouvelle distance et du nouvel angle
        distance = delta.getR();
        angle = delta.getA();

        // si on a besoin de se retourner pour suivre la consigne de isMovementForward on le fait ici
        if (isMovementForward && distance < 0 || (!isMovementForward && distance > 0)) {
            distance *= -1;
            angle += Math.PI;
        }

        if(isForcing)
        {
            if(turnOnly)
            {
                this.timeExpected = System.currentTimeMillis() + (long)(Math.PI*0.5*1000/this.rotSpeed);
            }
            else
            {
                this.timeExpected = System.currentTimeMillis() + (long)(1.5*1000*Math.abs(distance)/this.transSpeed);
            }
        }

        // on annule la correction si on est trop proche de la destination
        if(isCorrection)
        {
           Vec2 vectorTranslation = aimSymmetrized;
           vectorTranslation.minus( givenPosition );
           if( (  vectorTranslation.length() >  maxLengthCorrectionThreeshold ))
	        	moveToPointSerialOrder(aimSymmetrized, givenPosition, angle, distance, mustDetect, turnOnly, isCorrection);
	        else
	        	return;// Si on est trop proche, on ne fais rien.
        }
        else
        	moveToPointSerialOrder(aimSymmetrized, givenPosition, angle, distance, mustDetect, turnOnly, isCorrection);
    }
    
    /**
     * 
     * Non bloquant. 
     * Avance, envoi a la serie
     * @param symmetrisedAim la position visee sur la table (symetrise)
     * @param givenPosition la position de depart du deplacement
     * @param angle l'angle dont il faut tourner (ordre pour la serie)
     * @param distance la distance dont il faut avancer (ordre pour la serie)
     * @param turnOnly vrai si on veut uniquement tourner (et pas avancer)
     * @param isCorrection vrai si la consigne est une correction et pas un ordre de deplacement
     * @throws BlockedException si le robot rencontre un obstacle innatendu sur son chemin (par les capteurs)
     * @throws UnexpectedObstacleOnPathException
     * TODO Clean la fonction
     */
    private void moveToPointSerialOrder(Vec2 symmetrisedAim, Vec2 givenPosition, double angle, double distance, boolean mustDetect,boolean turnOnly, boolean isCorrection) throws BlockedException, UnexpectedObstacleOnPathException
    {
        // On copie la stratégie de rotation pour éviter qu'elle soit modifiée en plein mouvement
        TurningStrategy cTurningStrategy = turningStrategy;

    	boolean trajectoire_courbe = false;
        double delta = (angle - lowLevelOrientation) % (2 * Math.PI);


        if(cTurningStrategy == TurningStrategy.FASTEST) {
            // Ce code fait juste un modulo 2*pi, avec un résultat entre -PI et +PI
            if (delta > Math.PI)
                delta -= 2 * Math.PI;
            else if (delta < -Math.PI)
                delta += 2 * Math.PI;
        }

            /**
             * Si on fait une correction, il faut vérifier la distance à la consigne et la correction
             * Si la distance est grande et l'angle petit, alors on fait la correction en angle sans s'arrêter
             * Sinon on annule la correction
             * (par exemple, si le robot dépasse un peu la consigne, la correction le ferait se retourner ce qui n'est pas le résultat demandé)
             */
            if (isCorrection) {
                //Si l'angle petit, alors on fait la correction en angle
                if ((Math.abs(delta) < Math.PI / 8)) {
                    //on active la correction (on attendra pas d'avoir fini de tourner (le robot) pour reprendre le programme)
                    trajectoire_courbe = true;
                } else {
                    log.debug("correction en abandon; delta :" + delta);
                    return;
                }
            }

            try {
                if (isCorrection && Math.abs(delta) > maxRotationCorrectionThreeshold) {
                    isRobotTurning = true;// prochain ordre : on tourne

                    //On utilise la stratégie FASTEST pour les petits mouvements
                    serialWrapper.turn(angle, TurningStrategy.FASTEST);  // On ne tourne que si on est assez loin de l'orientation voulu

                    log.debug("Angle corrigé");
                }
                else if(!isCorrection && Math.abs(delta) < maxRotationTurningStrategyIgnore)//Si ce n'est pas une correction
                {
                    if (Math.abs(delta) > maxRotationCorrectionThreeshold) {// on ne tourne vraiment que si l'angle souhaité est vraiment different.
                        isRobotTurning = true;// prochain ordre : on tourne
                    }
                    serialWrapper.turn(angle, TurningStrategy.FASTEST);
                }
                else if (!isCorrection)// Si ca n'est pas  une correction et qu'on dépasse l'angle limite
                {
                    if (Math.abs(delta) > maxRotationCorrectionThreeshold) {// on ne tourne vraiment que si l'angle souhaité est vraiment different.
                        isRobotTurning = true;// prochain ordre : on tourne
                    }
                    serialWrapper.turn(angle, cTurningStrategy);
                }

                // sans virage : la première rotation est bloquante
                if (!trajectoire_courbe)
                    // on attend la fin du mouvement
                    while (!isMotionEnded()) {
                        if (mustDetect)
                        {
                            if(basicDetection)
                                basicDetect(false, true);
                        }
                        Sleep.sleep(feedbackLoopDelay);
                    }

                isRobotTurning = false; // fin du turn

                if (!(turnOnly || isCorrection))
                    serialWrapper.moveLengthwise(distance);
            } catch (SerialConnexionException e) {
                log.critical("Catch de " + e + " dans moveToPointSerialOrder");
                log.critical(e.logStack());
                isRobotTurning = false; // Meme avec un catch, on a fini de tourner
            }

    }
    
    /**
     * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
     *  	
     *  	false : si on roule
     *  	true : si on est arrivé à destination
     *  	exception : si patinage
     * 
     * 
     * @return oui si le robot est arrivé à destination, non si encore en mouvement
     * @throws BlockedException si patinage (donc bloquage mecanique)
     */
    private boolean isMotionEnded() throws BlockedException
    {
        try {
            // récupérations des informations d'acquittement
            boolean[] infos = serialWrapper.isRobotMovingAndAbnormal();
            // 0-false : le robot ne bouge pas

            //log.debug("Test deplacement : reponse "+ infos[0] +" :: "+ infos[1], this);

            if (!infos[0])//si le robot ne bouge plus
            {
                if (infos[1])//si le robot patine, il est bloqué
                {
                    log.critical("Robot bloqué, lancement de BlockedException dans isMotionEnded");
                    throw new BlockedException();
                } else {
                    return !infos[0];//On est arrivés
                }
            }
            else if(isForcing && System.currentTimeMillis() > this.timeExpected)
            {
                log.critical("Le robot force, on l'arrête.");
                this.immobilise();
                throw new BlockedException();
            }
            else {
                return !infos[0];//toujours pas arrivé
            }
        }
        catch (SerialConnexionException e) 
        {
            log.critical("Catch de "+e+" dans isMotionEnded");
			log.critical( e.logStack());
            return false;
        }
    }

    /**
     * fonction vérifiant que l'on ne va pas taper dans le robot adverse.
     * test si le cercle devant (ou derriere en fonction du mouvement) est vide d'obstacle
     * @param front vrai si on veut detecter a l'avant du robot (donc si on avance en marche avant)
     * @param isTurnOnly On detecte differement si on tourne ou translate
     * @param aim 
     * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
     */
    public void detectEnemyInDisk(boolean front, boolean isTurnOnly, Vec2 aim) throws UnexpectedObstacleOnPathException
    {
        int signe = -1;
        if(front)
            signe = 1;
        
        //rayon du cercle de detection
        int detectionRadius = robotLength/2 + detectionRay;
        
        //centre du cercle de detection
        Vec2 detectionCenter = new Vec2((int)(signe * detectionRadius * Math.cos(highLevelOrientation)), 
        								(int)(signe * detectionRadius * Math.sin(highLevelOrientation)) ); //centre par rapport au centre de position du robot
        	
        detectionCenter.plus(highLevelPosition);

        // si on ne tourne pas, on regarde devant nous : sinon, on regarde autour de nous
        if(isTurnOnly || isRobotTurning)
        	detectionCenter=highLevelPosition;
        
        if(table.getObstacleManager().isDiscObstructed(detectionCenter, detectionRay))
        {
            log.warning("Lancement de UnexpectedObstacleOnPathException dans detectEnemyInLocatedDisk");
            throw new UnexpectedObstacleOnPathException();
        }
    }

    /**
     * vérifie que la zone spécifiée est libre de tout obstacle
     * @param aim le centre de la zone à vérifier
     * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
     */
    public void detectEnemyInLocatedDisk(Vec2 aim) throws UnexpectedObstacleOnPathException {

        //rayon du cercle de detection
        int detectionRadius = robotLength/2 + detectionRay;

        if(table.getObstacleManager().isDiscObstructed(aim, detectionRadius))
        {
            log.warning("Lancement de UnexpectedObstacleOnPathException dans detectEnemyInDisk");
            throw new UnexpectedObstacleOnPathException();
        }
    }

    /**
     * Lance une exception si un ennemi se trouve a une distance inférieure a celle spécifiée
     * @param distance distance jusqu'a un ennemi en mm en dessous de laquelle on doit abandonner le mouvement
     * @param movementDirection direction de mouvment du robot
     * @throws UnexpectedObstacleOnPathException si obstacle sur le chemin
     */
    public void detectEnemyAtDistance(int distance, Vec2 movementDirection) throws UnexpectedObstacleOnPathException
    {
        int closest = table.getObstacleManager().distanceToClosestEnemy(highLevelPosition, movementDirection);
        //log.debug(closest);
        if(closest <= distance && closest > -150)
        {
        	log.debug("DetectEnemyAtDistance voit un ennemi trop proche pour continuer le déplacement (distance de " 
        			 + table.getObstacleManager().distanceToClosestEnemy(highLevelPosition, movementDirection) +" mm)");
        	immobilise();
        	throw new UnexpectedObstacleOnPathException();
        }
    }

    /**
     * Lance une exception si un ennemi se trouve sur le chemin à "detection Distance"
     * @param movementDirection direction du robot
     * @throws UnexpectedObstacleOnPathException si l'obstacle est sur le chemin
     */
    public void detectEnemyAtDistance2(Vec2 movementDirection) throws UnexpectedObstacleOnPathException
    {
        if(table.getObstacleManager().isEnnemyForwardorBackWard(detectionDistance + 50, highLevelPosition, movementDirection, highLevelOrientation)){
            log.debug("DetectEnemyAtDistance voie un ennemi sur le chemin");
            immobilise();
            throw new UnexpectedObstacleOnPathException();
        }
    }

    /**
     * Met à jour position et orientation via la carte d'asservissement.
     * Donne la veritable positions du robot sur la table
     * @throws SerialConnexionException
     */
    
    private void updateCurrentPositionAndOrientation()
    {
        try 
        {
            float[] infos = serialWrapper.getCurrentPositionAndOrientation();

            if(infos == null)
                return;
            
            lowLevelPosition.setX((int)infos[0]);
            lowLevelPosition.setY((int)infos[1]);

            double orientationConvention = infos[2]%(2*Math.PI);
            if (orientationConvention > Math.PI){
                orientationConvention -= 2*Math.PI;
            }
            else if(orientationConvention < -Math.PI){
                orientationConvention += 2*Math.PI;
            }

            lowLevelOrientation = orientationConvention;
            
            highLevelPosition=lowLevelPosition.clone();
            highLevelOrientation=lowLevelOrientation;
            
            if(symetry)
            {
            	highLevelPosition.setX( -highLevelPosition.getX());
            	highLevelOrientation=Math.PI-highLevelOrientation;
            }
            
        }
        catch(SerialConnexionException e)
        {
        	log.critical("Catch de "+e+" dans updateCurrentPositionAndOrientation");
			log.critical( e.logStack());
        }
    }

    public void setUSvalues(ArrayList<Integer> val)
    {
        this.USvalues = val;
    }


    @Override
    public void updateConfig()
    {
    	try 
    	{
	    	detectionRay = Integer.parseInt(config.getProperty("rayon_detection"));
	    	detectionDistance = Integer.parseInt(config.getProperty("distance_detection"));
	        distanceToDisengage = Integer.parseInt(config.getProperty("distance_degagement_robot"));
	        feedbackLoopDelay = Integer.parseInt(config.getProperty("sleep_boucle_acquittement"));
	        angleToDisengage = Double.parseDouble(config.getProperty("angle_degagement_robot"));
			symetry = config.getProperty("couleur").replaceAll(" ","").equals("jaune");
			robotLength = Integer.parseInt(config.getProperty("longueur_robot").replaceAll(" ",""));
            basicDetectDistance = Integer.parseInt(config.getProperty("basic_distance").replaceAll(" ",""));
            basicDetection = Boolean.parseBoolean(config.getProperty("basic_detection"));
            timeToWaitIfEnnemy = Integer.parseInt(config.getProperty("duree_checkout_ennemie"));
            timeOutEnnemyMove = Integer.parseInt(config.getProperty("duree_attente_ennemie"));
    	}
    	catch (ConfigPropertyNotFoundException e)
    	{
    		log.debug("Revoir le code : impossible de trouver la propriété "+e.getPropertyNotFound());
			log.critical( e.logStack());
    	}
    }

    /**
     * Arrête le robot.
     */
    public void immobilise()
    {
        log.warning("Arrêt du robot en "+lowLevelPosition);
        try 
        {
            serialWrapper.immobilise();
        } 
        catch (SerialConnexionException e) 
        {
            log.critical("Catch de "+e+" dans immobilise");
			log.critical( e.logStack());
        }           
    }

    /**
     * Met à jour la position. A ne faire qu'en début de match.
     * @param positionWanted
     * @throws SerialConnexionException
     */
    public void setPosition(Vec2 positionWanted)
    {
        this.lowLevelPosition = positionWanted.clone();
        this.highLevelPosition = positionWanted.clone();
        if(symetry)
        	this.lowLevelPosition.setX(-this.lowLevelPosition.getX());// on lui met la vraie position
		try 
		{
			serialWrapper.setX(this.lowLevelPosition.getX());
	        serialWrapper.setY(this.lowLevelPosition.getY());
		} 
		catch (SerialConnexionException e)
		{
            log.critical("Catch de "+e+" dans setPosition");
			log.critical( e.logStack());
		}
		Sleep.sleep(300);
    }

    /**
     * Met à jour l'orientation. A ne faire qu'en début de match.
     * @param orientation
     */
    public void setOrientation(double orientation)
    {
        this.lowLevelOrientation = orientation;
        this.highLevelOrientation = orientation;
        if(symetry)
        	this.lowLevelOrientation = Math.PI-this.lowLevelOrientation; // la vraie orientation
        try 
        {
    		serialWrapper.setOrientation(this.lowLevelOrientation);
        }
        catch (SerialConnexionException e) 
        {
            log.critical("Catch de "+e+" dans setOrientation");
			log.critical( e.logStack());
        }
    }

    /**
     * @return la position du robot en debut de match
     */
    public Vec2 getPosition()
    {
        updateCurrentPositionAndOrientation();
        Vec2 out = highLevelPosition.clone();
        return out;
    }

    /**
     * Pour les Thread ayant régulierement besoin de la position, on utilise un Fast pour éviter de flood la série
     * @return la dernière position connue de Locomotion
     */
    public Vec2 getPositionFast()
    {
        Vec2 out = highLevelPosition.clone();
        return out;
    }

    /**
     * 
     * @return l'orientation du robot en debut de match
     */
    public double getOrientation()
    {
        updateCurrentPositionAndOrientation();
        return highLevelOrientation;
    }

    /** De meme que la position mais pour l'orientation */
    public double getOrientationFast()
    {
        return highLevelOrientation;
    }
    
    public TurningStrategy getTurningOrders()
    {
    	return turningStrategy;
    }
    
    public void setTurningOrders(TurningStrategy turning)
    {
    	this.turningStrategy = turning;
    }
    
    public DirectionStrategy getDirectionStrategy()
    {
    	return directionStrategy;
    }
    
    public void setDirectionOrders(DirectionStrategy motion)
    {
    	this.directionStrategy = motion;
    }

    public void desasservit()
    {
        try
        {
            serialWrapper.disableRotationnalFeedbackLoop();
            serialWrapper.disableTranslationnalFeedbackLoop();
        } catch (SerialConnexionException e)
        {
            log.critical("Catch de "+e+" dans desasservit");
			log.critical( e.logStack());

        }
    }

    public void setRotationnalSpeed(double rotationSpeed) throws SerialConnexionException
    {
        serialWrapper.setRotationnalSpeed(rotationSpeed);
        this.rotSpeed = rotationSpeed;
    }

    public void setTranslationnalSpeed(float speed) throws SerialConnexionException
    {
        serialWrapper.setTranslationnalSpeed(speed);
        this.transSpeed = speed;
    }

    
    public void enableFeedbackLoop() throws SerialConnexionException
    {
        serialWrapper.enableRotationnalFeedbackLoop();
        serialWrapper.enableTranslationnalFeedbackLoop();
        serialWrapper.enableSpeedFeedbackLoop();
    }

    public void disableFeedbackLoop() throws SerialConnexionException
    {
        serialWrapper.disableRotationnalFeedbackLoop();
        serialWrapper.disableTranslationnalFeedbackLoop();
        serialWrapper.disableSpeedFeedbackLoop();
    }
    
    public void disableRotationnalFeedbackLoop() throws SerialConnexionException
    {
		serialWrapper.disableRotationnalFeedbackLoop();
    }

    public void enableRotationnalFeedbackLoop() throws SerialConnexionException
    {
		serialWrapper.enableRotationnalFeedbackLoop();
    }

	public void disableTranslationalFeedbackLoop() throws SerialConnexionException
	{
		serialWrapper.disableTranslationnalFeedbackLoop();
	}

    /**
     * Change le type de mouvement forcé/normal
     * @param choice true pour forcer les mouvements
     */
    public synchronized void setForceMovement(boolean choice) throws SerialConnexionException
    {
        if(isForcing != choice)
        {
            serialWrapper.setForceMovement(choice);
            this.isForcing = choice;
        }
    }

    /**
     * Change l'accélération en plus fluide mais plus lente
     */
    public synchronized void setSmoothAcceleration(boolean choice) throws SerialConnexionException
    {
        serialWrapper.setSmoothAcceleration(choice);
    }

    public void setBasicDetection(boolean basicDetection)
    {
        this.basicDetection = basicDetection;
    }

	public void close()
	{
		serialWrapper.closeLocomotion();
	}	
	
	/**************************************************
	 * 					JUNITS
	 **************************************************/

	// Aller Clément, tu peux le faire !
	// Je te laisse le carte blanche, tu fera mieux cette fois ci ^^
	
    /**
     * FONCTION JUNIT TEST
     */
	 @SuppressWarnings("javadoc")
	public void JUNIT_moveToPointForwardBackward(Vec2 aim, ArrayList<Hook> hooks, boolean mur, DirectionStrategy strategy, int turnOnly, boolean mustDetect) throws UnableToMoveException, EnnemyCrashedException
    {
		 moveToPointForwardBackward(aim, hooks, mur, strategy, turnOnly, mustDetect);
    }
	 
	    
    /**
     * FONCTION JUNIT TEST
     */
	 @SuppressWarnings("javadoc")
    public void JUNIT_moveToPointException(Vec2 aim, ArrayList<Hook> hooks, boolean isMovementForward, boolean headingToWall, int turnOnly, boolean mustDetect) throws UnableToMoveException, EnnemyCrashedException
    {

    	moveToPointException(aim, hooks, isMovementForward, headingToWall, turnOnly, mustDetect);
    }
    
    /**
     * FONCTION POUR JUNIT. 
     */
	 @SuppressWarnings("javadoc")
    public void JUNIT_moveToPointCorrectAngleAndDetectEnnemy(Vec2 aim, ArrayList<Hook> hooks, boolean isMovementForward, boolean turnOnly, boolean mustDetect) throws UnexpectedObstacleOnPathException, BlockedException, SerialConnexionException
    {
    	moveToPointCorrectAngleAndDetectEnnemy(aim, hooks, isMovementForward, turnOnly, mustDetect);
    }
    
    /**
     *  POUR JUNIT
     */
	 @SuppressWarnings("javadoc")
    public boolean JUNIT_isMotionEnded()
    {
    	return JUNIT_isMotionEnded();
    }

    /**
     *  POUR JUNIT
     */
	 @SuppressWarnings("javadoc")
    public void JUNIT_correctAngle(Vec2 aim, boolean isMovementForward, boolean mustDetect) throws BlockedException, UnexpectedObstacleOnPathException
    {
    	correctAngle(aim, isMovementForward, mustDetect);
    }

    /**
     *  POUR JUNIT
     */
    public void JUNIT_updateCurrentPositionAndOrientation()
    {
    	updateCurrentPositionAndOrientation();
    }

    /**
     *  POUR JUNIT
     */
	 @SuppressWarnings("javadoc")
    public void JUNIT_moveToPointSymmetry(Vec2 aim, boolean isMovementForward, boolean mustDetect, boolean turnOnly,boolean isCorrection) throws BlockedException, UnexpectedObstacleOnPathException
    {
    	moveToPointSymmetry(aim, isMovementForward, mustDetect, turnOnly, isCorrection);
    }

    /**
     *  POUR JUNIT
     */
	 @SuppressWarnings("javadoc")
    public void JUNIT_moveToPointSerialOrder(Vec2 symmetrisedAim, Vec2 givenPosition, double angle, double distance, boolean mustDetect,boolean turnOnly, boolean isCorrection) throws BlockedException, UnexpectedObstacleOnPathException
    {
    	moveToPointSerialOrder( symmetrisedAim, givenPosition, angle, distance, mustDetect, turnOnly,  isCorrection);
    }

}
