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
import hook.methods.PrepareToCatchModG;
import hook.methods.RepliAllActionneurs;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

import java.util.ArrayList;


/** FullScript sans pathfinding utilisant goTo (pointVisé)
 * version 0 : prend le module dans a fuée au début
 * @author melanie, gaelle, rem
 */


public class ScriptedGoTo extends AbstractScript
{

    /** PointsVisés, dstances & angles du script, override par la config */

    /** On prend le premier module */
    boolean prisePremierModule=false;

    /** Déplacements jusqu'à la zone du fond */
    Vec2 point1MilieuTable                  = new Vec2(540,800);
    Vec2 point2EntreeFinTable               = new Vec2(805,900);
    Vec2 pointContournementModule           = new Vec2(805, 1560);

    /** Manoeuvre pour attraper le 1er module */
    Vec2 point3AttrapperModule1             = new Vec2(805,1725);
    double angleAttraperModule1             = -3*Math.PI/4;

    /** Manoeuvre pour attraper les 1eres boules */
    Vec2 point4arriveDevantCratereFond      = new Vec2(650,1785);
    Vec2 posCratere1                        = new Vec2(420, 1880);
    int distanceCratereFondAvantBoules      = 55;
    int distanceCratereFondApresBoules      = -170;

    /** Manoeuvre pour drop le 1er module */
    double angleCratereFondAvantDepotModule = Math.PI/4;
    int distanceCratereFondAvantDepotModule = -135;
    int distanceCratereFondApresDepotModule = 55;

    /** Déplacements jusqu'à la zone de départ */
    Vec2 pointSortieCratereFond             = new Vec2(1115,1290);
    Vec2 pointIntermediaireVersModule       = new Vec2(1115, 1005); //new Vec2(1115,850);

    /** Manoeuvre pour attraper le 2e module */
    Vec2 pointAvantModule2                  = new Vec2(985, 742); //anciennement 770
    double angleDropModule2                 = Math.PI;
    int distanceApresModule2                = 60;

    /** Distance de recalage */
    int distanceRecalage = -250;

    /** Manoeuvre pour déposer les 1eres boules */
    int distanceAvantDeposeBoules1          = 230;
    int distanceReculApresDepotBoule1       = -200;

    /** Manoeuvre pour prendre les 2emes boules */
    Vec2 posCratere2                        = new Vec2(850, 540);
    int distanceCratereBaseAvantBoules      = 215;
    int distanceCratereBaseApresBoules      = -190;

    /** Manoeuvre pour déposer les 2emes boules */
    double angleAvantDeposeBoules           = -Math.PI/2 + 0.15;
    int distanceAvantDeposeBoules2          = 190;
    double angleDeposeBoules                = -Math.PI/2;

    /** Manoeuvre de fin !*/
    int distanceEsquiveRobot                = -120;

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
            Hook repliTout = hookFactory.newPositionHook(pointSortieCratereFond, (float) -Math.PI/4, 25, 500);
            repliTout.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));
            Hook prepareToCatch2ndMod = hookFactory.newPositionHook(pointIntermediaireVersModule, (float) - Math.PI/2, 25, 500);
            prepareToCatch2ndMod.addCallback(new Callback(new PrepareToCatchModG(), true, actualState));

            hooksToConsider.add(repliTout);
            hooksToConsider.add(prepareToCatch2ndMod);

            if(versionToExecute == 0) {

                // Timer afin de savoir combien de temps l'on prend en moyenne
                long debutMatch = System.currentTimeMillis();

                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                if (prisePremierModule) {

                    actualState.robot.setOrientation(Math.PI);
                    actualState.robot.setPosition(new Vec2(605,194));

                    // Avec le Hook pour prendre le module multicolore pret de la zone de départ
                    actualState.robot.moveLengthwiseAndWaitIfNeeded(75);
                    actualState.robot.turn(2 * Math.PI / 3 + 0.1);   // 250, 580 <- 578, 208
                    actualState.robot.moveLengthwise(550);
                    actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);

                    actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                    actualState.robot.moveLengthwise(-530, hooksToConsider);
                    actualState.robot.turn(Math.PI / 2);
                    actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
                    actualState.robot.moveLengthwise(250);

                    log.debug("position"+actualState.robot.getPosition());
                    log.debug("positionFast"+actualState.robot.getPositionFast());
                    log.debug("Orientation du HL :" + actualState.robot.getOrientationFast());
                }

                // Aller au cratère du fond
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                actualState.robot.goTo(point1MilieuTable);
                actualState.robot.goTo(point2EntreeFinTable);

                actualState.robot.goTo(pointContournementModule);

                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                 actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);

                // Prise du module 1er module (celui du fond)
                actualState.robot.goTo(point3AttrapperModule1);
                actualState.robot.turn(angleAttraperModule1);

                actualState.robot.prendModule(Side.RIGHT);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                // Prise des 1eres boules (celles du fond)
                actualState.robot.goTo(point4arriveDevantCratereFond);
                actualState.robot.turnTo(posCratere1);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantBoules);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                actualState.robot.prendBoules();

                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereBaseLunaire.isStillThere=false;

                // Livraison du 1er module
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISON_MODULEFOND,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule, new ArrayList<Hook>(), true, true);

                // Recalage
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(distanceCratereFondAvantDepotModule, new ArrayList<Hook>(), true, false);
                Vec2 oldPos = actualState.robot.getPosition();
                Vec2 newPos = oldPos.clone();
                //Potentiellement réajuster la position?
                actualState.robot.setOrientation(Math.PI/4);
                actualState.robot.setPosition(newPos);



                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);

                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);

                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule);

                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);

                // Aller vers la zone de départ
                actualState.robot.goTo(pointSortieCratereFond, hooksToConsider);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D,false);

                actualState.robot.goTo(pointIntermediaireVersModule, hooksToConsider);
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);

                // Prise du 2e module (celui de la zone de départ)
                actualState.robot.goTo(pointAvantModule2);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_BACK_MOTION);

                actualState.robot.turn(angleDropModule2);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);

                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_LIVRAISON_BOULES1,true);

                // Recalage
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(-300, new ArrayList<Hook>(), true, false);
                oldPos = actualState.robot.getPosition();
                 newPos = oldPos.clone();
                newPos.setX(1225);
                actualState.robot.setPosition(newPos);

             /*   log.debug("Orientation :" + actualState.robot.getOrientationFast());

                if (Math.abs(actualState.robot.getOrientationFast() - Math.PI)%(2*Math.PI) < recalageThresholdOrientation) {
                    log.debug("Recalage en orientation :" + Math.abs(actualState.robot.getOrientationFast() - Math.PI)%(2*Math.PI));
                    actualState.robot.setOrientation(Math.PI);
                }*/

                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                actualState.robot.prendModule(Side.LEFT);

                actualState.table.ballsCratereDepart.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

                // Replie des actionneurs arrières et drop le second module
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);

                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);

                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

                // Livraison des 1eres boules
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceApresModule2);
                actualState.robot.turn(-Math.PI/2+0.15);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1);
                actualState.robot.turn(-Math.PI/2);

                actualState.robot.livreBoules();

                actualState.robot.setRempliDeBoules(false);
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_PRES_BASE,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1);

                // Prise des 2emes boules
                actualState.robot.turnTo(posCratere2);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereBaseAvantBoules);

                actualState.robot.prendBoules();

                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereDepart.isStillThere=false;

                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATERE_LIVRAISON_BOULES2,true);

                // Livraison des 2emes boules
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereBaseApresBoules);
                actualState.robot.turn(angleAvantDeposeBoules);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules2);
                actualState.robot.turn(angleDeposeBoules);

                actualState.robot.livreBoules();

                actualState.robot.setRempliDeBoules(false);

                actualState.robot.moveLengthwise(distanceEsquiveRobot);

                log.debug("Temps du match : " + (System.currentTimeMillis() - debutMatch));
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
