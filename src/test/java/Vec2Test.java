import me.salamander.noisetest.noise.Vec2;
import org.junit.jupiter.api.Test;

public class Vec2Test {
    @Test
    public void runTest(){
        assert isVerySimilar(Vec2.fractionalPart(5.15), 0.15);
        assert isVerySimilar(Vec2.fractionalPart(-9.34), 0.66);
    }

    private static boolean isVerySimilar(double x, double y){
        return Math.abs(x - y) < 1e-8;
    }
}
