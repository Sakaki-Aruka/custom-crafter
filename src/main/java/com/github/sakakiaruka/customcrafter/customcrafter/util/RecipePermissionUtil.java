package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.command.PermissionCheck;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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

    public void playerPermissionWriter(Path path){
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        Map<String,List<String>> map = new HashMap<>();
        for(Map.Entry<UUID,List<RecipePermission>> entry : playerPermissions.entrySet()){
            UUID player = entry.getKey();
            for(RecipePermission perm : entry.getValue()){
                String permStr = perm.getPermissionName();
                if(!map.containsKey(permStr)) map.put(permStr,new ArrayList<>());
                map.get(permStr).add(player.toString());
            }
        }

        for(Map.Entry<String,List<String>> entry : map.entrySet()){
            config.set(entry.getKey(),entry.getValue());
        }
    }

    public boolean hasPermission(RecipePermission perm, Player player){
        UUID uuid = player.getUniqueId();
        if(!playerPermissions.containsKey(uuid)) return false;
        List<RecipePermission> perms = playerPermissions.get(uuid);
        for(RecipePermission rp : perms){
            if(rp.getPermissionName().equals(perm.getPermissionName())) return true;
        }
        return false;
    }


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


    public List<RecipePermission> removePermissionDuplications(List<RecipePermission> list){
        List<RecipePermission> sorted = permissionSort(list);
        Set<RecipePermission> removeBuffer = new HashSet<>();
        for(RecipePermission perm : sorted){
            int index = sorted.indexOf(perm);
            for(RecipePermission underPerm : sorted.subList(index+1,sorted.size())){
                if(inSameTree(perm,underPerm)) removeBuffer.add(underPerm);
            }
        }

        list.removeAll(removeBuffer);
        return list;
    }

    private List<RecipePermission> permissionSort(List<RecipePermission> list){
        // parents length: short -> long
        Map<Integer,List<RecipePermission>> map = new TreeMap<>();
        if(list.isEmpty()) return new ArrayList<>();
        for(RecipePermission perm : list){
            List<RecipePermission> l = new ArrayList<>();
            l.add(perm);
            recourse(perm,l);
            if(!map.containsKey(l.size())) map.put(l.size(),new ArrayList<>());
            map.get(l.size()).add(perm);
        }

        List<RecipePermission> result = new ArrayList<>();
        for(Map.Entry<Integer,List<RecipePermission>> entry : map.entrySet()){
            result.addAll(entry.getValue());
        }
        return result;
    }



    public boolean inSameTree(RecipePermission a, RecipePermission b){
        if(a.equals(b)) return true;
        List<RecipePermission> as = new ArrayList<>();
        List<RecipePermission> bs = new ArrayList<>();
        as.add(a);
        bs.add(b);
        recourse(a,as);
        recourse(b,bs);
        if(as.isEmpty() || bs.isEmpty()) return true; // When either one is "ROOT"

        RecipePermission point = getLonger(as,bs).equals(as) ? b : a;
        for(RecipePermission rp : getLonger(as,bs)){
            if(rp.equals(point)) return true;
        }
        return false;
    }

    private List<RecipePermission> getLonger(List<RecipePermission> a, List<RecipePermission> b){
        if(a.size() == b.size()) return a;
        return a.size() > b.size() ? a : b;
    }


    public boolean isUpper(RecipePermission source, RecipePermission target){
        // permission order: source > target (true) | source == target (false) | source < target (false)
        if(source.equals(target)) return false;
        if(!inSameTree(source,target)) return false;
        List<RecipePermission> sL = new ArrayList<>();
        List<RecipePermission> tL = new ArrayList<>();
        recourse(source,sL);
        recourse(target,tL);
        return sL.size() < tL.size(); // parent long -> nearer a bottom of the permission
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
