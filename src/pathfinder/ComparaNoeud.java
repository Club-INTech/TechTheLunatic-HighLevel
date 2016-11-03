package pathfinder;

import java.util.Comparator;

/**
 * Created by shininisan on 03/11/16.
 */

public class ComparaNoeud implements Comparator<Noeud> {


    @Override
    public int compare(Noeud noeud, Noeud t1) {
        return (int)(noeud.sommedepart+noeud.distarrivee - t1.sommedepart+t1.distarrivee);
    }

    @Override
    public boolean equals(Object o) {
        return this==o;
    }
}
