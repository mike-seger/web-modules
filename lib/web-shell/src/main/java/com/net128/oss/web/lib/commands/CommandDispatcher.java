package com.net128.oss.web.lib.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.net128.oss.web.lib.util.ShellInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

@Service
@Slf4j
public class CommandDispatcher {

    @SuppressWarnings({"unused", "SingleElementAnnotation", "MismatchedQueryAndUpdateOfCollection"})
    @Value("${web-shell.hostshells:sh}")
    private List<String> hostShells;

    private final String userHome = System.getProperty("user.home", ".");
    private ObjectMapper objectMapper;
    private ShellInfo shellInfo;

    private final List<String> stringOutputClasses = Arrays.asList("java.util.Date", "java.lang.Boolean", "java.lang.Void");

    private final List<CommandHandler> commandHandlers;

    public CommandDispatcher(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    @Data
    public static class CommandResult {
        public CommandResult(String text, String cwd) {
            this.text = text;
            this.cwd = cwd;
        }

        public String text;
        public String cwd;
    }

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        shellInfo = ShellInfo.determineShellInfo(hostShells);
    }

    public CommandResult executeCommand(String rawCommandLine, CommandContext commandContext) {
        rawCommandLine = rawCommandLine.trim();
        if ("?".equals(rawCommandLine)) {
            rawCommandLine = "help";
        }
        final String commandLine = rawCommandLine;
        String command;
        String arguments = null;
        int spaceIndex = commandLine.indexOf(" ");
        if (spaceIndex > 0) {
            command = commandLine.substring(0, spaceIndex);
            arguments = commandLine.substring(spaceIndex + 1).trim();
        } else {
            command = commandLine;
        }
        String result=null;
        try {
            for(CommandHandler ch : commandHandlers) {
                if (ch.canHandle(command)) {
                    result = ch.execute(commandLine);
                    break;
                }
            }
            if(result==null) {
                ProcessResult pr = executeOsCommand(shell(shellInfo, commandLine), commandContext);
                result = pr.outputUTF8();
                if (pr.getExitValue() != 0) {
                    result = formatError(result);
                }
                if ("cd".equals(command)) {
                    String path = userHome;
                    if (arguments != null && arguments.trim().length() > 0) {
                        path = arguments.trim().split(" ")[0];
                        if (path.startsWith("~/") || "~".equals(path)) {
                            path = path.replaceFirst("[~]", userHome);
                        } else {
                            path = new File(commandContext.get("path") + "", path).getAbsolutePath();
                        }
                    }
                    File wd = new File(path);
                    if (wd.exists() && wd.isDirectory()) {
                        try {
                            commandContext.put("path", wd.getCanonicalPath());
                        } catch (Exception e) {
                            log.error("Failed to get canonical path from: {}", path);
                        }
                    } else {
                        log.error("Failed to find path: {}", path);
                    }
                }
            }
        } catch (Exception e) {
            result = formatError(formatResult(e.getMessage()));
        }

        commandContext.put("shell", shellInfo.getName());
        return new CommandResult(result, commandContext.get("path"));
    }

    private ProcessResult executeOsCommand(String commandLine, CommandContext commandContext) throws Exception {
        List<String> command = Arrays.asList(CommandLineUtils.translateCommandline(commandLine));
        File path = new File(commandContext == null ? "." : commandContext.get("path"));
        return new ProcessExecutor().directory(path)
                .command(command)
                .readOutput(true).execute();
    }

    private String shell(ShellInfo shellInfo, String command) {
        return shellInfo.formatCommand(command);
    }

    private String formatError(String result) {
        return ansi().fg(RED).a(result).reset().toString();
    }

    private String formatResult(Object result) {
        String textOutput;
        if (result == null) {
            textOutput = "";
        } else {
            textOutput = formatObject(result);
        }

        // workaround for jquery.terminal ansi color issue
        textOutput = textOutput
                .replace('[', '\uff3b')
                .replace(']', '\uff3d');

        // text format for terminal
        if (!textOutput.contains("\r\n") && textOutput.contains("\n")) {
            return textOutput.replaceAll("\n", "\r\n");
        }

        return textOutput;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String formatObject(Object object) {
        if (object instanceof Exception) {
            return formatError(((Exception) object).getMessage());
        }
        if (object instanceof CharSequence || object instanceof Number || object instanceof Throwable) {
            return object.toString();
        } else if (object instanceof Collection) {
            return String.join("\r\n", (Collection) object);
        } else {
            // to string or json output
            String classFullName = object.getClass().getCanonicalName();
            // toString() declared
            try {
                return object.toString();
            } catch (Exception ignore) {
            }
            if (classFullName == null) {
                classFullName = object.getClass().getSimpleName();
            }
            if (stringOutputClasses.contains(classFullName)) {
                return object.toString();
            } else if (classFullName.startsWith("java.lang.") && classFullName.matches("java.lang.([A-Z]\\w*)")) {
                return object.toString();
            } else if (classFullName.startsWith("java.time.")) {
                return object.toString();
            } else {
                try {
                    return "Class: " + classFullName + "\n" + objectMapper.writeValueAsString(object);
                } catch (Exception ignore) {
                    return object.toString();
                }
            }
        }
    }
}
