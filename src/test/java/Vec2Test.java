import me.salamander.noisetest.noise.Vec2;
import org.junit.jupiter.api.Test;

public class Vec2Test {
    @Test
    public void runTest(){
        assert isVerySimilar(Vec2.fractionalPart(5.15f), 0.15f);
        assert isVerySimilar(Vec2.fractionalPart(-9.34f), 0.66f);
    }

    private static boolean isVerySimilar(float x, float y){
        return Math.abs(x - y) < 1e-4;
    }
}
