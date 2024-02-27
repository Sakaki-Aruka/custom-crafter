package Result;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
        String input = "10.1111111%"; //%
        int times = 1000000;
        int result = 0;
        for (int i = 0; i < times; i++) {
            if (ContainerUtil.RANDOM.apply(new HashMap<>(), input)) result++;
        }

        System.out.println("in="+input+", result="+ 100 * ((double) result / (double) times) + "%");
    }
}
