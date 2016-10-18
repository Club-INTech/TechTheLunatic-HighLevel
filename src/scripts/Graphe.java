package scripts;
import scripts.Noeud;
import java.util.ArrayList;

public class Graphe
{
    private ArrayList<Noeud> map;
    private ArrayList <ArrayList<Float>> distances;

    // calcule les distances euclidiennes explose, puis renvoie la colonne ajoutée. ATTENTION il s'agit d'une matrice triangulaire
    public ArrayList<Float> distance (Noeud neuneuf)
    {
        ArrayList dist= new ArrayList();
        int i=neuneuf.indice;
        for (int j=0;j<30;j++)
        {
            dist.add( Math.sqrt(Math.pow(neuneuf.x-map.get(j).x,2)+Math.pow(neuneuf.y - map.get(j).y,2)));

        }
        this.distances.add(dist);
        return dist;
    }
    //Explose, puis ajoute le vecteur des distances à la matrice
    public void distajout (Noeud neuf)
    {


        }

    }



