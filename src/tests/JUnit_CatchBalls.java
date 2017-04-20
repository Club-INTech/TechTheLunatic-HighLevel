package tests;

import enums.DirectionStrategy;
import enums.ScriptNames;
import enums.Speed;
import hook.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import threads.dataHandlers.ThreadEvents;

import java.util.ArrayList;


/**
 * teste le ramassage des balles par la version 0 du script
 * @author gaelle
 *
 */
public class JUnit_CatchBalls extends JUnit_Test
{
    private GameState mRobot;
    private ScriptManager scriptManager;
    private ArrayList<Hook> listHook = new ArrayList<Hook>();

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        log.debug("JUnit_DeplacementsTest.setUp()");
        mRobot = container.getService(GameState.class);
        //La position de depart est mise dans la Table (l'updtate config va la chercher)
        mRobot.updateConfig();
        mRobot.robot.setPosition(new Vec2(1000,1000));
        mRobot.robot.setOrientation(0);
        mRobot.robot.setLocomotionSpeed(Speed.MEDIUM_ALL);
        scriptManager = container.getService(ScriptManager.class);

        container.getService(ThreadEvents.class);
        container.startInstanciedThreads();

        //scriptManager.getScript(ScriptNames.INITIALISE_ROBOT).goToThenExec(1, mRobot, listHook);
    }

    @Test
    public void catchThoseBalls()
    {
        try
        {
            //On execute le script
            log.debug("Ramassage des balles");
            mRobot.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);
            scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(0, mRobot, listHook);

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