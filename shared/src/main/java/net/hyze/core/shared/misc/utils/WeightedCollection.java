package net.hyze.core.shared.misc.utils;

import com.google.common.collect.Maps;

import java.util.*;

public class WeightedCollection<E> extends AbstractList<WeightedCollection<E>.WeightedEntry> {

    private final NavigableMap<Double, WeightedEntry> navigable = new TreeMap<>();
    private final Map<Integer, WeightedEntry> indexMap = Maps.newHashMap();

    private int cursor = 0;
    private final Random random;
    private double total = 0;

    public WeightedCollection() {
        this(new Random());
    }

    @Override
    public int size() {
        return cursor;
    }

    @Override
    public WeightedEntry get(int index) {
        if (index >= 0 && index < this.cursor) {
            return indexMap.get(index);
        }

        throw new IndexOutOfBoundsException("Index: " + index);
    }

    public WeightedCollection(Random random) {
        this.random = random;
    }

    public void add(E object, double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("The weight must be greater than zero");
        }

        total += weight;

        WeightedEntry entry = new WeightedEntry(object, weight);

        navigable.put(total, entry);
        indexMap.put(cursor++, entry);
    }

    public WeightedEntry next() {
        double value = random.nextDouble() * total;
        return navigable.higherEntry(value).getValue();
    }

    public class WeightedEntry {

        private final E object;
        private final Double weight;

        private WeightedEntry(E e, Double weight) {
            this.object = e;
            this.weight = weight;
        }

        public Double getChance() {
            return this.weight * 100 / total;
        }

        public E getObject() {
            return this.object;
        }

        public Double getWeight() {
            return this.weight;
        }

    }
}
