package wordcounterapp.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();
    Stream<Path> loadAllOutputs();

    Resource loadAsResource(String filename);

    void deleteAll();

	Resource loadOutputAsResource(String filename);

	Path load(String filename, Path path);

}
