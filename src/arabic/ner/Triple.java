package arabic.ner;

import java.io.Serializable;

/**
 * s0ul on 1/12/15.
 */
public class Triple<K, T, Z> implements Serializable {
    public K first;
    public T second;
    public Z third;

    public Triple(K first, T second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triple triple = (Triple) o;

        if (first != null ? !first.equals(triple.first) : triple.first != null) return false;
        if (second != null ? !second.equals(triple.second) : triple.second != null) return false;
        if (third != null ? !third.equals(triple.third) : triple.third != null) return false;

        return true;
    }

    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }

    public String toString() {
        return String.format("(%s, %s, %s)", first, second, third);
    }
}
