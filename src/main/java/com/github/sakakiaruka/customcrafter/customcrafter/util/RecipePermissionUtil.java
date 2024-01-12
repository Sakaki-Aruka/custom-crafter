package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.UPPER_ARROW;

public class RecipePermissionUtil{

    public static Map<String,RecipePermission> RECIPE_PERMISSION_MAP = Collections.synchronizedMap(new HashMap<>());
    public static Map<UUID,Set<RecipePermission>> PLAYER_PERMISSIONS = Collections.synchronizedMap(new HashMap<>());


    private static final String PERMISSIONS_PATTERN = "name=(.+)/parent=(.+)";


    public static void makeRecipePermissionMap(List<RecipePermission> list){
        list.forEach(s-> RECIPE_PERMISSION_MAP.put(s.getPermissionName(),s));
        RECIPE_PERMISSION_MAP.put("ROOT",RecipePermission.ROOT);
    }

    public static void playerPermissionWriter(Path path){

        /*
         * (example)
         * ROOT:
         *   - 069a79f444e94726a5befca90e38aaf5
         * Potion:
         *   - af74a02d19cb445bb07f6866a861f783
         *
         */

        Map<String, List<String>> map = new HashMap<>();

        for (Map.Entry<UUID, Set<RecipePermission>> entry : PLAYER_PERMISSIONS.entrySet()) {
            for (RecipePermission perm : entry.getValue()) {
                if (!map.containsKey(perm.getPermissionName())) map.put(perm.getPermissionName(), new ArrayList<>());
                map.get(perm.getPermissionName()).add(entry.getKey().toString());
            }
        }

        permissionRelateLoad(path); // to get diff
        Map<String, List<String>> oldPermission = new HashMap<>();
        for (Map.Entry<UUID, Set<RecipePermission>> entry: PLAYER_PERMISSIONS.entrySet()) {
            for (RecipePermission perm : entry.getValue()) {
                if (!oldPermission.containsKey(perm.getPermissionName())) oldPermission.put(perm.getPermissionName(), new ArrayList<>());
                oldPermission.get(perm.getPermissionName()).add(entry.getKey().toString());
            }
        }

        Set<String> removeSections = new HashSet<>();
        Set<String> addSections = new HashSet<>();
        Set<String> refreshSections = new HashSet<>();

        for (String perm : oldPermission.keySet()) {
            if (!map.containsKey(perm)) removeSections.add(perm);
            if (map.containsKey(perm)) refreshSections.add(perm);
        }

        for (String perm : map.keySet()) {
            if (!oldPermission.containsKey(perm)) addSections.add(perm);
        }

        File file = new File(path.toString());
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        setWrapper(removeSections, null, config, file);  // remove
        setWrapper(addSections, map, config, file);  // add

        setWrapper(refreshSections, null, config, file); // refresh first step (clear)
        setWrapper(refreshSections, map, config, file);  // refresh second step (add)
    }

    private static void setWrapper(Set<String> set, Map<String, List<String>> map, FileConfiguration config, File file) {
        for (String element : set) {
            config.set(element, map == null ? null : map.get(element));
            try {
                config.save(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasPermission(RecipePermission perm, Player player){
        UUID uuid = player.getUniqueId();
        if(!PLAYER_PERMISSIONS.containsKey(uuid)) return false;
        Set<RecipePermission> perms = PLAYER_PERMISSIONS.get(uuid);
        for(RecipePermission rp : perms){
            if(rp.getPermissionName().equals(perm.getPermissionName())) return true;
        }
        return false;
    }


    private static boolean createFileWrapper(Path path) {
        // true = The file maybe has contents.
        // false = The file has not any contents.
        if (path.toFile().exists()) return true;
        try{
            Files.createFile(path);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void permissionRelateLoad(Path path){
        if (!createFileWrapper(path)) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        /*
        * (example)
        * ROOT:
        *   - 069a79f444e94726a5befca90e38aaf5
        * Potion:
        *   - af74a02d19cb445bb07f6866a861f783
        *
         */

        synchronized (RECIPE_PERMISSION_MAP) {
            for (String key : RECIPE_PERMISSION_MAP.keySet()) {
                if (!config.contains(key)) continue;
                for (String id : config.getStringList(key)) {
                    UUID uuid = UUID.fromString(id);
                    if (!PLAYER_PERMISSIONS.containsKey(uuid)) PLAYER_PERMISSIONS.put(uuid, new HashSet<>());
                    PLAYER_PERMISSIONS.get(uuid).add(RECIPE_PERMISSION_MAP.get(key));
                }
            }
        }
    }

    public static void permissionSettingsLoad(Path path){
        // to collect RecipePermission settings from the config file.
        /*
        * (example)
        * permissions:
        *   - name:Potion|parent:ROOT
        *   - name:SpeedPotion|parent:Potion
         */
        if (!createFileWrapper(path)) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        List<String> data = config.getStringList("permissions");
        List<RecipePermission> permissionList = new ArrayList<>();
        for(String perm : data){

            Matcher matcher = Pattern.compile(PERMISSIONS_PATTERN).matcher(perm);
            if (!matcher.matches()) continue;
            String name = matcher.group(1);
            String parent = matcher.group(2);
            RecipePermission permission = new RecipePermission(parent, name);
            permissionList.add(permission);
        }
        makeRecipePermissionMap(permissionList);
    }

    private static List<RecipePermission> permissionSort(List<RecipePermission> list){
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


    public static boolean inSameTree(RecipePermission a, RecipePermission b){
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

    private static List<RecipePermission> getLonger(List<RecipePermission> a, List<RecipePermission> b){
        if(a.size() == b.size()) return a;
        return a.size() > b.size() ? a : b;
    }


    public static boolean isUpper(RecipePermission source, RecipePermission target){
        // permission order: source > target (true) | source == target (false) | source < target (false)
        if(source.equals(target)) return false;
        if(!inSameTree(source,target)) return false;
        List<RecipePermission> sL = new ArrayList<>();
        List<RecipePermission> tL = new ArrayList<>();
        recourse(source,sL);
        recourse(target,tL);
        return sL.size() < tL.size(); // parent long -> nearer a bottom of the permission
    }

    public static boolean containsPermission(Player player, RecipePermission target){
        if(!PLAYER_PERMISSIONS.containsKey(player.getUniqueId())) return false;
        if(PLAYER_PERMISSIONS.get(player.getUniqueId()).isEmpty()) return false;
        Set<RecipePermission> list = PLAYER_PERMISSIONS.get(player.getUniqueId());
        for(RecipePermission perm : list){
            if(!inSameTree(perm,target)) continue;
            if(isUpper(perm,target) || perm.equals(target)) return true;
        }
        return false;
    }


    private static void recourse(RecipePermission rp, List<RecipePermission> list){
        if(rp.equals(RecipePermission.ROOT)) return;
        RecipePermission parent = RECIPE_PERMISSION_MAP.get(rp.getParent());
        list.add(parent);
        recourse(parent,list);
    }

    public static String getPermissionTree(RecipePermission perm){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("=== Permission Info ===%s", LINE_SEPARATOR));
        if(perm.equals(RecipePermission.ROOT)){
            // only ROOT
            builder.append(String.format("%s%s - Parent : --- %s - Name : ROOT%s%s%s",RecipePermission.ROOT.getPermissionName(), LINE_SEPARATOR, LINE_SEPARATOR, LINE_SEPARATOR, LINE_SEPARATOR,"=== Permission Info End ==="));
            return builder.toString();
        }
        List<RecipePermission> permList = new ArrayList<>();
        permList.add(perm); // add self
        recourse(perm,permList);
        Collections.reverse(permList); // ~,...,ROOT -> ROOT,~,...~
        for(RecipePermission rp : permList){
            String arrow = String.join("",Collections.nCopies(permList.indexOf(rp), UPPER_ARROW));
            String data = String.format("%s - Parent : %s %s - Name : %s %s", LINE_SEPARATOR,rp.getParent(), LINE_SEPARATOR,rp.getPermissionName(), LINE_SEPARATOR);
            builder.append(String.format("%s%s%s%s", LINE_SEPARATOR,arrow, LINE_SEPARATOR,data));
        }
        builder.append(LINE_SEPARATOR).append("=== Permission Info End ===").append(LINE_SEPARATOR);
        return builder.toString();
    }

}
