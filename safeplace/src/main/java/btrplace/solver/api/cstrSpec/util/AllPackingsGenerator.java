package btrplace.solver.api.cstrSpec.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class AllPackingsGenerator<T> implements Iterator<Set<Set<T>>>, Iterable<Set<Set<T>>> {

    private T[][] doms;

    private int[] indexes;

    private int nbStates;

    private int k;

    private T[] elem;

    private int nbPos = 0;
    private Class<T> cl;

    public AllPackingsGenerator(Class<T> cl, Collection<T> domains) {
        elem = (T[]) domains.toArray();
        doms = (T[][]) new Object[domains.size()][];
        indexes = new int[domains.size()];
        int i = 0;
        nbStates = 1;
        this.cl = cl;
        nbPos = domains.size() + 1;
        for (T v : domains) {
            indexes[i] = 0;
            nbStates *= nbPos;
        }
    }

    public void reset() {
        k = 0;
    }

    public int count() {
        return nbStates;
    }

    public int done() {
        return k;
    }

    @Override
    public boolean hasNext() {
        return k < nbStates;
    }

    @Override
    public Set<Set<T>> next() {
        T[] tuple = (T[]) Array.newInstance(cl, indexes.length);
        for (int x = 0; x < doms.length; x++) {
            indexes[x]++;
            if (indexes[x] < nbPos) {
                break;
            }
            indexes[x] = 0;
        }
        k++;
        //System.out.println(toSet());
        return toSet();
    }

    private Set<Set<T>> toSet() {
        List<Set<T>> l = new ArrayList<>(nbPos);
        for (int i = 0; i < elem.length; i++) {
            l.add(new HashSet<T>());
        }
        for (int i = 0; i < indexes.length; i++) {
            int v = indexes[i];
            if (v < nbPos - 1) {
                l.get(v).add(elem[i]);
            }
        }
        Set<Set<T>> s = new HashSet<>();
        for (Set<T> x : l) {
            if (!x.isEmpty()) {
                s.add(x);
            }
        }
        return s;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Set<Set<T>>> iterator() {
        return this;
    }
}
