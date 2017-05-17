package scripts;

import enums.ActuatorOrder;
import enums.ScriptNames;
import enums.Speed;
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

/**
 * Created by melanie on 09/05/17.
 */





public class ScriptedGoTo_LivraisonModuleFond  extends AbstractScript{


    /** PointsVisés, dstances & angles du script, override par la config */

    private Vec2 point1MilieuTable = new Vec2(580,800);
    private Vec2 point2EntreeFinTable = new Vec2(850,1400);
    private Vec2 point3AttrapperModule1 = new Vec2(850,1760);
    private Vec2 point4arriveDevantCratereFond = new Vec2(610,1810);
    private int distanceCratereFondApresBoules = -130;
    private double angleCratereFondAvantDepotModule = Math.PI/4;
    private int distanceCratereFondAvantDepotModule = -121;
    private int distanceCratereFondApresDepotModule = 110;
    private Vec2 point5sortieCratereFond=new Vec2(1150,1150);
    private int distanceReculModule2=-110;

    private Vec2 pointAvantModule2 = new Vec2(1080, 760);
    private int distanceApresModule2=150;                                       //TODO: peut être voir comment réduire ça, il avance trop et tourne sur lui même

    private Vec2 pointAvantDeposeBoules1 = new Vec2(1150, 790);
    private int distanceAvantDeposeBoules1=240;
    private int distanceReculApresDepotBoule1=-200;

    private Vec2 pointDevantCratere2 = new Vec2(1100, 650);

    private boolean detect = false;



    /**
     * Constructeur à appeller lorsqu'un script héritant de la classe AbstractScript est instancié.
     * Le constructeur se charge de renseigner la hookFactory, le système de config et de log.
     *
     * @param hookFactory La factory a utiliser pour générer les hooks dont pourra avoir besoin le script
     * @param config      le fichier de config a partir duquel le script pourra se configurer
     * @param log         le système de log qu'utilisera le script
     */

    protected ScriptedGoTo_LivraisonModuleFond(HookFactory hookFactory, Config config, Log log)
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
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND,true);
                //Livraison modules
                actualState.robot.moveLengthwise(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwise(distanceCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR_LENT, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);
                actualState.robot.moveLengthwise(distanceCratereFondApresDepotModule);
                actualState.robot.goTo(point5sortieCratereFond);

                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);



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
        catch(UnableToMoveException e)
        {
            log.critical("Robot ou actionneur bloqué dans DropBalls");
            finalize(actualState, e);
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
    public void finalize(GameState state, UnableToMoveException e) throws UnableToMoveException
    {
        log.debug("Exception " + e +"dans DropBalls : Lancement du finalize !");
        state.robot.setBasicDetection(false);
        throw e;
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
