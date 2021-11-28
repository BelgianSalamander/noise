import me.salamander.ourea.util.PosMap;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PosMapTest {
    @Test
    void containsTest() {
        Map<Pos, Integer> test = new HashMap<>();
        PosMap<Integer> map = new PosMap<>();

        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            int v = random.nextInt(100);
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            if(random.nextInt(3) == 0){
                assertEquals(map.remove(x, y), test.remove(new Pos(x, y)));
                assertFalse(map.containsKey(x, y));
            }else {
                assertEquals(test.put(new Pos(x, y), v), map.put(x, y, v));
                assertTrue(map.containsKey(x, y));
                assertTrue(map.containsValue(v));
            }
        }

        for (int i = 0; i < 1000; i++) {
            int v = random.nextInt(100);
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            assertEquals(test.containsKey(new Pos(x, y)), map.containsKey(x, y), "containsKey");
            assertEquals(test.containsValue(v), map.containsValue(v), "containsValue failed. Value = " + v);
        }

        map.forEachKeys((x, y) -> {
            assertTrue(test.containsKey(new Pos(x, y)));
            assertTrue(test.containsValue(map.get(x, y)));
        });

        test.keySet().forEach(pos -> {
            assertTrue(map.containsKey(pos.x, pos.y));
        });

        map.values().forEach(v -> {
            assertTrue(test.containsValue(v));
        });

        test.values().forEach(v -> {
            assertTrue(test.containsValue(v));
            assertTrue(map.containsValue(v));
        });

        assertEquals(test.size(), map.size());
    }

    private static record Pos(int x, int y){

    }
}
