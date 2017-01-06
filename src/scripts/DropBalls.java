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

//TODO: en supposant que le robot est placé à un point près de la zone de livraison(à quelques centimètres, donc doit tourner et avancer un peu)

/** Script permettant de livrer les boules déjà prises, dans la zone de départ ou sur le robot secondaire
 *
 * Version 0: suppose que le robot est déjà placé, sans la base roulante
 * Version 1: Va vers la zone de départ, face au robot secondaire, livre les boules.
 *
 * @author tic-tac
 */
public class DropBalls extends AbstractScript
{
    /**
     * Constructeur à appeller lorsqu'un script héritant de la classe AbstractScript est instancié.
     * Le constructeur se charge de renseigner la hookFactory, le système de config et de log.
     *
     * @param hookFactory La factory a utiliser pour générer les hooks dont pourra avoir besoin le script
     * @param config      le fichier de config a partir duquel le script pourra se configurer
     * @param log         le système de log qu'utilisera le script
     */
    protected DropBalls(HookFactory hookFactory, Config config, Log log)
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
                //abaisser les bras au plus bas
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);

                //rotation de la pelle jusqu'à la position de livraison
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);

                //éventuellement, attendre le temps que les boules tombent (en millisecondes) en fonction des tests
                     //actualstate.robot.sleep(1000); TODO:plutot utiliser le waitforcompletion

                //lever les bras jusqu'à la position intermédiaire
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

                //tourner la pelle jusqu'à la position initiale
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);

                //monter les bras le plus haut \o/
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, true);
            }
            else if (versionToExecute==1)
            {
                //Se déplacer vers la zone de départ (considérer les 2 zones possibles?), face au robot secondaire.
                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                //TODO:Quelle position pour le robot secondaire?

                //orientation
                actualState.robot.turn(-Math.PI/2);

                //Se caler contre la zone de livraison
                //TODO:mesures par rapport à envergure du robot pour déterminer la position d'entrée et distance à avancer

                //abaisser les bras au plus bas
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, false);

                //rotation de la pelle jusqu'à la position de livraison
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, false);

                //éventuellement, attendre le temps que les boules tombent (en millisecondes)
                //actualstate.robot.sleep(1000);

                //lever les bras jusqu'à la position intermédiaire
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

                //Reculer un peu
                actualState.robot.moveLengthwise(-150);  //TODO: distance ? la longueur des bras avec pelle dépliée?

                //tourner la pelle jusqu'à la position initiale
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                //monter les bras le plus haut \o/
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);

            }
        }
        catch(Exception e)
        {
            log.critical("Robot ou actionneur bloqué dans DropBalls"); //TODO: faire exceptions pour mouvements ET actionneurs séparémment
            finalize(actualState, e);
        }
    }

    @Override
    public int remainingScoreOfVersion(int version, GameState state)
    {

        //TODO: si on considère que l'action est complètement successfull, on a 5 boules au moins(pour une version
        //TODO:utilisée dans un petit cratère, donc 5*3=15 points obtenus par cette action

        int score = 0;
        return score;
    }

    @Override
    public Circle entryPosition(int version, int ray, Vec2 robotPosition) throws BadVersionException
    {
        if (version == 0 || version == 1) //Le robot va aller livrer depuis la position de départ du robot, où qu'il soit
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
