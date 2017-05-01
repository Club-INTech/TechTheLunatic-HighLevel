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



/** Script faisant tout un match sauf le début en scripté, pour ne pas appeler le pethfinding dans les zones compliquées
 *
 * @author gaelle
 */
public class FullScripted extends AbstractScript
{
    /**
     * Constructeur à appeller lorsqu'un script héritant de la classe AbstractScript est instancié.
     * Le constructeur se charge de renseigner la hookFactory, le système de config et de log.
     *
     * @param hookFactory La factory a utiliser pour générer les hooks dont pourra avoir besoin le script
     * @param config      le fichier de config a partir duquel le script pourra se configurer
     * @param log         le système de log qu'utilisera le script
     */
    protected FullScripted(HookFactory hookFactory, Config config, Log log)
    {
        super(hookFactory, config, log);

        versions = new Integer[]{0};
    }

    @Override
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws ExecuteException, UnableToMoveException, BlockedActuatorException
    {
        try{
            if (versionToExecute==0)
            {
                actualState.robot.turn(5*Math.PI/4);
                actualState.robot.moveLengthwise(-380);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwise(-570);

                //Attraper le module avec le côté droit

                // Manoeuvre pour arriver au niveau du module et être prêt à choper le module
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.moveLengthwise(-130);

                // Attraper le module
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                // Et remonte-le à l'aide de l'ascenceur
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR,false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);

                // Replie le tout
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);



                actualState.robot.moveLengthwise(190);
                //stateToConsider.robot.turn(Math.PI-0.55);
                // stateToConsider.robot.moveLengthwise(150);
                actualState.robot.turn(Math.PI-0.64);
                actualState.robot.moveLengthwise(260);





                // Prepare la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                // Déploie la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                // Fait tourner la pelleteuse (jusqu'à ~150 ou 200°)
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);

                // "Lèves les bras Maurice, c'est plus rigolo quand tu lèves les bras !", RIP King Julian
                actualState.robot.useActuator(ActuatorOrder.TIENT_BOULES,false);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

                actualState.robot.turn(Math.PI-0.40);

                actualState.robot.moveLengthwise(-60);
                actualState.robot.turn(Math.PI/4);
                actualState.robot.moveLengthwise(-60);
                // Drop un module
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR_LENT, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwise(60);
                actualState.robot.turn(-Math.PI/3);
                actualState.robot.moveLengthwise(250);


                //deuxième partie du match

                actualState.robot.turn(-Math.PI/2);

                actualState.robot.moveLengthwise(1000);



                actualState.robot.turn(Math.PI);

                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);

                actualState.robot.moveLengthwise(-100);
                // Attraper le module
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);

                // Et remonte-le à l'aide de l'ascenceur
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR,false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);

                // Replie le tout
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);


                //Drop le module
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwise(100);

                actualState.robot.turn(-Math.PI/2);

                actualState.robot.moveLengthwise(150);
                //abaisser les bras au plus bas
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                //rotation de la pelle jusqu'à la position de livraison
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);

                //éventuellement, attendre le temps que les boules tombent (en millisecondes)
                //actualstate.robot.sleep(1000);

                //lever les bras jusqu'à la position intermédiaire
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

                //Reculer un peu
                actualState.robot.moveLengthwise(-150);

                //tourner la pelle jusqu'à la position initiale
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                //monter les bras le plus haut \o/
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, true);

                actualState.robot.turn(Math.PI/2);


                // Calcule de l'angle pour se diriger vers le centre du robot
                Vec2 posCratere= new Vec2(850, 540);
                Vec2 posRobot=actualState.robot.getPosition();
                Vec2 vec = posCratere.minusNewVector(posRobot);

                // Manoeuvre pour se diriger vers le cratère
                //stateToConsider.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.turn(vec.getA());
                actualState.robot.moveLengthwise(160);

                // Prepare la pelleteuse avant déploiement(bras relevés mais légèrement abaissés pour ne pas bloquer la rotation de la pelle, puis pelle mise à 300°)
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                // Déploie la pelleteuse (descendre les bras, avec pelle toujours à 300 °)
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                // Fait tourner la pelleteuse (jusqu'à ~150 ou 200°)
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);

                // "Lèves les bras Maurice, c'est plus rigolo quand tu lèves les bras !", RIP King Julian
                actualState.robot.useActuator(ActuatorOrder.TIENT_BOULES,false);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);



            }

        }
        catch(Exception e)
        {
            log.critical("Robot ou actionneur bloqué dans DropBalls");
            finalize(actualState, e);
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
        if (version == 0) {
            return new Circle(robotPosition);
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
        log.debug("Exception " + e +"dans DropBalls : Lancement du finalize !");
        state.robot.setBasicDetection(false);
    }

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}

