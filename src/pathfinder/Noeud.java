package pathfinder;
import java.util.ArrayList;
import smartMath.Vec2;
// TODO la liste potentielle des scripts à lancer.
public class Noeud
{
    public Vec2 position;
    public int indice;
    public int nom;
    public ArrayList <Arrete> lArretes;

    public void attachelien(Noeud autre)
    {
        lArretes.add(new Arrete(this,autre));

    }

}
