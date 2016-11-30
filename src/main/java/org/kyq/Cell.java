package org.kyq;

/**
 * Created by jianghan on 2016/11/18.
 */
public class Cell<K extends Comparable<? super K>, V extends Comparable<? super V>> {

    public static final int KEY_NEXT = 0;
    public static final int VALUE_NEXT = 1;
    public K key;
    public V val;
    public Cell[][] next = null;

    public Cell(int level, K key, V val) {
        this.next = new Cell[2][level];
        this.key = key;
        this.val = val;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[key=").append(key);
        sb.append(",value=").append(val);
        sb.append(",knext=");
        for (Cell c : next[KEY_NEXT]) {
            if (c != null) {
                sb.append(c.key).append(",");
            } else {
                sb.append("null,");
            }
        }
        sb.append("vnext=");
        for (Cell c : next[VALUE_NEXT]) {
            if (c != null) {
                sb.append(c.key).append(",");
            } else {
                sb.append("null,");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}

