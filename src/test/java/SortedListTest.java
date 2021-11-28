import me.salamander.ourea.util.SortedList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

public class SortedListTest {
    @Test
    void testSortedList(){
        for (int i = 0; i < 100; i++) {
            SortedList<Integer> actual = new SortedList<>(Integer::compareTo);

            Random rand = new Random();

            for (int j = 0; j < 10000; j++) {
                int random = rand.nextInt(1000);
                actual.add(random);
            }

            //Check it is sorted
            for (int j = 0; j < actual.size() - 1; j++) {
                assert(actual.get(j) <= actual.get(j + 1));
            }
        }
    }
}
