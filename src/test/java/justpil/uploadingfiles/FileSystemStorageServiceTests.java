package justpil.uploadingfiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.condition.OS;

public class FileSystemStorageServiceTests {
    private StorageProperties properties = new StorageProperties();
    private FileSystemStorageService service;

    @BeforeEach
    public void init() {
        properties.setFolder("target/files/" + Math.abs(new Random().nextLong()));
        service = new FileSystemStorageService(properties);
        service.init();
    }

    @Test
    public void loadNonExistent() {
        assertThat(service.load("file.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        service.store(new MockMultipartFile("file", "file.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello".getBytes()));
        assertThat(service.load("file.txt")).exists();
    }

    @Test
    public void saveRelativePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            service.store(new MockMultipartFile("file", "../file.txt", MediaType.TEXT_PLAIN_VALUE,
                    "Hello".getBytes()));
        });
    }

    @Test
    public void saveAbsolutePathNotPermitted() {
        assertThrows(StorageException.class, () -> {
            service.store(new MockMultipartFile("file", "/etc/pass", MediaType.TEXT_PLAIN_VALUE,
                    "Hello".getBytes()));
        });
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    public void saveAbsolutePathInFilenamePermitted() {
        String filename = "\"etc\"pass";
        service.store(new MockMultipartFile(filename, filename, MediaType.TEXT_PLAIN_VALUE, "Hello".getBytes()));
        assertTrue(Files.exists(Paths.get(properties.getFolder()).resolve(Paths.get(filename))));
    }

    @Test
    public void savePermitted() {
        service.store(new MockMultipartFile("file", "some/../file.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello".getBytes()));
    }
}
