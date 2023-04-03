package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.namedRecipes;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.recipes;

public class Check implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
        //no args -> all recipes show
        //one argument -> search recipes
        if(args.length == 0) recipes.forEach(s->System.out.println(getGraphicalRecipe(s.getName())));
        else System.out.println(getGraphicalRecipe(args[0]));
        return true;
    }

    private String getGraphicalRecipe(String recipeName){
        if(!namedRecipes.containsKey(recipeName))return "";
        Recipe recipe = namedRecipes.get(recipeName);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Recipe Name : %s\n",recipe.getName()));
        builder.append(String.format("Tag : %s\n",recipe.getTag().toString()));
        if(recipe.getReturnItems() != null){
            builder.append("=== Return items relation ===");
            for(Map.Entry<Material, ItemStack> entry:recipe.getReturnItems().entrySet()){
                Material m = entry.getKey();
                ItemStack i = entry.getValue();
                builder.append(String.format("Material : %s | ItemStack : %s(%d)\n",m.name(),i.getType().name(),i.getAmount()));
            }
            builder.append("=== Return items relation end ===\n");
        }

        int xBuffer = recipe.getCoordinateList().get(0).getX();
        builder.append("|");
        int longestNameLen = getLongestNameLen(recipe);
        for(Map.Entry<Coordinate, Matter> entry:recipe.getCoordinate().entrySet()) {
            Coordinate c = entry.getKey();
            Matter m = entry.getValue();
            if (xBuffer != c.getX()) {
                // a pointer moved
                builder.append(String.format("\n|"));
            }
            if (m.getName().length() < longestNameLen && recipe.getTag().equals(Tag.NORMAL)) {
                int diff = longestNameLen - m.getName().length();
                for (int i = 0; i < diff; i++) {
                    builder.append(" ");
                }
            }
            String name = m.getName().equals("") ? "AIR" : m.getName();
            builder.append(String.format("%s |", name));
        }
        builder.append("\n");
        recipe.getContentsNoDuplicate().forEach(s->{
            String name = s.getName();
            String candidate = s.getCandidate().toString();
            int amount = s.getAmount();
            boolean mass = s.isMass();
            builder.append(String.format("Name : %s | candidate : %s | amount : %d | mass : %b\n",name,candidate,amount,mass));

        });


        //debug
        recipe.getCoordinate().entrySet().forEach(s->{
            System.out.println(String.format("coordinate : x:%d y:%d",s.getKey().getX(),s.getKey().getY()));
            System.out.println(String.format("candidate : %s",s.getValue().getCandidate()));
        });


        return builder.toString();
    }

    private int getLongestNameLen(Recipe r){
        int l = 0;
        for(Map.Entry<Coordinate,Matter> entry:r.getCoordinate().entrySet()){
            Matter m = entry.getValue();
            if(m.getName().length() > l)l = m.getName().length();
        }
        return l;
    }
}
