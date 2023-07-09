package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.permissions.Permission;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinedCommand {
    private String commandName;
    private boolean hasArgs;
    private List<String> args;
    private boolean console;
    private List<String> commandPermission; // when has no permissions, this value is an empty ArrayList<String>
    private Class<?> processClass;
    private Method processMethod;
    private int commandLen;

    public DefinedCommand(String commandName, boolean hasArgs, List<String> args, boolean console, List<String> commandPermission, Class<?> processClass, Method processMethod, int commandLen) {
        this.commandName = commandName;
        this.hasArgs = hasArgs;
        this.args = args;
        this.console = console;
        this.commandPermission = commandPermission;
        this.processClass = processClass;
        this.processMethod = processMethod;
        this.commandLen = commandLen;
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

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public boolean isConsole() {
        return console;
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

    public List<String> getCommandPermission() {
        return commandPermission;
    }

    public void setCommandPermission(List<String> permission) {
        this.commandPermission = permission;
    }

    public Class<?> getProcessClass() {
        return processClass;
    }

    public void setProcessClass(Class<?> processClass) {
        this.processClass = processClass;
    }

    public Method getProcessMethod() {
        return processMethod;
    }

    public void setProcessMethod(Method processMethod) {
        this.processMethod = processMethod;
    }

    public int getCommandLen() {
        return commandLen;
    }

    public void setCommandLen(int commandLen) {
        this.commandLen = commandLen;
    }

    public String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("name: "+this.commandName+"\n");
        builder.append("hasArgs: "+this.hasArgs+"\n");
        builder.append("args: "+this.args+"\n");
        builder.append("console: "+this.console+"\n");
        builder.append("permission: "+this.commandPermission+"\n");
        builder.append("class: "+this.processClass+"\n");
        builder.append("method: "+this.processMethod+"\n");
        builder.append("length: "+this.commandLen+"\n");
        return builder.toString();
    }
}