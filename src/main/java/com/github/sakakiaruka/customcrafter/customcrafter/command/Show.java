package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class Show {

    public void all(String[] args, CommandSender sender) {
        namedRecipes.keySet().forEach(s->sender.sendMessage(getGraphicalRecipe(s)));
    }
    public void one(String[] args, CommandSender sender) {
        sender.sendMessage(args[1]);
    }

    private String getGraphicalRecipe(String recipeName){
        if(!namedRecipes.containsKey(recipeName))return "Unknown recipe.";
        Recipe recipe = namedRecipes.get(recipeName);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s%s%s",bar,nl,nl));
        builder.append(String.format("Recipe Name : %s%s",recipeName,nl));
        builder.append(String.format("Tag : %s%s",recipe.getTag().toString(),nl));
        builder.append(String.format("RecipePermission : %s%s",recipe.hasPermission() ? recipe.getPermission().getPermissionName() : "NO PERMISSION",nl));
        if(recipe.getTag().equals(Tag.NORMAL)) return normal(builder,recipe);
        return amorphous(builder,recipe);

    }

    private String normal(StringBuilder builder,Recipe recipe){
        double size = Math.sqrt(recipe.getCoordinate().size());
        Map<Integer, Matter> map = new HashMap<>();
        builder.append(nl);
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
            builder.append(nl);
        }

        for(Map.Entry<Integer,Matter> entry : map.entrySet()){
            builder.append(nl);
            builder.append(String.format("%02d -> %s",entry.getKey(),nl));
            String info = entry.getValue().info();
            info = info.replace(nl,nl+"  ");
            builder.append(info);
        }



        return builder.toString();
    }

    private String amorphous(StringBuilder builder,Recipe recipe){
        builder.append(nl+"List : ");
        StringBuilder names = new StringBuilder();
        recipe.getContentsNoDuplicateRelateAmount().entrySet().forEach(s->{
            String name = s.getKey().getName().isEmpty() ? s.getKey().getCandidate().get(0).name() : s.getKey().getName();
            names.append(String.format("%s -> %d | ",name,s.getValue()));
        });
        builder.append(names);
        builder.append(nl);
        for(Matter matter : recipe.getContentsNoDuplicate()){
            builder.append(nl);
            String info = matter.info();
            info = info.replace(nl,nl+"  ");
            builder.append(info);
        }
        return builder.toString();
    }
}
