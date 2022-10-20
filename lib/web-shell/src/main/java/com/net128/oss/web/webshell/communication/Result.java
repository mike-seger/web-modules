package com.net128.oss.web.webshell.communication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Result {
    public String origin;
    public ZonedDateTime dateTime;
    public String result;
    public String host;
    public String cwd;
    public String shell;
}