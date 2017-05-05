package scripts;

import enums.ActuatorOrder;
import enums.DirectionStrategy;
import exceptions.BadVersionException;
import exceptions.BlockedActuatorException;
import exceptions.ConfigPropertyNotFoundException;
import exceptions.ExecuteException;
import exceptions.Locomotion.UnableToMoveException;
import hook.Callback;
import hook.Hook;
import hook.methods.PriseModule;
import hook.methods.ReposLargueModule;
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
    private int distanceCratereFondApresBoules;


    public void setPoint1MilieuTable(int X, int Y) {
        this.point1MilieuTable.setX(X);
        this.point1MilieuTable.setY(Y);
    }

    private Vec2 point2EntreeFinTable = new Vec2();
    public void setPoint2EntreeFinTable(int X, int Y) {
        this.point2EntreeFinTable.setX(X);
        this.point2EntreeFinTable.setY(Y);
    }
    private Vec2 point3AttrapperModule1 = new Vec2();
    public void setPoint3AttrapperModule1(int X, int Y) {
        this.point3AttrapperModule1.setX(X);
        this.point3AttrapperModule1.setY(Y);
    }
    private Vec2 arriveDevantCratereFond = new Vec2();
    public void arriveDevantCratereFond(int X, int Y) {
        this.arriveDevantCratereFond.setX(X);
        this.arriveDevantCratereFond.setY(Y);
    }

    private int distanceCratereFondAvantDepotModule;
    private double angleCratereFondAvantDepotModule;
    private int distanceCratereFondApresDepotModule;

    private Vec2 sortieCratereFond=new Vec2();
    public void setSortieCratereFond(int X, int Y) {
        this.sortieCratereFond.setX(X);
        this.sortieCratereFond.setY(Y);
    }

    private Vec2 point6SortieFinTable = new Vec2();
    public void setPoint6SortieFinTable(int X, int Y) {
        this.point6SortieFinTable.setX(X);
        this.point6SortieFinTable.setY(Y);
    }
    private Vec2 point7AttrapperModule2 = new Vec2();
    public void setPoint7AttrapperModule2(int X, int Y) {
        this.point7AttrapperModule2.setX(X);
        this.point7AttrapperModule2.setY(Y);
    }
    private double angleAttrapperModule2 = 0;

    private Vec2 point8ReculerPourAttrapperModule2 = new Vec2();
    public void setPoint8ReculerPourAttrapperModule2(int X, int Y) {
        this.point8ReculerPourAttrapperModule2.setX(X);
        this.point8ReculerPourAttrapperModule2.setY(Y);
    }
    private Vec2 point9ReavancerApresModule2 = new Vec2();
    public void setPoint9ReavancerApresModule2(int X, int Y) {
        this.point9ReavancerApresModule2.setX(X);
        this.point9ReavancerApresModule2.setY(Y);
    }
    private Vec2 point10DevantCratere2 = new Vec2();
    public void setPoint10DevantCratere2(int X, int Y) {
        this.point10DevantCratere2.setX(X);
        this.point10DevantCratere2.setY(Y);
    }
    private double angleCratere2 = 0;

    private Vec2 point11ReculerDuCratere = new Vec2();
    public void setPoint11ReculerDuCratere(int X, int Y) {
        this.point11ReculerDuCratere.setX(X);
        this.point11ReculerDuCratere.setY(Y);
    }
    private Vec2 point12LarguerBalles = new Vec2();
    public void setPoint12LarguerBalles(int X, int Y) {
        this.point12LarguerBalles.setX(X);
        this.point12LarguerBalles.setY(Y);
    }
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
            detect = false;

            setPoint1MilieuTable(578,800);
            setPoint2EntreeFinTable(850,1400);

            setPoint3AttrapperModule1(850,1760);

            arriveDevantCratereFond(625,1810);
            distanceCratereFondApresBoules=-130;

            angleCratereFondAvantDepotModule=Math.PI/4;
            distanceCratereFondAvantDepotModule=-120;
            distanceCratereFondApresDepotModule=110;

            setSortieCratereFond(1000,1200);

            setPoint7AttrapperModule2(0,0);
            angleAttrapperModule2 = 0;
            setPoint8ReculerPourAttrapperModule2(0,0);
            setPoint9ReavancerApresModule2(0,0);
            setPoint10DevantCratere2(0,0);
            angleCratere2 = 0;
            setPoint11ReculerDuCratere(0,0);
            setPoint12LarguerBalles(0,0);


            if(detect) {
                actualState.robot.switchSensor();
            }

            if (versionToExecute==0)
            {

                log.debug("point1" + point1MilieuTable);

                actualState.robot.goTo(point1MilieuTable);

                actualState.robot.goTo(point2EntreeFinTable);

                actualState.robot.useActuator(ActuatorOrder.REPOS_ATTRAPE_D, false); //se prépare à prendre le module
                actualState.robot.useActuator(ActuatorOrder.REPLI_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);

                actualState.robot.goTo(point3AttrapperModule1);

                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, true);
                actualState.robot.useActuator(ActuatorOrder.MID_ATTRAPE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_G, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_CALLE_D, true);
                actualState.robot.useActuator(ActuatorOrder.LEVE_ASC, false);
                actualState.robot.useActuator(ActuatorOrder.MED_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PRET_PELLE, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_D, false);
                actualState.robot.useActuator(ActuatorOrder.LIVRE_CALLE_G, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_D, false);
                actualState.robot.useActuator(ActuatorOrder.PREND_MODULE_G, false);
                actualState.robot.setDirectionStrategy(DirectionStrategy.FORCE_FORWARD_MOTION);

                actualState.robot.goTo(arriveDevantCratereFond);

                actualState.robot.setDirectionStrategy(DirectionStrategy.FASTEST);
                actualState.robot.useActuator(ActuatorOrder.DEPLOYER_PELLETEUSE, true);
                actualState.robot.useActuator(ActuatorOrder.PREND_PELLE, true);
                actualState.robot.useActuator(ActuatorOrder.REPLIER_PELLETEUSE, false);
                actualState.robot.useActuator(ActuatorOrder.RANGE_PELLE, false);

                actualState.robot.moveLengthwise(distanceCratereFondApresBoules);
                actualState.robot.turn(angleCratereFondAvantDepotModule);
                actualState.robot.moveLengthwise(distanceCratereFondAvantDepotModule);
                actualState.robot.useActuator(ActuatorOrder.POUSSE_LARGUEUR, true);
                actualState.robot.useActuator(ActuatorOrder.REPOS_LARGUEUR, false);
                actualState.robot.moveLengthwise(distanceCratereFondApresDepotModule);
                actualState.robot.goTo(sortieCratereFond);

     /*           actualState.robot.goTo(point5DevantCratere1);
                actualState.robot.moveLengthwise(distanceRepositionnementCratere);
                actualState.robot.moveLengthwise(reculCratereFondAvantDepotModule);
                actualState.robot.turn(angleRepositionnementCratere);
                actualState.robot.turn(anglePartirDuCratere);
                actualState.robot.moveLengthwise(distancePartirDuCratere);
                actualState.robot.goTo(point6SortieFinTable);
                actualState.robot.goTo(point7AttrapperModule2);
                actualState.robot.turn(angleAttrapperModule2);
                actualState.robot.goTo(point8ReculerPourAttrapperModule2);
                actualState.robot.goTo(point9ReavancerApresModule2);
                actualState.robot.goTo(point10DevantCratere2);
                actualState.robot.turn(angleCratere2);
                actualState.robot.goTo(point11ReculerDuCratere);
                actualState.robot.goTo(point12LarguerBalles);*/

                Hook PriseModule = hookFactory.newPositionHook(new Vec2(80, 1850), (float) Math.PI/2, 100, 10000);
                PriseModule.addCallback(new Callback(new PriseModule(), true, actualState));
                hooksToConsider.add(PriseModule);
                Hook ReposLargueModule = hookFactory.newPositionHook(new Vec2(550, 1650), (float) -Math.PI/4, 100, 10000);
                ReposLargueModule.addCallback(new Callback(new ReposLargueModule(), true, actualState));
                hooksToConsider.add(ReposLargueModule);

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
