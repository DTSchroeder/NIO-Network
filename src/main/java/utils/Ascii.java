package utils;

import org.apache.commons.lang3.StringUtils;
import utils.Utils.IntVar;
import utils.Utils.TypeCast;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Formatter;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static utils.Ascii.Ansi.*;

public enum Ascii {
    ;

    public static abstract class Ansi<T extends Ansi<T>> {
        protected final T self = TypeCast.<T> Of(getClass()).cast(this);

        public static final String CLEAR = "\033[H\033[2J";

        public enum Color implements Function<String, String> {
            BLACK("30"), RED("31"),    GREEN("32"), YELLOW("33"),
            BLUE("34"),  PURPLE("35"), CYAN("36"),  GREY("37");
            final String code;
            static boolean isActive = true;
            Color(String _code) { code = _code; }
            public String apply(String s) {
                if (!isActive) return s;
                return "\u001b[" + code + ";0m"
                        + s + "\u001b[0m";
            }

            public static void setActive(boolean val) {
                isActive = val;
            }
        }

        public enum Bold implements Function<String, String> {
            ON("1"), OFF("22");
            final String mode;
            Bold(String _mode) { mode = _mode; }
            public String apply(String s) {
                return "\u001b[" + mode + ";0m"
                        + s + "\u001b[0m";
            }
        }

        public enum Line implements Function<String, String> {
            ON("4"), OFF("24");
            final String mode;
            Line(String _mode) { mode = _mode; }
            public String apply(String s) {
                return "\u001b[" + mode + ";0m"
                        + s + "\u001b[0m";
            }
        }

        protected Color color = Color.BLACK;
        protected Line  line  = Line.OFF;
        protected Bold  bold  = Bold.OFF;


        public T red()      { color = Color.RED;    return self; }
        public T blue()     { color = Color.BLUE;   return self; }
        public T cyan()     { color = Color.CYAN;   return self; }
        public T grey()     { color = Color.GREY;   return self; }
        public T black()    { color = Color.BLACK;  return self; }
        public T green()    { color = Color.GREEN;  return self; }
        public T yellow()   { color = Color.YELLOW; return self; }
        public T purple()   { color = Color.PURPLE; return self; }

        public T boldOff()  { bold = Bold.OFF; return self; }
        public T lineOff()  { line = Line.OFF; return self; }
        public T line()     { line = Line.ON;  return self; }
        public T bold()     { bold = Bold.ON;  return self; }

        protected String apply(String s) {
            return bold.compose(line.compose(color))
                    .apply(s);
        }
    }

    public static String bold(String s)     { return Color.BLACK.apply(s); }
    public static String line(String s)     { return Color.RED.apply(s); }
    public static String red(String s)      { return Color.RED.apply(s); }
    public static String blue(String s)     { return Color.BLUE.apply(s); }
    public static String green(String s)    { return Color.GREEN.apply(s); }
    public static String black(String s)    { return Color.BLACK.apply(s); }
    public static String yellow(String s)   { return Color.YELLOW.apply(s); }
    public static String purple(String s)   { return Color.PURPLE.apply(s); }
    public static String cyan(String s)     { return Color.CYAN.apply(s); }
    public static String gray(String s)     { return Color.GREY.apply(s); }


    public static final class AsciiCanvas {
        private DecimalFormat format;
        private final char[][] pane;
        private final int w, h;

        AsciiCanvas(long _w, long _h) {
            pane = new char[h = (int) _h][w = (int) _w];
            drawSolid(0, 0, w, h, ' ');
            numbers(1, 2);
        }

        public void drawBox(long xp, long yp, long xw, long yh, int sty) {
            draw(xp, yp, box(sty, 0));
            draw(xp + xw - 1, yp, box(sty, 1));
            draw(xp, yp + yh - 1, box(sty, 2));
            draw(xp + xw - 1, yp + yh - 1, box(sty, 3));
            for (long i = xp + 1; i < xp + xw - 1; ++i)
                draw(i, yp, box(sty, 4));
            for (long i = xp + 1; i < xp + xw - 1; ++i)
                draw(i, yp + yh - 1, box(sty, 4));
            for (long i = yp + 1; i < yp + yh - 1; ++i)
                draw(xp, i, box(sty, 5));
            for (long i = yp + 1; i < yp + yh - 1; ++i)
                draw(xp + xw - 1, i, box(sty, 5));
        }

        /*public void drawBox(Domain dom, int sty) {
            int xp = (int) dom.axes()[0].lo();
            int yp = (int) dom.axes()[1].lo();
            int xw = (int) dom.shape()[0];
            int yh = (int) dom.shape()[1];
            drawBox(xp, yp, xw, yh, sty);
        }*/

        public void drawSolid(long xp, long yp, long xw, long yh, char c) {
            for (long i = xp; i < xp + xw; ++i) {
                for (long j = yp; j < yp + yh; ++j) {
                    draw(i, j, "" + c);
                }
            }
        }

        /*public AsciiCanvas drawSolid(Domain dom, char c) {
            int xp = (int) dom.axes()[0].lo();
            int yp = (int) dom.axes()[1].lo();
            int xw = (int) dom.shape()[0];
            int yh = (int) dom.shape()[1];
            drawSolid(xp, yp, xw, yh, c);
            return this;
        }*/

        public AsciiCanvas draw(long xp, long yp, char c) {
            return draw(xp, yp, c + "");
        }

        public AsciiCanvas draw(long xp, long yp, String s) {
            if (yp >= 0 || yp < h) {
                char[] cs = s.toCharArray();
                long n = cs.length;
                long e = xp + n;
                n = e > w ? (e - w + 1) : n;
                System.arraycopy(cs, 0, pane[(int) yp], (int) xp, (int) n);
            }
            return this;
        }

        public AsciiCanvas put(String s) {
            int x = 0, y = 0;
            for (char c : s.toCharArray()) {
                pane[y][x++] = c;
                if (c == '\n') {
                    ++y;
                    x = 0;
                }
            }
            return this;
        }

        public AsciiCanvas draw(long xp, long yp, Number v) {
            draw(xp, yp, format.format(v));
            return this;
        }

        /*public AsciiCanvas drawCenter(Domain dom, String s) {
            long xp = (dom.axes()[0].lo + ((dom.shape()[0] / 2) - s.length() / 2));
            long yp = (dom.axes()[1].lo + (dom.shape()[1] / 2));
            return draw(xp, yp, s);
        }

        public AsciiCanvas drawHCenter(Domain dom, long yp, String s) {
            long xp = (int) (dom.axes()[0].lo + ((dom.shape()[0] / 2) - s.length() / 2));
            return draw(xp, yp, s);
        }

        public AsciiCanvas drawVCenter(Domain dom, long xp, String s) {
            int yp = (int) (dom.axes()[1].lo + (dom.shape()[1] / 2));
            return draw(xp, yp, s);
        }*/

        public int numbers(int ints, int fracs) {
            String bn = StringUtils.repeat('0', ints);
            String en = StringUtils.repeat('0', fracs);
            DecimalFormatSymbols ds = new DecimalFormatSymbols(Locale.getDefault());
            ds.setDecimalSeparator('.');
            ds.setGroupingSeparator(' ');
            format = new DecimalFormat(bn + "." + en, ds);
            return ints + fracs + 1;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < h; ++i) {
                sb.append(pane[i]).append('\n');
            }
            return sb.toString();
        }

        // FACTORY METHOD.
        public static AsciiCanvas of(long w, long h) {
            return new AsciiCanvas(w, h);
        }

        /*public static AsciiCanvas of(Domain dom) {
            int w = (int) dom.shape()[0];
            int h = (int) dom.shape()[1];
            return new AsciiCanvas(w, h);
        }*/

        // INTERNALS.
        static String box(int sty, int id) {
            return BOX_STYLE[sty][id];
        }
    }

    public static final class TextTable {

        public static final class Column implements Function<Object, String[]> {
            final Class<?> type;
            final IntVar width;
            final String name;
            public Column(Class<?> _type, String _name) {
                width = new IntVar();
                type = _type;
                name = _name;
            }
            public String[] apply(Object data) {
                int len = Array.getLength(data);
                String[] items = new String[len];
                for (int col = 0; col < len; ++col) {
                    items[col] = Array.get(data, col).toString();
                    int strLen = items[col].length();
                    if (strLen > width.val) {
                        width.val = strLen;
                    }
                }
                return items;
            }
            public static <T0> Column of(Class<T0> type, String name) {
                return new Column(type, name);
            }
        }

        final Column[] columns;
        final StringBuilder table;
        final boolean index;

        TextTable(boolean _index, Column[] cols) {
            columns = checkNotNull(cols);
            table = new StringBuilder();
            index = _index;
        }

        public String apply(Collection<?>... lists) {
            Object[][] data = new Object[lists.length][];
            for (int i = 0; i < data.length; ++i)
                data[i] = lists[i].toArray();
            return apply((Object[]) data);
        }

        public String apply(Object... data) {
            checkState(data != null && data.length > 0);
            int cols = data.length;
            int rows = Array.getLength(data[0]) + 1;
            String[][] rowTable = new String[rows][cols];
            for (int c = 0; c < cols; ++c) {
                rowTable[0][c] = '[' + columns[c].name + ']';
                columns[c].width.val = rowTable[0][c].length();
            }
            for (int c = 0; c < cols; ++c) {
                String[] col = columns[c].apply(data[c]);
                for (int r = 0; r < rows - 1; ++r) {
                    if (col != null) {
                        rowTable[1 + r][c] = col[r];
                    }
                }
            }
            Formatter fmt = new Formatter();
            StringBuilder spec = new StringBuilder();
            for (int c = 0; c < cols; ++c) {
                int wd = columns[c].width.val;
                spec.append("%-").append(wd).append("s ");
            }
            Object[] rowData = rowTable[0];
            String rowSpec = spec.toString();
            String row = String.format(rowSpec, rowData);

            if (index) {
                table.append("[IDX] ");
            }

            table.append(row).append('\n');
            for (int r = 1; r < rows; ++r) {
                rowData = rowTable[r];
                row = String.format(rowSpec, rowData);
                if (index) {
                    table.append(" ").append(r - 1).append(":   ");
                }
                table.append(row).append('\n');
            }
            return table.toString();
        }

        public String toString() {
            return table.toString();
        }

        public static TextTable of(Column...columns) {
            return new TextTable(false, columns);
        }
        public static TextTable indexedOf(Column...columns) {
            return new TextTable(true, columns);
        }
    }


    public static String toBoxString(String text) {
        int len = 80;
        String gap = ((text.length() % 2 == 0) ? "" : " ");
        String mrg = Stream.generate(() -> " ").limit((len - text.length()) / 2).reduce((a, b) -> a + b).orElse("");
        String tbl = Stream.generate(() -> "─").limit(len).reduce((a, b) -> a + b).orElse("");
        return "\n┌" + tbl + "┐\n" + "│" + mrg + text + mrg + gap + "│\n" + "└" + tbl + "┘\n";
    }

    private final static String[][] BOX_STYLE = {
            {"┌","┐","└","┘","─","│"},
            {"┌","┐","└","┘"," ","│"},
            {"╔","╗","╚","╝","═","║"},
            {"╭","╮","╰","╯","─","│"},
            {"╭","╮","╰","╯"," ","│"}
    };
}
