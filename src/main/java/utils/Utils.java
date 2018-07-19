package utils;

public enum Utils {
    ;
    public static final class TypeCast {
        @SuppressWarnings("unchecked")
        public static <T> Class<T> Of(Class<?> aClass) {
            return (Class<T>) aClass;
        }
    }

    // --------------------------------------------------
    //  VARIABLE INTERFACE.
    // --------------------------------------------------
    public interface PrimitiveVar<T> extends Comparable<T> {}

    // --------------------------------------------------
    //  INTEGER VAR.
    // --------------------------------------------------
    public static final class IntVar implements PrimitiveVar<Integer> {
        public int val;

        // CONSTRUCTOR.
        public IntVar() {}
        public IntVar(int v) { val = v; }

        // GETTER / SETTER
        public int get() { return val; }
        public IntVar set(int v) { val = v; return this; }
        public int inc(int n) { val += n; return val; }

        // CONVERSION.
        public int      intValue()      { return val; }
        public long     longValue()     { return val; }
        public float    floatValue()    { return val; }
        public double   doubleValue()   { return val; }

        public int hashCode() {
            return val;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntVar)) return false;
            IntVar i32Var = (IntVar) o;
            return val == i32Var.val;
        }

        public int compareTo(Integer o) {
            return (val < o) ? -1 : ((val == o) ? 0 : 1);
        }

        public String toString() { return Integer.toString(val); }

        // FACTORY METHODS.
        public static IntVar of() { return of(0); }
        public static IntVar of(int val) { return new IntVar(val); }
        public static IntVar of(String s) { return new IntVar(Integer.parseInt(s)); }
    }
}
