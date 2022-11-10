package justpil.uploadingfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {
    private final StorageService STORAGE_SERVICE;

    @Autowired
    public FileUploadController(StorageService storage) {
        STORAGE_SERVICE = storage;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files", STORAGE_SERVICE.loadAll().map(
                        path ->
                                MvcUriComponentsBuilder.fromMethodName(FileUploadController.class, "serveFile", path
                                                .getFileName()
                                                .toString())
                                        .build()
                                        .toUri()
                                        .toString())
                .collect(Collectors.toList()
                ));
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = STORAGE_SERVICE.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        STORAGE_SERVICE.store(file);
        redirect.addFlashAttribute("message", "You successfully uploaded" +
                file.getOriginalFilename() + "!");
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
