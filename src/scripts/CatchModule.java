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
 * Version 0 : Pour la fusée de la zone de départ par la gauche
 * Version 1 : Pour la fusé de la zone de départ par la droite
 * Version 2 : Pour la fusé pret du cratere
 *
 * @author Rem
 */

public class CatchModule extends AbstractScript {


    protected CatchModule(HookFactory hookFactory, Config config, Log log) {
        super(hookFactory, config, log);

        versions = new Integer[]{0,1,2};
    }

    // TODO : Calibrer les waitForCompletions, notamment ceux qui ne représentent pas le temps réel de l'action (LIVRE_CALLE_G et peut-être BAISSE_ASC)

    @Override
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws UnableToMoveException, ExecuteException, SerialConnexionException, BlockedActuatorException {

        try {

            if (versionToExecute == 0) {

                // Se place dans la bonne direction
                actualState.robot.turn(0, hooksToConsider, false, false);

                // Avance pour arriver devant la fusé
                actualState.robot.moveLengthwise(250, hooksToConsider);

                // Déploie l'attrape-module et la calle-bisou
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);

            } else if (versionToExecute == 1) {

                // Se place dans la bonne direction
                actualState.robot.turn(Math.PI, hooksToConsider, false, false);

                // Avance pour arriver devant la fusé
                actualState.robot.moveLengthwise(250, hooksToConsider);

                // Déploie l'attrape-module et la calle-bisou
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, true);

            }
            else if (versionToExecute == 2) {

                // Fait une manoeuvre pour arriver à la bonne position sans risque de toucher un obstacle
                actualState.robot.turn(Math.PI / 2 - Math.acos(0.8), hooksToConsider, false, false);
                actualState.robot.moveLengthwise(250, hooksToConsider);
                actualState.robot.turn(Math.PI / 2, hooksToConsider, false, false);

                // Déploie l'attrape-module et la calle-bisou
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);

            }

            if (versionToExecute == 0 || versionToExecute == 2) {

                for (int i = 0; i < 4; i++) {

                    // Attrape le module
                    actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);

                    // Recule l'attrape module pour laisser passer le bras de la calle
                    actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);

                    // Calle le module dans l'ascenceur
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                    // Repli l'attrape-module
                    actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D,true);

                    // Repli les calles
                    actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                    actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);

                    if (i != 3) {

                        // Monte la plaque
                        actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);

                        // Baisse la plaque
                        actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);

                    }
                }
            }

            else {
                for (int i = 0; i < 4; i++) {

                    // Attrape le module
                    actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);

                    // recule l'attrape module pour laisser passer le bras de la calle
                    actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);

                    // Calle le module dans l'ascenceur
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                    actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);

                    // Repli l'attrape-module
                    actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G,true);

                    // Repli les calles
                    actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                    actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, true);

                    if (i != 3) {

                        // Monte la plaque
                        actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);

                        // Baisse la plaque
                        actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);

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

                return new Circle(new Vec2(100,226));
            }

            else if (version == 1){

                return new Circle(new Vec2(600,226));
            }

            else if (version == 2){

                return new Circle(new Vec2(1124,1150));
            }

            else{
                log.debug("erreur : mauvaise version de script");
                throw new BadVersionException();
            }
        }

        @Override
        public void finalize(GameState state, Exception e) throws UnableToMoveException
        {
            log.debug("Exception " + e + "dans CatchModule : Lancement du Finalize !");
            state.robot.setBasicDetection(false);
        }

        @Override
        public Integer[] getVersion(GameState stateToConsider) {
            return versions;
        }
    }