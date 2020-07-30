package com.net128.oss.web.lib.communication.websocket;

import com.net128.oss.web.lib.commands.CommandDispatcher;
import com.net128.oss.web.lib.communication.Command;
import com.net128.oss.web.lib.communication.CommandAware;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketCommandController implements CommandAware {
    private final SimpMessagingTemplate template;
    private final CommandDispatcher commandDispatcher;

    public WebSocketCommandController(
            SimpMessagingTemplate template,
            CommandDispatcher commandDispatcher) {
        this.template = template;
        this.commandDispatcher = commandDispatcher;
    }

    @MessageMapping("/command/send")
    public void onSendMessage(Command command, SimpMessageHeaderAccessor headerAccessor) {
        //FIXME use Mono and make this asynchronous
        template.convertAndSend("/command",
                createResponse(commandDispatcher, headerAccessor.getSessionId(), command));
    }
}
