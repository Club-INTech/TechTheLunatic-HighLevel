package strategie;

import enums.DirectionStrategy;
import enums.ScriptNames;
import hook.Hook;
import scripts.ScriptManager;
import tests.JUnit_Match_scripted;

import java.util.ArrayList;

/**
 * Created by shininisan on 02.05.17.
 */
public class IA {
    public static void decision(GameState game, ScriptManager scriptManager)
    {

        ArrayList<Hook> emptyList = new ArrayList<Hook>();
        try
        {
            //On execute le script de match scripté

            game.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);
            scriptManager.getScript(ScriptNames.FULLSCRIPTED).goToThenExec(0, game, new ArrayList<Hook>());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
