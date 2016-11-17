package pathfinder;
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
     * @param depart noeud de depart de l'A*
     * @param arrivee noeud d'arrivée
     * @return Liste des noeuds
     */


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


public Graphe(Log log, Config config, Table table)
{
    this.log=log;
    this.config=config;
    this.table=table;

}
    /**
     * Graphe initial
     */
    public void initGraphe() //le graphe initial V1
    {
        //on fabrique les noeuds. On les relie TOUS. On supprime ceux bloqués. C'est sale, mais ça fait un graphe bien fourni
        Noeud n1=new Noeud(this,new Vec2(0,0));
        this.lNoeuds.add(n1);
        ArrayList <Noeud> lN=new ArrayList<Noeud>();
        ArrayList <Arrete> lA=new ArrayList<Arrete>();
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
            for (Noeud noeud2: lN)
            {
                if(noeud1 != noeud2)
                {
                    noeud1.attachelien(noeud2);

                }
            }
            for(Arrete y:noeud1.lArretes) {
                for (ObstacleCircular z : a.getFixedObstacles()) {
                    y.isBloquant(z);

                }
                for (ObstacleRectangular z : a.getRectangles()) {
                    y.isBloquant(z);

                }
            }
        }

        }
        //on relie tous les obstacles
        /**for (ObstacleCircular x:a.getFixedObstacles())
        {
            for (ObstacleCircular x1:a.getFixedObstacles())
            {
                x.relieObstacle(x1,this,this.n,this.ecart);

            }



        }
        for (ObstacleRectangular x:a.getRectangles())
        {
            for (ObstacleCircular x1:a.getFixedObstacles())
            {
                x1.relieObstacle(x,this,this.n,this.ecart);

            }

        }
        // On détruit tous ceux qui sont bloqués
        for (Noeud x:this.getlNoeuds())
        {
            for(Arrete y:x.lArretes)
            {
                for (ObstacleCircular z:a.getFixedObstacles())
                {
                    y.isBloquant(z);
                }
                for (ObstacleRectangular z:a.getRectangles())
                {
                    y.isBloquant(z);
                }
            }
        }
         */

    /**
     * Surcharge du constructeur: construit un sous-graphe à partir de
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
        detecte.fabriqueNoeud(this,8,3);
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

    /**
     *
     * @param position la position du robot
     * @param detecte l'obstacle détecté
     * @param chemin la liste des Noeuds que le robot était censé suivre
     * @param indice l'indice de la liste précédente
     * @return la liste des noeuds intermédiaires pour éviter l'obstacle
     */

}




