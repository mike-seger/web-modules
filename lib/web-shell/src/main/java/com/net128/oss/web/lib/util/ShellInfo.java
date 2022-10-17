package com.net128.oss.web.lib.util;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class ShellInfo {
    private String name;
    private String shellPath;
    private String commandFormatString;
    private final static String defaultPath = System.getProperty("user.home", ".");

    public static ShellInfo determineShellInfo(String... hostShells) {
        return determineShellInfo(Arrays.asList(hostShells));
    }

    public static ShellInfo determineShellInfo(List<String> hostShells) {
        String shell = System.getenv("SHELL");
        ShellInfo shellInfo;
        if (defaultPath.indexOf('\\') >= 0) {
            shellInfo = new ShellInfo("cmd", "cmd /c %s");
        } else if (shell != null && shell.trim().length() > 0) {
            shellInfo = new ShellInfo(shell, unixShell(shell));
        } else {
            shellInfo = new ShellInfo();
            for (String hostShell : hostShells) {
                shell = checkShell(hostShell);
                if (shell != null) {
                    shellInfo = new ShellInfo(shell, unixShell(shell));
                    break;
                }
            }
        }

        if (shellInfo.shellPath != null && !"cmd".equals(shellInfo.name)) {
            try {
                String commandLine = shellInfo.formatCommand("pwd");
                List<String> commandArgs = Arrays.asList(CommandLineUtils.translateCommandline(commandLine));
                ProcessResult pr = new ProcessExecutor()
                        .command(commandArgs).readOutput(true).execute();
                if (pr.getExitValue() != 0) {
                    throw new RuntimeException("Cannot run pwd");
                }
            } catch (Exception e) {
                log.error("Defaulting to no shell from: {}", shellInfo, e);
                shellInfo = new ShellInfo();
            }
        }

        return shellInfo;
    }

    private ShellInfo() {
    }

    private ShellInfo(String shellPath, String commandFormatString) {
        this.shellPath = shellPath;
        this.name = shellPath != null ?
                shellPath.replaceAll(".*/", "") : null;
        this.commandFormatString = commandFormatString;
    }

    public String formatCommand(String command) {
        if (commandFormatString == null) {
            return command;
        }
        if (commandFormatString.contains("'")) {
            command = command.replace("'", "\\'");
        }
        return String.format(commandFormatString, command);
    }

    private static String unixShell(String executable) {
        return executable + " -c '%s'";
    }

    private static String checkShell(String shell) {
        try {
            ProcessResult pr = new ProcessExecutor()
                    .command("which", shell).readOutput(true).execute();
            String shellPath = pr.outputString().trim();
            if (shellPath.contains(shell)) {
                return shellPath;
            }
            log.warn("Error checking {}: {} ({})",
                    shell, pr.outputString(), pr.getExitValue());
            return null;
        } catch (Exception e) {
            log.warn("Error checking {}: {}", shell, e.getMessage());
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getShellPath() {
        return shellPath;
    }

    @Override
    public String toString() {
        return "ShellInfo{" +
                "name='" + name + '\'' +
                ", commandFormatString='" + commandFormatString + '\'' +
                '}';
    }
}
