package io.tchepannou.kiosk.core.nlp.toolkit;

import java.util.HashMap;
import java.util.Map;

public class NLPToolkitFactory {
    private final Map<String, NLPToolkit> toolkitMap = new HashMap<>();

    public NLPToolkitFactory(){
        toolkitMap.put("en", new EnglishToolkit());
        toolkitMap.put("fr", new FrenchToolkit());
    }

    public NLPToolkit get(final String language){
        return toolkitMap.get(language);
    }
}
