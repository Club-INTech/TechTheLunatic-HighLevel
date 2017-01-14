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
import exceptions.Locomotion.PointInObstacleException;
import smartMath.Circle;
import smartMath.Geometry;
import smartMath.Segment;
import smartMath.Vec2;
import table.Table;
import table.obstacles.ObstacleManager;
import table.obstacles.ObstacleProximity;
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

    private Pathfinding(Log log, Config config, Table table) {
        this.log = log;
        this.config = config;
        this.table = table;
        this.graphe=new Graphe(log,config,table);

    }

    public void setGraphe(Graphe graphe) {
        this.graphe = graphe;
    }

    public Graphe getGraphe() {
        return this.graphe;
    }

    /**
     * L'algorithme créer deux noeuds sur l'arrivée et le début, relie a tous les points accessibles, et lance Astarfoullah
     * @param departV la position de début du pathfinding
     * @param arriveeV la position d'arrivée
      * @return une arrayliste des positions intermédiaires
     */
    public ArrayList<Vec2> Astarfoulah(Vec2 departV, Vec2 arriveeV, double robotOrientation) throws PointInObstacleException {
       Graphe g=this.graphe;
        //on crée les noeuds et on récupère les obstacles
        Noeud depart = new Noeud(g, departV);
        g.getlNoeuds().add(depart);
        Noeud arrivee = new Noeud(g, arriveeV);
        g.getlNoeuds().add(arrivee);
        ObstacleManager a = this.table.getObstacleManager();

        if (Math.abs(departV.x)>1500-a.mRobotRadius ||
                Math.abs(departV.y-1000) > 1000 - a.mRobotRadius)
        {
            log.debug("Retourne sur la table connard => Point de départ " + departV);

            // Cas "simple", où le robot est perpendiculaire au côté de la table sur lequel il est bloqué (ya pas d'obstacle derriere, faut pas déconner)
            if (departV.y < a.mRobotRadius && Math.abs(robotOrientation)>Math.PI/4 && Math.abs(robotOrientation)<3*Math.PI/4)
            {
                ArrayList<Vec2> newPath = Astarfoulah(new Vec2(departV.x, a.mRobotRadius + 1), arriveeV, robotOrientation);
                newPath.add(0, departV);
                return newPath;
            }
            else if (departV.y > 2000-a.mRobotRadius && Math.abs(robotOrientation)>Math.PI/4 && Math.abs(robotOrientation)<3*Math.PI/4)
            {
                ArrayList<Vec2> newPath = Astarfoulah(new Vec2(departV.x, 1999-a.mRobotRadius), arriveeV, robotOrientation);
                newPath.add(0, departV);
                return newPath;
            }
            else if (Math.abs(departV.x)>1500-a.mRobotRadius && Math.abs(robotOrientation)<Math.PI/4 && Math.abs(robotOrientation)>3*Math.PI/4)
            {
                ArrayList<Vec2> newPath = Astarfoulah(new Vec2((1500-a.mRobotRadius)*departV.x/Math.abs(departV.x), departV.y), arriveeV, robotOrientation);
                newPath.add(0, departV);
                return newPath;
            }

            // TODO Cas bien plus compliqués, où le robot est tangent au côté de la table sur lequel il est bloqué : Ici il faut gérer les possibles obstacles...

            else if (departV.y < a.mRobotRadius && Math.abs(robotOrientation)<Math.PI/4 && Math.abs(robotOrientation)>3*Math.PI/4)
            {

            }
        }

        if(Math.abs(arriveeV.x)>1500-a.mRobotRadius ||
                arriveeV.y<a.mRobotRadius ||
                arriveeV.y>2000-a.mRobotRadius)
        {
            log.debug("Je ne quitterai pas cette table => Point d'arrivée "+arriveeV);
            return new ArrayList();
        }

        for (int i=0 ; i<g.getlNoeuds().size() ; i++) //On vérifie que ça n'intersecte ni les obstacles circulaires ni ca
        {
            int j=0;
            boolean creerdep=true;
            boolean creerarr=true;
            int nombobst= a.getFixedObstacles().size();
            int nombobstRec=a.getRectangles().size();


            // on arrete les boucles si on voit que l'on ne doit créer ni un lien vers l'arrivée ni vers le début
            while ((creerdep||creerarr) && j<nombobst)  {
                if(a.getFixedObstacles().get(j).isInObstacle(departV))// si on est dans le depart on rappelle cette fonction depuis le noeud le plus proche
                {
                    log.debug("depart dans obstacle");
                    Vec2 w=a.getFixedObstacles().get(j).noeudProche(departV).position;
                     ArrayList<Vec2> aRenvoyer=Astarfoulah(w,arriveeV, robotOrientation);
                    aRenvoyer.add(0,departV);
                    return aRenvoyer;
                }
                if(a.getFixedObstacles().get(j).isInObstacle(arriveeV))
                {
                    creerarr=false;
                    log.debug("U  stupid or somethin'?");
                    throw new PointInObstacleException(arriveeV);
                }
                if(a.getFixedObstacles().get(j).isInObstacle(g.getlNoeuds().get(i).position))
                {
                    creerdep=false;
                    creerarr=false;
                }

                creerdep= creerdep && !(Geometry.intersects(new Segment(depart.position, g.getlNoeuds().get(i).position), new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius()))) ;
                creerarr= creerarr && !(Geometry.intersects(new Segment(arrivee.position,g.getlNoeuds().get(i).position),new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius())));
                j++;
                     }
            j=0;
            while ((creerdep||creerarr) && j<nombobstRec)  {
                if(a.getRectangles().get(j).isInObstacle(departV))// si on est dans le depart on rappelle cette fonction depuis le noeud le plus proche
                {
                    log.debug("depart dans obstacle");
                    Vec2 w=a.getRectangles().get(j).noeudProche(departV).position;
                    ArrayList<Vec2> aRenvoyer=Astarfoulah(w,arriveeV, robotOrientation);
                    aRenvoyer.add(0,departV);
                    return aRenvoyer;
                }
                if(a.getRectangles().get(j).isInObstacle(arriveeV))
                {
                    log.debug("U  stupid or somethin'?");
                    creerarr=false;
                    throw new PointInObstacleException(arriveeV);
                }
                if(a.getRectangles().get(j).isInObstacle(g.getlNoeuds().get(i).position))
                {
                    creerdep=false;
                    creerarr=false;
                }


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

    /**
     *  A star
     * @param depart Noeud de départ
     * @param arrivee Noeud d'arrivée
     * @param g graphe
     * @return Liste de vec2 des points de passage
     */
    public ArrayList<Vec2> Astarfoulah(Noeud depart, Noeud arrivee, Graphe g) throws PointInObstacleException{


        ArrayList<Vec2> chemin = new ArrayList<>();
        ArrayList<Noeud> closedlist = new ArrayList<>();

        //on stocke dedans les noeuds qu'on rencontre, comparaNoeud est l'heuristique
        PriorityQueue<Noeud> pq = new PriorityQueue(g.getlNoeuds().size(), new ComparaNoeud());

        depart.noeudPrecedent = null;
        closedlist.add(depart);
        depart.distheuristique(arrivee);
        depart.sommedepart=0;
        Noeud noeudCourant = depart;


        do {
            if(noeudCourant==null)
            {
                log.debug("noeudCourant est null");
                return null;
            }
            for (int i = 0 ; i < noeudCourant.lArretes.size() ; i++) // on parcourt les arrêtes de noeudCourant
            {
                noeudCourant.lArretes.get(i).arrivee.distheuristique(arrivee);//on actualise la distance à l'arrivée
                double b = noeudCourant.sommedepart + noeudCourant.lArretes.get(i).cout ;
                    if (!closedlist.contains(noeudCourant.lArretes.get(i).arrivee) && noeudCourant.lArretes.get(i).arrivee.sommedepart>b  ) // On vérifie qu'il n'est pas dans la closedList ou que on a déjà trouvé mieux
                {

                    //on modifie la sommedepuis le départ et le noeud précédent, puis on ajoute a la priorityQueue
                    noeudCourant.lArretes.get(i).arrivee.sommedepart = b;

                    noeudCourant.lArretes.get(i).arrivee.noeudPrecedent = noeudCourant;
                    pq.add(noeudCourant.lArretes.get(i).arrivee);


                }

            }



            if(pq.isEmpty()) break;

            noeudCourant = pq.poll();


            if (arrivee == noeudCourant) { // on reconstruit le chemin (limité à 100 au cas où il y a des boucles)
                while (noeudCourant != null && chemin.size() < 100) {
                    chemin.add(0, noeudCourant.position);
                    noeudCourant = noeudCourant.noeudPrecedent;
                }
                for (Noeud a:g.getlNoeuds()) //on réinitialise les noeuds pour le suivant
                {

                    a.sommedepart=100000000;
                    a.noeudPrecedent=null;

                }

                //on supprime les liens dans le sens retour liés au départ
                for (int j=0; j<depart.lArretes.size();j++) {
                    int p=depart.lArretes.get(j).arrivee.lArretes.size();
                    for (int i = 0; i < p; i++) {

                        if (depart.lArretes.get(j).arrivee!=depart &&  depart.lArretes.get(j).arrivee.lArretes.get(i).arrivee == depart) { // Il peut arriver que le noeud créer un lien vers lui même, on vérifie donc que ce n'est pas le cas
                            depart.lArretes.get(j).arrivee.lArretes.remove(depart.lArretes.get(j).arrivee.lArretes.get(i));
                            i--;
                            p--;

                        }


                    }
                }
                //on supprime les liens dans le sens retour liés à l'arrivée
                for (int j=0; j<arrivee.lArretes.size();j++) {
                    int p=arrivee.lArretes.get(j).arrivee.lArretes.size();
                    for (int i = 0; i < p; i++) {

                        if (arrivee.lArretes.get(j).arrivee!=arrivee && arrivee.lArretes.get(j).arrivee.lArretes.get(i).arrivee == arrivee) {
                            arrivee.lArretes.get(j).arrivee.lArretes.remove(arrivee.lArretes.get(j).arrivee.lArretes.get(i));
                            i--;
                            p--;

                        }

                    }
                }

                //On détache le noeud de départ et d'arrivée
                g.getlNoeuds().remove(depart);
                g.getlNoeuds().remove(arrivee);
                return chemin;
            }


            closedlist.add(noeudCourant);


        } while (!pq.isEmpty());




        log.debug("No path found");

        //on supprime les liens dans le sens retour liés au départ
        for (int j=0; j<depart.lArretes.size();j++) {
            int p=depart.lArretes.get(j).arrivee.lArretes.size();
            for (int i = 0; i < p; i++) {

                if (depart.lArretes.get(j).arrivee!=depart &&  depart.lArretes.get(j).arrivee.lArretes.get(i).arrivee == depart) { // Il peut arriver que le noeud créer un lien vers lui même, on vérifie donc que ce n'est pas le cas
                    depart.lArretes.get(j).arrivee.lArretes.remove(depart.lArretes.get(j).arrivee.lArretes.get(i));
                    i--;
                    p--;

                }


            }
        }
        //on supprime les liens dans le sens retour liés à l'arrivée
        for (int j=0; j<arrivee.lArretes.size();j++) {
            int p=arrivee.lArretes.get(j).arrivee.lArretes.size();
            for (int i = 0; i < p; i++) {

                if (arrivee.lArretes.get(j).arrivee!=arrivee && arrivee.lArretes.get(j).arrivee.lArretes.get(i).arrivee == arrivee) {
                    arrivee.lArretes.get(j).arrivee.lArretes.remove(arrivee.lArretes.get(j).arrivee.lArretes.get(i));
                    i--;
                    p--;

                }

            }
        }

        //On détache le noeud de départ et d'arrivée
        g.getlNoeuds().remove(depart);
        g.getlNoeuds().remove(arrivee);
        return new ArrayList<Vec2>();
    }

    // TODO : savoir à quoi sert cette méthode
public ArrayList<Vec2> ennemiDetecte(Vec2 posRobot,Vec2 cible, double robotOrientation)
{
    ObstacleManager a = this.table.getObstacleManager();

    //creerNoeudoptimal();
    ObstacleProximity ennemi=table.getObstacleManager().getMobileObstacles().get(0);

    double angle = Math.acos(ennemi.getRadius()/posRobot.distance(ennemi.getPosition()));
    Vec2 jeanMichel = new Vec2((int)(ennemi.getRadius()*Math.cos(angle)), (int) (ennemi.getRadius()*Math.asin(angle)));
    Vec2 m=ennemi.getPosition().plusNewVector(jeanMichel);

    double angleArrivee = Math.acos(ennemi.getRadius()/cible.distance(ennemi.getPosition()));
    Vec2 jeanMichelArrivee = new Vec2((int)(ennemi.getRadius()*Math.cos(angle)), (int) (ennemi.getRadius()*Math.asin(angle)));
    Vec2 m2=ennemi.getPosition().plusNewVector(jeanMichelArrivee);

    Vec2 k=Geometry.intersection(new Segment(posRobot,m),new Segment(cible,m2));

    boolean creedepart=true;
    boolean creearrivee=true;
    int j=0;
    int nbObstCirc=a.getFixedObstacles().size();
    int nbObstRect=a.getRectangles().size();
    Segment segdep=new Segment(posRobot,k);
    Segment segarr=new Segment(k,cible);
    while(creearrivee && creedepart && j< nbObstCirc)
    {
        creearrivee= !(a.getFixedObstacles().get(j).isInObstacle(cible));
        creedepart= !(a.getFixedObstacles().get(j).isInObstacle(k));

        creedepart= creedepart && !(Geometry.intersects(segdep, new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius()))) ;
        creearrivee= creearrivee && !(Geometry.intersects(segarr,new Circle(a.getFixedObstacles().get(j).getPosition(), a.getFixedObstacles().get(j).getRadius())));

        j++;
    }
j=0;
    while(creearrivee && creedepart && j< nbObstRect)
    {
        creearrivee= !(a.getRectangles().get(j).isInObstacle(cible));
        creedepart= !(a.getRectangles().get(j).isInObstacle(k));

        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(1).position));
        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(3).position));
        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(2).position));
        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(2).position,a.getRectangles().get(j).getlNoeud().get(3).position));
        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(2).position));
        creedepart= creedepart && !Geometry.intersects(segdep,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(3).position));

        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(1).position));
        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(3).position));
        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(2).position));
        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(2).position,a.getRectangles().get(j).getlNoeud().get(3).position));
        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(1).position,a.getRectangles().get(j).getlNoeud().get(2).position));
        creearrivee= creearrivee && !Geometry.intersects(segarr,new Segment(a.getRectangles().get(j).getlNoeud().get(0).position,a.getRectangles().get(j).getlNoeud().get(3).position));

        j++;
    }
if(creearrivee && creedepart)
{
    // vecteur OM

    // si aucun des deux pointsest pas dans un obstacle et que aucun des deux traits intersecte avec un obstacle
    // on le renvoie
    // Sinon on tente de décaler l'obstacle
    //sinon on refait le graphe
    ArrayList <Vec2> renvoi= new ArrayList<Vec2>();
    renvoi.add(k);
return  renvoi;}
    else
{
    this.graphe.initGraphe();
    try {
        return Astarfoulah(posRobot, cible, robotOrientation);
    }
    catch (PointInObstacleException e)
    {

    }

}
return new ArrayList<Vec2>();
}
    @Override
    public void updateConfig()
    {

    }
}