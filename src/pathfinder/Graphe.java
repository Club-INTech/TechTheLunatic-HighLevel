package pathfinder;
import smartMath.Vec2;
import table.obstacles.ObstacleCircular;

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
        // TODO la fonction d'update d'une arrête; la création du graphe;

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

                    pq.add(aux.arrivee);




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
    public Graphe() //le graphe initial
    {
        Noeud n1=new Noeud(this,new Vec2(0,0));
        this.lNoeuds.add(n1);

    }
    public Graphe(Vec2 position,ObstacleCircular detecte, Noeud ini,Noeud fin)
    {


        for (Arrete x:ini.lArretes)
        {
            x.calcCout();
        }
        //on ajoute les noeuds dans le sous graphe
        this.lNoeuds.add(ini);
        this.lNoeuds.add(fin);
        detecte.fabriqueNoeud(this,8,3);
        //on revoit nos indices pour des raisons cosmétiques pour l'instant
        this.reorder();


    }
    public void reorder()
    {
        for (int i=0;i<lNoeuds.size();i++)
        {
            this.lNoeuds.get(i).indice=i;
            this.noeudsurtable=this.getlNoeuds().size();
        }
    }

    // Pour créer des sous graphes
    public ArrayList<Noeud> actGraphe(Vec2 position, ObstacleCircular detecte,ArrayList<Noeud> chemin,int indice)
    {
        //on veut une copie de ini et de fin
        Noeud ini=new Noeud(chemin.get(indice));
        Noeud fin=new Noeud(chemin.get(indice+1));


        ini.position=position;
        ini.update(detecte);// on supprime les liens inutiles et actualise les valeurs

        ini.lArretes.add(new Arrete(ini,fin));
        // on crée un nouveau graphe, en recopiant les arêtes du noeud précédent, actualisant les valeurs
        Graphe sousg=new Graphe(position,detecte,ini,fin);
        return sousg.Astarfoulah(ini,fin);


    }
}




