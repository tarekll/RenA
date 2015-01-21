package arabic.ner;

import java.io.Serializable;

/**
 * s0ul on 1/12/15.
 */
public class Tuple<K, T> implements Serializable {
    protected K first;
    protected T second;

    public Tuple(K first, T second) {
        this.first = first;
        this.second = second;
    }

    public K first() {
        return first;
    }
    public T second() {
        return second;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
        if (second != null ? !second.equals(tuple.second) : tuple.second != null) return false;

        return true;
    }

    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    public String toString() {
        return String.format("(%s, %s)", first, second);
    }
}
