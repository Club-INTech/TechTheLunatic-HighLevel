package pathfinder;
import smartMath.Vec2;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCircular;
import table.obstacles.ObstacleRectangular;

/**
 * Created by shininisan on 19/10/16.
 */
public class Arrete {
    public Noeud depart;
    public Noeud arrivee;
    public double cout;
    public boolean isUpdated= true;

    public Arrete(Noeud n1, Noeud n2)
    {
        depart=n1;
        arrivee=n2;
        this.cout=0;
        this.calcCout();

    }

    /**
     * détruit le lien entre arrivee et départ
     * @param obstacle
     */
    public void isBloquant(ObstacleCircular obstacle) // calcul de l'intersection de la ligne et des objets
    {


            Vec2 da= new Vec2(depart.position.x-arrivee.position.x,depart.position.y-arrivee.position.y);
            Vec2 dc= new Vec2(depart.position.x-obstacle.getPosition().x,depart.position.y-obstacle.getPosition().y);
            double distcentre= da.dot(dc);
            //... découverte de la fonction isinobstacle
            if (distcentre< obstacle.getRadius()*obstacle.getRadius())
            {
                // on le détache de ses noeuds
                this.depart.lArretes.remove(this.depart);
                this.arrivee.lArretes.remove(this.arrivee);
            }

        ;

        /*
        si on est update on renvoit bloquant
        sinon on vérifie qu'on ne passe pas a distance radius de r pour les cercle
        pour les recangles min R pour un dégrossisage), sinon l'algo précis de traçage de ligne
        */
    }
    public void isBloquant(ObstacleRectangular obstacle)
    {
        if(obstacle.getlNoeud()[0]!=null)
        {
            Vec2 ab=this.depart.position.minusNewVector(this.arrivee.position);
            Vec2 ac=this.depart.position.minusNewVector(obstacle.getlNoeud()[0].position);
            Vec2 ad=this.depart.position.minusNewVector(obstacle.getlNoeud()[1].position);
            Vec2 ae=this.depart.position.minusNewVector(obstacle.getlNoeud()[2].position);
            Vec2 af=this.depart.position.minusNewVector(obstacle.getlNoeud()[3].position);
            if(!((ab.dot(ac) * ab.dot(ad)>=0) && (ab.dot(ad)* ab.dot(ae)>=0) && (ab.dot(ae)* ab.dot(af)>=0) ))
            {
                this.depart.lArretes.remove(this.depart);
                this.arrivee.lArretes.remove(this.arrivee);
            }
        }
    }

    /**
     * actualise le cout de l'arrête
     */
    public void calcCout()
    {
        this.cout=new Vec2 (this.depart.position.x-this.arrivee.position.x,this.depart.position.y-this.arrivee.position.y).length();
    }
}
