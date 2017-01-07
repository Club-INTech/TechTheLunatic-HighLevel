package tests;

import enums.ActuatorOrder;
import enums.ScriptNames;
import enums.Speed;
import hook.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import robot.Locomotion;
import scripts.ScriptManager;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
import table.Table;

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

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        log.debug("JUnit_DeplacementsTest.setUp()");
        mRobot = container.getService(GameState.class);

        //La position de depart est mise dans le updateConfig() //TODO
        mRobot.updateConfig();
        mRobot.robot.setPosition(Table.entryPosition);
        mRobot.robot.setOrientation(Math.PI);
        mRobot.robot.setLocomotionSpeed(Speed.SLOW_ALL);
        scriptManager = container.getService(ScriptManager.class);
        mRobot.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, true);

        //container.getService(ServiceNames.THREAD_INTERFACE);
        //container.startInstanciedThreads();
    }

    @Test
    public void catchThoseBalls()
    {
        ArrayList<Hook> emptyList = new ArrayList<Hook>();
        try
        {
            //On execute le script
            log.debug("Ramassage des balles");
           // mRobot.robot.moveToCircle(new Circle(new Vec2(850,540), 400),emptyList,mRobot.table);
           scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(0, mRobot, emptyList);

        //mRobot.robot.moveLengthwise(100);
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