package com.net128.oss.web.lib.commands.picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * JVM commands
 */
public class JvmCommands {

    @Command(description = "Current java command line options.", mixinStandardHelpOptions = true)
    public String options() {
        return String.join("\n", ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    @Command(description = "Running application threads.", mixinStandardHelpOptions = true)
    public String threads() {
        String threadPattern = "%4s %5s %10s %8s %16s  %s";
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        List<String> lines = new ArrayList<>();
        lines.add(String.format(threadPattern, "ID", "Alive", "State", "Priority", "Group", "Name", "Daemon"));
        threadSet.stream().map(thread -> String.format(threadPattern, thread.getId(), thread.isAlive() ? 1 : 0, thread.getState().name(),
                thread.getPriority(), (thread.getThreadGroup() == null ? "" : thread.getThreadGroup().getName()),
                thread.getName(), thread.isDaemon())).forEach(lines::add);
        lines.add("");
        lines.add("Thread Count: " + (lines.size() - 1));
        return String.join("\n", lines);
    }

    @Command(description = "Thread details.", mixinStandardHelpOptions = true)
    public String thread(@Parameters(paramLabel = "Thread Id") Integer threadId) {
        return "";
    }

    @Command(description = "CPU usage information", mixinStandardHelpOptions = true)
    public String cpu() {
        OperatingSystemMXBean operatingSystemMXBean =
                ManagementFactory.getOperatingSystemMXBean();
        return String.format("%-15s: %d\n%-15s: %.2f",
                "Number of CPUs",
                operatingSystemMXBean.getAvailableProcessors(),
                "Load Avg",
                operatingSystemMXBean.getSystemLoadAverage());
    }

    @Command(description = "Display classpath info", mixinStandardHelpOptions = true)
    public String classpath() {
        List<String> lines = new ArrayList<>();
        String classPath = System.getProperty("CLASSPATH");
        if (classPath == null || classPath.isEmpty()) {
            classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        }
        if (classPath != null) {
            lines.addAll(Arrays.asList(classPath.split(File.pathSeparator)));
        } else {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            URL[] urls = ((URLClassLoader) cl).getURLs();
            Arrays.stream(urls).map(URL::toString).forEach(lines::add);
        }
        return String.join("\n", lines);
    }
}
