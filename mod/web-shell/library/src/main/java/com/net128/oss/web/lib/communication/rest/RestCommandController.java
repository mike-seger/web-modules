package com.net128.oss.web.lib.communication.rest;

import com.net128.oss.web.lib.commands.CommandDispatcher;
import com.net128.oss.web.lib.communication.Command;
import com.net128.oss.web.lib.communication.CommandAware;
import com.net128.oss.web.lib.communication.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class RestCommandController implements CommandAware {
    private final CommandDispatcher commandDispatcher;

    public RestCommandController(
            CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @ResponseBody
    @PostMapping("/web-shell/complete")
    public List<String> completeCommand(@RequestBody Command command, HttpSession session) {
        log.info("Complete command; {}", command);
        List<String> result = new ArrayList<>();
        result.add("ls");
        return result;
    }

    @ResponseBody
    @PostMapping("/web-shell/execute")
    public Result execute(@RequestBody Command command, HttpSession session) {
        return createResponse(commandDispatcher, session.getId(), command);
    }
}
