package pathfinder;
import java.util.ArrayList;
import smartMath.Vec2;
// TODO la liste potentielle des scripts à lancer.
public class Noeud
{
    public Vec2 position;
    public int indice;// pour éviter d'avoir a rechercher plus tard
    public int nom; // useless pour l'instant, mais peut être utile a l'affichage
    public double distarrivee; // distance vers l'arrivee
    public ArrayList <Arrete> lArretes;
    public double sommedepart;
    public void attachelien(Noeud autre)
    {
        lArretes.add(new Arrete(this,autre));

    }

    public void distheuristique(Noeud arrivee)
    {
        this.distarrivee= this.lArretes.get(arrivee.indice).cout;
    }


}
