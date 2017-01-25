package scripts;

import enums.Speed;
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
 * Script pour deposer les modules
 * Version 0 : ???
 * Version 1 : Pour la zone de côté droit
 * Version 2 : Pour la zone de côté gauche
 *
 * @author Rem
 */

public class DropModule extends AbstractScript{

    protected DropModule(HookFactory hookFactory, Config config, Log log){
        super(hookFactory, config, log);

        versions = new Integer[]{0,1};
    }

    @Override
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws UnableToMoveException, ExecuteException, SerialConnexionException, BlockedActuatorException {

        try
        {

            if(versionToExecute==0) {

                // Manoeuvre pour se caller contre le depose-module
                actualState.robot.turn(Math.PI);
                actualState.robot.moveLengthwise(-80, hooksToConsider, true);
            }
            else {

                actualState.robot.moveLengthwise(-100, hooksToConsider, true, true, Speed.SLOW_ALL);
            }

            for (int i = 0; i < 3; i++) {

                // Drop un module
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                // Manoeuvre degueu pour se décaler
                actualState.robot.moveLengthwise(60, hooksToConsider, false);

                // Bon discord tu vas geuler mais j'avais la flemme
                if (versionToExecute==0) {
                    actualState.robot.turn(Math.PI - Math.asin((double)120 / 150));
                }

                else{
                    actualState.robot.turn(Math.asin(120 / 150));
                }
                actualState.robot.moveLengthwise(150);

                if (versionToExecute==0) {
                    actualState.robot.turn(Math.PI);
                    actualState.robot.moveLengthwise(-80, hooksToConsider, true);
                }
                else{
                    actualState.robot.turn(0);
                }

                // Callage contre le depose-module
                actualState.robot.moveLengthwise(-220, hooksToConsider, true, false, Speed.SLOW_ALL);
            }

            // Monte le dernier module et le drop
            actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
            actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
            actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
            actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
            actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
            actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

            // Se décale de depose-module
            actualState.robot.moveLengthwise(100, hooksToConsider, false);

        }
        catch(Exception e) {
            finalize(actualState, e);
        }
    }
    @Override
    public int remainingScoreOfVersion(int version, GameState state) {
        return 0;
    }

    @Override
    public Circle entryPosition(int version, int ray, Vec2 robotPosition) throws BadVersionException {

        if(version==0){

            return new Circle(new Vec2(1170,760));
        }
        else if(version==1){

            return new Circle(new Vec2(-1170,760));
        }
        else {
            log.debug("mauvaise version de script");
            throw new BadVersionException();
        }
    }

    @Override
    public void finalize(GameState state, Exception e) throws UnableToMoveException
    {
        log.debug("Exception " + e + "dans DropModule : Lancement du Finalize !");
        state.robot.setBasicDetection(false);
    }

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}