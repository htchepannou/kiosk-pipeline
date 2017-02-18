package io.tchepannou.kiosk.pipeline.service.similarity;

import java.util.ArrayList;
import java.util.List;

public class ShingleExtractor {
    public List<String> extract(final String text, final int len) {
        String[] parts = text.split("\\s");
        List<String> result = new ArrayList<>();
        for(int i = 0; i < parts.length - len + 1; i++) {
            StringBuilder sb = new StringBuilder();
            for(int k = 0; k < len; k++) {
                if(k > 0) sb.append(' ');
                sb.append(parts[i+k]);
            }
            result.add(sb.toString());
        }
        return result;
    }
}
