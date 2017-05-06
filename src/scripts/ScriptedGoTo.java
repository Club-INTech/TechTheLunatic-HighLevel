package scripts;

import enums.ActuatorOrder;
import enums.DirectionStrategy;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Callback;
import hook.Hook;
import hook.methods.PriseModule;
import hook.methods.ReposLargueModule;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

import java.util.ArrayList;



/** Script sans pathfinding utilisant goTo(pointVisé)
 *
 * @author melanie
 */


public class ScriptedGoTo extends AbstractScript
{


    /** PointsVisés, dstances & angles du script, override par la config */

    private Vec2 point1MilieuTable = new Vec2(578,800);
    private Vec2 point2EntreeFinTable = new Vec2(850,1400);
    private Vec2 point3AttrapperModule1 = new Vec2(850,1760);
    private Vec2 point4arriveDevantCratereFond = new Vec2(625,1810);
    private int distanceCratereFondApresBoules = -130;
    private double angleCratereFondAvantDepotModule = Math.PI/4;
    private int distanceCratereFondAvantDepotModule = -120;
    private int distanceCratereFondApresDepotModule = 110;
    private Vec2 point5sortieCratereFond=new Vec2(1000,1200);

    private Vec2 point6SortieFinTable = new Vec2();
    private Vec2 point7AttrapperModule2 = new Vec2();
    private double angleAttrapperModule2 = 0;
    private Vec2 point8ReculerPourAttrapperModule2 = new Vec2();
    private Vec2 point9ReavancerApresModule2 = new Vec2();
    private Vec2 point10DevantCratere2 = new Vec2();
    private double angleCratere2 = 0;
    private Vec2 point11ReculerDuCratere = new Vec2();
    private Vec2 point12LarguerBalles = new Vec2();

    private boolean detect = false;



    /**
     * Constructeur à appeller lorsqu'un script héritant de la classe AbstractScript est instancié.
     * Le constructeur se charge de renseigner la hookFactory, le système de config et de log.
     *
     * @param hookFactory La factory a utiliser pour générer les hooks dont pourra avoir besoin le script
     * @param config      le fichier de config a partir duquel le script pourra se configurer
     * @param log         le système de log qu'utilisera le script
     */

    protected ScriptedGoTo(HookFactory hookFactory, Config config, Log log)
    {
        super(hookFactory, config, log);

        versions = new Integer[]{0};
    }


    @Override
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws ExecuteException, UnableToMoveException, BlockedActuatorException
    {
        updateConfig();

        try{

            if(detect) {
                actualState.robot.switchSensor();
            }

            if (versionToExecute==0)
            {

                actualState.robot.goTo(point1MilieuTable);

                actualState.robot.goTo(point2EntreeFinTable);

                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRInitialisationAPE_D, false); //se prépare à prendre le module
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);

                actualState.robot.goTo(point3AttrapperModule1);

                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                actualState.robot.goTo(point4arriveDevantCratereFond);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);

                actualState.robot.moveLengthwise(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.moveLengthwise(distanceCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);
                actualState.robot.moveLengthwise(distanceCratereFondApresDepotModule);
                actualState.robot.goTo(point5sortieCratereFond);

                /*actualState.robot.goTo(point6SortieFinTable);
                actualState.robot.goTo(point7AttrapperModule2);
                actualState.robot.turn(angleAttrapperModule2);
                actualState.robot.goTo(point8ReculerPourAttrapperModule2);
                actualState.robot.goTo(point9ReavancerApresModule2);
                actualState.robot.goTo(point10DevantCratere2);
                actualState.robot.turn(angleCratere2);
                actualState.robot.goTo(point11ReculerDuCratere);
                actualState.robot.goTo(point12LarguerBalles);*/



                //Initialisation des hooks pour permettre de replier les actionneurs pendant les déplacements
                //Hook prise module 1
                Hook PriseModule = hookFactory.newPositionHook(new Vec2(80, 1850), (float) Math.PI/2, 100, 10000);
                PriseModule.addCallback(new Callback(new PriseModule(), true, actualState));
                hooksToConsider.add(PriseModule);
                //Hook repli du largue module
                Hook ReposLargueModule = hookFactory.newPositionHook(new Vec2(550, 1650), (float) -Math.PI/4, 100, 10000);
                ReposLargueModule.addCallback(new Callback(new ReposLargueModule(), true, actualState));
                hooksToConsider.add(ReposLargueModule);

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
    public void updateConfig()
    {
        try{

            detect = Boolean.parseBoolean(config.getProperty("capteurs_on"));

        } catch (ConfigPropertyNotFoundException e){
            log.debug("Revoir le code : impossible de trouver la propriété " + e.getPropertyNotFound());
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
