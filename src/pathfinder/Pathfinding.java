package pathfinder;

import container.Service;
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
import java.util.PriorityQueue;

/**
 * Created by shininisan on 17/11/16.
 */
 public class Pathfinding implements Service {
    private Graphe graphe;
    private Table table;
    private Config config;
    private Log log;

    public Pathfinding(Log log, Config config, Table table) {
        this.log = log;
        this.config = config;
        this.table = table;

    }

    public void setGraphe(Graphe graphe) {
        this.graphe = graphe;
    }

    public Graphe getGraphe() {
        return this.graphe;
    }

    /**
     * @param departV  noeud de depart de l'A*
     * @param arriveeV noeud d'arrivée
     * @return Liste des noeuds
     */
    public ArrayList<Vec2> Astarfoulah(Vec2 departV, Vec2 arriveeV, Graphe g) {// on met les noeuds dans une priority queue


        Noeud depart = new Noeud(g, departV);
        g.getlNoeuds().add(depart);
        Noeud arrivee = new Noeud(g, arriveeV);
        g.getlNoeuds().add(arrivee);
        ObstacleManager a = this.table.getObstacleManager();
        for (int i=0 ; i<g.getlNoeuds().size() ; i++)
        {
            depart.attachelien(g.getlNoeuds().get(i));
            g.getlNoeuds().get(i).attachelien(depart);
            arrivee.attachelien(g.getlNoeuds().get(i));
            g.getlNoeuds().get(i).attachelien(arrivee);
        }

        for (ObstacleCircular z : a.getFixedObstacles()) {
            for (int i = 0; i < depart.lArretes.size(); i++) {
                if (Geometry.intersects(new Segment(depart.position,depart.lArretes.get(i).arrivee.position),new Circle(z.getPosition(),z.getRadius())))
                    {
                        depart.lArretes.remove(i);

                        i--;
                }
            }
            for (int i = 0; i < arrivee.lArretes.size(); i++) {
                if (Geometry.intersects(new Segment(arrivee.position,arrivee.lArretes.get(i).arrivee.position),new Circle(z.getPosition(),z.getRadius())))
                {
                    arrivee.lArretes.remove(i);

                    i--;
                }
            }
        }
        for (ObstacleRectangular z : a.getRectangles()) {
            for (int i = 0; i < depart.lArretes.size(); i++) {
                if (depart.lArretes.get(i).isBloquant(z)) {
                    i--;
                }


            }
            for (int i = 0; i < arrivee.lArretes.size(); i++) {
                if (arrivee.lArretes.get(i).isBloquant(z)) {
                    i--;
                }


            }

        }
        return Astarfoulah(depart, arrivee, g);
    }


    public ArrayList<Vec2> Astarfoulah(Noeud depart, Noeud arrivee, Graphe g) {// on met les noeuds dans une priority queue


        ArrayList<Vec2> chemin = new ArrayList();
        ArrayList<Noeud> closedlist = new ArrayList();


        PriorityQueue<Noeud> pq = new PriorityQueue(g.getNoeudsurtable(), new ComparaNoeud());
        //pq.add(depart);
        depart.noeudPrecedent = null;
        closedlist.add(depart);
        depart.distheuristique(arrivee);
        Noeud noeudCourant = depart;


        do {
            if (noeudCourant.getIndice() == -1) {
                noeudCourant.sommedepart = 0;
            }

            for (int i = 0 ; i < noeudCourant.lArretes.size() ; i++)
            {

                double b = noeudCourant.sommedepart + noeudCourant.lArretes.get(i).cout;
                if (!closedlist.contains(noeudCourant.lArretes.get(i).arrivee) && noeudCourant.lArretes.get(i).arrivee.sommedepart < b  ) // la dernière partie fait existe dans openlist. si y a pas mieux dans Closed list non plus
                {
                    noeudCourant.lArretes.get(i).arrivee.sommedepart = b;
                    pq.add(noeudCourant.lArretes.get(i).arrivee);
                    noeudCourant.lArretes.get(i).arrivee.distheuristique(arrivee);
                    noeudCourant.lArretes.get(i).arrivee.noeudPrecedent = noeudCourant;
                }

            }

            log.debug("pq size" + pq.size());

            if(pq.isEmpty()) break;

            noeudCourant = pq.poll();

            if(noeudCourant == null)
            {
                log.debug("NULL NODE");
            }

            if (arrivee == noeudCourant) {
                while (noeudCourant != null && chemin.size() < 100) {
                    chemin.add(0, noeudCourant.position);
                    noeudCourant = noeudCourant.noeudPrecedent;
                }
                return chemin;
            }


            closedlist.add(noeudCourant);


        } while (noeudCourant != arrivee && !pq.isEmpty());



        log.debug("No path found");
        return new ArrayList();
    }



    /**
     *
     * @param position la position du robot
     * @param detecte l'obstacle détecté
     * @param chemin la liste des Noeuds que le robot était censé suivre
     * @param indice l'indice de la liste précédente
     * @return la liste des noeuds intermédiaires pour éviter l'obstacle
     */
    public ArrayList<Vec2> actGraphe(Vec2 position, ObstacleCircular detecte, ArrayList<Noeud> chemin, int indice)
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
        return this.Astarfoulah(ini,fin,sousg);


    }

    @Override
    public void updateConfig()
    {
        //TODO charger la config
    }
}