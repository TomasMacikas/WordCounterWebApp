package wordcounterapp.wordcounter;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public interface WordCounter {

    void doCounting();
    void saveToFiles();
    void printWordsWithFreqs();
    void countWordsInFile(File f);
    Map<String, Integer> getWordsCount();
    void readFiles();
	void init();
}
