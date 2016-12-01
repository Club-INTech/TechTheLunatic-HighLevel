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

import container.Service;
import smartMath.Circle;
import smartMath.Geometry;
import smartMath.Segment;
import smartMath.Vec2;
import table.Table;
import table.obstacles.ObstacleCircular;
import table.obstacles.ObstacleManager;
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
        for (int i=0 ; i<g.getlNoeuds().size() ; i++) //Nnoeud *tcréa + Obst * Nnoeud *tremove
        {
            int j=0;
            boolean creerdep=true;
            boolean creerarr=true;
            int nombobst= a.getFixedObstacles().size();
            int nombobstRec=a.getRectangles().size();
            while ((creerdep||creerarr) && j<nombobst)  {
                creerdep= creerdep && !(Geometry.intersects(new Segment(depart.position, g.getlNoeuds().get(i).position), new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius()))) ;
                creerarr= creerarr && !(Geometry.intersects(new Segment(arrivee.position,g.getlNoeuds().get(i).position),new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius())));
                j++;
                     }
            j=0;
            while ((creerdep||creerarr) && j<nombobstRec)  {
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(1).position));
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(3).position));
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(2).position));
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(2).position,a.getRectangles().get(j).getlNoeud().get(3).position));
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(2).position));
                creerdep= creerdep && !Geometry.intersects(new Segment(departV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(3).position));

                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(1).position));
                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(3).position));
                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(2).position));
                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(2).position,a.getRectangles().get(j).getlNoeud().get(3).position));
                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(2).position));
                creerarr= creerarr && !Geometry.intersects(new Segment(arriveeV, g.getlNoeuds().get(i).position),new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(3).position));

                j++;
            }
            if(creerdep) {
                depart.attachelien(g.getlNoeuds().get(i));
                g.getlNoeuds().get(i).attachelien(depart);
            }
            if(creerarr){
                    arrivee.attachelien(g.getlNoeuds().get(i));
                    g.getlNoeuds().get(i).attachelien(arrivee);

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
        depart.sommedepart=0;
        Noeud noeudCourant = depart;


        do {
            if (noeudCourant.getIndice() == -1) {
                noeudCourant.sommedepart = 0;
            }

            for (int i = 0 ; i < noeudCourant.lArretes.size() ; i++)
            {
                noeudCourant.lArretes.get(i).arrivee.distheuristique(arrivee);
                double b = noeudCourant.sommedepart + noeudCourant.lArretes.get(i).cout ;
                    if (!closedlist.contains(noeudCourant.lArretes.get(i).arrivee) && noeudCourant.lArretes.get(i).arrivee.sommedepart>b  ) // la dernière partie fait existe dans openlist. si y a pas mieux dans Closed list non plus
                {

                    noeudCourant.lArretes.get(i).arrivee.sommedepart = b;
                    noeudCourant.lArretes.get(i).arrivee.distheuristique(arrivee);
                    noeudCourant.lArretes.get(i).arrivee.noeudPrecedent = noeudCourant;
                    pq.add(noeudCourant.lArretes.get(i).arrivee);

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
                for (Noeud a:g.getlNoeuds()) //on réinitialise les noeuds
                {

                    a.sommedepart=100000000;
                    a.noeudPrecedent=null;

                }

                for (int j=0; j<depart.lArretes.size();j++) {
                    int p=depart.lArretes.get(j).arrivee.lArretes.size();
                    for (int i = 0; i < p; i++) {

                        if (depart.lArretes.get(j).arrivee.lArretes.get(i).arrivee == depart) {
                            depart.lArretes.get(j).arrivee.lArretes.remove(depart.lArretes.get(j).arrivee.lArretes.get(i));
                            i--;
                            p--;
                        }
                    }
                }
                for (int j=0; j<arrivee.lArretes.size();j++) {
                    int p=arrivee.lArretes.get(j).arrivee.lArretes.size();
                    for (int i = 0; i < p; i++) {

                        if (arrivee.lArretes.get(j).arrivee.lArretes.get(i).arrivee == arrivee) {
                            arrivee.lArretes.get(j).arrivee.lArretes.remove(arrivee.lArretes.get(j).arrivee.lArretes.get(i));
                            i--;
                            p--;
                        }
                    }
                }

                g.getlNoeuds().remove(depart);
                g.getlNoeuds().remove(arrivee);
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