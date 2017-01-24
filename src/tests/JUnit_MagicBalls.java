package tests;

import enums.ActuatorOrder;
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
 * Tests les 2 scripts l'un après l'autre ! (c'est plus propre que d'appeler les 2 JUnits l'un après l'autre)
 * @autor Rem
 */
public class JUnit_MagicBalls extends JUnit_Test {
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
        mRobot.robot.setOrientation(Math.PI);
        mRobot.robot.setLocomotionSpeed(Speed.SLOW_ALL);
        scriptManager = container.getService(ScriptManager.class);
        // mRobot.robot.turn(13*Math.PI/16);
        mRobot.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
        mRobot.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
        // mRobot.robot.moveLengthwise(400);

        container.getService(ThreadEvents.class);
        container.startInstanciedThreads();
    }

    @Test
    public void catchThoseBalls()
    {
        ArrayList<Hook> emptyList = new ArrayList<Hook>();
        try
        {
            //On execute le script
            log.debug("Ramassage des balles");
            mRobot.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);
            scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(1, mRobot, emptyList);
            mRobot.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
            scriptManager.getScript(ScriptNames.DROP_BALLS).goToThenExec(2, mRobot, emptyList);
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
