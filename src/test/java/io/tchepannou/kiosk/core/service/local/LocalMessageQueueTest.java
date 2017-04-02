package io.tchepannou.kiosk.core.service.local;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalMessageQueueTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    LocalMessageQueue queue = new LocalMessageQueue();
    File home;

    @Before
    public void setUp() throws Exception {
        home = folder.newFolder();
        queue.setHome(home.getAbsolutePath());
        queue.setPollMaxSize(5);
    }


    @Test
    public void testPush() throws Exception {
        // When
        queue.push("toto");

        // Then
        assertThat(home.listFiles()).hasSize(1);
        assertThat(home.listFiles()[0]).hasContent("toto");
    }

    @Test
    public void testPoll() throws Exception {
        // Given
        home.mkdirs();

        Files.write(new File(home, "01.txt").toPath(), "1".getBytes());
        Files.write(new File(home, "02.txt").toPath(), "2".getBytes());
        Files.write(new File(home, "03.txt").toPath(), "3".getBytes());
        Files.write(new File(home, "04.txt").toPath(), "4".getBytes());
        Files.write(new File(home, "05.txt").toPath(), "5".getBytes());
        Files.write(new File(home, "06.txt").toPath(), "6".getBytes());
        Files.write(new File(home, "07.txt").toPath(), "7".getBytes());

        // When
        List<String> result = queue.poll();

        // Then
        assertThat(result).hasSize(5);
//        assertThat(result).contains("1", "2", "3", "4", "5");

        assertThat(home.listFiles()).hasSize(2);
    }

    @Test
    public void testPollReturnsNothingOnEmptyDirectory() throws Exception {
        // Given

        // When
        List<String> result = queue.poll();

        // Then
        assertThat(result).isEmpty();
    }
}
