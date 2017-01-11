

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

package scripts;

import enums.ActuatorOrder;
import enums.Speed;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Hook;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

import java.util.ArrayList;

//TODO: cf le todo du haut de DropBalls

/**
 * Script pour ramasser les balles dans un cratère avec la pelleteuse
 *
 * Version 0: robot déjà placé, il ramasse juste les balles (pour tests sans base roulante)
 * Version 1: ramassage dans un cratère proche de la zone de départ(l'autre petit cratère est plus teshenique)
 *
 * @author Gaelle, tic-tac
 */

public class CatchBalls extends AbstractScript {


    public CatchBalls(HookFactory hookFactory, Config config, Log log)
    {
        super(hookFactory, config, log);

        versions = new Integer[]{0,1};
    }


    /**
     * On lance le script choisi.
     * @param versionToExecute Version a lancer
     * @param stateToConsider Notre bon vieux robot
     * @param hooksToConsider Les hooks nécessaires pour l'execution du script
     *
     */

    // TODO : prendre en compte le cratère considéré (4 différents sur la table/ 2 en considérant la symétrie)

    @Override
    public void execute(int versionToExecute, GameState stateToConsider, ArrayList<Hook> hooksToConsider) throws ExecuteException, UnableToMoveException, BlockedActuatorException
    {
        try
        {
            if(versionToExecute == 0) {

                //Preparer la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                stateToConsider.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                //déployer la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                stateToConsider.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                //faire tourner la pelleteuse (jusqu'à ~150 ou 200°) => prend les boules

                //TODO:changer les waitForCompletion de tient pelle et autres
                stateToConsider.robot.useActuator(ActuatorOrder.PREND_PELLE, true);

            }

            if(versionToExecute ==1) {

                //s'orienter face au cratère
                stateToConsider.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                Vec2 posCratere= new Vec2(850,540);   //Centre du cratère
                Vec2 posRobot=stateToConsider.robot.getPosition();           //Position du robot
                Vec2 vec = new Vec2(posCratere.x-posRobot.x, posCratere.y-posRobot.y);
                double angle= vec.angle();  //angle du vecteur (robot,centre_cratère) par rapp axe Ox
                stateToConsider.robot.turn(angle); //rotation vers le cratère
                stateToConsider.robot.moveLengthwise(120);

                    //TODO : le robot devrait avancer un peu pour se caler sur la zone de départ, pour prendre en compte sa taille dans le déplacement le menant là

                //Preparer la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                stateToConsider.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                stateToConsider.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                //déployer la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                stateToConsider.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                //faire tourner la pelleteuse (jusqu'à ~150 ou 200°)
                stateToConsider.robot.useActuator(ActuatorOrder.PREND_PELLE, true);

                //"lèves les bras Maurice, c'est plus rigolo quand tu lèves les bras", RIP King Julian
                stateToConsider.robot.useActuator(ActuatorOrder.TIENT_BOULES,false);

                // reculer
                stateToConsider.robot.moveLengthwise(-150);

            }
        }
        catch(Exception e)
        {
            finalize(stateToConsider,e);
        }
    }

    @Override
    public int remainingScoreOfVersion(int version, GameState state)
    {
        int score = 0;
        return score;
    }

    @Override
    public Circle entryPosition(int version, int ray, Vec2 robotPosition) throws BadVersionException
    {
        if (version == 0 )
        {
            return new Circle(robotPosition);
        }
        else if (version == 1) {
            return new Circle(new Vec2(850,540), 320); //Pour avoir de la marge j'ai mit 85(rayon du cratère)+150(~demi largeur du robot?)
        }
        else
        {
            log.debug("erreur : mauvaise version de script");
            throw new BadVersionException();
        }
    }

    @Override
    public void finalize(GameState state, Exception e) throws UnableToMoveException
    {
        log.debug("Exception " + e + "dans CatchBalls : Lancement du Finalize !");
        state.robot.setBasicDetection(false);
    }

    @Override
    public Integer[] getVersion(GameState stateToConsider)
    {
        return versions;
    }

}