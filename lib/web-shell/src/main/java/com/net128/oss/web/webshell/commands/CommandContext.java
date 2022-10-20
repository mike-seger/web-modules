package com.net128.oss.web.webshell.commands;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandContext {

    HashMap<Object, Object> holder = new HashMap<>();

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T get(@NotNull Object key) {
        return (T) holder.get(key);
    }

    public boolean hasKey(@NotNull Object key) {
        return holder.containsKey(key);
    }

    @NotNull
    public CommandContext put(@NotNull Object key, @NotNull Object value) {
        holder.put(key, value);
        return this;
    }

    @NotNull
    public CommandContext delete(@NotNull Object key) {
        holder.remove(key);
        return this;
    }

    public int size() {
        return holder.size();
    }

    @NotNull
    public Stream<Map.Entry<Object, Object>> stream() {
        return holder.entrySet().stream();
    }

}
