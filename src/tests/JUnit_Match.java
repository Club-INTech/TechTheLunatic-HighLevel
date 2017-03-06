package tests;

import enums.DirectionStrategy;
import enums.ScriptNames;
import enums.Speed;
import hook.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import threads.dataHandlers.ThreadEvents;

import java.util.ArrayList;

/**
 * Tentative de match
 * @autor Gaelle
 */
public class JUnit_Match extends JUnit_Test {
    private GameState mRobot;
    private ScriptManager scriptManager;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        log.debug("JUnit_DeplacementsTest.setUp()");
        mRobot = container.getService(GameState.class);
        //La position de depart est mise dans la Table (l'updtate config va la chercher)
        mRobot.updateConfig();
        mRobot.robot.setPosition(Table.entryPosition);
        mRobot.robot.setOrientation(0);
        mRobot.robot.setLocomotionSpeed(Speed.SLOW_ALL);
        scriptManager = container.getService(ScriptManager.class);

        container.getService(ThreadEvents.class);
        container.startInstanciedThreads();

        //départ en arrière
        scriptManager.getScript(ScriptNames.INITIALISE_ROBOT).goToThenExec(1, mRobot, new ArrayList<Hook>());
    }

    @Test
    public void catchThoseBalls()
    {
        ArrayList<Hook> emptyList = new ArrayList<Hook>();
        try
        {
            //On execute le script
            log.debug("Ramassage des balles");
            mRobot.robot.setDirectionStrategy(DirectionStrategy.FASTEST);

            //Attraper modules fusée
            //scriptManager.getScript(ScriptNames.CATCH_MODULE).goToThenExec(1, mRobot, emptyList);

            //Attraper balles premier cratère
            scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(1, mRobot, emptyList);
            mRobot.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
            mRobot.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);

            //Déposer balles premier cratère, et attraper le module
            scriptManager.getScript(ScriptNames.DROP_BALLS).goToThenExec(2, mRobot, emptyList);

            //Déposer 3 modules
            scriptManager.getScript(ScriptNames.DROP_MODULE).goToThenExec(4, mRobot, emptyList);

            //Attraper balles cratère du fond, et attraper puis déposer le module sur le chemin
            scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(2, mRobot,emptyList);

            //Déposer les balles qui restent
            scriptManager.getScript(ScriptNames.DROP_BALLS).goToThenExec(1, mRobot, emptyList);




        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    @After
    public void finish()
    {
        mRobot.robot.immobilise();
    }
}
