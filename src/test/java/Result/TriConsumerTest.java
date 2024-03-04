package Result;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class TriConsumerTest {
    private ServerMock server;
    private CustomCrafter plugin;


//    @BeforeEach
//    void setup() {
//        server = MockBukkit.getMock();
//    }
//
//    @AfterEach
//    void tearDown() {
//        MockBukkit.unmock();
//    }

    @Test
    public void set_placeholder_value_test() {
        Map<String, String> data = new HashMap<>();
        data.put("test.string", "hello");
        data.put("test2.string", "world");
        Assertions.assertEquals("hello, world", ContainerUtil.setEvalValue(ContainerUtil.setPlaceholderValue(data, "%test.string%, %test2.string%")));
        Assertions.assertEquals("None, None", ContainerUtil.setEvalValue(ContainerUtil.setPlaceholderValue(Collections.emptyMap(), "%test.string%, %test.string%")));
    }

    @Test
    public void randomize_test() {
        String input = "23.18";
        int times = 10000;
        int result = 0;
        for (int i = 0; i < times; i++) {
            if (ContainerUtil.RANDOM.apply(new HashMap<>(), input)) result++;
        }

        System.out.println("in="+input+", result="+ 100 * ((double) result / (double) times) + "%");
    }

    @Test
    public void random_number_test() {
        String f1 = "random[:]";
        int under = 1;
        int upper = 255;
        System.out.println(ContainerUtil.getRandomNumber(f1, under, upper));

        String f2 = "random[1:1]";
        Assertions.assertEquals(1, ContainerUtil.getRandomNumber(f2, under, upper));

        String f3 = "random[3:]";
        String f4 = "random[:10]";
        String f5 = "random[5:20]";
        String f6 = "random[:]";
        for (int i = 0; i < 1000000; i++) {
            Assertions.assertTrue(3 <= ContainerUtil.getRandomNumber(f3, under, upper));
            Assertions.assertTrue(ContainerUtil.getRandomNumber(f4, under, upper) <= 10);
            int f5r = ContainerUtil.getRandomNumber(f5, under, upper);
            Assertions.assertTrue(5 <= f5r && f5r <= 20);
            int f6r = ContainerUtil.getRandomNumber(f6, under, upper);
            Assertions.assertTrue(0 < f6r && f6r < 256);
        }
    }
}
