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

                // Se placer devant une fusée dans la bonne position
                actualState.robot.turn(Math.PI, hooksToConsider,true,false);

                for (int i=0; i<4; i++){

                    // Attrape le module
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_ATTRAPE_M, false);

                    // Va en position intermédiaire pour laisser passer le bras de la calle
                    actualState.robot.useActuator(ActuatorOrder.RETOUR_ATTRAPE_M, false);

                    // Calle le module dans le Stockage vertical
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE, false);
                    actualState.robot.useActuator(ActuatorOrder.PRET_ATTRAPE_M,false);
                    actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE, false);

                    if (i!=3) {

                        // Monte la plaque

                        // Baisse la plaque

                    }

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

            return new Circle(new Vec2(-350,1780));
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
