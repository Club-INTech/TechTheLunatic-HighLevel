

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

/**
 * Script pour ramasser les balles dans un cratère avec la pelleteuse
 * Version 0: robot déjà placé, il ramasse juste les balles
 * Version 1: ramassage dans le cratère juste devant la zone de départ, avec pathfinding
 *
 * @author Gaelle, tic-tac
 */

public class CatchBalls extends AbstractScript {


    public CatchBalls(HookFactory hookFactory, Config config, Log log) {
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

    // TODO : prendre en compte le cratère considéré (4 différents sur la table)

    @Override
    public void execute(int versionToExecute, GameState stateToConsider, ArrayList<Hook> hooksToConsider) throws ExecuteException, UnableToMoveException
    {


        try
        {

            if(versionToExecute == 0) {



                //Preparer la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                stateToConsider.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                stateToConsider.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                //déployer la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                stateToConsider.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, false);

                //faire tourner la pelleteuse (jusqu'à ~150 ou 200°)
                stateToConsider.robot.useActuator(ActuatorOrder.TIENT_PELLE, false);

            }

            if(versionToExecute ==1) {

                //aller jusqu'au cratère considéré (740,540)

                //TODO



                //s'orienter face au cratère
                stateToConsider.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                stateToConsider.robot.turn(0);

                //Preparer la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                stateToConsider.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                stateToConsider.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                //déployer la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                stateToConsider.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, false);

                //faire tourner la pelleteuse (jusqu'à ~150 ou 200°)
                stateToConsider.robot.useActuator(ActuatorOrder.TIENT_PELLE, false);

                // reculer
                stateToConsider.robot.moveLengthwise(-100);



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
        if (version == 0 || version == 1 )
        {
            // modification possible selon l'envergure du robot new Vec2(1135,1600)
            return new Circle(robotPosition);
        }

        else
        {
            log.debug("erreur : mauvaise version de script");
            throw new BadVersionException();
        }
    }

    @Override
    public void finalize(GameState state, Exception e)
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