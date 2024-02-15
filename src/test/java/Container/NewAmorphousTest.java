package Container;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.inventory.ItemFactoryMock;
import be.seeseemelk.mockbukkit.inventory.meta.ItemMetaMock;
import be.seeseemelk.mockbukkit.persistence.PersistentDataContainerMock;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.AnchorTagType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.ContainerType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.MatterContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class NewAmorphousTest {

    private ServerMock server;
    private CustomCrafter plugin;


    @BeforeEach
    void setup() {
        server = MockBukkit.getMock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void derived_data_test() {
        int size = new Random().nextInt(10);
        Assertions.assertEquals(2, size / 2 - (size % 2 == 0 ? 1 : 0));
    }

    @Test
    public void placeholder_test() {
        Map<String, String> data = new HashMap<>();
        data.put("test_container_1", "20");
        data.put("test_container_2", "30");
        data.put("test_container_3", "40");
        String formula1 = "{%test_container_1% * 2}";
        String formula2 = "{\\%test_container_1\\% = %test_container_1%}";
        Assertions.assertEquals("{20 * 2}", ContainerUtil.setPlaceholderValue(data, formula1));
        Assertions.assertEquals("{%test_container_1% = 20}", ContainerUtil.setPlaceholderValue(data, formula2));
        Assertions.assertEquals("{None}", ContainerUtil.setPlaceholderValue(data, "{%a%}"));
        Assertions.assertEquals("{a}", ContainerUtil.setPlaceholderValue(data, "{%a}"));
    }

    private String getValue(Map<String, String> data, String formula) {
        return ContainerUtil.setEvalValue(ContainerUtil.setPlaceholderValue(data, formula));
    }

    @Test
    public void eval_test() {
        Map<String, String> data = new HashMap<>();
        data.put("test_container_1", "20");
        data.put("test_container_2", "30");
        data.put("test_container_3", "40");
        String formula = "{20 * 2}";
        Assertions.assertEquals("40.0", getValue(data, formula));
        String formula2 = "{%test_container_1% * 2}";
        Assertions.assertEquals("40.0", getValue(data, formula2));
        String formula3 = "{(%test_container_1%+%test_container_2%) * 2}";
        Assertions.assertEquals("100.0", getValue(data, formula3));
        Assertions.assertEquals("8.0", getValue(null, "{2^3}"));
        Assertions.assertEquals("1.0", getValue(null, "{3\\%2}"));
        Assertions.assertEquals(String.valueOf(Math.pow(2, (double) 1 /2)), getValue(null, "{2^(1/2)}"));
        Assertions.assertEquals("{test}", getValue(null, "\\{test\\}"));
        Assertions.assertEquals("{tc_1} = 20", getValue(data, "\\{tc_1\\} = %test_container_1%"));
        Assertions.assertEquals("false", getValue(null, "{None==50}"));
        Assertions.assertEquals("true", getValue(data, "{(%test_container_1%+%test_container_2%)*2<(%test_container_3%*4)}"));
    }

    @Test
    @Deprecated
    public void string_match_test() {
        // "HellO, worLd" matches "(?i)(hello, world)" test.
        Recipe recipe = getDirtRecipe();
        Recipe input = new Recipe();
        Matter matter1 = new Matter(List.of(Material.DIRT), 1);
        Matter matter2 = new Matter(List.of(Material.DIRT), 1);
        Matter matter3 = new Matter(List.of(Material.COARSE_DIRT), 1);
        PersistentDataContainerMock container1 = new PersistentDataContainerMock();
        PersistentDataContainerMock container2 = new PersistentDataContainerMock();
        PersistentDataContainerMock container3 = new PersistentDataContainerMock();
        container1.set(
                new NamespacedKey("custom_crafter", "long-test_container_1"),
                PersistentDataType.LONG,
                20L);
        container2.set(
                new NamespacedKey("custom_crafter", "long-test_container_2"),
                PersistentDataType.LONG,
                30L
        );
        container3.set(
                new NamespacedKey("custom_crafter", "string-test_container_1"),
                PersistentDataType.STRING,
                "HellO"
        );
        container3.set(
                new NamespacedKey("custom_crafter", "string-test_container_2"),
                PersistentDataType.STRING,
                "worLd"
        );
        matter1.setPDC(container1);
        matter2.setPDC(container2);
        matter3.setPDC(container3);
        Map<Coordinate, Matter> map = new HashMap<>();
        map.put(new Coordinate(0, 0), matter1);
        map.put(new Coordinate(0, 1), matter2);
        map.put(new Coordinate(0, 2), matter3);
        input.setCoordinate(map);

        MatterContainer mc1 = new MatterContainer(
                ContainerUtil.STRING_MATCH,
                ContainerType.ALLOW_VALUE,
                "2:(?i)(hello, world),%test_container_1%, %test_container_2%"
        );
        recipe.getMatterFromCoordinate(new Coordinate(-1, 2)).setContainers(List.of(mc1));

        Map<String, String> result = asStringMap(sortXYNatural(ContainerUtil._amorphous(recipe, input)));
        Assertions.assertEquals("[x=0,y=2]", result.get("[x=-1,y=0]"));

    }

    @Test
    @Deprecated
    public void allow_value_test() {
        Recipe recipe = getDirtRecipe();
        MatterContainer container1 = new MatterContainer(
                ContainerUtil.VALUE_ALLOW,
                ContainerType.ALLOW_VALUE,
                "{(%test_container_3% / 2)<=20}"
        );
        recipe.
                getMatterFromCoordinate(new Coordinate(-1, 0)).
                setContainers(List.of(container1));
        recipe.
                getMatterFromCoordinate(new Coordinate(-1, 1)).
                setContainers(List.of(container1));
        Recipe input = getDirtInput();

        Map<String, String> result = asStringMap(sortXYNatural(ContainerUtil._amorphous(recipe, input)));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1],[x=0,y=2]", result.get("[x=-1,y=0]"));
    }

    @Test
    @Deprecated
    public void deny_value_test() {
        Recipe recipe = getDirtRecipe();
        Recipe input = getDirtInput();
        MatterContainer container = new MatterContainer(
                ContainerUtil.VALUE_DENY,
                ContainerType.DENY_VALUE,
                "{%test_container_4%==50}"
        );
        recipe.getMatterFromCoordinate(new Coordinate(-1, 0)).setContainers(List.of(container));

        Map<String, String> result = asStringMap(sortXYNatural(ContainerUtil._amorphous(recipe, input)));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1]", result.get("[x=-1,y=0]"));
    }


    @Test
    @Deprecated
    public void allow_tag_test() {
        // --- generate a recipe. === //
        Recipe recipe = getDirtRecipe();

        // === generate an input inventory === //
        Recipe input = getDirtInput();

        Map<Coordinate, List<Coordinate>> map = ContainerUtil._amorphous(recipe, input);
        display(map);
        Map<String, String> result = asStringMap(sortXYNatural(map));
        //display(ContainerUtil._amorphous(recipe, input));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1],[x=0,y=2]", result.get("[x=-1,y=0]"));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1],[x=0,y=2]", result.get("[x=-1,y=1]"));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1],[x=0,y=2]", result.get("[x=-1,y=2]"));
    }

    @Test
    @Deprecated
    public void deny_tag_test() {
        Recipe recipe = getDirtRecipe();
        Recipe input = getDirtInput();

        MatterContainer container = new MatterContainer(
                ContainerUtil.TAG_DENY,
                ContainerType.DENY_TAG,
                "test_container_4"
        );

        recipe.getMatterFromCoordinate(new Coordinate(-1, 0)).setContainers(List.of(container));
        recipe.getMatterFromCoordinate(new Coordinate(-1, 1)).setContainers(List.of(container));

        Map<String, String> result = asStringMap(sortXYNatural(ContainerUtil._amorphous(recipe, input)));
        Assertions.assertEquals("[x=0,y=0],[x=0,y=1]", result.get("[x=-1,y=0]"));
    }

    @Test
    @Deprecated
    public void normal_recipe_test() {
        // mock of ContainerUtil#isPass(ItemStack, Matter)
        Matter matter = new Matter(List.of(Material.DIRT), 1);
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        pdc.set(
                new NamespacedKey("custom_crafter", "test_container_1.double"),
                PersistentDataType.DOUBLE,
                20.0);
        MatterContainer container = new MatterContainer(
                ContainerUtil.VALUE_ALLOW,
                ContainerType.ALLOW_VALUE,
                "{(%test_container_1.double%*2)==40.0}"
        );

        matter.setContainers(List.of(container));

        int count = 0;
        Map<String, String> data = ContainerUtil.getData(pdc);
        for (MatterContainer c : matter.getContainers()) {
            if (c.judge(data)) count++;
        }

        Assertions.assertEquals(count, matter.getContainers().size());
    }

    @Test
    @Deprecated
    public void get_anchor_data_test() {
        PersistentDataContainer pdc1 = new PersistentDataContainerMock();
        pdc1.set(
                new NamespacedKey("custom_crafter", "pdc1.anchor"),
                new AnchorTagType(),
                UUID.randomUUID()
        );

        Map<String, String> result = ContainerUtil.getData(pdc1);
        Assertions.assertEquals("{pdc1.anchor=}", result.toString());

        Assertions.assertEquals(
                true,
                ContainerUtil.TAG_ALLOW.apply(result, "pdc1.anchor"));

        pdc1.set(
                new NamespacedKey("custom_crafter", "pdc1_2.anchor"),
                new AnchorTagType(),
                UUID.randomUUID()
        );

        Map<String, String> result2 = ContainerUtil.getData(pdc1);
        Assertions.assertEquals(
                true,
                ContainerUtil.TAG_ALLOW.apply(result2, "pdc1.anchor,pdc1_2.anchor")
        );
    }


    private Recipe getDirtRecipe() {
        Recipe recipe = new Recipe();
        recipe.setName("test_1");
        recipe.setTag(Tag.AMORPHOUS);
        recipe.setResult(new Result("test_result_1", null, 1, null, "DIAMOND", -1));

        Map<Coordinate, Matter> dirtCoordinate = new HashMap<>();
        Matter dirt = new Matter("test_matter_1",  List.of(Material.DIRT), null, 1, false);
        MatterContainer dirtContainer = new MatterContainer(
                ContainerUtil.TAG_ALLOW,
                ContainerType.ALLOW_TAG,
                "test_container_1,test_container_2,test_container_3"
        );
        dirt.setContainers(List.of(dirtContainer));

        // A recipe (named "dirt") requires x3 dirt those are required to contain a data container.
        dirtCoordinate.put(new Coordinate(-1, 0), dirt);
        dirtCoordinate.put(new Coordinate(-1, 1), dirt);
        dirtCoordinate.put(new Coordinate(-1, 2), dirt);
        recipe.setCoordinate(dirtCoordinate);
        return recipe;
    }

    @Deprecated
    private Recipe getDirtInput() {
        // test_container -> tc
        // container-1 = "tc_1=20, tc_2=30, tc_3=40;
        // container-2 = "tc_1=20, tc_2=30, tc_3=40, tc_4=50;
        Recipe input = new Recipe();
        input.setName("test_input_1");
        input.setTag(Tag.NORMAL);
        input.setResult(null);

        Map<Coordinate, Matter> inputCoordinate_1 = new HashMap<>();
        Matter inputMatter_1 = new Matter(
                "test_input_matter_1",
                List.of(Material.DIRT, Material.COARSE_DIRT),
                null,
                1,
                false
        );

        PersistentDataContainerMock containerMock_1 = new PersistentDataContainerMock();
        containerMock_1.set(
                new NamespacedKey("custom_crafter", "test_container_1.long"),
                PersistentDataType.LONG,
                20L
        );

        containerMock_1.set(
                new NamespacedKey("custom_crafter", "test_container_2.long"),
                PersistentDataType.LONG,
                30L
        );

        containerMock_1.set(
                new NamespacedKey("custom_crafter", "test_container_3.long"),
                PersistentDataType.LONG,
                40L
        );

        inputMatter_1.setPDC(containerMock_1);
        inputCoordinate_1.put(new Coordinate(0, 0), inputMatter_1);
        inputCoordinate_1.put(new Coordinate(0, 1), inputMatter_1);

        Matter inputMatter_2 = new Matter(
                "test_input_matter_2",
                List.of(Material.DIRT, Material.COARSE_DIRT),
                null,
                1,
                false
        );

        PersistentDataContainerMock containerMock_2 = new PersistentDataContainerMock();
        containerMock_2.set(
                new NamespacedKey("custom_crafter", "test_container_1.long"),
                PersistentDataType.LONG,
                20L
        );

        containerMock_2.set(
                new NamespacedKey("custom_crafter", "test_container_2.long"),
                PersistentDataType.LONG,
                30L
        );

        containerMock_2.set(
                new NamespacedKey("custom_crafter", "test_container_3.long"),
                PersistentDataType.LONG,
                40L
        );

        containerMock_2.set(
                new NamespacedKey("custom_crafter", "test_container_4.long"),
                PersistentDataType.LONG,
                50L
        );

        inputMatter_2.setPDC(containerMock_2);
        inputCoordinate_1.put(new Coordinate(0, 2), inputMatter_2);
        input.setCoordinate(inputCoordinate_1);
        return input;
    }


    private void display(Map<Coordinate, List<Coordinate>> data) {
        for (Map.Entry<Coordinate, List<Coordinate>> entry : data.entrySet()) {
            System.out.print(entry.getKey().toString() + " => ");
            for (Coordinate c : entry.getValue()) {
                System.out.print(c.toString() + ",");
            }
            System.out.println();
        }
    }

    private Map<Coordinate, List<Coordinate>> sortXYNatural(Map<Coordinate,List<Coordinate>> data) {
        Map<Coordinate, List<Coordinate>> result = new HashMap<>();

        for (Map.Entry<Coordinate, List<Coordinate>> entry : data.entrySet()) {
            Map<Integer, List<Integer>> temporary = new HashMap<>();
            for (Coordinate c : entry.getValue()) {
                int x = c.getX();
                int y = c.getY();
                if (!temporary.containsKey(x)) temporary.put(x, new LinkedList<>());
                temporary.get(x).add(y);
            }

            for (int x = -1; x < 6; x++) {
                if (!temporary.containsKey(x)) continue;
                Collections.sort(temporary.get(x));
                for (int y : temporary.get(x)) {
                    Coordinate c = new Coordinate(x, y);
                    if (!result.containsKey(entry.getKey())) result.put(entry.getKey(), new LinkedList<>());
                    result.get(entry.getKey()).add(c);
                }
            }
        }
        return result;
    }

    private Map<String, String> asStringMap(Map<Coordinate, List<Coordinate>> data) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Coordinate, List<Coordinate>> entry : data.entrySet()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < entry.getValue().size();i++) {
                builder.append(entry.getValue().get(i));
                if (i != entry.getValue().size() - 1) builder.append(",");
            }
            map.put(entry.getKey().toString(), builder.toString());
        }
        return map;
    }
}
