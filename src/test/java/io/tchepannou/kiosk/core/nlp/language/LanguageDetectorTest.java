package io.tchepannou.kiosk.core.nlp.language;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageDetectorTest {
    LanguageDetector detector = new LanguageDetector();

    @Before
    public void setUp() {
        detector.setLanguageList(Arrays.asList("en", "fr"));
    }

    @Test
    public void testDetectFH() throws Exception {
        final String text =
                "On part pour Pointe Noire au Congo Brazzaville... Dans la capitale économique, qui est aussi la ville pétrolière congolaise, l'essence se fait rare. Depuis une semaine la vie tourne au ralenti. C'est l'arrêt de la production de la compagnie congolaise de raffinage qui explique cette pénurie.";

        assertThat(detector.detect(text)).isEqualTo("fr");
    }

    @Test
    public void testDetectEN() throws Exception {
        final String text =
                "Learn the public speaking secrets behind the most powerful TED talks. Chris Anderson, the Head of TED, brings you insights from working with influential speakers like Elizabeth Gilbert, Sir Ken Robinson, Amy Cuddy, and more.";

        assertThat(detector.detect(text)).isEqualTo("en");
    }

    @Test
    public void testDetectUnsupported() throws Exception {
        final String text =
                "Российское руководство непременно поинтересуется у Рекса Тиллерсона, что именно заставило Белый дом нанести ракетный удар по Сирии, не дожидаясь расследования ООН, отмечает в эфире CNN специалист по России Стивен Коэн. Другой гость программы, Гарри Каспаров, призывает не тратить время на выяснение фактов, поскольку именно Кремль всегда требует доказательств.\n"
                        + "\n"
                        + "Оригинал новости ИноТВ: \n"
                        + "https://russian.rt.com/inotv/2017-04-12/Stiven-Koen-nelzya-perestupat-rossijskuyu";

        assertThat(detector.detect(text)).isNull();
    }
}
