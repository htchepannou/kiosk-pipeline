package io.tchepannou.kiosk.pipeline.service.content;

public class HtmlEntityFilter implements Filter<String> {
    @Override
    public String filter(final String str) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c > 127) {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
