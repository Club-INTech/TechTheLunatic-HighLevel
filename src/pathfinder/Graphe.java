/*
 * Copyright (c) 2016, INTech.
 *
 * This file is part of INTech's HighLevel.
 *
 *  INTech's HighLevel is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  INTech's HighLevel is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with it.  If not, see <http://www.gnu.org/licenses/>.
 */

package pathfinder;

import smartMath.Circle;
import smartMath.Geometry;
import smartMath.Segment;
import smartMath.Vec2;
import table.Table;
import table.obstacles.ObstacleCircular;
import table.obstacles.ObstacleManager;
import table.obstacles.ObstacleRectangular;
import utils.Config;
import utils.Log;

import java.util.ArrayList;


public class Graphe {
    private  Table table;
    private Config config;
    private  Log log;
    private int noeudsurtable;

    private ArrayList<Noeud> lNoeuds;
    private int n=8;
    private int ecart=5;

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

        ObstacleManager a=this.table.getObstacleManager();
        for (ObstacleCircular x:a.getFixedObstacles())
        {
            x.fabriqueNoeud(this,this.n,this.ecart);


        }
        for (ObstacleRectangular x:a.getRectangles())
        {
            x.fabriqueNoeud(this, this.ecart);
        }

        for (Noeud noeud1: this.getlNoeuds())
        {
            for (int i=0;i<this.getlNoeuds().size();i++)
            {
                if(noeud1 != this.getlNoeuds().get(i))
                {
                    if (noeud1==null ||this.getlNoeuds().get(i)==null)
                    {
                        log.debug("null except");
                    }
                    else {
                        //the double while
                        int j=0;
                        boolean creer=true;

                        int nombobst= a.getFixedObstacles().size();
                        int nombobstRec=a.getRectangles().size();
                        while (creer && j<nombobst)  {
                            creer= creer && !(Geometry.intersects(new Segment(noeud1.position, this.getlNoeuds().get(i).position), new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius()))) ;

                            j++;
                        }
                        j=0;
                        while (creer && j<nombobstRec)  {

                            creer= creer && Geometry.CohenSutherlandLineClipAndDraw(noeud1.position,this.getlNoeuds().get(i).position,a.getRectangles().get(j).getlNoeud().get(3).position,a.getRectangles().get(j).getlNoeud().get(0).position) ;
                            if(!creer)
                            {

                            }
                            j++;
                        }

                        if(creer)
                        {noeud1.attachelien(this.getlNoeuds().get(i));}
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
            x.attachelien(ini);
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




