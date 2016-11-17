package scripts;

import pathfinder.Arrete;
import pathfinder.ComparaNoeud;
import pathfinder.Graphe;
import pathfinder.Noeud;
import smartMath.Vec2;
import table.Table;
import table.obstacles.ObstacleCircular;
import utils.Config;
import utils.Log;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by shininisan on 17/11/16.
 */
 class Pathfinding {
    Graphe  graphe;
    private  Table table;
    private Config config;
    private  Log log;
    private Pathfinding(Log log, Config config, Table table)
    {
        this.log=log;
        this.config=config;
        this.table=table;

    }





    public ArrayList<Noeud> Astarfoulah(Noeud depart, Noeud arrivee)
    {// on met les noeuds dans une priority queue


        ArrayList <Noeud> chemin= new ArrayList() ;


        PriorityQueue<Noeud> pq = new PriorityQueue( graphe.getNoeudsurtable(), new ComparaNoeud());
        pq.add(depart);
        Noeud noeudCourant=null;
        Noeud noeudPrecedent=null;
        while(noeudCourant != arrivee || pq.size()==0)
        {

            noeudCourant=pq.poll();
            chemin.add(noeudCourant);
            noeudCourant.distheuristique(arrivee);

            if(noeudPrecedent==null)
            {
                noeudCourant.sommedepart = 0;
            }
            else {
                noeudCourant.sommedepart = noeudPrecedent.sommedepart + graphe.Nazareth(noeudPrecedent, noeudCourant).cout;
            }
            for (Arrete aux : noeudCourant.lArretes)
            {

                pq.add(aux.arrivee);




            }
            noeudPrecedent=noeudCourant;
        }

        return chemin;
    }
    public ArrayList<Noeud> actGraphe(Vec2 position, ObstacleCircular detecte, ArrayList<Noeud> chemin, int indice)
    {
        //on veut une copie de ini et de fin
        Noeud ini=new Noeud(chemin.get(indice));
        Noeud fin=new Noeud(chemin.get(indice+1));


        ini.position=position;
        ini.update(detecte);// on supprime les liens inutiles et actualise les valeurs

        ini.lArretes.add(new Arrete(ini,fin));
        // on crée un nouveau graphe, en recopiant les arêtes du noeud précédent, actualisant les valeurs
        Graphe sousg=new Graphe(this.log,this.config,this.table);

        sousg.initGraphe(position,detecte,ini,fin);
        return this.Astarfoulah(ini,fin);


    }
}