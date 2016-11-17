package pathfinder;

import container.Service;
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
 public class Pathfinding implements Service {
    private Graphe  graphe;
    private  Table table;
    private Config config;
    private  Log log;
    public Pathfinding(Log log, Config config, Table table)
    {
        this.log=log;
        this.config=config;
        this.table=table;

    }

    public void setGraphe(Graphe graphe) {
        this.graphe = graphe;
    }
    public Graphe getGraphe(){return this.graphe;}

    /**
     *
     * @param depart noeud de depart de l'A*
     * @param arrivee noeud d'arrivée
     * @return Liste des noeuds
     */
    public ArrayList<Noeud> Astarfoulah(Noeud depart, Noeud arrivee)
    {// on met les noeuds dans une priority queue


        ArrayList <Noeud> chemin= new ArrayList() ;


        PriorityQueue<Noeud> pq = new PriorityQueue( this.graphe.getNoeudsurtable(), new ComparaNoeud());
        pq.add(depart);
        Noeud noeudCourant=new Noeud();
        Noeud noeudPrecedent=new Noeud();
        while(noeudCourant != arrivee || pq.size()==0)
        {

            noeudCourant=pq.poll();
            chemin.add(noeudCourant);
            noeudCourant.distheuristique(arrivee);
            log.debug(noeudCourant.indice+" taille pq :"+pq.size()+"nombre arrete de ce noeud"+noeudCourant.lArretes.size());

            if(noeudPrecedent.getIndice()== -1)
            {
                noeudCourant.sommedepart = 0;
            }
            else {
                noeudCourant.sommedepart = noeudPrecedent.sommedepart;
                log.debug(noeudCourant+" <=suivant prec =>"+noeudPrecedent);
                noeudCourant.sommedepart += graphe.Nazareth(noeudPrecedent, noeudCourant).cout;
            }
            for (Arrete aux : noeudCourant.lArretes)
            {
                if(!pq.contains(aux)) {

                    pq.add(aux.arrivee);
                }
            }
            noeudPrecedent=noeudCourant;
        }

        return chemin;
    }
    /**
     *
     * @param position la position du robot
     * @param detecte l'obstacle détecté
     * @param chemin la liste des Noeuds que le robot était censé suivre
     * @param indice l'indice de la liste précédente
     * @return la liste des noeuds intermédiaires pour éviter l'obstacle
     */
    public ArrayList<Noeud> actGraphe(Vec2 position, ObstacleCircular detecte, ArrayList<Noeud> chemin, int indice)
    {
        //on veut une copie de ini et de fin
        Noeud ini=new Noeud(chemin.get(indice));
        Noeud fin=new Noeud(chemin.get(indice+1));

        ini.position=position;
        // on supprime les liens inutiles et actualise les valeurs on rajoute les noeuds de l'obstacles, on relie

        ini.lArretes.add(new Arrete(ini,fin));
        // on crée un nouveau graphe, en recopiant les arêtes du noeud précédent, actualisant les valeurs
        Graphe sousg=new Graphe(this.log,this.config,this.table);

        sousg.initGraphe(position,detecte,ini,fin);
        return this.Astarfoulah(ini,fin);


    }

    @Override
    public void updateConfig()
    {
        //TODO charger la config
    }
}