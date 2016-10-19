package pathfinder;
import smartMath.Vec2;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCircular;

/**
 * Created by shininisan on 19/10/16.
 */
public class Arrete {
    private Noeud depart;
    private Noeud arrivee;
    private double cout;
    private boolean isUpdated= true;
    private boolean bloquant= false;
    public Arrete(Noeud n1, Noeud n2)
    {
        depart=n1;
        arrivee=n2;
        bloquant=false;
        isUpdated=true;
        this.calcCout();

    }

    public boolean isBloquant(ObstacleCircular obstacle) // calcul de l'intersection de la ligne et des objets
    {

        if(!isUpdated)
        {
            Vec2 da= new Vec2(depart.position.x-arrivee.position.x,depart.position.y-arrivee.position.y);
            Vec2 dc= new Vec2(depart.position.x-obstacle.getPosition().x,depart.position.y-obstacle.getPosition().y);
            double distcentre= da.dot(dc) / dc.length();
            if (distcentre< obstacle.getRadius())
            {
                this.bloquant=true;
            }
            else
            {
                this.bloquant=false;
            }

        }
        this.isUpdated=false;
        return this.bloquant;

        /*
        si on est update on renvoit bloquant
        sinon on vérifie qu'on ne passe pas a distance radius de r pour les cercle
        pour les recangles min R pour un dégrossisage), sinon l'algo précis de traçage de ligne
        */
    }

    public void calcCout()
    {
        this.cout=new Vec2 (this.depart.position.x-this.arrivee.position.x,this.depart.position.y-this.arrivee.position.y).length();
    }
}
