package com.github.sakakiaruka.customcrafter.customcrafter.object;

import java.util.List;

public class DefinedCommand {
    private String commandName;
    private boolean hasArgs;
    private int argsLen;
    private List<String> args;

    public DefinedCommand(String commandName, boolean hasArgs, int argsLen, List<String> args) {
        this.commandName = commandName;
        this.hasArgs = hasArgs;
        this.argsLen = argsLen;
        this.args = args;
    }
}