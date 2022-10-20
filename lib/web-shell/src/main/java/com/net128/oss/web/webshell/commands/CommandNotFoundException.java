package com.net128.oss.web.webshell.commands;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String command) {
        super("Command: " + command);
    }
}
