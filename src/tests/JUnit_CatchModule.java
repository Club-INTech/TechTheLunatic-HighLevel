package tests;

import enums.ActuatorOrder;
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
 * @author Rem
 */
public class JUnit_CatchModule extends JUnit_Test {

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
        mRobot.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, false);
        mRobot.robot.useActuator(ActuatorOrder.TIENT_BOULES, false);
        mRobot.robot.turn(13*Math.PI/16);
        mRobot.robot.moveLengthwise(400);

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
            log.debug("Ramassage des modules");
            scriptManager.getScript(ScriptNames.CATCH_MODULE).goToThenExec(1, mRobot, emptyList);
            // mRobot.robot.useActuator(ActuatorOrder.LEVE_ASC, true);
            // mRobot.robot.useActuator(ActuatorOrder.BAISSE_ASC, true);
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
