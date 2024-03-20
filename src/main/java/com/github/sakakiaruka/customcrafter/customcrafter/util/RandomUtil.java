package com.github.sakakiaruka.customcrafter.customcrafter.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;

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

    public static Material getRandomMaterial(String formula, Set<Material> limit) {
        final String pattern = "random\\[([a-z_0-9!,]+)]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula.toLowerCase());
        if (!parsed.matches()) return Material.AIR;
        Set<Material> candidate = new HashSet<>();

        for (String element : parsed.group(1).split(";")) {
            boolean isIgnore = element.startsWith("!");
            element = element.replace("!", "");
            if (element.equals("all")) {
                if (isIgnore) candidate.removeAll(Arrays.stream(Material.values()).collect(Collectors.toSet()));
                else candidate.addAll(Arrays.stream(Material.values()).collect(Collectors.toSet()));
            } else if (element.matches(ALL_MATERIAL_REGEX_PATTERN) && limit.contains(Material.valueOf(element.toUpperCase()))) {
                if (isIgnore) candidate.remove(Material.valueOf(element.toUpperCase()));
                else candidate.add(Material.valueOf(element.toUpperCase()));
            }
        }
        if (candidate.isEmpty()) return Material.AIR;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }

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
                if (isIgnore) candidate.removeAll(blockMaterials);
                else candidate.addAll(blockMaterials);
            } else if (element.equals("burnable")) {
                if (isIgnore) candidate.removeAll(burnableMaterials);
                else candidate.addAll(burnableMaterials);
            } else if (element.equals("collidable")) {
                if (isIgnore) candidate.removeAll(collidableMaterials);
                else candidate.addAll(collidableMaterials);
            } else if (element.equals("edible")) {
                if (isIgnore) candidate.removeAll(edibleMaterials);
                else candidate.addAll(edibleMaterials);
            } else if (element.equals("flammable")) {
                if (isIgnore) candidate.removeAll(flammableMaterials);
                else candidate.addAll(flammableMaterials);
            } else if (element.equals("fuel")) {
                if (isIgnore) candidate.removeAll(fuelMaterials);
                else candidate.addAll(fuelMaterials);
            } else if (element.equals("item")) {
                if (isIgnore) candidate.removeAll(itemMaterials);
                else candidate.removeAll(itemMaterials);
            } else if (element.equals("occluding")) {
                if (isIgnore) candidate.removeAll(occludingMaterials);
                else candidate.addAll(occludingMaterials);
            } else if (element.equals("record")) {
                if (isIgnore) candidate.removeAll(recordMaterials);
                else candidate.addAll(recordMaterials);
            } else if (element.equals("solid")) {
                if (isIgnore) candidate.removeAll(solidMaterials);
                else candidate.addAll(solidMaterials);
            } else if (element.equals("all")) {
                if (isIgnore) candidate.removeAll(Arrays.stream(Material.values()).collect(Collectors.toSet()));
                else candidate.addAll(Arrays.stream(Material.values()).collect(Collectors.toSet()));
            }
        }
        if (candidate.isEmpty()) return Material.AIR;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }

    public static final Set<Material> blockMaterials = Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toSet());
    public static final Set<Material> burnableMaterials = Arrays.stream(Material.values()).filter(Material::isBurnable).collect(Collectors.toSet());
    public static final Set<Material> collidableMaterials = Arrays.stream(Material.values()).filter(Material::isBlock).filter(Material::isCollidable).collect(Collectors.toSet());
    public static final Set<Material> edibleMaterials = Arrays.stream(Material.values()).filter(Material::isEdible).collect(Collectors.toSet());
    public static final Set<Material> flammableMaterials = Arrays.stream(Material.values()).filter(Material::isFlammable).collect(Collectors.toSet());
    public static final Set<Material> fuelMaterials = Arrays.stream(Material.values()).filter(Material::isFuel).collect(Collectors.toSet());
    public static final Set<Material> itemMaterials = Arrays.stream(Material.values()).filter(Material::isItem).collect(Collectors.toSet());
    public static final Set<Material> occludingMaterials = Arrays.stream(Material.values()).filter(Material::isOccluding).collect(Collectors.toSet());
    public static final Set<Material> recordMaterials = Arrays.stream(Material.values()).filter(Material::isRecord).collect(Collectors.toSet());
    public static final Set<Material> solidMaterials = Arrays.stream(Material.values()).filter(Material::isSolid).collect(Collectors.toSet());


    private static final String allDyeColorRegexPattern = "(" + Arrays.stream(DyeColor.values()).map(Enum::name).collect(Collectors.joining("|")) + ")";
    public static DyeColor getRandomDyeColor(String formula, DyeColor current) {
        // random[~~,~~,~~]
        final String pattern = "random\\[([a-zA-Z!,_]+)]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return null;
        Set<DyeColor> candidate = new HashSet<>();
        for (String element : parsed.group(1).split(",")) {
            boolean isIgnore = element.startsWith("!");
            element = element.replace("!", "");
            if (element.equals("all")) {
                if (isIgnore) Arrays.asList(DyeColor.values()).forEach(candidate::remove);
                else candidate.addAll(Arrays.asList(DyeColor.values()));
            } else if (element.equals("self")) {
                if (isIgnore) candidate.remove(current);
                else candidate.add(current);
            } else if (element.toUpperCase().matches(allDyeColorRegexPattern)) {
                if (isIgnore) candidate.remove(DyeColor.valueOf(element.toUpperCase()));
                else candidate.add(DyeColor.valueOf(element.toUpperCase()));
            }
        }
        if (candidate.isEmpty()) return null;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }

    public static float getRandomFloat(String formula, float min, float max) {
        // random[10.1:10.5:0.1]
        if (max < min) {
            float temp = max;
            max = min;
            min = temp;
        }
        final String pattern = "random\\[(-?[0-9]+(\\.[0-9]+)?)?:(-?[0-9]+(\\.[0-9]+)?)?]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return Float.MIN_VALUE;
        if (min == max) return max;
        if (parsed.group(1) == null && parsed.group(3) == null) {
            // [:]
            float total = max / 2 - min / 2;
            return min + total * new Random().nextFloat() * 2;
        } else if (parsed.group(1) != null && parsed.group(3) == null) {
            // [num:]
            float under = Float.parseFloat(parsed.group(1)) / 2;
            float total = max / 2 - under / 2;
            return under + total * new Random().nextFloat() * 2;
        } else if (parsed.group(1) == null && parsed.group(3) != null) {
            // [:num]
            float upper = Float.parseFloat(parsed.group(3));
            float total = upper / 2 - min / 2;
            return min + total * new Random().nextFloat() * 2;
        } else {
            // [num:num]
            float under = Float.parseFloat(parsed.group(1));
            float upper = Float.parseFloat(parsed.group(3));
            float total = upper / 2 - under / 2;
            return under + total * new Random().nextFloat() * 2;
        }
    }


    public static final Set<Particle> allParticles = Arrays.stream(Particle.values()).collect(Collectors.toSet());
    public static final String allParticlesRegexPattern = "(" + Arrays.stream(Particle.values()).map(Enum::name).collect(Collectors.joining("|")) + ")";
    public static Particle getRandomParticle(String formula) {
        // random[~~,~~,~~]
        final String pattern = "random\\[([a-zA-Z_0-9,!]+)]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) return null;
        Set<Particle> candidate = new HashSet<>();
        for (String element : parsed.group(1).split(",")) {
            boolean isIgnore = element.startsWith("!");
            element = element.replace("!", "");
            if (element.equals("all")) {
                if (isIgnore) candidate.removeAll(allParticles);
                else candidate.addAll(allParticles);
            } else if (element.toLowerCase().matches(allParticlesRegexPattern)) {
                if (isIgnore) candidate.remove(Particle.valueOf(element.toUpperCase()));
                else candidate.add(Particle.valueOf(element.toUpperCase()));
            }
        }
        if (candidate.isEmpty()) return null;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }

    public static final Set<Color> allColor = Set.of(
            Color.AQUA,
            Color.BLACK,
            Color.BLUE,
            Color.FUCHSIA,
            Color.GRAY,
            Color.LIME,
            Color.MAROON,
            Color.NAVY,
            Color.OLIVE,
            Color.PURPLE,
            Color.RED,
            Color.SILVER,
            Color.TEAL,
            Color.WHITE,
            Color.YELLOW
    );

    public static Color getRandomColor(String formula) {
        final String pattern = "random\\[([a-zA-Z_,!]+)]";
        if (!formula.matches(pattern)) return null;
        formula = formula.replaceAll("random|\\[|]", "");
        Set<Color> candidate = new HashSet<>();
        for (String element : formula.split(",")) {
            boolean isIgnore = element.startsWith("!");
            element = element.replace("!", "");
            if (element.matches("all")) {
                if (isIgnore) candidate.removeAll(allColor);
                else candidate.addAll(allColor);
            } else {
                Color color = InventoryUtil.getColor(element);
                if (color == null) continue;
                if (isIgnore) candidate.remove(color);
                else candidate.add(color);
            }
        }
        if (candidate.isEmpty()) return null;
        return new ArrayList<>(candidate).get(new Random().nextInt(candidate.size()));
    }
}