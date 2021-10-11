package me.salamander.noisetest.glsl;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormattableText {
    private final List<MessageComponent> components;

    public FormattableText(String template){
        components = new ArrayList<>();

        int startIndex = 0;
        int currentIndex = 0;

        while(currentIndex < template.length()){
            while (currentIndex < template.length()){
                if(template.charAt(currentIndex) == '{'){
                    break;
                }
                currentIndex++;
            }

            if(currentIndex > startIndex)
                components.add(new TextComponent(template.substring(startIndex, currentIndex)));
            startIndex = currentIndex;

            if(currentIndex >= template.length()) break;

            if(template.charAt(currentIndex) == '{'){
                boolean exited = false;
                while (currentIndex < template.length()){
                    char c = template.charAt(currentIndex);
                    if(!Character.isAlphabetic(c) && c != '{' && c != '}'){
                        while (currentIndex < template.length()){
                            if(template.charAt(currentIndex) == '{'){
                                break;
                            }
                            currentIndex++;
                        }
                        exited = true;
                        break;
                    }else if(c == '}'){
                        break;
                    }
                    currentIndex++;
                }

                if(exited){
                    components.add(new TextComponent(template.substring(startIndex, currentIndex)));
                    startIndex = currentIndex;
                    continue;
                }

                if(template.charAt(currentIndex) == '}'){
                    String value = template.substring(startIndex + 1, currentIndex); //Remove curly braces
                    components.add(new VariableComponent(value));
                }

                startIndex = ++currentIndex;
            }
        }
    }

    public String evaluate(Map<String, Object> data){
        String message = "";
        for(MessageComponent component : components){
            message += component.evaluate(data);
        }
        return message;
    }

    public String evaluate(){
        return evaluate(new HashMap<>());
    }

    private interface MessageComponent{
        String evaluate(Map<String, Object> data);
    }

    private static class TextComponent implements MessageComponent{
        private final String text;

        public TextComponent(String text){this.text = text;}

        @Override
        public String evaluate(Map<String, Object> data) {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    public String toString() {
        return components.stream().map(MessageComponent::toString).collect(Collectors.joining());
    }

    private static class VariableComponent implements MessageComponent{
        private final String wanted;

        public VariableComponent(@NotNull String wantedVar){
            wanted = wantedVar;
        }

        @Override
        public String evaluate(Map<String, Object> data) {
            Object wantedInfo = data.get(wanted);
            if(wantedInfo == null){
                return "['" + wanted + "' not provided]";
            }
            return data.get(wanted).toString();
        }

        @Override
        public String toString() {
            return "[Variable " + wanted + "]";
        }
    }
}
