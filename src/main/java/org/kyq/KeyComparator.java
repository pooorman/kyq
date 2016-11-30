package org.kyq;

import java.util.Comparator;

/**
 * Created by jianghan on 2016/11/25.
 */
public class KeyComparator<K extends Comparable<K>, V extends Comparable<V>> implements Comparator<Cell<K, V>> {

    @Override
    public int compare(Cell<K, V> o1, Cell<K, V> o2) {
        K s1 = o1.key;
        K s2 = o2.key;
        return s1.compareTo(s2);
    }

}
