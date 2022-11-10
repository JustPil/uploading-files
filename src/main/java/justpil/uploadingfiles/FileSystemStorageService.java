package justpil.uploadingfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {
    private final Path ROOT;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        ROOT = Paths.get(properties.getFolder());
    }

    public void store(MultipartFile file) {
        try {
            if(file.isEmpty()) {
                throw new StorageException("File is empty");
            }
            Path destination = ROOT.resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename()))).normalize().toAbsolutePath();
            if(!destination.getParent().equals(ROOT.toAbsolutePath())) {
                throw new StorageException("Outside current directory");
            }
            try(InputStream input = file.getInputStream()) {
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(ROOT, 1).filter(path -> !path.equals(ROOT)).map(ROOT::relativize);
        } catch(IOException e) {
            throw new StorageException("Failed to read files", e);
        }
    }

    public Path load(String filename) {
        return ROOT.resolve(filename);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + file);
            }
        } catch(MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(ROOT.toFile());
    }

    public void init() {
        try {
            Files.createDirectories(ROOT);
        } catch(IOException e) {
            throw new StorageException("Could not initialize", e);
        }
    }
}
