package scripts;

import enums.ActuatorOrder;
import enums.DirectionStrategy;
import enums.ScriptNames;
import enums.Speed;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Callback;
import hook.Hook;
import hook.methods.*;
import hook.methods.RepliAllActionneurs;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import table.Table;
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

    private Vec2 point1MilieuTable = new Vec2(620,800);
    private Vec2 point2EntreeFinTable = new Vec2(890,1400);
    private Vec2 point3AttrapperModule1 = new Vec2(890,1760);
    private Vec2 point4arriveDevantCratereFond = new Vec2(630,1790);
    private double angleDevantCratereFond = Math.PI - 0.42;
    private int distanceCratereFondApresBoules = -150;

    private double angleCratereFondAvantDepotModule = Math.PI/4;

    private int distanceCratereFondAvantDepotModule = -92;
    private int distanceCratereFondApresDepotModule = 55;

    private Vec2 pointSortieCratereFond =new Vec2(1160,1210);
    private Vec2 pointAvantModule2 = new Vec2(1000, 720);
    private int distanceReculModule2=-160;
    private int distanceApresModule2=100;

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
            Hook replieTout0 = hookFactory.newPositionHook(new Vec2 (480, 600), (float) -Math.PI/2, 100, 400);
            replieTout0.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));
            Hook priseModuleDroit = hookFactory.newPositionHook(point2EntreeFinTable, (float) -Math.PI/2, 300, 400);
            priseModuleDroit.addCallback(new Callback(new PrepareToCatchModD(), true, actualState));
            Hook repliTout1 = hookFactory.newPositionHook(point3AttrapperModule1, (float) -Math.PI/2, 50, 100);
            repliTout1.addCallback(new Callback(new RepliAllActionneurs(), true, actualState));

            hooksToConsider.add(replieTout0);
            hooksToConsider.add(priseModuleDroit);
            hooksToConsider.add(repliTout1);

            if (versionToExecute==0)
            {

                actualState.robot.goTo(new Vec2(320, 267));
                actualState.robot.turn(Math.PI);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(120);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, true);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150);
                actualState.table.cylindreDevantDepart.isStillThere=false;


                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
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
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);


                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, true);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150);

                //Aller au cratère du fond
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_MODULEFOND,true);


                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.goTo(point1MilieuTable, hooksToConsider);
                actualState.robot.goTo(point2EntreeFinTable, hooksToConsider);

                actualState.robot.goTo(point3AttrapperModule1);

                //prise du module du fond

                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
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

                //La on devrait avoir le module
                actualState.table.cylindreCratereBase.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);



                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);


                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND,true);

                //changement de vitesse pour ne pas pousser les balles
                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(100, hooksToConsider);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                actualState.robot.goTo(point4arriveDevantCratereFond);
                actualState.robot.turn(angleDevantCratereFond);

                actualState.robot.setLocomotionSpeed(Speed.FAST_T_MEDIUM_R);

                //Prise des boules
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);

                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereBaseLunaire.isStillThere=false;


                //Livraison module
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND,true);


                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                //On vide un module
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);


                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);

                actualState.robot.goTo(pointSortieCratereFond);

                //Ici commence livraison boules1
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1,true);

                actualState.robot.goTo(pointAvantModule2);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_BACK_MOTION);

                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                actualState.robot.turn(Math.PI);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculModule2);
                //actualState.robot.goTo(point7AttrapperModule2);

                //Prise de module 2
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
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

                //y a un cylindre de plus
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);
                actualState.table.cylindreCratereDepart.isStillThere=false;

                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);



                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                // Manoeuvre degueu pour se décaler
                actualState.robot.moveLengthwiseAndWaitIfNeeded(60);
                // Bon discord tu vas geuler mais j'avais la flemme
                actualState.robot.turn(Math.PI - Math.asin(110.0 / 150));
                actualState.robot.moveLengthwiseAndWaitIfNeeded(150);
                actualState.robot.turn(Math.PI);
                // Callage contre le depose-module
                actualState.robot.moveLengthwise(-190, hooksToConsider, true, false, Speed.SLOW_ALL);
                // Drop un module
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceApresModule2);
                actualState.robot.goTo(pointAvantDeposeBoules1);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1);

                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1);
                actualState.robot.goTo(pointDevantCratere2);
                actualState.robot.turn(-29*Math.PI/32);
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(130);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(150);


                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);


            }

            Hook catchMD = hookFactory.newPositionHook(Table.entryPosition.plusNewVector(new Vec2(440, 2.319)), (float)(2.319), 10, 100);
            catchMD.addCallback(new Callback(new CatchModuleD(), true, actualState));
            hooksToConsider.add(catchMD);

            if (versionToExecute==1)
            {


                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
                // Avec le Hook pour prendre le module multicolore pret de la zone de départ
                actualState.robot.moveLengthwise(80);
                actualState.robot.turn(2*Math.PI/3);   // 250, 580 <- 578, 208
                actualState.robot.moveLengthwise(600);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);

                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwise(-547, hooksToConsider);
                actualState.robot.turn(Math.PI/2);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);
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

                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
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
//La on devrait avoir le module
                actualState.table.cylindreCratereBase.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);

                /*
                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                */

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                //changement de vitesse pour ne pas pousser les balles
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND,true);

                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(105, hooksToConsider);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);

                actualState.robot.goTo(point4arriveDevantCratereFond);
                actualState.robot.turn(angleDevantCratereFond);

                actualState.robot.setLocomotionSpeed(Speed.FAST_T_MEDIUM_R);

                //Prise des boules
                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);
// la on a des boulasses
                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereBaseLunaire.isStillThere=false;


                //Livraison module
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);    //Ralentit pour éviter de défoncer la zone
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondAvantDepotModule);
                actualState.robot.setLocomotionSpeed(Speed.FAST_T_SLOW_R);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                //La on l'a largué
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()-1);



                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceCratereFondApresDepotModule);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, false);

                //actualState.robot.goTo(point5sortieCratereFond);

                actualState.robot.goTo(new Vec2(1215,1210), hooksToConsider);

                actualState.robot.goTo(pointAvantModule2);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_BACK_MOTION);

                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_G, false);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);

                actualState.robot.turn(Math.PI-0.1);

                //Là on commence le script livraison 1 (à vérifier)
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculModule2);
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
// la on a le module 2

                actualState.table.ballsCratereDepart.isStillThere=false;
                actualState.robot.setChargementModule(actualState.robot.getChargementModule()+1);

                actualState.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);

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
                actualState.robot.goTo(pointAvantDeposeBoules1);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceAvantDeposeBoules1);

                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);

                //la on drop nos BALLS et on lance le script suivant
                actualState.robot.setRempliDeBoules(false);
                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE,true);



                actualState.robot.moveLengthwiseAndWaitIfNeeded(distanceReculApresDepotBoule1);
                actualState.robot.goTo(pointDevantCratere2);

                Vec2 posCratere= new Vec2(850, 540);
                Vec2 posRobot=actualState.robot.getPosition();
                Vec2 vec = posCratere.minusNewVector(posRobot);

                actualState.robot.turn(vec.getA());
                actualState.robot.setLocomotionSpeed(Speed.SLOW_ALL);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(155);
                actualState.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);

                actualState.robot.setRempliDeBoules(true);
                actualState.table.ballsCratereDepart.isStillThere=false;

                actualState.robot.dejaFait.put(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES2,true);

                actualState.robot.moveLengthwiseAndWaitIfNeeded(-150);
                actualState.robot.turn(-Math.PI/2);
                actualState.robot.moveLengthwiseAndWaitIfNeeded(165);

                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PELLE_REASSERV, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_PELLE, true);

                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.setRempliDeBoules(false);
                actualState.robot.moveLengthwise(-120);
            }


        }
        catch(UnableToMoveException e)
        {
            log.critical("Robot ou actionneur bloqué dans ScriptedGoTo");
            finalize(actualState, e);

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


    public void finalize(GameState state, UnableToMoveException e) throws UnableToMoveException
    {
        log.debug("Exception " + e +"dans DropBalls : Lancement du finalize !");
        state.robot.setBasicDetection(false);
        throw e;
    }

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}
