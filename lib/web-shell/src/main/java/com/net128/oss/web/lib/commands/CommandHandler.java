package com.net128.oss.web.lib.commands;

import java.util.Set;

public interface CommandHandler {
    boolean canHandle(String command);
    Set<String> commands();
    String execute(String commandLine);
    String help();
}
