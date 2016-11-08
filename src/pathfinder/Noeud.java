package pathfinder;
import java.security.PublicKey;
import java.util.ArrayList;
import smartMath.Vec2;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCircular;

// TODO la liste potentielle des scripts à lancer.
public class Noeud
{

    public Vec2 position;
    public int indice;// pour éviter d'avoir a rechercher plus tard
    public int nom; // useless pour l'instant, mais peut être utile a l'affichage
    public double distarrivee; // distance vers l'arrivee
    public ArrayList <Arrete> lArretes;
    public double sommedepart;

    /**
     * Constructeur
     * @param g graphe
     * @param position
     */
    public Noeud(Graphe g, Vec2 position)
    {
        this.position=position;
        this.indice=g.getNoeudsurtable()+1;
        g.setNoeudsurtable(this.indice);


    }

    /**
     * Ctor de copy
     * @param n1 noeud à copier
     */
    public Noeud (Noeud n1)
    {
        this.position=n1.position;
        this.indice=n1.indice;
        this.distarrivee=n1.distarrivee;
        this.lArretes=n1.lArretes;
        this.sommedepart=n1.sommedepart;
    }

    /**
     * Crée l'arête entre this et le noeud fourni
     * Fabrique également le lien dans l'autre sens
     * @param autre Noeud destination
     */
    public void attachelien(Noeud autre)
    {
        this.lArretes.add(new Arrete(this,autre));
        autre.lArretes.add(new Arrete(autre,this));

    }

    /**
     * Actualise la distance euclidienne jusqu'à l'arrivée
     * @param arrivee
     */
    public void distheuristique(Noeud arrivee)
    {
        this.distarrivee=  this.position.minusNewVector(arrivee.position).length();
    }

    /**
     * Détruit les liens bloque par l'obstacle fourni en argument
     * @param detecte obstacle détecté
     */
    public void update(ObstacleCircular detecte)
    {
        for (Arrete x:this.lArretes)
        {
            x.isBloquant(detecte);
            x.calcCout();
        }


    }

}
