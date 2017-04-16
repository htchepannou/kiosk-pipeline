package io.tchepannou.kiosk.core.nlp.toolkit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NLPToolkitFactoryTest {
    NLPToolkitFactory factory = new NLPToolkitFactory();

    @Test
    public void testGet() throws Exception {
        assertThat(factory.get("en")).isInstanceOf(EnglishToolkit.class);
        assertThat(factory.get("fr")).isInstanceOf(FrenchToolkit.class);
        assertThat(factory.get("ru")).isNull();
    }
}
