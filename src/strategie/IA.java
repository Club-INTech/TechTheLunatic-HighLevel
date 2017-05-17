package strategie;

import enums.DirectionStrategy;
import enums.ScriptNames;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ExecuteException;
import exceptions.Locomotion.PointInObstacleException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.Hook;
import pathfinder.Pathfinding;
import scripts.ScriptManager;
import smartMath.Geometry;
import smartMath.Vec2;
import tests.JUnit_Match_scripted;
import threads.ThreadTimer;
import utils.Log;

import java.util.ArrayList;

/**
 * Created by shininisan on 02.05.17.
 * L'IA va calculer des chemins, vérifier quels sont les scripts potentiellements utilisables, appeler le pathfinding si
 * ils sont pas dans des endroits trop pourris
 */
public class IA {
    public static void decision(GameState realState,ScriptManager scriptmanager,Pathfinding pf) throws UnableToMoveException, BadVersionException, SerialConnexionException, ExecuteException, BlockedActuatorException, PointInObstacleException
    {
        ArrayList<Vec2> cheminCratereFond=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminCraterePresBase=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonBoules1=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonBoules2=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonModuleFond=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminModuleFond=new ArrayList<Vec2>();
        Vec2 safePointFond=new Vec2(920,1320);
        Vec2 safePointDepart=new Vec2(1100,880);
        //ArrayList<Vec2> cheminModulePresBase=new ArrayList<Vec2>();
        try {
             cheminCratereFond = pf.Astarfoulah(realState.robot.getPosition(),safePointFond,
                     realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);

        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        try {
             cheminCraterePresBase = pf.Astarfoulah(realState.robot.getPosition(), new Vec2(1100, 650),
            realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        try {
             cheminLivraisonBoules1 = pf.Astarfoulah(realState.robot.getPosition(), safePointDepart,
                             realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        try {
             cheminLivraisonBoules2 = pf.Astarfoulah(realState.robot.getPosition(),safePointDepart,
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        try {
             cheminLivraisonModuleFond = pf.Astarfoulah(realState.robot.getPosition(),safePointFond
                   , realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        try {
            cheminModuleFond = pf.Astarfoulah(realState.robot.getPosition(),safePointFond,
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (PointInObstacleException v) {
            System.out.println("Pointinobstacle");

        }
        /*
        try {
            cheminModulePresBase = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_MODULEPRESBASE).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }Inclu dans la livraison de boules 1 */
        ArrayList<Hook> emptyHook = new ArrayList<>();
//Là on regarde ce qui est nul ou pas et on prend en conséquence
        if(!cheminModuleFond.isEmpty() && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_MODULEFOND) && realState.table.cratereBase.isStillThere && realState.robot.getChargementModule()<=2)
        {
                realState.robot.followPath(cheminModuleFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_MODULEFOND).goToThenExec(0,realState,emptyHook);


        }
        else if(realState.table.cratereBaseLunaire.isStillThere && !realState.robot.isRempliDeBoules() &&
                !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND)
                && (!cheminCratereFond.isEmpty() ||(realState.robot.getPosition().getY()>1400||realState.robot.getPosition().getX()>1100)))//(!chemincratèrefond.isEmpty) On ne teste pas l'existence du chemin pathfinding car on est dans un objet?
        {

             //   realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND).goToThenExec(0,realState,emptyHook);

        }

        else if( realState.robot.getChargementModule()>=1 && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND) )//Pareil qu'au dessus !cheminLivraisonModuleFond.isEmpty() &&
        {
                 realState.robot.followPath(cheminLivraisonModuleFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND).goToThenExec(0,realState,emptyHook);

        }
        else if( realState.robot.getChargementModule()>=1 && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND) &&
        (!cheminLivraisonModuleFond.isEmpty() ||(realState.robot.getPosition().getY()>1400||realState.robot.getPosition().getX()>1100)))//Pareil qu'au dessus !cheminLivraisonModuleFond.isEmpty() &&
        {
               // realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND).goToThenExec(0,realState,emptyHook);

        }
        else if( realState.robot.getChargementModule()>=1 && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND)){
                 realState.robot.followPath(cheminModuleFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND).goToThenExec(0,realState,emptyHook);

        }
        else if(!cheminLivraisonBoules1.isEmpty() &&!realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1)  && realState.robot.isRempliDeBoules() && realState.table.cratere.isStillThere)//si on peut y aller et qu'on a nos boulasses et que le module est toujours là
        {
                 realState.robot.followPath(cheminLivraisonBoules1, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1).goToThenExec(0,realState,emptyHook);

        }

        else if(!cheminCraterePresBase.isEmpty()&& !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE)  && !realState.robot.isRempliDeBoules() && realState.table.cratere.isStillThere)//si on peut y aller et qu'on a pas de boulasses cratère est plein
        {
                realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE).goToThenExec(0,realState,emptyHook);

        }
        else if(!cheminLivraisonBoules2.isEmpty()  && realState.robot.isRempliDeBoules() &&!realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_MODULEFOND))//si on peut y aller et qu'on a nos boulasses
        {
                realState.robot.followPath(cheminLivraisonBoules2, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES2).goToThenExec(0,realState,emptyHook);

        }

        }
}
