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

package threads.dataHandlers;

import exceptions.ConfigPropertyNotFoundException;
import graphics.Window;
import robot.Robot;
import robot.SerialWrapper;
import smartMath.Circle;
import smartMath.Vec2;
import smartMath.Geometry;
import table.Table;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCircular;
import table.obstacles.ObstacleProximity;
import threads.AbstractThread;
import threads.ThreadTimer;
import utils.Sleep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.LinkedList;

import static java.lang.Math.PI;
import static smartMath.Geometry.isBetween;
import static smartMath.Geometry.square;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs,
 * Et enleve ceux qui ont disparu, de meme que verifie les capteurs de contact
 *
 * @author pf, Krissprolls, marsu, discord
 */

public class ThreadSensor extends AbstractThread
{
	/** Le robot */
	private Robot mRobot;

    /** La table */
    private Table mTable;

	/** La stm avec laquelle on doit communiquer */
	private SerialWrapper serialWrapper;

    /** Buffer de valeurs */
    private LinkedList<String> valuesReceived;
	
	/** interface graphique */
	public Window window;
	
	// Valeurs par défaut s'il y a un problème de config
	
	/** fréquence de mise a jour des valeurs renvoyés par les capteurs. Valeurs par défaut de 5 fois par seconde s'il y a un problème de config */
	// Overide par la config
	private int sensorFrequency = 15;

    /**
     * Temps maximal entre deux séries de valeurs (ms) : si cette série est incomplète, on la vire; cela évite les déclages
     */
    private int thresholdUSseries = 20;
    // TODO Mettre un canal par capteur afin de fiabiliser les séries de valeurs US

    /**
     * Si l'on doit symétriser
     */
    private boolean symetry;

    /**
     * Rayon du robot adverse
     */
    private int radius;

    /**
     * Permet de désactiver les capteurs de la porte que récupère du sable, évite de récupérer des fausses valeurs
     */
    private static boolean modeBorgne = false;

	/**
	 * Distance maximale fiable pour les capteurs : au dela, valeurs abberentes
	 * Override par la config
	 */
	double maxSensorRange;

	/**
	 * Distance minimale à laquelle on peut se fier aux capteurs : ne pas detecter notre propre root par exemple
     * Override par la config
	 */
	double minSensorRange;

    private BufferedWriter out;
    private final boolean debug = true;
	
	/**
	 *  Angle de visibilité qu'a le capteur 
	 * Override par la config
	 * 
	 * Calcul de l'angle :
	 * 
	 *   |angle/ \angle|		 
	 * 	 |	  /	  \    |		 
	 * 	 |	 /	   \   |		
	 * 	 |	/	    \  |		
	 * 	 |o/         \o|		
	 * 		  Robot			o : capteur
	 * 
	 */
	double detectionAngle;
	double sensorPositionAngle;

    /**
     * Angles des capteurs relatifs à l'axe avant-arrière du robot (radians)
     * Convention: on effectue les calculs dans le reprère du robot, ce dernier étant orienté vers 0 (axe x)
     * Pour changer de repère, il faut effectuer une rotation des vecteurs de l'orientation du robot + une translation sur sa position.
     */
    private final double angleLF = sensorPositionAngle;
    private final double angleRF = -sensorPositionAngle;
    private final double angleLB = sensorPositionAngle - Math.PI;
    private final double angleRB = -sensorPositionAngle + Math.PI;


    /**
     * Positions relatives au centre du robot
     */

    private final Vec2 positionLF = new Vec2(240, 150);
    private final Vec2 positionRF = new Vec2(240, -150);
    private final Vec2 positionLB = new Vec2(-150,135);
    private final Vec2 positionRB = new Vec2(-150,-135);

    /**
     * Delai d'attente avant de lancer le thread
     * Pour éviter de détecter la main du lanceur
     */
    private static boolean delay = true;

    /**
     * Valeurs des capteurs US {avant-gauche, avant-droit, arrière gauche, arrière-droit}
     */
    ArrayList<Integer> USvalues = new ArrayList<Integer>(4);

    /**
     * Valeurs de capteurs modifiées pour la suppression d'obstacle
     * Ainsi si l'un des capteurs nous indique 4km, c'est sûrement qu'il n'y a rien devant lui
     * On sépare ce qui sert à détecter de ce qui sert à ne pas détecter (oui c'est trop méta pour toi...)
     * PS : Si il indique 4 km, y'a un pb hein...
     */
    ArrayList<Integer> USvaluesForDeletion = new ArrayList<>();

	/**
	 * Largeur du robot recuperée sur la config
	 */
	int robotWidth;
	
	/**
	 * 	Longueur du robot recuperée sur la config
	 */
	int robotLenght;


    /**
	 * Crée un nouveau thread de capteurs
	 *
	 * @param table La table a l'intérieure de laquelle le thread doit croire évoluer
	 * @param sensorsCardWrapper La carte capteurs avec laquelle le thread va parler
	 */
	public ThreadSensor (Table table, Robot robot, SerialWrapper sensorsCardWrapper, ThreadSerial serial)
	{
		super(config, log);
		this.serialWrapper = sensorsCardWrapper;
        this.valuesReceived = serial.getUltrasoundBuffer();
		Thread.currentThread().setPriority(6);
		mRobot = robot;
        mTable = table;
	}
	
	@Override
	public void run()
	{
		updateConfig();

        try
        {
            File file = new File("us.txt");

            if (!file.exists()) {
                //file.delete();
                file.createNewFile();
            }

            out = new BufferedWriter(new FileWriter(file));

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*while(serialWrapper.isJumperAbsent())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(!serialWrapper.isJumperAbsent())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

		// maintenant que le jumper est retiré, le match a commencé
		ThreadTimer.matchEnded = false;

        if(ThreadSensor.delay)
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		
		// boucle principale, celle qui dure tout le match
		log.debug("Activation des capteurs");
		while(!ThreadTimer.matchEnded)
		{

			// on s'arrete si le ThreadManager le demande
			if(stopThreads)
			{
				log.debug("Stop du thread capteurs");
				return;
			}
            //long time = System.currentTimeMillis();

			getDistances();
            
            if( !USvalues.contains(-1)) // si on n'a pas spammé
			{
				// On enleve les obstacles qu'on sait absents de la table : si le robot ennemi a bougé,
				// On l'enleve de notre memoire
                mRobot.getPositionFast();
                removeObstacle();

                for(int i=0 ; i<USvalues.size(); i++)
                {
                    if(USvalues.get(i) != 0)
                        USvalues.set(i, USvalues.get(i)/*+radius/2*/);
                }

				//ajout d'obstacles mobiles dans l'obstacleManager
				addObstacle();
			}

			try 
			{
				// On ne spamme pas la serie, on attend un peu avant de redemander les valeurs
				// Et on laisse le temps aux autres Threads

				Thread.sleep((long)(1000./sensorFrequency));
			} 
			catch (InterruptedException e)
			{
				break;
			}			
		}
        log.debug("Fin du thread de capteurs");
		
	}
	
	/**
	 * ajoute les obstacles a l'obstacleManager
	 */
	private void addObstacle()
	{		
        if(USvalues.get(0) != 0 && USvalues.get(1) != 0) {
            addFrontObstacleBoth();
        }
        else if((USvalues.get(0) != 0 || USvalues.get(1) != 0)) {
            addFrontObstacleSingle(USvalues.get(0) != 0);
        }

        if(USvalues.get(2) != 0 && USvalues.get(3) != 0 ) {
            addBackObstacleBoth();
        }
        else if((USvalues.get(2) != 0 || USvalues.get(3) != 0)) {
            addBackObstacleSingle(USvalues.get(2) != 0);
        }
	}

    /**
     * Ajoute un obstacle en face du robot, avec les deux capteurs ayant détecté quelque chose
     * Convention: la droite du robot est l'orientation 0 (on travaille dans le repère du robot, et on garde les memes conventions que pour la table)
     */

    private void addFrontObstacleBoth() {

        // On résoudre l'équation du second degrée afin de trouver les deux points d'intersections des deux cercles
        // On joue sur le rayon du robot adverse pour etre sur d'avoir des solutions
        double robotX1, robotX2;
        double robotY1;
        double b, c, delta;
        int constante, R1, R2;
        Vec2 vec = new Vec2();

        R1 = USvalues.get(0) + radius;
        R2 = USvalues.get(1) + radius;
        constante = square(R1) - square(R2) - square(positionLF.getX()) + square(positionRF.getX()) - square(positionLF.getY()) + square(positionRF.getY());

        b = -2 * positionRF.getX() + 2 * positionRF.getY();
        c = (double) square(constante) / (4 * square(positionRF.getY() - positionLF.getY())) + (double) (constante * positionRF.getY()) / (positionRF.getY() - positionLF.getY()) + square(positionRF.getX()) + square(positionRF.getY()) - square(R2);

        delta = b*b - 4*c;
        if (!isBetween(delta, -1, 1)) {
            robotX1 = (int) ((-b - Math.sqrt(delta)) / 2);
            robotX2 = (int) ((-b + Math.sqrt(delta)) / 2);
            robotY1 = (double) constante / (4*positionLF.getX());

            Vec2 vec0 = new Vec2((int)robotX1, (int)robotY1);
            Vec2 vec1 = new Vec2((int)robotX2, (int)robotY1);
            ArrayList<Vec2> listVec = new ArrayList<Vec2>(4);
            listVec.add(vec0);
            listVec.add(vec1);

            for (Vec2 v:listVec){
                if (v.getX()>=0){
                     vec=v;
                }
            }
        }
        else{
            robotX1 = (int) -b/2;
            robotY1 = - constante / (2 * (positionLF.getY() - positionRF.getY()));
            vec = new Vec2(robotX1, robotY1);
        }

        vec.setA(vec.getA()+mRobot.getOrientationFast());
        mTable.getObstacleManager().addObstacle(mRobot.getPositionFast().plusNewVector(vec), radius, 200);
    }
    /**
     * Ajoute un obstacle derrière le robot, avec les deux capteurs ayant détecté quelque chose
     */
    private void addBackObstacleBoth()
    {
        // De meme que le front, seule la selection de la bonne solution change
        double robotX1, robotX2;
        double robotY1;
        double b, c, delta;
        int constante, R1, R2;
        Vec2 vec = new Vec2();

        R1 = USvalues.get(2) + radius;
        R2 = USvalues.get(3) + radius;
        constante = square(R1) - square(R2) - square(positionLB.getX()) + square(positionRB.getX()) - square(positionLB.getY()) + square(positionRB.getY());

        b = -2 * positionLB.getX() - 2 * positionLB.getY();
        c = (double) square(constante) / (4 * square(positionLB.getY() - positionRB.getY())) + (double) constante / (2 * (positionLB.getY() - positionRB.getY())) + square(positionLB.getX()) + square(positionLB.getY()) - square(R1);

        delta = b*b - 4*c;
        if (!isBetween(delta, -1, 1)) {
            robotX1 = (int) ((-b - Math.sqrt(delta)) / 2);
            robotX2 = (int) ((-b + Math.sqrt(delta)) / 2);
            robotY1 = (double) constante / (4*positionLB.getX());

            Vec2 vec0 = new Vec2((int)robotX1, (int)robotY1);
            Vec2 vec1 = new Vec2((int)robotX2, (int)robotY1);

            ArrayList<Vec2> listVec = new ArrayList<Vec2>(4);
            listVec.add(vec0);
            listVec.add(vec1);

            for(Vec2 v : listVec){
                if(v.getX() <0){
                    vec=v;
                }
            }
        }
        else{
            robotX1 = (int) -b/2;
            robotY1 = -constante / (2 * (positionLB.getY() - positionRB.getY()));
            vec = new Vec2(robotX1, robotY1);
        }

        vec.setA(vec.getA()+mRobot.getOrientationFast());
        mTable.getObstacleManager().addObstacle(mRobot.getPositionFast().plusNewVector(vec), radius, 200);
    }

    /**
     * Ajoute un obstacle devant le robot, avec un seul capteur ayant détecté quelque chose
     * @param isLeft si c'est le capteur gauche
     */
    private void addFrontObstacleSingle(boolean isLeft)
    {
        // On modélise les arcs de cercle detecté par l'un des capteurs, puis on prend le point le plus à l'exterieur
        // Et on place le robot ennemie tangent en ce point : la position calculée n'est pas la position réelle du robot adverse mais elle suffit

        Circle arcL = new Circle(positionLF, USvalues.get(0), detectionAngle, angleLF, false);
        Circle arcR = new Circle(positionRF, USvalues.get(1), detectionAngle, angleRF, false);
        Vec2 posEn = new Vec2();

        if (isLeft){
            // On choisit le point à l'extrémité de l'arc à coté du capteur pour la position de l'ennemie: à courte distance, la position est réaliste,
            // à longue distance (>1m au vue des dimensions), l'ennemie est en réalité de l'autre coté
            Vec2 posDetect = new Vec2(USvalues.get(0), angleLF + detectionAngle/2);
            double angleEn = angleRF + detectionAngle/2;
            posEn = posDetect.plusNewVector(new Vec2(radius, angleEn)).plusNewVector(positionLF);
        }
        else{
            Vec2 posDetect = new Vec2(USvalues.get(1), angleRF - detectionAngle/2);
            double angleEn = angleLF - detectionAngle/2;
            posEn = posDetect.plusNewVector(new Vec2(radius, angleEn)).plusNewVector(positionRF);
        }

        posEn.setA(posEn.getA()+mRobot.getOrientationFast());
        mTable.getObstacleManager().addObstacle(mRobot.getPositionFast().plusNewVector(posEn), radius, 200);
    }

    /**
     * Ajoute un obstacle derrière le robot, avec un seul capteur ayant détecté quelque chose
     * @param isLeft si c'est le capteur gauche
     */
    private void addBackObstacleSingle(boolean isLeft) {
        // De meme qu'avec le front

        Circle arcL = new Circle(positionLB, USvalues.get(2), detectionAngle, angleLB, false);
        Circle arcR = new Circle(positionRB, USvalues.get(3), detectionAngle, angleRB, false);
        Vec2 posEn;

        if (isLeft){
            Vec2 posDetect = new Vec2(USvalues.get(2),angleLB - detectionAngle/2);
            double angleEn = angleRB - detectionAngle/2;
            posEn = posDetect.plusNewVector(new Vec2(radius, angleEn)).plusNewVector(positionLB);
        }
        else{
            Vec2 posDetect = new Vec2(USvalues.get(3),angleRB + detectionAngle/2);
            double angleEn = angleLB + detectionAngle/2;
            posEn = posDetect.plusNewVector(new Vec2(radius, angleEn)).plusNewVector(positionRB);
        }

        posEn.setA(posEn.getA()+mRobot.getOrientationFast());
        mTable.getObstacleManager().addObstacle(mRobot.getPositionFast().plusNewVector(posEn), radius, 200);
    }


    /**
     * Passe du référentiel centré sur le point donné au référentiel terrestre
     * @param posPoint la position relative dont on cherche les coordonées absolues
     * @param posOrigin la position de l'origine du reférentiel
     * @param orientation l'orientation du référentiel par rapport au terrestre
     */
    private Vec2 changeRef(Vec2 posPoint, Vec2 posOrigin, double orientation)
    {
        return new Vec2((int)(posPoint.getX()*Math.cos(orientation)+posPoint.getY()*Math.sin(orientation)+posOrigin.getX()),
                (int)(posPoint.getX()*Math.sin(orientation)-posPoint.getY()*Math.cos(orientation)+posOrigin.getY()));
    }

	/**
	 * Recupere la distance lue par les ultrasons 
	 * @return la distance selon les ultrasons
	 */
	@SuppressWarnings("unchecked")
    public void getDistances()
	{
        try
		{
            ArrayList<String> r = new ArrayList<>();
            ArrayList<Integer> res = new ArrayList<>();
            byte count=0;
            long timeBetween;
            String toKeep;
            long timeToKeep;

            ArrayList<Long> sensorTime = new ArrayList<Long>(4);

            while(count < 4)
            {
                // On attend tant que l'on a pas reçu 4 valeurs
                if(valuesReceived.peek() != null)
                {
                    sensorTime.add(System.currentTimeMillis());
                    r.add(valuesReceived.poll());

                    if (count !=0){
                        timeBetween = sensorTime.get(count) - sensorTime.get(count-1);
                        try{
                            // Si l'on a attendu trop longtemps entre 2 valeurs, c'est que la dernière fait partie d'une nouvelle série et
                            // que la série actuelle est incomplète; on clear cette série de valeurs et on prend la suivante
                            if (timeBetween > thresholdUSseries) {

                                toKeep = r.get(count);
                                timeToKeep = sensorTime.get(count);
                                r.clear();
                                sensorTime.clear();
                                r.add(toKeep);
                                sensorTime.add(timeToKeep);
                                count = 0;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    count++;
                }
                else
                    Sleep.sleep(100);
            }

            for(String s : r) {
                res.add(Integer.parseInt(s.substring(2)));
            }

            USvalues = res;

            if(this.debug)
            {
               try {
                    out.write(USvalues.get(0).toString());
                    out.newLine();
                    out.write(USvalues.get(1).toString());
                    out.newLine();
                    out.write(USvalues.get(2).toString());
                    out.newLine();
                    out.write(USvalues.get(3).toString());
                    out.newLine();
                    out.newLine();
                    out.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Etant donnée que l'on travaille sur le référentiel du robot, la symétrie devrait normalement etre inutile => A tester
            if(symetry) //Inversion gauche/droite pour symétriser
            {
                int temp = USvalues.get(0);
                USvalues.set(0, USvalues.get(1));
                USvalues.set(1, temp);
                temp = USvalues.get(2);
                USvalues.set(2, USvalues.get(3));
                USvalues.set(3, temp);
            }

            if(modeBorgne)
                USvalues.set(1, 0);

            mRobot.setUSvalues((ArrayList<Integer>) USvalues.clone());

            USvaluesForDeletion.clear();
            for(int i=0 ; i<4 ; i++)
            {
                USvaluesForDeletion.add((int)(USvalues.get(i).intValue()*0.8));
            }

            for(int i=0 ; i<USvalues.size() ; i++)
            {
                // On met tout les capteurs qui detectent un objet trop proche du robot ou à plus de maxSensorRange a 0
                // TODO : a passer en traitement de bas niveau ? Non, ce traitement peut dépendre de la façon dont on calcule la position adverse
                if ( USvalues.get(i) > maxSensorRange)
                {
                    USvalues.set(i, 0);
                    USvaluesForDeletion.set(i, (int)(maxSensorRange*0.9));
                }
                else if (USvalues.get(i) < minSensorRange)
                {
                    USvalues.set(i, 0);
                    USvaluesForDeletion.set(i, 0);
                }
                else if(i == 1 && modeBorgne)
                {
                    USvaluesForDeletion.set(i, 0);
                }
            }
		}
		catch(Exception e)
        {}
	}

	public void updateConfig()
	{
		try
		{
			sensorFrequency = Integer.parseInt(config.getProperty("capteurs_frequence"));
			
			//plus que cette distance (environ 50cm) on est beaucoup moins precis sur la position adverse (donc on ne l'ecrit pas !)
            // TODO expliquer le calcul de la distance
			maxSensorRange = Integer.parseInt(config.getProperty("largeur_robot"))
							 / Math.sin(Float.parseFloat(config.getProperty("angle_detection_capteur")));

			minSensorRange = Integer.parseInt(config.getProperty("portee_mini_capteurs"));

			sensorPositionAngle = Float.parseFloat(config.getProperty("angle_position_capteur"));

			detectionAngle=Float.parseFloat(config.getProperty("angle_detection_capteur"));
            symetry = config.getProperty("couleur").replaceAll(" ","").equals("violet");
			
			robotWidth = Integer.parseInt(config.getProperty("largeur_robot"));
			robotLenght = Integer.parseInt(config.getProperty("longueur_robot"));
            radius = Integer.parseInt(config.getProperty("rayon_robot_adverse"));

		}
		catch (ConfigPropertyNotFoundException e)
		{
    		log.debug("Revoir le code : impossible de trouver la propriété "+e.getPropertyNotFound());
        }
	}

	
	/**
	 *  On enleve les obstacles qu'on ne voit pas
	 */
	private void removeObstacle()
	{
	    for (ObstacleProximity obstacle : mTable.getObstacleManager().getMobileObstacles()){
	        if (obstacle.getOutDatedTime() > System.currentTimeMillis()){
	            mTable.getObstacleManager().removeObstacle(obstacle);
	            log.debug("Obstacle retiré ");
            }
        }
		// TODO enlever les obstacles qu'on devrait voir mais qu'on ne detecte plus

	}

    /**
     * Active/desactive le mode borgne
     * @param value oui/non
     */
    public static void modeBorgne(boolean value)
    {
        ThreadSensor.modeBorgne = value;
    }

    public static void noDelay()
    {
        ThreadSensor.delay = false;
    }

}
