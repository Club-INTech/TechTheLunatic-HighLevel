package strategie;

import enums.DirectionStrategy;
import enums.ScriptNames;
import exceptions.BadVersionException;
import exceptions.Locomotion.PointInObstacleException;
import exceptions.Locomotion.UnableToMoveException;
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
    public static void decision(GameState realState,ScriptManager scriptmanager,Pathfinding pf) throws PointInObstacleException
    {
        ArrayList<Vec2> cheminCratereFond=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminCraterePresBase=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonBoules1=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonBoules2=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminLivraisonModuleFond=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminModuleFond=new ArrayList<Vec2>();
        ArrayList<Vec2> cheminModuleFondPresBase=new ArrayList<Vec2>();
        try {
             cheminCratereFond = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);

        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
             cheminCraterePresBase = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
             cheminLivraisonBoules1 = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
             cheminLivraisonBoules2 = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES2).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
             cheminLivraisonModuleFond = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
            cheminModuleFond = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_MODULEFOND).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        try {
            cheminModuleFondPresBase = pf.Astarfoulah(realState.robot.getPosition(),
                    Geometry.pointProche(realState.robot.getPosition(), scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_MODULEPRESBASE).entryPosition(0, 0, realState.robot.getPosition())),
                    realState.robot.getOrientation(), realState.robot.getLocomotionSpeed().translationSpeed, realState.robot.getLocomotionSpeed().rotationSpeed);
        } catch (BadVersionException v) {
            System.out.println("BadVersionException");

        }
        ArrayList<Hook> emptyHook = new ArrayList<>();
//Là on regarde ce qui est nul ou pas et on prend en conséquence
        if(!cheminModuleFond.isEmpty() && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_MODULEFOND) && realState.table.cratereBase.isStillThere && realState.robot.getChargementModule()<=2)
        {
            try {
                realState.robot.followPath(cheminModuleFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_MODULEFOND).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  module fond");
            }

        }
        else if(realState.table.cratereBaseLunaire.isStillThere && !realState.robot.isRempliDeBoules() && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND))//(!chemincratèrefond.isEmpty) On ne teste pas l'existence du chemin pathfinding car on est dans un objet?
        {
            try {
             //   realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREFOND).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  cratere fond");
            }

        }
        else if( realState.robot.getChargementModule()>=1 && !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND))//Pareil qu'au dessus !cheminLivraisonModuleFond.isEmpty() &&
        {
            try {
               // realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONMODULEFOND).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  livraison module");
            }

        }
        else if(!cheminLivraisonBoules1.isEmpty() &&!realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1)  && realState.robot.isRempliDeBoules() && realState.table.cratere.isStillThere)//si on peut y aller et qu'on a nos boulasses et que le module est toujours là
        {
            try {
                 realState.robot.followPath(cheminLivraisonBoules1, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES1).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  cratere fond");
            }

        }

        else if(!cheminCraterePresBase.isEmpty()&& !realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE)  && !realState.robot.isRempliDeBoules() && realState.table.cratere.isStillThere)//si on peut y aller et qu'on a pas de boulasses cratère est plein
        {
            try {
                realState.robot.followPath(cheminCratereFond, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_CRATEREPRESBASE).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  cratere fond");
            }

        }
        else if(!cheminLivraisonBoules2.isEmpty()  && realState.robot.isRempliDeBoules() &&!realState.robot.dejaFait.get(ScriptNames.SCRIPTED_GO_TO_MODULEFOND))//si on peut y aller et qu'on a nos boulasses
        {
            try {
                realState.robot.followPath(cheminLivraisonBoules2, emptyHook);
                scriptmanager.getScript(ScriptNames.SCRIPTED_GO_TO_LIVRAISONBOULES2).goToThenExec(0,realState,emptyHook);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected exception caught dans l'ia allant vers  cratere fond");
            }

        }
        }
}
