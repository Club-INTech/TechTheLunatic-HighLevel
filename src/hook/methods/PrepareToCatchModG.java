package hook.methods;

import enums.ActuatorOrder;
import hook.Executable;
import strategie.GameState;

/**
 * Created by rem on 5/12/17.
 */
public class PrepareToCatchModG implements Executable {

    @Override
    public boolean execute(GameState state){
        try{

            state.robot.useActuator(ActuatorOrder.MID_ATTRAPE_G, false);
            state.robot.useActuator(ActuatorOrder.REPLI_CALLE_G, false);

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
