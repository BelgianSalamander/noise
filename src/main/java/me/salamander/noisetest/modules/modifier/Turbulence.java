package me.salamander.noisetest.modules.modifier;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FormattableText;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.source.NoiseSourceModule;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.modules.types.ModifierModule;
import me.salamander.noisetest.util.Util;

import java.io.IOException;
import java.util.*;

public class Turbulence extends ModifierModule implements GLSLCompilable {
    private final NoiseSourceModule xTurbulence, yTurbulence;

    private static final int TURBULENCE_POWER_INDEX = 0, TURBULENCE_FREQUENCY_INDEX = 1;
    private static final FormattableText functionCode;

    private final Function function = new Function();
    private final Set<FunctionInfo> required = new HashSet<>();

    public Turbulence(SerializableNoiseModule source){
        this(source, (new Random()).nextLong());
        this.source = source;

        required.add(function);
    }

    public Turbulence(SerializableNoiseModule source, long seed){
        super(2);
        initParameters();
        xTurbulence = new NoiseSourceModule(3, (int) seed + 3, NoiseType.PERLIN);
        yTurbulence = new NoiseSourceModule(3, (int) seed * 4723537 ^ 4264, NoiseType.PERLIN);
        this.source = source;
    }

    public Turbulence(SerializableNoiseModule source, double turbulencePower){
        this(source);
        parameters[TURBULENCE_POWER_INDEX] = turbulencePower;
    }

    public Turbulence(SerializableNoiseModule source, long seed, double turbulencePower){
        this(source, seed);
        parameters[TURBULENCE_POWER_INDEX] = turbulencePower;
    }

    private void initParameters(){
        parameters[TURBULENCE_POWER_INDEX] = 1.0;
    }

    @Override
    public double sample(double x, double y) {
        if(source == null) return 0;

        final double x0 = x + (12148.0 / 65536.0);
        final double y0 = y + (56346.0 / 65536.0);
        final double x1 = x + (23436.0 / 65536.0);
        final double y1 = y + (43765.0 / 65536.0);

        final double distortedX = x + xTurbulence.sample(x0, y0) * parameters[TURBULENCE_POWER_INDEX];
        final double distortedY = y + yTurbulence.sample(x1, y1) * parameters[TURBULENCE_POWER_INDEX];

        return source.sample(distortedX, distortedY);
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        super.readNBT(tag, sourceLookup);

        setFrequency(parameters[TURBULENCE_FREQUENCY_INDEX]);
        xTurbulence.setSeed(tag.getLong("xSeed"));
        yTurbulence.setSeed(tag.getLong("ySeed"));
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        parameters[TURBULENCE_FREQUENCY_INDEX] = xTurbulence.getFrequency();
        super.writeNBT(tag, indexLookup);

        tag.putLong("xSeed", xTurbulence.getSeed());
        tag.putLong("ySeed", yTurbulence.getSeed());
    }

    public void setFrequency(double frequency){
        xTurbulence.setFrequency(frequency);
        yTurbulence.setFrequency(frequency);
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == TURBULENCE_FREQUENCY_INDEX) setFrequency(value);
        else super.setParameter(index, value);
    }

    @Override
    public double getParameter(int index) {
        if(index == TURBULENCE_FREQUENCY_INDEX) return xTurbulence.getFrequency();
        else return super.getParameter(index);
    }

	@Override
	public String getNodeRegistryName() {
		return "Turbulence";
	}

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return function.name() + "(" + vec2Name + ", " + seedName + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turbulence that = (Turbulence) o;
        return Objects.equals(xTurbulence, that.xTurbulence) && Objects.equals(yTurbulence, that.yTurbulence) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xTurbulence, yTurbulence, source);
    }

    private final class Function implements FunctionInfo{

        @Override
        public String name() {
            return "turbulence_" + Math.abs(Turbulence.this.hashCode());
        }

        @Override
        public String generateCode() {
            if(source instanceof GLSLCompilable compilable){
                Map<String, Object> lookup = new HashMap<>();

                lookup.put("name", name());

                lookup.put("turbulencePower", parameters[TURBULENCE_POWER_INDEX]);

                lookup.put("sampleX", xTurbulence.glslExpression("sampleX", "seed"));
                lookup.put("sampleY", yTurbulence.glslExpression("sampleY", "seed * 968177961"));

                lookup.put("sample", compilable.glslExpression("distortedPos", "seed + 60874159"));

                return functionCode.evaluate(lookup);
            }else{
                throw new IllegalStateException("Can't compile turbulence source");
            }
        }

        @Override
        public String forwardDeclaration() {
            return "float " + name() + " (vec2, int)";
        }

        @Override
        public Set<FunctionInfo> requiredFunctions() {
            if(source instanceof GLSLCompilable compilable){
                Set<FunctionInfo> combined = new HashSet<>(xTurbulence.requiredFunctions());
                combined.addAll(yTurbulence.requiredFunctions());
                combined.addAll(compilable.requiredFunctions());
                return combined;
            }else{
                throw new IllegalStateException("Can't compile turbulence source");
            }
        }
    }

    static {
        try{
            functionCode = new FormattableText(Util.loadResource("/glsl/extra/turbulence.func"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load function");
        }
    }
}
