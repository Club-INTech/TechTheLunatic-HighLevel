package tests

import enums.Speed;
import enums.ScriptNames
import hook.Hook;
import org.junit.Before;
import scripts.ScriptManager;
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


        scriptManager = container.getService(ScriptManager.class);

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
            scriptManager.getScript(ScriptNames.CATCH_BALLS).goToThenExec(0, mRobot, emptyList);
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