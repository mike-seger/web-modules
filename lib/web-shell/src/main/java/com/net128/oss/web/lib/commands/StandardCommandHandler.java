package com.net128.oss.web.lib.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StandardCommandHandler implements CommandHandler {
    private final Map<String, Method> methodMap;
    private final Map<String, String> aliasMap;
    private final Map<String, Map<String, Command>> commandGroups;

    private final CommandLine.IFactory factory;

    public StandardCommandHandler(IFactory factory) {
        this.factory = factory;
        Package basePackage = StandardCommandHandler.class.getPackage();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(basePackage.getName()))
            .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(Command.class);
        methodMap = methods.stream()
            .map(m -> new MethodCommandLine(m, new CommandLine(m, defaultingIFactory(factory))))
            .collect(Collectors.toMap(mc -> mc.c.getCommandName().toLowerCase(), mc -> mc.m));

        commandGroups = new TreeMap<>();
        aliasMap = new HashMap<>();
        methods.forEach(m -> {
            Class<?> commandClass = m.getDeclaringClass();
            if (!getClass().equals(commandClass)) {
                String group = commandClass.getSimpleName();
                Map<String, Command> commandDescriptionMap =
                    commandGroups.computeIfAbsent(group, k -> new TreeMap<>());
                final Command command = m.getAnnotation(Command.class);
                commandDescriptionMap.put(m.getName(), command);
                if(command.aliases().length>0) {
                    List<String> aliasList=Arrays.asList(command.aliases());
                    aliasList.forEach(a -> aliasMap.put(a, command.name()));
                }
            }
        });
    }

    private static IFactory defaultingIFactory(IFactory springFactory) {
        return new IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                K inst = springFactory.create(cls);
                if (inst == null) {
                    inst = CommandLine.defaultFactory().create(cls);
                }
                return inst;
            }
        };
    }

    @Override
    public boolean canHandle(String command) {
        command = command.toLowerCase();
        return methodMap.containsKey(command) || aliasMap.containsKey(command);
    }

    public Set<String> commands() {
        return Collections.unmodifiableSet(methodMap.keySet());
    }

    public String execute(String commandLine) {
        CommandLineExtra cex = createCommandLine(commandLine);
        CommandLine cmd = cex.c;
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        try {
            cmd.setErr(out);
            cmd.setOut(out);
            if (cmd.isUsageHelpRequested()) {
                cmd.usage(out);
                return sw.toString();
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(out);
                return sw.toString();
            }

            String[] args;
            if(cex.raw) {
                args = new String []{commandLine};
            } else {
                args = commandLineArgs(commandLine);
            }

            if (cmd.execute(args) == 0) {
                return cmd.getExecutionResult();
            } else {
                return sw.toString();
            }
        } catch (CommandLine.ParameterException ex) {
            out.println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, out)) {
                ex.getCommandLine().usage(out);
            }
            return sw.toString();
        } catch (Exception ex) {
            ex.printStackTrace(out);
            return sw.toString();
        }
    }

    @Command(description = "Display command help.")
    public String help() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s commands:\n",
            getClass().getSimpleName().replaceAll("CommandHandler$", "")));
        commandGroups.forEach((groupName, commandList) -> {
            sb.append(String.format("\n\t%s\n",
                groupName.replaceAll("Commands$", "")));
            commandList.forEach((commandName, command) ->
                sb.append(String.format("\t\t%-14s:  %s\n", commandName,
                String.join(", ", command.description()))));
        });
        return sb.toString();
    }

    private CommandLineExtra createCommandLine(final String commandLineString) {
        String command=getCommandName(commandLineString);
        Method method = methodMap.get(command);
        if (method == null) {
            throw new CommandNotFoundException(getCommandName(commandLineString));
        }
        CommandLine commandLine = new CommandLine(method, defaultingIFactory(factory));
        boolean rawCommand = method.isAnnotationPresent(RawCommand.class);
        if(method.isAnnotationPresent(RawCommand.class)) {
            String commandRaw=getCommandNameRaw(commandLineString);
            commandLine.parseArgs(command, String.format("\"%s %s\"", commandRaw, commandLineString));
        } else {
            commandLine.parseArgs(commandLineArgs(commandLineString));
        }
        return new CommandLineExtra(commandLine, rawCommand);
    }

    private String getCommandNameRaw(String commandLine) {
        return commandLine.trim().replaceAll("\\s.*", "").toLowerCase();
    }

    private String getCommandName(String commandLine) {
        String name = commandLine.trim().replaceAll("\\s.*", "").toLowerCase();
        if(aliasMap.get(name)!=null) {
            name = aliasMap.get(name);
        }
        return name;
    }

    private String[] commandLineArgs(String commandLine) {
        String[] allArgs = commandLine.trim().split(" ");
        return Arrays.stream(allArgs, 1,
                allArgs.length).toArray(String[]::new);
    }

    @Data
    @AllArgsConstructor
    private static class CommandLineExtra {
        CommandLine c;
        boolean raw;
    }

    @Data
    @AllArgsConstructor
    private static class MethodCommandLine {
        Method m;
        CommandLine c;
    }
}
