package scripts;

import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Hook;
import hook.types.HookFactory;
import smartMath.Circle;
import smartMath.Vec2;
import strategie.GameState;
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

    private Vec2 point1MilieuTable = new Vec2();
    private Vec2 point2EntreeFinTable = new Vec2();
    private Vec2 point3AttrapperModule1 = new Vec2();
    private Vec2 point4RepositionnementAvantDAllerAuCratere = new Vec2();
    private Vec2 point5DevantCratere1 = new Vec2();

    private int distanceRepositionnementCratere = 0;
    private double angleRepositionnementCratere = 0;

    private double anglePartirDuCratere = 0;
    private int distancePartirDuCratere = 0;

    private Vec2 point6SortieFinTable = new Vec2();
    private Vec2 point7AttrapperModule2 = new Vec2();

    private double angleAttrapperModule2 = 0;

    private Vec2 point8ReculerPourAttrapperModule2 = new Vec2();
    private Vec2 point9ReavancerApresModule2 = new Vec2();
    private Vec2 point10DevantCratere2 = new Vec2();
    private double angleCratere2 = 0;

    private Vec2 point11ReculerDuCratere = new Vec2();
    private Vec2 point12LarguerBalles = new Vec2();

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

            if(detect) {
                actualState.robot.switchSensor();
            }

            if (versionToExecute==0)
            {

                //script:...

            }

        }
        catch(Exception e)
        {
            log.critical("Robot ou actionneur bloqué dans DropBalls");
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
        if (version == 0) {
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

    @Override
    public Integer[] getVersion(GameState stateToConsider) {
        return versions;
    }
}
