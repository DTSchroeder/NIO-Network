package utils;

import utils.Ascii.Ansi.Color;

import java.io.PrintWriter;
import java.io.StringWriter;

// ------------------------------------------------------------
//                        KERNEL LOGGER
// ------------------------------------------------------------
// Low overhead, lightweight logging system.
//
public enum Logger {
    ;
    private static KernelLog logger = KernelLog.OUT;
    private static LogWriter logWriter = StdOutLogger.get;
    private static boolean isColored = false;

    public static void color(boolean val) { isColored = val; }
    public static boolean isColored() { return isColored; }

    // ERROR.
    public static void error(String msg) { error(msg, null); }
    public static void error(String msg, Throwable ex) { KernelLog.ERROR.write(msg, ex); }
    public static void error(Throwable ex) { KernelLog.ERROR.write(ex.getMessage(), null); }

    // WARN.
    public static void warn(String msg) { warn(msg, null); }
    public static void warn(Object msg) { warn(String.valueOf(msg), null); }
    public static void warn(String msg, Throwable ex) { KernelLog.WARN.write(msg, ex); }

    // INFO.
    public static void info(String msg) { info(msg, null); }
    public static void info(Object msg) { info(String.valueOf(msg), null); }
    public static void info(String msg, Throwable ex) { KernelLog.INFO.write(msg, ex); }

    // STD-OUT.
    public static void out(String msg) { out(msg, null); }
    public static void out(Object msg) { out(String.valueOf(msg), null); }
    public static void out(Color color, String msg) { out(isColored ? color.apply(msg) : msg, null); }
    public static void out(String msg, Throwable ex) { KernelLog.OUT.write(msg, ex); }

    // DEBUG.
    public static void debug(String msg) { debug(msg, null); }
    public static void debug(Object msg) { debug(String.valueOf(msg), null); }
    public static void debug(String msg, Throwable ex) { KernelLog.DEBUG.write(msg, ex); }

    // TRACE.
    public static void trace(String msg) { trace(msg, null); }
    public static void trace(Object msg) { trace(String.valueOf(msg), null); }
    public static void trace(String msg, Throwable ex) { KernelLog.TRACE.write(msg, ex); }

    // -- KERNEL LOG-PROVIDER ---------------------------
    //
    enum KernelLog implements LogProvider<KernelLog> {
        // No logging at all.
        NONE {
            public void write(String message, Throwable th) {}
        },
        // Critical errors.
        // The application may no longer work correctly.
        ERROR {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(Color.RED, message, th);
                }
            }
        },
        // Important warnings.
        // The application will continue to work correctly.
        WARN {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(Color.PURPLE, message, th);
                }
            }
        },
        // Informative messages.
        // Typically used for deployment.
        INFO {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(Color.GREY, message, th);
                }
            }
        },
        // System.out.println substitute.
        OUT {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(null, message, th);
                }
            }
        },
        // Debug messages.
        // This level is useful during development.
        DEBUG {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(Color.BLUE, message, th);
                }
            }
        },
        // Trace messages.
        // A lot of information is logged, so this level is
        // usually only needed when debugging a problem.
        TRACE {
            public void write(String message, Throwable th) {
                if (ordinal() <= logger.ordinal()) {
                    logWriter.write(Color.GREEN, message, th);
                }
            }
        };
        public void set(KernelLog level) {
            // logger = level;
        }
    }

    // -- LOG LEVEL -------------------------------------
    // Interface of a logging level.
    //
    public interface LogProvider<E extends Enum<E> & LogProvider<E>> extends Self<E> {

        default void active() { set(level()); }

        default E level() { return self(); }

        void write(String message, Throwable th);

        void set(E level);
    }

    // -- LOG-WRITER INTERFACE --------------------------
    //
    interface LogWriter {

        void write(Color color, String message, Throwable ex);
    }

    // -- STDOUT LOG-WRITER -----------------------------
    // Writes log message to the std-out stream.
    //
    enum StdOutLogger implements LogWriter {
        get;

        static final ThreadLocal<Context> context =
                ThreadLocal.withInitial(Context::new);

        static final class Context {
            final long firstLogTime = System.currentTimeMillis();
            StringBuilder msg = new StringBuilder(256);
        }

        public void write(Color color, String message, Throwable ex) {
            Context ctx = context.get();
            if (isColored && color != null) {
                message = color.apply(message);
            }
            ctx.msg.append(message);
            if (ex != null) {
                StringWriter writer = new StringWriter(256);
                ex.printStackTrace(new PrintWriter(writer));
                ctx.msg.append('\n').append(writer.toString().trim());
            }
            String devicePrefix;
            int deviceID = 10; //Kernel.device() != null? Kernel.device().deviceID() : -1; TODO
            if (deviceID != -1) {
                devicePrefix = "[device@" + deviceID + "] ";
            }
            else {
                devicePrefix = "[local-device] ";
            }
            Console.out().println(devicePrefix + ctx.msg.toString());
            ctx.msg.setLength(0);
        }
    }

    // Set log writer.
    public static void set(LogWriter writer) {
        logWriter = writer;
    }
}


/*long time = System.currentTimeMillis() - ctx.firstLogTime;
long minutes = time / 1000;
long seconds = time / (1000) % 60;
if (minutes <= 9) ctx.msg.append('0');
ctx.msg.append(minutes);
ctx.msg.append(':');
if (seconds <= 9) ctx.msg.append('0');
ctx.msg.append(seconds);*/
/*switch (level) {
    case ERROR:
        ctx.msg.append(" ERROR: ");
        break;
    case WARN:
        ctx.msg.append("  WARN: ");
        break;
    case INFO:
        ctx.msg.append("  INFO: ");
        break;
    case DEBUG:
        ctx.msg.append(" DEBUG: ");
        break;
    case TRACE:
        ctx.msg.append(" TRACE: ");
        break;
}*/