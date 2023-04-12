package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.CloseCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class Check implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
        //no args -> all recipes show
        //one argument -> search recipes
        if(args.length == 0){
            recipes.forEach(s->System.out.println(getGraphicalRecipe(s.getName())));
        }
        else if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.isOp())return false;
            reload();
        }
        else {
            System.out.println(getGraphicalRecipe(args[0]));
        }
        return true;
    }

    private void reload(){
        /*
        * /cc reload
        * -> close all players custom crafter GUI (contained "opening")
        * -> clear "whatMaking"
        * -> clear "opening"
        * -> reloadConfig
        * -> load config files
        * -> notice to all players that the system reloaded.
        *
         */

        List<Player> copy = new ArrayList<>();
        opening.forEach(s->copy.add(s));
        copy.forEach(s->{
            new CloseCraftingTable().close(s,s.getInventory());
            s.closeInventory();
        });

        whatMaking.clear();
        opening.clear();
        CustomCrafter.getInstance().reloadConfig();
        new SettingsLoad().load();
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        if(config.contains("notice")){
            if(config.getStringList("notice") == null)return;
            if(config.getStringList("notice").isEmpty())return;
            config.getStringList("notice").forEach(s->Bukkit.broadcastMessage(s));
        }
    }

    private String getGraphicalRecipe(String recipeName){
        if(!namedRecipes.containsKey(recipeName))return "Unknown recipe.";
        Recipe recipe = namedRecipes.get(recipeName);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s\n\n",String.join("", Collections.nCopies(40,"="))));
        builder.append(String.format("Recipe Name : %s\n",recipeName));
        builder.append(String.format("Tag : %s",recipe.getTag().toString()));
        if(recipe.getTag().equals(Tag.NORMAL)) return normal(builder,recipe);
        return amorphous(builder,recipe);

    }

    private String normal(StringBuilder builder,Recipe recipe){
        double size = Math.sqrt(recipe.getCoordinate().size());
        Map<Integer,Matter> map = new HashMap<>();
        builder.append("\n");
        for(int y=0;y<size;y++){
            Point : for(int x=0;x<size;x++){
                Coordinate coordinate = new Coordinate(x,y);
                Matter matter = recipe.getMatterFromCoordinate(coordinate);
                if(matter.getCandidate().size() ==1 && matter.getCandidate().get(0).equals(Material.AIR)){
                    builder.append(" Null |");
                    continue;
                }
                for(Map.Entry<Integer,Matter> entry : map.entrySet()){
                    if (new Search().isSameMatter(matter,entry.getValue())){
                        builder.append(String.format("  %02d  |",entry.getKey()));
                        continue Point;
                    }
                }

                builder.append(String.format("  %02d  |",map.size()));
                map.put(map.size(),matter);

            }
            builder.append("\n");
        }

        for(Map.Entry<Integer,Matter> entry : map.entrySet()){
            builder.append("\n");
            builder.append(String.format("%02d -> \n",entry.getKey()));
            String info = entry.getValue().info();
            info = info.replace("\n","\n  ");
            builder.append(info);
        }

        return builder.toString();
    }

    private String amorphous(StringBuilder builder,Recipe recipe){
        builder.append("\nList : ");
        StringBuilder names = new StringBuilder();
        recipe.getContentsNoDuplicateRelateAmount().entrySet().forEach(s->{
            names.append(String.format("%s -> %d | ",s.getKey().getName(),s.getValue()));
        });
        builder.append(names);
        builder.append("\n");
        for(Matter matter : recipe.getContentsNoDuplicate()){
            builder.append("\n");
            String info = matter.info();
            info = info.replace("\n","\n  ");
            builder.append(info);
        }
        return builder.toString();
    }
}
