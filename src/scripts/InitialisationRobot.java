package scripts;

import enums.ActuatorOrder;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.Callback;
import hook.Hook;
import hook.methods.CatchModuleD;
import hook.methods.CatchModuleG;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import table.Table;
import utils.Config;
import utils.Log;

import java.util.ArrayList;

/**
 * Created by rem on 25/01/17.
 * Initialise tout les actionneurs du robot
 * Version 0: Ne sort pas de la zone de départ et se contente d'initialiser les actionneurs
 * Version 1: Sort de la zone de départ par l'avant en prenant le module multicolor et en le mettant dans la zone de départ
 * Version 2: Sort de la zone de départ par l'arrière en prenant le module multicolor et en le mettant dans la zone de départ
 * Version 3: Initialise les actionneurs sans replier les attrapes-modules (version pour les tests des fusées)
 */
public class InitialisationRobot extends AbstractScript {

    protected InitialisationRobot(HookFactory hookFactory, Config config, Log log){
        super(hookFactory, config, log);

        versions = new Integer[]{0,1,2,3};
    }

    @Override
    public void execute(int versionToExecute, GameState gameState, ArrayList<Hook> hookToConsider) throws UnableToMoveException, ExecuteException, SerialConnexionException, BlockedActuatorException {
        try
        {
            Hook catchMD = hookFactory.newPositionHook(Table.entryPosition.plusNewVector(new Vec2(440, 2.319)), (float)(2.319), 30, 200);
            catchMD.addCallback(new Callback(new CatchModuleD()));
            Hook catchMG = hookFactory.newPositionHook(new Vec2(480, 320), (float)(-Math.PI + 2.41), 8, 50);
            catchMG.addCallback(new Callback(new CatchModuleG()));
            hookToConsider.add(catchMD);
            hookToConsider.add(catchMG);

            // Initialisation des actionneurs
            gameState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
            gameState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
            gameState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
            gameState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
            gameState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, true);

            gameState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
            gameState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
            gameState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);

            if (versionToExecute == 3) {
                gameState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                gameState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
            }
            else {
                gameState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                gameState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
            }

            gameState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
            gameState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

            // Se dégage de la zone de départ

            if (versionToExecute == 1) {

                // Avec le Hook pour prendre le module multicolore pret de la zone de départ
                gameState.robot.turn(2.319);   // 250, 580 <- 578, 208
                gameState.robot.moveLengthwise(496);
                gameState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);
                gameState.robot.moveLengthwise(-180, hookToConsider);
                gameState.robot.turn(3 * Math.PI / 8);
                gameState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);

                //départ à l'endroit (pelleteuse vers PI)
                // gameState.robot.turn(13 * Math.PI / 16);
                // gameState.robot.moveLengthwise(130);

                //départ à l'envers (pelleteuse vers 0)
            } else if (versionToExecute == 2) {
                gameState.robot.turn(-3 * Math.PI / 16);
                gameState.robot.moveLengthwise(-100, hookToConsider);

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

        if(version==0 || version==1 || version==2){

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