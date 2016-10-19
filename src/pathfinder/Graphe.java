package pathfinder;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Graphe {
    private ArrayList<Noeud> map;
    private ArrayList<ArrayList<Double>> distances;

    // calcule les distances euclidiennes explose, puis renvoie la colonne ajoutée. ATTENTION il s'agit d'une matrice triangulaire
    public ArrayList<Double> distance(Noeud neuneuf) {
        ArrayList dist = new ArrayList();
        int i = neuneuf.indice;
        for (int j = 0; j < 30; j++) {
            dist.add(neuneuf.squaredDistance(map.get(j)));

        }
        this.distances.add(dist);
        return dist;
    }
    /*
    on va stocker les résultats de la recherche dans une file de priorité, ou un tas minimal lors de A*
    On fait une matrice initialisée a infini(MAXINT)
    dans l'ordre logique le premier noeud est la position du robot lorsqu'il détecte, le second l'arrivée
    */
    public void Astarfoulah(Noeud arrivee)
    {// on met les noeud plus celui du calcul dans l'ordre de leur distance carrée a l'arrivée
       PriorityQueue pq= new PriorityQueue();// a intégrer a l'objet graphe plutot

    }
}




