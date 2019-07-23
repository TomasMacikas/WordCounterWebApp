package wordcounterapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import wordcounterapp.storage.StorageFileNotFoundException;
import wordcounterapp.storage.StorageService;
import wordcounterapp.wordcounter.WordCounter;
import wordcounterapp.wordcounter.WordCounterService;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    private WordCounter wordCounter;

    @Autowired
    public FileUploadController(StorageService storageService, WordCounter wordCounter) {
        this.storageService = storageService;
        this.wordCounter = wordCounter;
    }
    
    @PostMapping("/result")
    public String postResult() {
        return "redirect:/result";
    }
    
    
    @GetMapping("/result")
    public String getResult(Model model) throws IOException {
    	
    	wordCounter.doCounting();
    	model.addAttribute("file", storageService.loadAllOutputs().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveOutputFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
    	model.addAttribute("words", wordCounter.getWordsCount());
    	
        return "result";
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }
    
    @GetMapping("/result/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveOutputFile(@PathVariable String filename) {

        Resource file = storageService.loadOutputAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }
    
    @PostMapping("/delete")
    public String deleteFiles() {
    	storageService.deleteAll();
        storageService.init();
        
        return "redirect:/";
    }
    
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
