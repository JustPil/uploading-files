package justpil.uploadingfiles;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Paths;
import java.util.stream.Stream;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private StorageService storage;

    @Test
    public void listAllFiles() throws Exception {
        given(storage.loadAll()).willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect((model().attribute("files",
                Matchers.contains("http://localhost/files/first.txt", "http://localhost/files/second.txt"))));
    }

    @Test
    public void saveFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain",
                "Spring Framework".getBytes());
        mvc.perform(multipart("/").file(file)).andExpect(status().isFound()).andExpect(header()
                .string("Location", "/"));
        then(storage).should().store(file);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void missingFileError() throws Exception {
        given(storage.loadAsResource("test.txt")).willThrow(StorageFileNotFoundException.class);
        mvc.perform(get("/files/test.txt")).andExpect(status().isNotFound());
    }
}
