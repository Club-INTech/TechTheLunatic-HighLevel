package pathfinder;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Graphe {
    private ArrayList<Noeud> lNoeuds;



    public void Astarfoulah(Noeud depart, Noeud arrivee)
    {// on met les noeuds dans une priority queue
        // TODO créér la fonction de comparaison de noeuds/ la fonction d'update d'une arrête

        PriorityQueue <Noeud> pq= new PriorityQueue();
        pq.add(depart);
        Noeud noeudcourant=null;
        while(noeudcourant != arrivee)
        {
            noeudcourant=pq.poll();

            for (Arrete aux : noeudcourant.lArretes)
            {
                if(aux.isUpdated)
                {
                    if (!aux.bloquant && aux.isUpdated)
                        pq.add(aux.arrivee);
                }

                else
                {
                    aux.update();
                }


            }
        }


    }
}




