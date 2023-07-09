package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.upperArrow;

public class RecipePermissionUtil{

    public static Map<String,RecipePermission> recipePermissionMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<UUID,List<RecipePermission>> playerPermissions = Collections.synchronizedMap(new HashMap<>());



    public void makeRecipePermissionMap(List<RecipePermission> list){
        list.forEach(s-> recipePermissionMap.put(s.getPermissionName(),s));
        recipePermissionMap.put("ROOT",RecipePermission.ROOT);
    }

    public void playerPermissionWriter(Path path){
        Map<String,List<String>> map = new HashMap<>();
        synchronized (playerPermissions) {
            Iterator<Map.Entry<UUID,List<RecipePermission>>> iterator = playerPermissions.entrySet().iterator();
            while (iterator.hasNext()) {
                for(RecipePermission perm : iterator.next().getValue()) {
                    String permStr = perm.getPermissionName();
                    if(!map.containsKey(permStr)) map.put(permStr, new ArrayList<>());
                    map.get(permStr).add(iterator.next().getKey().toString());
                }
            }
        }

        path.toFile().delete(); // file delete
        File newFile = new File(path.toString()); // To create a new file. (same name)
        try{
            newFile.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(newFile);

        for(Map.Entry<String,List<String>> entry : map.entrySet()){
            if(!config.contains(entry.getKey())) config.createSection(entry.getKey());
            config.set(entry.getKey(),entry.getValue());
            try{
                config.save(path.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
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


    private void createFileWrapper(Path path) {
        if (path.toFile().exists()) return;
        try{
            Files.createFile(path);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void permissionRelateLoad(Path path){
        createFileWrapper(path);
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        /*
        * (example)
        * ROOT:
        *   - 069a79f444e94726a5befca90e38aaf5
        * Potion:
        *   - af74a02d19cb445bb07f6866a861f783
        *
         */

        synchronized (recipePermissionMap) {
            Iterator<String> iterator = recipePermissionMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if(!config.contains(key)) continue;
                for (String id : config.getStringList(key)) {
                    UUID uuid = UUID.fromString(id);
                    if(!playerPermissions.containsKey(uuid)) playerPermissions.put(uuid, new ArrayList<>());
                    playerPermissions.get(uuid).add(recipePermissionMap.get(key));
                }
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
        createFileWrapper(path);
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
        if(list.size()==1) return list;
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

    public boolean containsPermission(Player player, RecipePermission target){
        if(!playerPermissions.containsKey(player.getUniqueId())) return false;
        if(playerPermissions.get(player.getUniqueId()).isEmpty()) return false;
        List<RecipePermission> list = playerPermissions.get(player.getUniqueId());
        for(RecipePermission perm : list){
            if(!inSameTree(perm,target)) continue;
            if(isUpper(perm,target) || perm.equals(target)) return true;
        }
        return false;
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
