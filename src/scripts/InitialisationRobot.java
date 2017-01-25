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
 * Created by rem on 25/01/17.
 * Initialise tout les actionneurs du robot
 */
public class InitialisationRobot extends AbstractScript {

    protected InitialisationRobot(HookFactory hookFactory, Config config, Log log){
        super(hookFactory, config, log);

        versions = new Integer[]{0};
    }

    @Override
    public void execute(int versionToExecute, GameState gameState, ArrayList<Hook> hookToConsider) throws UnableToMoveException, ExecuteException, SerialConnexionException, BlockedActuatorException {
        try
        {
            if (versionToExecute == 0){
                // Initialisation des actionneurs
                gameState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                gameState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
                gameState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                gameState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                gameState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, true);

                gameState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
                gameState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                gameState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                gameState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                gameState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);

                gameState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE,false);
                gameState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);
            }
        }
        catch (Exception e){
            finalize(gameState, e);
        }
    }

    @Override
    public int remainingScoreOfVersion(int version, GameState state) {
        return 0;
    }

    @Override
    public Circle entryPosition(int version, int ray, Vec2 robotPosition) throws BadVersionException {

        if(version==0){

            return new Circle(robotPosition);
        }
        else {
            log.debug("mauvaise version de script");
            throw new BadVersionException();
        }
    }

    @Override
    public void finalize(GameState state, Exception e) throws UnableToMoveException
    {
        log.debug("Exception " + e + "dans InitialisationRobot : Lancement du Finalize !");
        state.robot.setBasicDetection(false);
    }

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}