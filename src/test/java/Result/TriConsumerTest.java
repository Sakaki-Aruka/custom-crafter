package Result;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TriConsumerTest {
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
    public void set_placeholder_value_test() {
        Map<String, String> data = new HashMap<>();
        data.put("test.string", "hello");
        data.put("test2.string", "world");
        Assertions.assertEquals("hello, world", ContainerUtil.setEvalValue(ContainerUtil.setPlaceholderValue(data, "%test.string%, %test2.string%")));
        Assertions.assertEquals("None, None", ContainerUtil.setEvalValue(ContainerUtil.setPlaceholderValue(Collections.emptyMap(), "%test.string%, %test.string%")));
    }

//    @Test
//    public void consumer_test() {
//        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
//        Map<String, String> data = new HashMap<>();
//        data.put("test.string", "hello");
//        data.put("test2.string", "world");
//        ContainerUtil.LORE.accept(data, sword, "{%test.string%, %test2.string%}");
//        Assertions.assertEquals("hello, world", sword.lore().get(0).insertion());
//    }

}
