package utils;


import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formatter;

import static com.google.common.base.Preconditions.checkNotNull;

// ------------------------------------------------------------
//                           CONSOLE
// ------------------------------------------------------------
// Abstracts the standard input, output, and error streams for
// exa applications.
//
public enum Console {
    ;

    static final OutWriter out = new OutWriter(new PrintWriter(System.out, true));

    static final ErrWriter err = new ErrWriter(new PrintWriter(System.err, true));

    // --------------------------------------------------

    public static OutWriter out() { return out; }

    public static ErrWriter err() { return err; }

    // --------------------------------------------------

    public static final class OutWriter {

        private final Formatter formatter = new Formatter();

        private final StringBuilder sb = new StringBuilder();

        private final PrintWriter writer;

        private boolean useColors = true;

        private OutWriter(PrintWriter _writer) { writer = checkNotNull(_writer); }

        public OutWriter print(int x)     { apply(false, Integer.toString(x)); return this; }

        public OutWriter print(char x)    { apply(false, Character.toString(x)); return this; }

        public OutWriter print(long x)    { apply(false, Long.toString(x)); return this; }

        public OutWriter print(float x)   { apply(false, Float.toString(x)); return this; }

        public OutWriter print(double x)  { apply(false, Double.toString(x)); return this; }

        public OutWriter print(Object x)  { apply(false, x.toString()); return this; }

        public OutWriter print(char x[])  { apply(false, Arrays.toString(x)); return this; }

        public OutWriter print(boolean x) { apply(false, Boolean.toString(x)); return this; }

        public OutWriter print(String x)  { apply(false, x); return this; }

        public void   println(int x)      { apply(true, Integer.toString(x)); }

        public void   println(char x)     { apply(true, Character.toString(x)); }

        public void   println(long x)     { apply(true, Long.toString(x)); }

        public void   println(float x)    { apply(true, Float.toString(x)); }

        public void   println(double x)   { apply(true, Double.toString(x)); }

        public void   println(Object x)   { apply(true, x.toString()); }

        public void   println(char x[])   { apply(true, Arrays.toString(x)); }

        public void   println(boolean x)  { apply(true, Boolean.toString(x)); }

        public void   println(String x)   { apply(true, x); }

        public void   println() { apply(true, ""); }

        private void apply(boolean newLine, String s) {
            if (useColors) sb.append(s);
            if (newLine)
                writer.println(sb.toString());
            else {
                writer.print(sb.toString());
                writer.flush();
            }
            sb.setLength(0);
        }
    }

    public static final class ErrWriter {
        private final PrintWriter pw;
        public ErrWriter(PrintWriter _pw)  { pw = _pw; }
        public void printErr(String err)   { pw.println(err); }
        public void printErr(Throwable th) { pw.println(th.getMessage()); }
        public void printErr(String err, Throwable th) {
            pw.println(err);
            pw.println();
            pw.println(th.getMessage());
        }
    }
}