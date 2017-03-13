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

package graphics;

import enums.ActuatorOrder;
import enums.TurningStrategy;
import exceptions.serial.SerialConnexionException;
import robot.Robot;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Gestionnaire des actions clavier pour l'interface graphique, ajoutez vos actions aux blocks correspondants
 * @author etienne, discord, florian
 */
public class Keyboard implements KeyListener {
	private Robot mRobot;
	private TurningStrategy turningStr = TurningStrategy.FASTEST;
	private boolean modeActual = false;
	
	public Keyboard(Robot robot)
	{
		mRobot= robot;
	}

	int isUpPressed = 0;
	int isDownPressed = 0;
	int isLeftPressed = 0;
	int isRightPressed = 0;

	boolean isUpPressedb;
	boolean isDownPressedb;
	boolean isLeftPressedb;
	boolean isRightPressedb;
	boolean isPpressed;
	boolean isLpressed;
	boolean isDpressed;
	int lastEvent;

	void doThat() throws SerialConnexionException {
		if(/*isUpPressed < 15 &&*/ isUpPressedb)
		{
			try
			{
				mRobot.useActuator(ActuatorOrder.MOVE_FORWARD, false);
			}
			catch(Exception exception)
			{
				System.out.println("ça marche pas bien trololo");
			}
		}
		else if(/*isDownPressed < 15 &&*/ isDownPressedb)
		{
			try
			{
				mRobot.useActuator(ActuatorOrder.MOVE_BACKWARD, false);
			}
			catch(Exception exception)
			{
				System.out.println("ça marche pas bien trololo");
			}
		}
		else if(/*isLeftPressed < 15 &&*/ isLeftPressedb)
		{
			try
			{
				mRobot.useActuator(ActuatorOrder.TURN_LEFT, false);
			}
			catch(Exception exception)
			{
				System.out.println("ça marche pas bien trololo");
			}
		}
		else if( /*isRightPressed < 15 && */isRightPressedb)
		{
			try
			{
				mRobot.useActuator(ActuatorOrder.TURN_RIGHT, false);
			}
			catch(Exception exception)
			{
				System.out.println("ça marche pas bien trololo");
			}
		}
		else if (isPpressed) {
			try {
				// Déploie la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
				mRobot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

				// Fait tourner la pelleteuse (jusqu'à ~150 ou 200°)
				mRobot.useActuator(ActuatorOrder.PREND_PELLE, true);

				// "Lèves les bras Maurice, c'est plus rigolo quand tu lèves les bras !", RIP King Julian
				mRobot.useActuator(ActuatorOrder.TIENT_BOULES, false);
				mRobot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
			} catch (Exception exception) {
				System.out.println("laule");
			}
		}else if (isLpressed) {
			try {

				mRobot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
				mRobot.useActuator(ActuatorOrder.PRET_PELLE, true);
			} catch (Exception exception) {
				System.out.println("laule");
			}
		}else if (isDpressed){
			try{
				//abaisser les bras au plus bas
				mRobot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

				//rotation de la pelle jusqu'à la position de livraison
				mRobot.useActuator(ActuatorOrder.LIVRE_PELLE, true);

				//lever les bras jusqu'à la position intermédiaire
				mRobot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

				//tourner la pelle jusqu'à la position initiale
				mRobot.useActuator(ActuatorOrder.PRET_PELLE, true);

				//monter les bras le plus haut \o/
				mRobot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, true);
			}catch(Exception exception){

			}
		}else{
			release();
		}

	}


	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() != lastEvent) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					isUpPressed++;
					isUpPressedb = true;
					lastEvent = e.getKeyCode();
					break;
				case KeyEvent.VK_DOWN:
					isDownPressed++;
					isDownPressedb = true;
					lastEvent = e.getKeyCode();
					break;
				case KeyEvent.VK_LEFT:
					isLeftPressed++;
					isLeftPressedb = true;
					lastEvent = e.getKeyCode();
					break;
				case KeyEvent.VK_RIGHT:
					isRightPressed++;
					isRightPressedb = true;
					lastEvent = e.getKeyCode();
					break;
				case KeyEvent.VK_P:
					isPpressed = true;
					break;
				case KeyEvent.VK_L:
					isLpressed = true;
					break;
				case KeyEvent.VK_D:
					isDpressed = true;
					break;
			}
			try {
				doThat();
			} catch (SerialConnexionException e1) {
				e1.printStackTrace();
			}
		}
	}


	void release(){
		try
		{
			mRobot.useActuator(ActuatorOrder.SSTOP,false);
		}catch(Exception exception)
		{
			System.out.println("ça marche pas bien trololo");
		}
	}
	@Override
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP:
				lastEvent = 0;isUpPressedb = false;break;
			case KeyEvent.VK_DOWN:
				lastEvent = 0;isDownPressedb = false;break;
			case KeyEvent.VK_LEFT:
				lastEvent = 0;isLeftPressedb = false;break;
			case KeyEvent.VK_RIGHT:
				lastEvent = 0;isRightPressedb = false;break;
			case KeyEvent.VK_P:
				isPpressed = false; break;
			case KeyEvent.VK_L:
				isLpressed = false; break;
			case KeyEvent.VK_D:
				isDpressed = false; break;
		}
		release();
	}
	public boolean isModeActual()
    {
        return modeActual;
    }

    public void resetModeActual()
    {
        modeActual = false;
    }


    public TurningStrategy getTurningStrategy()
    {
        return turningStr;
    }
    
	@Override
	public void keyTyped(KeyEvent e)
	{
	}

}
