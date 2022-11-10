package justpil.uploadingfiles;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {
    private String folder = "upload-folder";

    public String getFolder() {
        return folder;
    }

    public void setFolder(String f) {
        folder = f;
    }
}
