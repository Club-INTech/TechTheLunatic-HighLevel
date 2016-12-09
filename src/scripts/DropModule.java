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
 * Script pour deposer les modules
 * Version 1 : Pour la zone de côté
 * Version 2 : Pour la zone centrale
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

            if(versionToExecute==0){

                // Manoeuvre pour se caller contre le depose-module
                actualState.robot.turn(-Math.PI, hooksToConsider, false, false);
                actualState.robot.moveLengthwise(-80, hooksToConsider, false);

                for(int i=0; i<3; i++){

                    // Drop un module
                    actualState.robot.useActuator(ActuatorOrder.POUSSE_MODULE, true);
                    actualState.robot.useActuator(ActuatorOrder.REPOS_MODULE, false);

                    // Manoeuvre degeu pour se décaler
                    actualState.robot.moveLengthwise(80, hooksToConsider, false, false);
                    actualState.robot.turn(-Math.PI + Math.asin(150 / 190), hooksToConsider, false, false);
                    actualState.robot.moveLengthwise(190, hooksToConsider, false, false);
                    actualState.robot.turn(-Math.PI, hooksToConsider, false,false);
                    actualState.robot.moveLengthwise(-197, hooksToConsider,false, false);
                }

                // Monte le dernier module et le drop
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_MODULE, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_MODULE, false);

            }
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

            return new Circle(new Vec2(1170,1080));
        }
        else {
            return new Circle(new Vec2(1170, 1080));
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