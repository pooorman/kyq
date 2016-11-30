package org.kyq;

import java.util.*;

/**
 * Created by jianghan on 2016/11/17.
 */
public class SkipListMap<K extends Comparable<? super K>, V extends Comparable<? super V>> {

    private Cell<K, V>[] head = null;
    private int level;
    private int length;
    private ByteChunk chunk;

    private int calcLevel(int s) {
        int l = 1;
        while ((1 << l) < s) l++;
        return l;
    }

    private void init(Map<String, Double> map) {
        List<Cell> data = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            data.add(new Cell(level, entry.getKey(), entry.getValue()));
        }
        Collections.sort(data, new ValueComparator());
        init(data, Cell.VALUE_NEXT);
        Collections.sort(data, new KeyComparator());
        init(data, Cell.KEY_NEXT);
    }

    private void init(List<Cell> data, int t) {
        Cell head = data.get(0);
        Cell[] pre = new Cell[head.next[t].length];
        int[] cnt = new int[pre.length];
        Arrays.fill(pre, head);
        Arrays.fill(cnt, 0);
        for (int i = 1; i < data.size(); ++i) {
            Cell c = data.get(i);
            for (int j = 0; j < level; ++j) {
                ++cnt[j];
                if (cnt[j] % (1 << j) == 0) {
                    pre[j].next[t][j] = c;
                    pre[j] = c;
                    cnt[j] = 0;
                }
            }
        }
        this.head[t] = head;
    }

    public SkipListMap(Map<String, Double> map) {
        if (map == null) {
            throw new IllegalArgumentException("Initialize data is null.");
        }
        length = map.size();
        level = calcLevel(length / 4) + 1;
        head = new Cell[2];
        init(map);
    }

    public void add(K key, V value) {
        Cell p = find(key, head[Cell.KEY_NEXT]);
        if (p.key.equals(key)) {
            return;
        } else {
            Cell np = new Cell(1, key, value);
        }
    }

    public void prettyPrint() {
        Cell point = head[Cell.VALUE_NEXT];
        for (int i = 0; i < length(); i++) {
            System.out.println(point);
            point = point.next[Cell.VALUE_NEXT][0];
        }
    }

    public void prettyPrint(int t) {
        Cell point = head[t];
        for (int i = 0; i < length(); i++) {
            System.out.println(point);
            point = point.next[t][0];
        }
    }

    public int length() {
        return this.length;
    }

    public V opt(K key, V defaultValue) {
        Cell<K, V> p = find(key, head[Cell.KEY_NEXT]);
        if (p == null || !p.key.equals(key)) {
            return defaultValue;
        }
        return p.val;
    }

    private Cell<K, V> find(K key, Cell<K, V> point) {
        if (point == null) {
            return null;
        }
        if (key.equals(point.key)) {
            return point;
        } else {
            int idx = -1;
            for (int i = 0; i < point.next.length; ++i) {
                if (point.next[Cell.KEY_NEXT][i].key.compareTo(key) <= 0) {
                    idx = i;
                } else {
                    break;
                }
            }
            if (idx == -1) {
                return point;
            } else {
                return find(key, point.next[Cell.KEY_NEXT][idx]);
            }
        }
    }

    public ByteChunk write() {
        int[] index = new int[length()];
        ByteChunk chunk = new ByteChunk();
        Cell point = head[Cell.KEY_NEXT];
        return chunk;
    }

}
