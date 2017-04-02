package io.tchepannou.kiosk.core.service.local;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalFileRepositoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    LocalFileRepository repo = new LocalFileRepository();
    File home;

    @Before
    public void setUp() throws Exception {
        home = folder.newFolder();
        repo.setHome(home.getAbsolutePath());
    }

    @Test
    public void testRead() throws Exception {
        // Given
        File file = new File(home, "testRead.txt");
        Files.write(file.toPath(), "toto".getBytes());

        // When
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repo.read("testRead.txt", out);

        // Then
        assertThat(out.toString()).isEqualTo("toto");
    }

    @Test
    public void testWrite() throws Exception {
        // Given
        final ByteArrayInputStream in = new ByteArrayInputStream("toto".getBytes());

        // When
        repo.write("testWrite/01.txt", in);

        // Then
        File file = Paths.get(home.getAbsolutePath(), "/testWrite/01.txt").toFile();
        assertThat(file).exists();
        assertThat(file).hasContent("toto");
    }
}
