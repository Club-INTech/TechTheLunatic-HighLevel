package scripts;

import enums.ActuatorOrder;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.Hook;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

import java.util.ArrayList;

/**
 * Script pour prendre les modules et les stocker
 * Version 1 : Pour la fusé de la zone de départ
 * Version 2 : Pour la fusé pret du cratere
 *
 * @author Rem
 */

public class CatchModule extends AbstractScript {


    protected CatchModule(HookFactory hookFactory, Config config, Log log) {
        super(hookFactory, config, log);

        versions = new Integer[]{0};
    }

    // TODO : Ajouter des waitForCompletions pour les actions et prendre en compte la fusée considérée

    @Override
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws UnableToMoveException, ExecuteException, SerialConnexionException, BlockedActuatorException {

        try
        {

            if(versionToExecute == 0){

                // Se place dans la bonne direction
                actualState.robot.turn(0, hooksToConsider,false,false);

                // Avance pour arriver devant la fusé
                actualState.robot.moveLengthwise(350, hooksToConsider);

                // Déploie l'attrape-module
                actualState.robot.useActuator(ActuatorOrder.PRET_ATTRAPE_M0, true);

            }

            else if(versionToExecute == 1){

                // Fait une manoeuvre pour arriver à la bonne position sans risque de toucher un obstacle
                actualState.robot.turn(Math.PI/2-Math.acos(0.8), hooksToConsider,true,false);
                actualState.robot.moveLengthwise(250, hooksToConsider);
                actualState.robot.turn(Math.PI/2, hooksToConsider,false,false);

            }

            for (int i=0; i<4; i++) {

                // Attrape le module
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_M, true);

                // Va en position intermédiaire pour laisser passer le bras de la calle
                actualState.robot.useActuator(ActuatorOrder.INTER_ATTRAPE_M, true);

                // Calle le module dans le Stockage vertical
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_ATTRAPE_M1, true);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE, false);

                if (i != 3) {

                    // Monte la plaque
                    actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);

                    // Baisse la plaque
                    actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, false);

                }
            }
        }
        catch (Exception e)
        {
            finalize(actualState,e);
        }
    }

    @Override
    public int remainingScoreOfVersion(int version, GameState state) {
        return 0;
    }

    @Override
    public Circle entryPosition(int version, int ray, Vec2 robotPosition) throws BadVersionException {

        if (version == 0){

            return new Circle(new Vec2(0,1774));
        }

        else if (version == 1){

            return new Circle(new Vec2(-1124,850));
        }

        else{
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
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}
