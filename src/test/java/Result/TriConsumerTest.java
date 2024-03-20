package Result;

import be.seeseemelk.mockbukkit.ServerMock;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.CalcUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        Assertions.assertEquals("hello, world", CalcUtil.setEvalValue(CalcUtil.setPlaceholderValue(data, "%test.string%, %test2.string%")));
        Assertions.assertEquals("None, None", CalcUtil.setEvalValue(CalcUtil.setPlaceholderValue(Collections.emptyMap(), "%test.string%, %test.string%")));
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
        System.out.println(CalcUtil.getRandomNumber(f1, under, upper));

        String f2 = "random[1:1]";
        Assertions.assertEquals(1, CalcUtil.getRandomNumber(f2, under, upper));

        String f3 = "random[3:]";
        String f4 = "random[:10]";
        String f5 = "random[5:20]";
        String f6 = "random[:]";
        for (int i = 0; i < 1000000; i++) {
            Assertions.assertTrue(3 <= CalcUtil.getRandomNumber(f3, under, upper));
            Assertions.assertTrue(CalcUtil.getRandomNumber(f4, under, upper) <= 10);
            int f5r = CalcUtil.getRandomNumber(f5, under, upper);
            Assertions.assertTrue(5 <= f5r && f5r <= 20);
            int f6r = CalcUtil.getRandomNumber(f6, under, upper);
            Assertions.assertTrue(0 < f6r && f6r < 256);
        }
    }
}
