package com.net128.oss.web.webshell.communication;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.net128.oss.web.webshell.commands.CommandContext;
import com.net128.oss.web.webshell.commands.CommandDispatcher;
import com.net128.oss.web.webshell.util.NetInfo;

import javax.annotation.Nonnull;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public interface CommandAware {
    LoadingCache<String, CommandContext> userContextMap = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(240, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, CommandContext>() {
                        public CommandContext load(@Nonnull String key) {
                            CommandContext commandContext = new CommandContext();
                            commandContext.put(key, commandContext);
                            commandContext.put("path", new File(System.getProperty("user.home", ".")).getAbsolutePath());
                            return commandContext;
                        }
                    });

    default Result createResponse(CommandDispatcher commandDispatcher, String sessionId, Command command) {
        sessionId = sessionId == null ? "default" : sessionId;
        CommandContext commandContext = userContextMap.getUnchecked(sessionId);
        CommandDispatcher.CommandResult result = commandDispatcher.executeCommand(command.getInput(), commandContext);
        String host = NetInfo.getLocalAddress() == null ? null : NetInfo.getLocalAddress().getHostName();
        host = host == null ? "localhost" : host;
        return new Result(command.getOrigin(),
                ZonedDateTime.now(), result.text, host, result.cwd, commandContext.get("shell"));
    }
}
