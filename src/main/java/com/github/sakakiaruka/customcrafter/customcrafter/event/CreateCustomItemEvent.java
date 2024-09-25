package com.github.sakakiaruka.customcrafter.customcrafter.event;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreateCustomItemEvent extends Event {
    public static HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }


    private final Player player;
    private final Recipe recipe;
    private final ItemStack item;

    public CreateCustomItemEvent(Player player, Recipe recipe, ItemStack item) {
        this.player = player;
        this.recipe = recipe;
        this.item = item;
    }

    public Player getPlayer() {
        return player;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public ItemStack getItem() {
        return item;
    }
}
