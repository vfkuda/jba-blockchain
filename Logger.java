package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Logger {
    final public static int ERROR = 1;
    final public static int WARN = 2;
    final public static int INFO = 3;
    final public static int DEBUG = 4;
    private static Logger instance;
    String modes = "-EWid";
    private List<String> filtersClasses = new ArrayList<>();
    private List<Predicate<Object>> filters = new ArrayList<>();
    private int level;

    public static Logger get() {
        if (null == instance) {
            instance = new Logger();
        }
        return instance;
    }

    public static void info(String format, Object... objs) {
        get().log(Logger.INFO, format, objs);
    }

    public static void debug(String format, Object... objs) {
        get().log(Logger.DEBUG, format, objs);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addFiler(Class clazz, Predicate<Object> filter) {
        filtersClasses.add(clazz.getName());
        filters.add(filter);

    }

    public boolean isFiltered(Object... objs) {
        for (Object obj : objs) {
            for (int i = 0; i < filters.size(); i++) {
                if (obj.getClass().getName().equals(filtersClasses.get(i))) {
                    if (!filters.get(i).test(obj)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void log(int level, String format, Object... objs) {
        if (level <= this.level) {
            if (!isFiltered(objs)) {
                System.out.print(modes.charAt(level) + " ");
                System.out.printf(format, objs);
                System.out.println();
            }
        }
    }

}
