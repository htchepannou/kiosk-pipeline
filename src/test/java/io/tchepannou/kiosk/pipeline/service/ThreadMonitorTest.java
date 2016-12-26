package io.tchepannou.kiosk.pipeline.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadMonitorTest {
    ThreadMonitor monitor = new ThreadMonitor();

    @Test
    public void testStarted() {
        final Runnable worker = () -> {
        };

        monitor.started(worker);

        assertThat(monitor.isAlive()).isTrue();
    }

    @Test
    public void testFinished() {
        final Runnable worker = () -> {
        };
        monitor.started(worker);

        monitor.started(worker);
        monitor.finished(worker);

        assertThat(monitor.isAlive()).isFalse();
    }

    @Test
    public void testWaitUntilExpired() throws Exception{
        // Given
        final Thread t1 = newThread(30000, monitor);
        final Thread t2 = newThread(30000, monitor);
        final Thread t3 = newThread(30000, monitor);

        // When
        monitor.waitAllThreads(100, 100);

        // Then
        assertThat(t1.isAlive()).isTrue();
        assertThat(t2.isAlive()).isTrue();
        assertThat(t3.isAlive()).isTrue();
    }

    @Test
    public void testWaitUntilAllThreadCompleted() throws Exception{
        // Given
        final Thread t1 = newThread(100, monitor);
        final Thread t2 = newThread(100, monitor);
        final Thread t3 = newThread(100, monitor);

        // When
        monitor.waitAllThreads(500, 10000);

        // Then
        assertThat(t1.isAlive()).isFalse();
        assertThat(t2.isAlive()).isFalse();
        assertThat(t3.isAlive()).isFalse();
    }

    private Thread newThread(final long sleepMillis, final ThreadMonitor monitor) {
        final Runnable worker = new Runnable() {
            @Override
            public void run() {
                monitor.started(this);
                try {
                    Thread.sleep(sleepMillis);
                } catch (Exception e){

                } finally {
                    monitor.finished(this);
                }
            }
        };
        final Thread thread = new Thread(worker);
        thread.start();
        return thread;
    }
}
