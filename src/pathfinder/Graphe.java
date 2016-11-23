package pathfinder;
import smartMath.Circle;
import smartMath.Geometry;
import smartMath.Segment;
import smartMath.Vec2;
import table.Table;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCircular;
import table.obstacles.ObstacleManager;
import table.obstacles.ObstacleRectangular;
import utils.Config;
import utils.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;


public class Graphe {
    private  Table table;
    private Config config;
    private  Log log;
    private int noeudsurtable;

    private ArrayList<Noeud> lNoeuds;
    private int n=8;
    private int ecart=10;

    public int getN() {
        return n;
    }

    public Table getTable() {
        return table;
    }

    public int getEcart() {
        return ecart;
    }

    public void setNoeudsurtable(int noeudsurtable) {
        this.noeudsurtable = noeudsurtable;
    }

    public int getNoeudsurtable() {
        return noeudsurtable;
    }

    public ArrayList<Noeud> getlNoeuds() {
        return lNoeuds;
    }





    /**
     *
     * @param t1 Le noeud dans lequel t2 se trouve potentiellement
     * @param t2 Le noeud à chercher
     * @return L'Arrete qui relie t1 et t2
     */
    public Arrete Nazareth(Noeud t1, Noeud t2)
    {
       for (Arrete x :t1.lArretes)
       {
           if(x.arrivee.equals(t2))
           {
               return x;
           }
       }

        return null;
    }

    /**
     * Constructeur standard du graphe
     * @param log
     * @param config
     * @param table
     */
    public Graphe(Log log, Config config, Table table)
{
    this.log=log;
    this.config=config;
    this.table=table;
    this.noeudsurtable=0;

    this.lNoeuds=new ArrayList<Noeud>();
    initGraphe();
}

    /**
     * Construit le graphe initial de la table, en reliant tous les noeuds et supprimant ceux bloqués par des obstacles
     */
    public void initGraphe() //le graphe initial V1
    {
        //on fabrique les noeuds. On les relie TOUS. On supprime ceux bloqués. C'est sale, mais ça fait un graphe bien fourni
       /** Noeud n1=new Noeud(this,new Vec2(200,200));

        this.getlNoeuds().add(n1);*/
        ArrayList <Noeud> lN=new ArrayList<Noeud>();

        ObstacleManager a=this.table.getObstacleManager();
        for (ObstacleCircular x:a.getFixedObstacles())
        {
            for (Noeud y: x.fabriqueNoeud(this,this.n,this.ecart))

            {
                lN.add(y);
            }
        }

        for (Noeud noeud1: lN)
        {
            for (int i=0;i<lN.size();i++)
            {
                if(noeud1 != lN.get(i))
                {
                    if (noeud1==null ||lN.get(i)==null)
                    {
                        log.debug("null expcept");
                    }
                    else {
                        noeud1.attachelien(lN.get(i));
                    }
                }
            }



                for (ObstacleCircular z : a.getFixedObstacles()) {
                    for (int i = 0; i < noeud1.lArretes.size(); i++) {

                        if (Geometry.intersects(new Segment(noeud1.position,noeud1.lArretes.get(i).arrivee.position),new Circle(z.getPosition(),z.getRadius()))) {
                            i--;
                        }

                    }
                }
                for (ObstacleRectangular z : a.getRectangles()) {
                    for (int i = 0; i < noeud1.lArretes.size(); i++) {
                    if(noeud1.lArretes.get(i).isBloquant(z))
                    {
                        i--;
                    }


                }
            }
        }
        for (int i = 0; i < this.lNoeuds.size(); i++) {
         if(this.lNoeuds.get(i).lArretes.size()==0)
         {
             this.lNoeuds.remove(this.lNoeuds.get(i));
             i--;
         }
        }



        }

    /**
     * Surcharge de l'initialiseur: construit un sous-graphe à partir de
     * @param position du robot
     * @param detecte l'obstacle détecté
     * @param ini le noeud où était le robot avant la détection
     * @param fin le noeud vers lequel se dirige le robot
     */
    public void initGraphe(Vec2 position,ObstacleCircular detecte, Noeud ini,Noeud fin)
    {


        for (Arrete x:ini.lArretes)
        {
            x.calcCout();
        }
        //on ajoute les noeuds dans le sous graphe
        this.lNoeuds.add(ini);
        this.lNoeuds.add(fin);

        for (Noeud x : detecte.fabriqueNoeudRelie(this,this.n,this.ecart))// on fait les noeuds de l'obstacle, on les relie entre eux et on les relie
        {
            ini.attachelien(x);
        }
        ini.update(detecte);// on supprime les liens que l'obstacle bouche
        //on revoit nos indices pour des raisons cosmétiques pour l'instant
        this.reorder();


    }

    /**
     * réharmonise le nombre de noeud sur la table d'un graphe et leur indice
     */
    public void reorder()
    {
        for (int i=0;i<this.lNoeuds.size();i++)
        {
            this.lNoeuds.get(i).indice=i;
            this.noeudsurtable=this.getlNoeuds().size();
        }
    }



}




