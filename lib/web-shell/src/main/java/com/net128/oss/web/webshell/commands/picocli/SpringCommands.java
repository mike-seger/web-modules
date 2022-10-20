package com.net128.oss.web.webshell.commands.picocli;

import com.net128.oss.web.webshell.util.NetInfo;
import com.net128.oss.web.webshell.util.TabUtils;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.net128.oss.web.webshell.util.StringUtils.*;
import static org.fusesource.jansi.Ansi.ansi;

@Service
public class SpringCommands {
    private final String startedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'").format(new Date());

    private final ApplicationContext applicationContext;
    private final AbstractEnvironment env;
    private final ConfigurableListableBeanFactory beanFactory;
    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;

    public SpringCommands(ApplicationContext applicationContext, AbstractEnvironment env, ConfigurableListableBeanFactory beanFactory, HealthEndpoint healthEndpoint, MeterRegistry meterRegistry) {
        this.applicationContext = applicationContext;
        this.env = env;
        this.beanFactory = beanFactory;
        this.healthEndpoint = healthEndpoint;
        this.meterRegistry = meterRegistry;
    }

    @Command(description = "Display application information.", mixinStandardHelpOptions = true)
    public String app() {
        int mb = 1024 * 1024;
        int gb = mb * 1024;
        Runtime runtime = Runtime.getRuntime();
        File path = new File(".");
        InetAddress address = NetInfo.getLocalAddress();
        String [][] table = new String [][] {
            {"Application Name", env.getProperty("spring.application.name")},
            {"Host Name", address==null?"":address.getHostName()},
            {"IP Address", address==null?"":address.getHostAddress()},
            {"Start Time", startedTime},
            {"Home Directory", env.getProperty("user.home")},
            {"Work Directory", env.getProperty("user.dir")},
            {"Shell", env.getProperty("SHELL")},
            {"PID", env.getProperty("PID")},
            {"SpringBoot Ver.", SpringBootVersion.getVersion()},
            {"Java Version", System.getProperty("java.version")},
            {"OS Name", System.getProperty("os.name")},
            {"OS Version", System.getProperty("os.version")},
            {"OS Arch", System.getProperty("os.arch")},
            {"CPU Cores", ""+runtime.availableProcessors()},
            {"Total Memory", humanReadableByteCountSI(runtime.totalMemory())},
            {"Free Memory", humanReadableByteCountSI(runtime.freeMemory())},
            {"Used Memory", humanReadableByteCountSI(runtime.totalMemory() - runtime.freeMemory())},
            {"Max Memory", humanReadableByteCountSI(runtime.maxMemory())},
            {"Disk Total", humanReadableByteCountSI(path.getTotalSpace())},
            {"Disk Free", humanReadableByteCountSI(path.getUsableSpace())},
        };

        StringBuilder result=new StringBuilder(TabUtils.formatFixedWidthColumnsWithBorders(noBreak(table), false));
        int pos = 0;
        for (String[] cols : table) {
            pos = colorizeOne(result, cols[0], ansi().fgGreen(), pos);
            pos = colorizeOne(result, cols[1], ansi().fgCyan(), pos);
        }

        return undoNoBreak(result.toString());
    }

    @Command(description = "List bean info")
    public String beans() {
        List<String> lines = new ArrayList<>(Arrays.asList(beanFactory.getBeanDefinitionNames()));
        lines.add("");
        lines.add("Beans: " + beanFactory.getBeanDefinitionNames().length);
        return String.join("\n", lines);
    }

    private void addRow(List<List<String>> matrix, String ... cols) {
        addRow(matrix, Arrays.asList(cols));
    }

    private void addRow(List<List<String>> matrix, List<String> cols) {
        if(cols.size()>1 && cols.get(1)==null) {
            return;
        }
        matrix.add(cols);
    }

    @Command(description = "Display bean details", mixinStandardHelpOptions = true)
    public String bean(@Parameters(description = "Specifies the bean or class name to show details about.") String beanNameOrClass) {
        List<List<String>> matrix = new ArrayList<>();
        String nameForSearch = beanNameOrClass;
        if (beanNameOrClass.contains("*")) {
            nameForSearch = beanNameOrClass.replaceAll("\\*", "").toLowerCase();
        }
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            Object bean = applicationContext.getBean(beanName);
            if (beanClassName == null) {
                beanClassName = bean.getClass().getCanonicalName();
                if (beanClassName == null) {
                    beanClassName = bean.getClass().getSimpleName();
                }
            }
            boolean matched;
            if (beanNameOrClass.contains("*")) {
                matched = beanName.toLowerCase().contains(nameForSearch)
                        || beanClassName.toLowerCase().contains(nameForSearch);
            } else {
                matched = beanNameOrClass.equalsIgnoreCase(beanName) || beanNameOrClass.equalsIgnoreCase(beanClassName);
            }
            if (matched) {
                addRow(matrix, null, "-------------------------------");
                addRow(matrix, "Name", beanName);
                addRow(matrix, "Class", beanClassName);
                addRow(matrix, "Factory Bean", beanDefinition.getFactoryBeanName());
                addRow(matrix, "Factory Method", beanDefinition.getFactoryMethodName());

                addRow(matrix, "Scope", beanDefinition.getScope());
                addRow(matrix, "DependsOn", String.join("\n", beanDefinition.getDependsOn()));
                addRow(matrix, "Parent", beanDefinition.getParentName());

                Class<?>[] allInterfaces = ClassUtils.getAllInterfaces(bean);
                for (Class<?> beanInterface : allInterfaces) {
                    addRow(matrix, null, "=========" + beanInterface.getCanonicalName() + " ========");
                    addRow(matrix, getSignature(beanInterface));
                }
            }
        }

        return TabUtils.formatFixedWidthColumnsWithBorders(matrix, false, 100);
    }

    private String getSignature(Class<?> beanInterface) {
        return Arrays.stream(beanInterface.getDeclaredMethods()).map(method -> {
            if (method.getParameterCount() == 0) {
                return formatClassName(method.getGenericReturnType().getTypeName())+"."+method.getName()+ "()";
            } else {
                String parameterTypes = Arrays.stream(method.getParameters()).map(parameter -> {
                    String parameterName = parameter.getName();
                    return formatClassName(parameter.getParameterizedType().getTypeName())+"."+parameterName;
                }).collect(Collectors.joining(", "));
                return formatClassName(method.getGenericReturnType().getTypeName())+"."+method.getName()
                        + "(" + parameterTypes + ")";
            }
        }).collect(Collectors.joining(","));
    }

    @Command(description = "Display Spring environment information.", mixinStandardHelpOptions = true)
    public String springenv(@Parameters(description = "Specifies which env value to display. If left empty, all Spring env values are displayed.",
            defaultValue = "") String envName) {
        List<List<String>> matrix = new ArrayList<>();
        if (!envName.isEmpty()) {
            String key = envName;
            String value = env.getProperty(key);
            if (value == null) {
                key = envName.toUpperCase();
                value = env.getProperty(key);
            }
            if (value != null) {
                addRow(matrix, key,  value);
            }
        } else {
            for (PropertySource<?> propertySource : env.getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    EnumerablePropertySource<?> enumerablePropertySource =
                        (EnumerablePropertySource<?>) propertySource;
                    for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                        addRow(matrix, propertyName, env.getProperty(propertyName));
                    }
                }
            }
        }
        return TabUtils.formatFixedWidthColumnsWithBorders(TabUtils.sort(matrix), false, 100);
    }

    @Command(description = "Display metrics", mixinStandardHelpOptions = true)
    public String metrics(@Parameters(description =
            "Specifies which env value to display. If left empty, all Spring metrics values are displayed.",
            defaultValue = "") String metricsName) {
        List<List<String>> matrix = new ArrayList<>();
        if (!metricsName.isEmpty()) {
            for (Meter meter : meterRegistry.getMeters()) {
                String meterName = meter.getId().getName();
                if (meterName.contains(metricsName)) {
                    addRow(matrix, meterName(metricsName, meter.getId().getTags()),
                        meter.measure().iterator().next().getValue()+"");
                }
            }
        } else {
            for (Meter meter : meterRegistry.getMeters()) {
                addRow(matrix,meterName(meter.getId().getName(), meter.getId().getTags()),
                    meter.measure().iterator().next().getValue()+"");
            }
        }
        StringBuilder result= new StringBuilder(String.join("\n",
            undoNoBreak(TabUtils.formatFixedWidthColumnsWithBorders(TabUtils.sort(noBreak(matrix)), false, 150)),
            "\nMetrics Count: " + (matrix.size() - 1)));

        int pos = 0;
        for (List<String> cols : matrix) {
            pos = colorizeOne(result, cols.get(0), ansi().fgGreen(), pos);
            pos = colorizeOne(result, cols.get(1), ansi().fgCyan(), pos);
        }
        return result.toString();
    }

    @Command(description = "Display health", mixinStandardHelpOptions = true)
    public String health(@Parameters(description =
            "Specifies which health value to display. If left empty, all Spring health values are displayed.",
            defaultValue = "") String componentName) {
        List<List<String>> matrix = new ArrayList<>();
        HealthComponent healthComponent = healthEndpoint.health();
        Map<String, HealthComponent> healthComponents;
        if (healthComponent instanceof SystemHealth) {
            healthComponents = ((SystemHealth) healthComponent).getComponents();
        } else if (healthComponent instanceof CompositeHealth) {
            healthComponents = ((CompositeHealth) healthComponent).getComponents();
        } else {
            healthComponents = new HashMap<>();
            healthComponents.put("Default", healthComponent);
        }
        healthComponents.entrySet().forEach(e -> addHealthRow(matrix, e));
        //String result = TabUtils.formatFixedWidthColumnsWithBorders(TabUtils.sort(matrix), false);
        StringBuilder result=new StringBuilder(undoNoBreak(TabUtils.formatFixedWidthColumnsWithBorders(noBreak(matrix), false, 100)));
        int pos = 0;
        for (List<String> cols : matrix) {
            pos = colorizeOne(result, cols.get(0), ansi().fgGreen(), pos);
            pos = colorizeOne(result, cols.get(1), ansi().fgGreen(), pos);
            pos = colorizeOne(result, cols.get(2), ansi().fgCyan(), pos);
        }
        return result.toString();
    }

    private void addHealthRow(List<List<String>> matrix, Map.Entry<String, HealthComponent> entry) {
        Status status = entry.getValue().getStatus();
        String [] healthParts = healthName(entry.getKey(), entry.getValue()).split("\t");
        addRow(matrix, healthParts[0], status.getCode(), healthParts[1]);
    }

    private static String healthName(String name, HealthComponent healthComponent) {
        if (healthComponent instanceof Health) {
            Map<String, Object> details = ((Health) healthComponent).getDetails();
            if (details != null && !details.isEmpty()) {
                return details.entrySet().stream().map(entry -> entry.getKey() + ":" +
                        formatValue(entry.getValue()))
                        .collect(Collectors.joining(",", name + "\t(", ")"));
            }
        }
        return name+"\t ";
    }

    private static String formatValue(Object o) {
        return (((o instanceof Long) && o.toString().replace("-","").length()>4)?
                humanReadableByteCountSIAbbr((Long)o):o.toString());
    }

    @Command(description = "Display profiles")
    public String profiles() {
        String[] profiles = env.getActiveProfiles();
        if (profiles.length > 0) {
            return "Active Profiles: [" + String.join(",", profiles) + "]";
        }
        return "No active profiles";
    }

    @Command(description = "Display logfile")
    public List<String> log(@Parameters(paramLabel = "Number of last log lines") Integer logLines) throws Exception {
        if (logLines < 1) {
            logLines = 1;
        }
        String loggingFilePath = env.getProperty("logging.file.path");
        String loggingFileName = env.getProperty("logging.file.name");
        File loggingFile;
        if (loggingFilePath == null && loggingFileName == null) {
            throw new Exception("Missing 'logging.file.name' or 'logging.file.path' properties");
        } else if (loggingFileName == null) {
            loggingFile = new File(loggingFilePath, "spring.log");
        } else {
            loggingFile = new File(loggingFileName);
        }
        if (!loggingFile.exists()) {
            throw new Exception("Logging file not found: " + loggingFile.getAbsolutePath());
        }
        int blockSize = ((logLines * 128 + 4095) / 4096) * 4096;
        ReversedLinesFileReader object = new ReversedLinesFileReader(loggingFile, blockSize, StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < logLines; i++) {
            String line = object.readLine();
            if (line == null)
                break;
            lines.add(line);
        }
        Collections.reverse(lines);
        return lines;
    }

    private static String meterName(String name, List<Tag> tags) {
        if (tags != null && tags.size() > 0) {
            return tags.stream().map(tag -> tag.getKey() + ":" + tag.getValue())
                .collect(Collectors.joining(",", name + "(", ")"));
        }
        return name;
    }

    private String formatClassName(String classFullName) {
        if (classFullName.contains("java.lang.")) {
            return classFullName.replaceAll("java.lang.([A-Z]\\w*)", "$1");
        } else {
            return classFullName;
        }
    }
}
