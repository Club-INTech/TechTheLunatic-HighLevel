package pathfinder;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;


public class Graphe {
    private int noeudsurtable;

    private ArrayList<Noeud> lNoeuds;

    public void setNoeudsurtable(int noeudsurtable) {
        this.noeudsurtable = noeudsurtable;
    }

    public int getNoeudsurtable() {
        return noeudsurtable;
    }

    public ArrayList<Noeud> getlNoeuds() {
        return lNoeuds;
    }

    public ArrayList <Noeud> Astarfoulah(Noeud depart, Noeud arrivee)
    {// on met les noeuds dans une priority queue
        // TODO la fonction d'update d'une arrête; la création du graphe; faire n noeuds autours des obstacles pour les cercles

        ArrayList <Noeud> chemin= new ArrayList() ;


        PriorityQueue <Noeud> pq = new PriorityQueue( noeudsurtable, new ComparaNoeud());
        pq.add(depart);
        Noeud noeudCourant=null;
        Noeud noeudPrecedent=null;
        while(noeudCourant != arrivee)
        {

            noeudCourant=pq.poll();
            chemin.add(noeudCourant);
            noeudCourant.distheuristique(arrivee);
            Arrete arr = new Arrete(noeudCourant,noeudCourant);

           noeudCourant.sommedepart=noeudPrecedent.sommedepart + Nazareth(0,noeudPrecedent,noeudCourant).cout;

            for (Arrete aux : noeudCourant.lArretes)
            {
                if(aux.isUpdated)
                {

                    pq.add(aux.arrivee);
                }

                else
                {
                    aux.update();
                }


            }
            noeudPrecedent=noeudCourant;
        }

    return chemin;
    }

    Arrete Nazareth (int i,Noeud t1, Noeud t2) //retrouve le noeud t2 dans la liste de t1
    {
        if(t1.lArretes.get(i).arrivee==t2)
        {
            return t1.lArretes.get(i);
        }
        else
        { return Nazareth(i+1,t1,t2);
        }

    }
}




