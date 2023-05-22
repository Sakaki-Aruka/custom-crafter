package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Path;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.upperArrow;

public class RecipePermissionUtil{

    public static Map<String,RecipePermission> recipePermissionMap = new HashMap<>();
    public static Map<UUID,List<RecipePermission>> playerPermissions = new HashMap<>();



    public void makeRecipePermissionMap(List<RecipePermission> list){
        list.forEach(s-> recipePermissionMap.put(s.getPermissionName(),s));
        recipePermissionMap.put("ROOT",RecipePermission.ROOT);
    }

    // TODO : write a data writer about playerPermissions
    public void permissionRelateLoad(Path path){
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        /*
        * (example)
        * ROOT:
        *   - 069a79f444e94726a5befca90e38aaf5
        * Potion:
        *   - af74a02d19cb445bb07f6866a861f783
        *
         */
        for(String key : recipePermissionMap.keySet()){
            if(!config.contains(key)) continue;
            for(String id : config.getStringList(key)){
                UUID uuid = UUID.fromString(id);
                if(!playerPermissions.containsKey(uuid)) playerPermissions.put(uuid,new ArrayList<>());
                playerPermissions.get(uuid).add(recipePermissionMap.get(key));
            }
        }
    }

    public void permissionSettingsLoad(Path path){
        // to collect RecipePermission settings from the config file.
        /*
        * (example)
        * permissions:
        *   - name:Potion|parent:ROOT
        *   - name:SpeedPotion|parent:Potion
         */
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        List<String> data = config.getStringList("permissions");
        List<RecipePermission> permissionList = new ArrayList<>();
        for(String perm : data){
            List<String> list = Arrays.asList(perm.split("!"));
            String name = list.get(0).replace("name:","");
            String parent = list.get(1).replace("parent:","");
            RecipePermission permission = new RecipePermission(parent,name);
            permissionList.add(permission);
        }
        makeRecipePermissionMap(permissionList);
    }

    public void removePermissionConflicts(List<RecipePermission> list){
        //
        Map<Integer,List<RecipePermission>> related = new HashMap<>();
        for(RecipePermission perm : list){
            List<RecipePermission> rpList = new ArrayList<>();
            recourse(perm,rpList);
            if(!related.containsKey(rpList.size())) related.put(rpList.size(),new ArrayList<>());
            related.get(rpList.size()).add(perm);
        }

        related.remove(0); // remove "ROOT"

        Set<RecipePermission> removeBuffer = new HashSet<>();
        for(Map.Entry<Integer,List<RecipePermission>> entry : related.entrySet()){
            int gen = entry.getKey();
            List<RecipePermission> source = entry.getValue();
            List<RecipePermission> under = new ArrayList<>();
            for(Map.Entry<Integer,List<RecipePermission>> entry2 : related.entrySet()){
                if(entry2.getKey() <= gen) continue;
                under.addAll(entry2.getValue());
            }
            for(RecipePermission rp : under){
                for(RecipePermission rpp : source){
                    if(isInSameLine(rp,rpp)) removeBuffer.add(rp);
                }
            }
        }

        for(RecipePermission perm : removeBuffer){
            if(list.contains(perm)) list.remove(perm);
        }
    }


    public boolean permContains(RecipePermission target, RecipePermission source){
        // wrapper
        if(!isInSameLine(target,source)) return false;
        return isUpper(target, source) || isSame(target, source);
    }

    public boolean isInSameLine(RecipePermission target, RecipePermission source){
        List<RecipePermission> targetParents = new ArrayList<>();
        List<RecipePermission> sourceParents = new ArrayList<>();
        recourse(target, targetParents);
        recourse(source, sourceParents);
        if(targetParents.isEmpty() || sourceParents.isEmpty()) return true; // When either one is "ROOT".
        for(int i=0;i<Math.min(targetParents.size(), sourceParents.size());i++){
            if(!targetParents.get(i).getPermissionName().equals(sourceParents.get(i).getPermissionName())) return false;
        }
        return true;
    }
    public boolean isUpper(RecipePermission target, RecipePermission source){
        if(target.getParent().equals(RecipePermission.ROOT.getPermissionName())) {
            return source.getParent().equals(RecipePermission.ROOT.getPermissionName());
        }
        if(!isInSameLine(target, source)) return false;
        List<RecipePermission> targetParents = new ArrayList<>();
        List<RecipePermission> sourceParents = new ArrayList<>();
        recourse(target,targetParents);
        recourse(source,sourceParents);
        return targetParents.size() > sourceParents.size();

    }

    public boolean isUnder(RecipePermission target, RecipePermission source){
        if(!isInSameLine(target, source)) return false;
        return !isUpper(target, source);
    }

    public boolean isSame(RecipePermission target, RecipePermission source){
        return target.getPermissionName().equals(source.getPermissionName());
    }

    private void recourse(RecipePermission rp, List<RecipePermission> list){
        if(rp.equals(RecipePermission.ROOT)) return;
        RecipePermission parent = recipePermissionMap.get(rp.getParent());
        list.add(parent);
        recourse(parent,list);
    }

    public String getPermissionTree(RecipePermission perm){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("=== Permission Info ===%s",nl));
        if(perm.equals(RecipePermission.ROOT)){
            // only ROOT
            builder.append(String.format("%s%s - Parent : --- %s - Name : ROOT%s",RecipePermission.ROOT.getPermissionName(),nl,nl,nl));
            return builder.toString();
        }
        List<RecipePermission> permList = new ArrayList<>();
        permList.add(perm); // add self
        recourse(perm,permList);
        Collections.reverse(permList); // ~,...,ROOT -> ROOT,~,...~
        for(RecipePermission rp : permList){
            String arrow = String.join("",Collections.nCopies(permList.indexOf(rp),upperArrow));
            String data = String.format("%s - Parent : %s %s - Name : %s %s",nl,rp.getParent(),nl,rp.getPermissionName(),nl);
            builder.append(String.format("%s%s%s%s",nl,arrow,nl,data));
        }
        return builder.toString();
    }

}
