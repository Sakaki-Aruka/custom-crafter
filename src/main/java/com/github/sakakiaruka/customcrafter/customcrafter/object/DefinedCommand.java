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

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public boolean isHasArgs() {
        return hasArgs;
    }

    public void setHasArgs(boolean hasArgs) {
        this.hasArgs = hasArgs;
    }

    public int getArgsLen() {
        return argsLen;
    }

    public void setArgsLen(int argsLen) {
        this.argsLen = argsLen;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}