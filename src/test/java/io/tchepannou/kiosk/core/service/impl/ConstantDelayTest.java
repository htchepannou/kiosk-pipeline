package io.tchepannou.kiosk.core.service.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstantDelayTest {

    @Test
    public void testSleep() throws Exception {
        ConstantDelay delay = new ConstantDelay(10, 50);

        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isFalse();
    }

    @Test
    public void testReset() throws Exception {
        ConstantDelay delay = new ConstantDelay(10, 50);

        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();
        assertThat(delay.sleep()).isTrue();

        delay.reset();
        assertThat(delay.sleep()).isTrue();
    }
}
