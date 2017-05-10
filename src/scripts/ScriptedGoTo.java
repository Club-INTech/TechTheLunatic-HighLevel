package scripts;

import enums.ActuatorOrder;
import enums.DirectionStrategy;
import enums.Speed;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Callback;
import hook.Hook;
import hook.methods.CatchModuleD;
import hook.methods.PriseModule;
import hook.methods.RepliAllActionneurs;
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

    private Vec2 point1MilieuTable = new Vec2(580,800);
    private Vec2 point2EntreeFinTable = new Vec2(850,1400);
    private Vec2 point3AttrapperModule1 = new Vec2(850,1760);
    private Vec2 point4arriveDevantCratereFond = new Vec2(610,1810);
    private int distanceCratereFondApresBoules = -130;
    private double angleCratereFondAvantDepotModule = Math.PI/4;
    private int distanceCratereFondAvantDepotModule = -121;
    private int distanceCratereFondApresDepotModule = 60;
    private Vec2 point5sortieCratereFond=new Vec2(1150,1150);
    private int distanceReculModule2=-110;

    private Vec2 pointAvantModule2 = new Vec2(1080, 760);
    private int distanceApresModule2=100;                                       //TODO: peut être voir comment réduire ça, il avance trop et tourne sur lui même

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

            //Initialisation des hooks pour permettre de replier les actionneurs pendant les déplacements
            //Hook prise module 1
            /*Hook PriseModule = hookFactory.newPositionHook(new Vec2(80, 1850), (float) Math.PI/2, 100, 10000);
            PriseModule.addCallback(new Callback(new PriseModule(), true, actualState));
            hooksToConsider.add(PriseModule);
            //Hook prise module fusée
            Hook PriseModulef = hookFactory.newPositionHook(new Vec2(320, 250), (float) Math.PI/2, 100, 10000);
            PriseModule.addCallback(new Callback(new PriseModule(), true, actualState));
            hooksToConsider.add(PriseModulef);
            //Hook repli du largue module
            Hook ReposLargueModule = hookFactory.newPositionHook(new Vec2(550, 1650), (float) -Math.PI/4, 100, 10000);
            ReposLargueModule.addCallback(new Callback(new ReposLargueModule(), true, actualState));
            hooksToConsider.add(ReposLargueModule);*/

            Hook replieTout = hookFactory.newPositionHook(new Vec2 (480, 350), (float) Math.PI/2, 100, 400);
            replieTout.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));
            Hook priseModuleDroit = hookFactory.newPositionHook(new Vec2 (780, 1200), (float) Math.PI/2, 100, 400);
            priseModuleDroit.addCallback(new Callback(new CatchModuleD(), true, actualState));

            hooksToConsider.add(replieTout);
            hooksToConsider.add(priseModuleDroit);


            ArrayList<Hook> emptyHook = new ArrayList<Hook>();
            if (versionToExecute==0)
            {

                //Choper un/plusieurs modules de la fusée

                actualState.robot.goTo(new Vec2(320, 250));
                actualState.robot.turn(Math.PI);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(120,emptyHook);

                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(-130,emptyHook);


                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);

                //actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(-100, emptyHook);
                //actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);


                //Aller au cratère du fond
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.goTo(point1MilieuTable, hooksToConsider);

                actualState.robot.goTo(point2EntreeFinTable, hooksToConsider);

                // actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                // actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                // actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);

                actualState.robot.goTo(point3AttrapperModule1);

                //prise du module du fond
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                //changement de vitesse pour ne pas pousser les balles
                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                actualState.robot.goTo(point4arriveDevantCratereFond);

                actualState.robot.setLocomotionSpeed(Speed.FAST_T_MEDIUM_R);

                //Prise des boules
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);

                //Livraison modules
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules, emptyHook);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule, emptyHook);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule, emptyHook);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.goTo(point5sortieCratereFond);

                actualState.robot.goTo(pointAvantModule2);

                //actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_BACK_MOTION);

                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, true);

                actualState.robot.turn(Math.PI);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculModule2, emptyHook);
                //actualState.robot.goTo(point7AttrapperModule2);

                //Prise de module 2
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);

                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, true);

                // Manoeuvre degueu pour se décaler
                actualState.robot.moveLengthwiseAndWaitIfNeeded(60, emptyHook);
                // Bon discord tu vas geuler mais j'avais la flemme
                actualState.robot.turn(Math.PI - Math.asin(110.0 / 150));
                actualState.robot.moveLengthwiseAndWaitIfNeeded(150, emptyHook);
                actualState.robot.turn(Math.PI);
                // Callage contre le depose-module
                actualState.robot.moveLengthwise(-200, hooksToConsider, true, false, Speed.SLOW_ALL);
                // Drop un module
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceApresModule2, emptyHook);
                actualState.robot.goTo(pointAvantDeposeBoules1);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1, emptyHook);

                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1, emptyHook);
                actualState.robot.goTo(pointDevantCratere2);
                actualState.robot.turn(-29*Math.PI/32);
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(120, emptyHook);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150, emptyHook);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(150, emptyHook);


                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);
            }

        }
        catch(Exception e)
        {
            log.critical("Robot ou actionneur bloqué dans ScriptedGoTo");
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
