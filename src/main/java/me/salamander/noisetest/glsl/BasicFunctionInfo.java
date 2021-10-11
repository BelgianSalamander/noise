package me.salamander.noisetest.glsl;

import java.util.*;

public class BasicFunctionInfo implements FunctionInfo {
    private final String name;
    private final String returns;
    private final String signature;

    private final FormattableText code;

    private final String[] requiredFunctions;
    private final Set<FunctionInfo> resolvedRequiredFunctions;
    boolean areResolved = false;

    /**
     * Reads the method info from a stream.
     * The file format is as such:
     * First Line : Method Name
     * Second Line: Method return type ('void' if none)
     * Third Line: Method Signature
     * Fourth Line : 'Requires ' + Amount of Required Functions (N) (Can be left out if there are none)
     * Next N Lines: Name of required methods
     * Rest of file: GLSL code
     * @param scanner The stream
     */
    public BasicFunctionInfo(Scanner scanner){
        this.name = scanner.nextLine().strip();
        this.returns = scanner.nextLine().strip();
        this.signature = scanner.nextLine().strip();

        String requiresInfo = scanner.nextLine().strip();
        boolean isRequires = false;
        if(requiresInfo.startsWith("Requires")){
            isRequires = true;

            int requires = Integer.parseInt(requiresInfo.split(" ")[1]);

            requiredFunctions = new String[requires];
            resolvedRequiredFunctions = new HashSet<>();

            for (int i = 0; i < requires; i++) {
                requiredFunctions[i] = scanner.nextLine().strip();
            }
        }else{
            requiredFunctions = new String[0];
            resolvedRequiredFunctions = new HashSet<>();
        }

        StringBuilder code = new StringBuilder(isRequires ? "" : requiresInfo + "\n");

        while(scanner.hasNext()){
            code.append(scanner.nextLine()).append("\n");
        }

        this.code = new FormattableText(code.toString());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String generateCode() {
        Map<String, Object> names = new HashMap<>();
        return code.evaluate(names);
    }

    @Override
    public String forwardDeclaration() {
        return returns + " " + name + signature;
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        resolve();
        return resolvedRequiredFunctions;
    }

    private void resolve(){
        if(areResolved) return;
        areResolved = true;

        for (String requiredFunction : requiredFunctions) {
            resolvedRequiredFunctions.add(FunctionRegistry.getFunction(requiredFunction));
        }
    }
}
