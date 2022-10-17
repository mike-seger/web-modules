package com.net128.oss.web.lib.communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    private String origin;
    private String input;
    private long created = Instant.now().getEpochSecond();
    private long index;

    public Command(String origin, String input) {
        this.origin = origin;
        this.input = input;
        this.index = 0;
    }

    public Command(String origin, String input, long index) {
        this.origin = origin;
        this.input = input;
        this.index = index;
    }
}
