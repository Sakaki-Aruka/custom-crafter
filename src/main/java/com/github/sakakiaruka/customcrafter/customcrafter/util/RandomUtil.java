package com.github.sakakiaruka.customcrafter.customcrafter.util;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RandomUtil {

    private static final String ALL_MATERIAL_REGEX_PATTERN = "(" + Arrays.stream(Material.values()).map(Enum::name).collect(Collectors.joining("|")) + ")";
    public static Material getRandomMaterial(String formula) {
        // when not found from the specified formula, returns Material.AIR
        final String pattern = "random\\[([a-z_0-9!,]+)]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula.toLowerCase());
        if (!parsed.matches()) return Material.AIR;
        Set<Material> candidate = new HashSet<>();
        for (String element : parsed.group(1).split(",")) {
            boolean isIgnore = element.startsWith("!");
            element = element.replace("!", "");
            if (element.matches(ALL_MATERIAL_REGEX_PATTERN)) {
                if (isIgnore) candidate.remove(Material.valueOf(element.toUpperCase()));
                else candidate.add(Material.valueOf(element.toUpperCase()));
            } else if (element.equals("block")) {
                if (isIgnore) candidate.removeAll(getBlockMaterials());
                else candidate.addAll(getBlockMaterials());
            } else if (element.equals("burnable")) {
                if (isIgnore) candidate.removeAll(getBurnableMaterials());
                else candidate.addAll(getBurnableMaterials());
            } else if (element.equals("collidable")) {
                if (isIgnore) candidate.removeAll(getCollidableMaterials());
                else candidate.addAll(getCollidableMaterials());
            } else if (element.equals("edible")) {
                if (isIgnore) candidate.removeAll(getEdibleMaterials());
                else candidate.addAll(getEdibleMaterials());
            } else if (element.equals("flammable")) {
                if (isIgnore) candidate.removeAll(getFlammableMaterials());
                else candidate.addAll(getFlammableMaterials());
            } else if (element.equals("fuel")) {
                if (isIgnore) candidate.removeAll(getFuelMaterials());
                else candidate.addAll(getFuelMaterials());
            } else if (element.equals("item")) {
                if (isIgnore) candidate.removeAll(getItemMaterials());
                else candidate.removeAll(getItemMaterials());
            } else if (element.equals("occluding")) {
                if (isIgnore) candidate.removeAll(getOccludingMaterials());
                else candidate.addAll(getOccludingMaterials());
            } else if (element.equals("record")) {
                if (isIgnore) candidate.removeAll(getRecordMaterials());
                else candidate.addAll(getRecordMaterials());
            } else if (element.equals("solid")) {
                if (isIgnore) candidate.removeAll(getSolidMaterials());
                else candidate.addAll(getSolidMaterials());
            }
        }
        if (candidate.isEmpty()) return Material.AIR;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }

    public static Set<Material> getBlockMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toSet());
    }

    public static Set<Material> getBurnableMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isBurnable).collect(Collectors.toSet());
    }

    public static Set<Material> getCollidableMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isCollidable).collect(Collectors.toSet());
    }

    public static Set<Material> getEdibleMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isEdible).collect(Collectors.toSet());
    }

    public static Set<Material> getFlammableMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isFlammable).collect(Collectors.toSet());
    }

    public static Set<Material> getFuelMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isFuel).collect(Collectors.toSet());
    }

    public static Set<Material> getItemMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isItem).collect(Collectors.toSet());
    }

    public static Set<Material> getOccludingMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isOccluding).collect(Collectors.toSet());
    }

    public static Set<Material> getRecordMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isRecord).collect(Collectors.toSet());
    }

    public static Set<Material> getSolidMaterials() {
        return Arrays.stream(Material.values()).filter(Material::isSolid).collect(Collectors.toSet());
    }
}
