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

import smartMath.Vec2;
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
    public boolean isBloquant(ObstacleCircular obstacle) // calcul de l'intersection de la ligne et des objets
    {



            Vec2 da= new Vec2(depart.position.x-arrivee.position.x,depart.position.y-arrivee.position.y);
            Vec2 dc= new Vec2(depart.position.x-obstacle.getPosition().x,depart.position.y-obstacle.getPosition().y);
            double distcentre= (double)(da.dot(dc))/dc.length();
            //... découverte de la fonction isinobstacle
            if (Math.abs(distcentre)< obstacle.getRadius())
            {
                // on le détache de ses noeuds
                this.depart.lArretes.remove(this);

                this.arrivee.lArretes.remove(this);
                return true;
            }
        return false;

    }
        /*
        si on est update on renvoit bloquant
        sinon on vérifie qu'on ne passe pas a distance radius de r pour les cercle
        pour les recangles min R pour un dégrossisage), sinon l'algo précis de traçage de ligne
        */


    public boolean isBloquant(ObstacleRectangular obstacle)
    {

        if(obstacle.getlNoeud().size()>0)
        {
            Vec2 ab=this.depart.position.minusNewVector(this.arrivee.position);
            Vec2 ac=this.depart.position.minusNewVector(obstacle.getlNoeud().get(0).position);
            Vec2 ad=this.depart.position.minusNewVector(obstacle.getlNoeud().get(1).position);
            Vec2 ae=this.depart.position.minusNewVector(obstacle.getlNoeud().get(2).position);
            Vec2 af=this.depart.position.minusNewVector(obstacle.getlNoeud().get(3).position);
            if(!((ab.dot(ac) * ab.dot(ad)>=0) && (ab.dot(ad)* ab.dot(ae)>=0) && (ab.dot(ae)* ab.dot(af)>=0) ))
            {

                this.depart.lArretes.remove(this.depart);
                this.arrivee.lArretes.remove(this.arrivee);
                return true;
            }
        }
        return false;
    }

    /**
     * actualise le cout de l'arrête
     */
    public void calcCout()
    {
        this.cout=new Vec2 (this.depart.position.x-this.arrivee.position.x,this.depart.position.y-this.arrivee.position.y).length();
    }
}
