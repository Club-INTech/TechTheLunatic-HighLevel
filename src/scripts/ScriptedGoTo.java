package scripts;

import enums.*;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.Callback;
import hook.Hook;
import hook.methods.PrepareToCatchModD;
import hook.methods.RepliAllActionneurs;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

import java.util.ArrayList;



/** Script sans pathfinding utilisant goTo(pointVisé)
 * version 0 : prend le module dans a fuée au début
 * version 1 : prend le module multicolore devant la zone de départ à la place de la fusée
 * version 2 : meme chose que la version 1 mais avec 2 manoeuvres de recalages
 * @author melanie
 */


public class ScriptedGoTo extends AbstractScript
{

    /** PointsVisés, dstances & angles du script, override par la config */

    private Vec2 point1MilieuTable = new Vec2(620,800);
    private Vec2 point2EntreeFinTable = new Vec2(888,1400);
    private Vec2 point3AttrapperModule1 = new Vec2(875,1760);
    private Vec2 point4arriveDevantCratereFond = new Vec2(710,1805);
    private double angleDevantCratereFond = Math.PI - 0.32;
    private int distanceCratereFondApresBoules = -170;

    private double angleCratereFondAvantDepotModule = Math.PI/4;
    private int distanceCratereFondAvantDepotModule = -150;
    private int distanceCratereFondApresDepotModule = 55;

    private Vec2 pointSortieCratereFond =new Vec2(1150,1210);
    private Vec2 pointIntermediaireVersModule =new Vec2(1150,950);
    private Vec2 pointAvantModule2 = new Vec2(1030, 710);
    private int distanceReculModule2=-150;
    private int distanceApresModule2=60;

    private Vec2 pointAvantDeposeBoules1 = new Vec2(1150, 790);
    private int distanceAvantDeposeBoules1=180;
    private int distanceReculApresDepotBoule1=-200;
    private Vec2 pointDevantCratere2 = new Vec2(1100, 680);



    Vec2 posCratere1= new Vec2(420, 1880);
    Vec2 posCratere2= new Vec2(850, 540);

    private boolean detect = false;
    private double recalageThresholdOrientation;

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
    public void execute(int versionToExecute, GameState actualState, ArrayList<Hook> hooksToConsider) throws ExecuteException, UnableToMoveException, BlockedActuatorException,SerialConnexionException
    {
        updateConfig();
        try{

            //Initialisation des hooks pour permettre de replier les actionneurs pendant les déplacements
            Hook replieTout0 = hookFactory.newPositionHook(new Vec2 (480, 600), (float) Math.PI/2, 100, 400);
            replieTout0.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));
            Hook priseModuleDroit = hookFactory.newPositionHook(new Vec2(890,1500), (float) -Math.PI/2, 300, 400);
            priseModuleDroit.addCallback(new Callback(new PrepareToCatchModD(), true, actualState));
            Hook repliTout1 = hookFactory.newPositionHook(point3AttrapperModule1, (float) -Math.PI/2, 50, 100);
            repliTout1.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));

            hooksToConsider.add(replieTout0);
            hooksToConsider.add(priseModuleDroit);
            hooksToConsider.add(repliTout1);

            if (versionToExecute==0)
            {

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                // Avec le Hook pour prendre le module multicolore pret de la zone de départ
                actualState.robot.moveLengthwiseAndWaitIfNeeded(80);
                actualState.robot.turn(2*Math.PI/3);   // 250, 580 <- 578, 208
                actualState.robot.moveLengthwise(560);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);

                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(-547, hooksToConsider);
                actualState.robot.turn(Math.PI/2);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.moveLengthwise(250);

                actualState.table.cylindreDevantDepart.isStillThere=false;

                //Aller au cratère du fond
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_MODULEFOND,true);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.goTo(point1MilieuTable, hooksToConsider);
                actualState.robot.goTo(point2EntreeFinTable, hooksToConsider);

                actualState.robot.turn(-Math.PI/2);

                actualState.robot.goTo(point3AttrapperModule1);

                //prise du module du fond

                actualState.robot.prendModule(Side.RIGHT);

                //La on devrait avoir le module
                actualState.table.cylindreCratereBase.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                //changement de vitesse pour ne pas pousser les balles
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND,true);
                // Changement de vitesse pour ne pas pousser les balles

                actualState.robot.moveLengthwiseAndWaitIfNeeded(178, hooksToConsider);
                // ActualState.robot.moveLengthwiseAndWaitIfNeeded(105, hooksToConsider);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                actualState.robot.goTo(point4arriveDevantCratereFond);
                actualState.robot.turn(angleDevantCratereFond);

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                // Prise des boules
                actualState.robot.prendBoules();

                // Livraison module
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);

                actualState.robot.goTo(pointSortieCratereFond, hooksToConsider);
                actualState.robot.goTo(pointAvantModule2);

                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                actualState.robot.turn(Math.PI-0.1);

                //Prise de module 2

                actualState.robot.prendModule(Side.LEFT);


                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceApresModule2);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1);

                //Livraison boules

                actualState.robot.livreBoules();


                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1);
                actualState.robot.goTo(pointDevantCratere2);

                Vec2 posCratere= new Vec2(850, 540);
                Vec2 posRobot=actualState.robot.getPosition();
                Vec2 vec = posCratere.minusNewVector(posRobot);

                actualState.robot.turn(vec.getA());
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(140);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);

                actualState.robot.prendBoules();

                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(165);

                actualState.robot.livreBoules();

                actualState.robot.moveLengthwise(-120); //Recul pour laisser le robot secondaire finir
            }

            if(versionToExecute == 1) {

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);

                // Avec le Hook pour prendre le module multicolore pret de la zone de départ
                actualState.robot.moveLengthwiseAndWaitIfNeeded(85);
                actualState.robot.turn(2 * Math.PI / 3 + 0.1);   // 250, 580 <- 578, 208
                actualState.robot.moveLengthwise(550);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);

                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(-547, hooksToConsider);
                actualState.robot.turn(Math.PI / 2);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.moveLengthwise(250);
                log.debug("position"+actualState.robot.getPosition());
                log.debug("positionFast"+actualState.robot.getPositionFast());

                log.debug("Orientation du HL :" + actualState.robot.getOrientationFast());

                //Aller au cratère du fond
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.goTo(point1MilieuTable, hooksToConsider);

                actualState.robot.goTo(point2EntreeFinTable);
                actualState.robot.turn(-Math.PI/2);

                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);

                actualState.robot.goTo(point3AttrapperModule1);

                //prise du module du fond

                actualState.robot.prendModule(Side.RIGHT);

                // Recalage

                actualState.robot.setLocomotionSpeed(Speed.SLOW_T_MEDIUM_R);
                actualState.robot.moveLengthwise(-300, new ArrayList<Hook>(), true);
                Vec2 oldPos = actualState.robot.getPosition();
                Vec2 newPos = oldPos.clone();
                newPos.setY(1835);
                actualState.robot.setPosition(newPos);

                log.debug("Orientation :" + actualState.robot.getOrientationFast());
                if (Math.abs(actualState.robot.getOrientationFast() + Math.PI/2)%(2*Math.PI) < recalageThresholdOrientation) {
                    log.debug("Recalage en orientation :" + Math.abs(actualState.robot.getOrientationFast() + Math.PI/2)%(2*Math.PI));
                    actualState.robot.setOrientation(-Math.PI / 2);
                }

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(180, hooksToConsider);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                //changement de vitesse pour ne pas pousser les balles
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                actualState.robot.goTo(point4arriveDevantCratereFond);
                actualState.robot.turnTo(posCratere1);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(70);

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

//Prise des boules
                actualState.robot.prendBoules();

                // la on a des boulasses
                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereBaseLunaire.isStillThere=false;

//Livraison module
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISON_MODULEFOND,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);

                //La on l'a largué
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);


                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);

                actualState.robot.goTo(pointSortieCratereFond);
                actualState.robot.goTo(pointIntermediaireVersModule);

                actualState.robot.goTo(pointAvantModule2);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_BACK_MOTION);

                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                actualState.robot.turn(Math.PI);

                //Là on commence le script livraison 1 (à vérifier)
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_LIVRAISON_BOULES1,true);
                // Recalage
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(-300, new ArrayList<Hook>(), true, false);
                oldPos = actualState.robot.getPosition();
                newPos = oldPos.clone();
                newPos.setX(1225);
                actualState.robot.setPosition(newPos);

                log.debug("Orientation :" + actualState.robot.getOrientationFast());
                if (Math.abs(actualState.robot.getOrientationFast() - Math.PI)%(2*Math.PI) < recalageThresholdOrientation) {
                    log.debug("Recalage en orientation :" + Math.abs(actualState.robot.getOrientationFast() - Math.PI)%(2*Math.PI));
                    actualState.robot.setOrientation(Math.PI);
                }

                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);

                //Prise de module 2

                actualState.robot.prendModule(Side.LEFT);

// la on a le module 2

                actualState.table.ballsCratereDepart.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

//Range les actionneurs à l'arrière
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);

//la on drop le module
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);

                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceApresModule2);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1);

                actualState.robot.livreBoules();
//la on drop nos BALLS et on lance le script suivant

                actualState.robot.setRempliDeBoules(false);
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_PRES_BASE,true);


                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1);
                actualState.robot.goTo(pointDevantCratere2);

                actualState.robot.turnTo(posCratere2);

                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(135);
                actualState.robot.setLocomotionSpeed(Speed.FAST_ALL);

//Prise de boules 2
                actualState.robot.prendBoules();

                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereDepart.isStillThere=false;

                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_LIVRAISON_BOULES2,true);

//Placement livraison boules
                actualState.robot.moveLengthwiseAndWaitIfNeeded(-190);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(130);

//Livraison de boules 2
                actualState.robot.livreBoules();

                actualState.robot.setRempliDeBoules(false);

                actualState.robot.moveLengthwise(-120); //Esquive le robot secondaire !
            }


        }
        catch(Exception e)
        {
            log.critical("Robot ou actionneur bloqué dans ScriptedGoTo");
            finalize(actualState, e);
            throw e;

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
        if (version == 0 || version ==1) {
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
            recalageThresholdOrientation = Double.parseDouble(config.getProperty("tolerance_orientation_recalage"));

        } catch (ConfigPropertyNotFoundException e){
            log.debug("Revoir le code : impossible de trouver la propriété " + e.getPropertyNotFound());
        }
    }


    public void finalize(GameState state, Exception e) throws UnableToMoveException
    {
        log.debug("Exception " + e +"dans scriptedGOTO : Lancement du finalize !");
        state.robot.setBasicDetection(false);


    }

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}
