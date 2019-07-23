package wordcounterapp.wordcounter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import wordcounterapp.storage.StorageException;
import wordcounterapp.storage.StorageProperties;

@Service
public class WordCounterService implements WordCounter {
	
	
	private Map<String, Integer> wordsCount;
	private List<File> files;
	private Path rootLocation;
	private Path outputLocation;
	
	
	@Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(outputLocation);
    		this.files = new LinkedList<File>();
    		this.wordsCount = new HashMap<String, Integer>();
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
	
	public Map<String, Integer> getWordsCount() {
		return wordsCount;
	}
	
	@Autowired
	public WordCounterService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
		this.outputLocation = Paths.get(properties.getCreatedFilesLocation());
	}

	public void doCounting() {
		init();
		readFiles();
		for(File f : files) {
			countWordsInFile(f);
		}
		printWordsWithFreqs();
		saveToFiles();
		
//		try {
//			saveToFilesUsingThreads();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@Async
	private void saveToFilesUsingThreads() throws IOException {
		//Filter entries for writing to exact file
		
		Set<Entry<String, Integer>> ag = wordsCount.entrySet().stream().
				filter(c -> c.getKey().charAt(0) >= 'a' &&	
						c.getKey().charAt(0) <= 'g').collect(Collectors.toSet());
		
		Set<Entry<String, Integer>> hn = wordsCount.entrySet().stream().
				filter(c -> c.getKey().charAt(0) >= 'h' &&
						c.getKey().charAt(0) <= 'n').collect(Collectors.toSet());
		
		Set<Entry<String, Integer>> ou = wordsCount.entrySet().stream().
				filter(c -> c.getKey().charAt(0) >= 'o' &&
						c.getKey().charAt(0) <= 'u').collect(Collectors.toSet());
		
		Set<Entry<String, Integer>> vz = wordsCount.entrySet().stream().
				filter(c -> c.getKey().charAt(0) >= 'v' &&
						c.getKey().charAt(0) <= 'z').collect(Collectors.toSet());
		
		//Start Threads
		WriterThread writerThread1 = new WriterThread(ag, this.outputLocation + "/" + "ag.txt");
		writerThread1.start();
		WriterThread writerThread2 = new WriterThread(hn, this.outputLocation + "/" + "hn.txt");
		writerThread2.start();
		WriterThread writerThread3 = new WriterThread(ou, this.outputLocation + "/" + "ou.txt");
		writerThread3.start();
		WriterThread writerThread4 = new WriterThread(vz, this.outputLocation + "/" + "vz.txt");
		writerThread4.start();
	}

	public void saveToFiles(){
		try {
			FileWriter agFile = new FileWriter(this.outputLocation + "/" + "ag.txt", false);
			FileWriter hnFile = new FileWriter(this.outputLocation + "/" + "hn.txt", false); //Words from H to N
			FileWriter ouFile = new FileWriter(this.outputLocation + "/" + "ou.txt", false); //Words from O to U
			FileWriter vzFile = new FileWriter(this.outputLocation + "/" + "vz.txt", false); //Words from V to Z
			
			BufferedWriter agOut = new BufferedWriter(agFile);
			BufferedWriter hnOut = new BufferedWriter(hnFile);
			BufferedWriter ouOut = new BufferedWriter(ouFile);
			BufferedWriter vzOut = new BufferedWriter(vzFile);
		    
			for(Map.Entry<String, Integer> entry: wordsCount.entrySet()) {
				if(entry.getKey().length()!=0) {
					char firstLetter = entry.getKey().charAt(0);
					if(firstLetter >= 'a' && firstLetter <= 'g'){
						agOut.write(entry.getKey()+ " " + entry.getValue() + "\n");
					}
					else if(firstLetter >= 'h' && firstLetter <= 'n'){
						hnOut.write(entry.getKey()+ " " + entry.getValue() + "\n");
					}
					else if(firstLetter >= 'o' && firstLetter <= 'u'){
						ouOut.write(entry.getKey()+ " " + entry.getValue() + "\n");
					}
					else if(firstLetter >= 'v' && firstLetter <= 'z'){
						vzOut.write(entry.getKey()+ " " + entry.getValue() + "\n");
					}
				}
		    }
			agOut.close();
			hnOut.close();
			ouOut.close();
			vzOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printWordsWithFreqs() {
		//Print the map
	    for(Map.Entry<String, Integer> entry: wordsCount.entrySet()) {
	    	System.out.println(entry.getKey()+ " " + entry.getValue());
	    }
	}
	
	public void countWordsInFile(File f){
		FileReader fr = null;
		try {
			fr = new FileReader(f);
		} catch (FileNotFoundException e) {
			System.out.println("File/s Not Found");
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		String[] words=null; 
	    try {
			while((line=br.readLine())!=null) {
				words=line.split("\\W+");
				for(String word : words) {
					if(!word.equals("")) { //Exclude whitespace
						word = word.toLowerCase(); //example Hello == hello
				        if(!this.wordsCount.containsKey(word)) {
							this.wordsCount.put(word, 1);
						}else {
							this.wordsCount.put(word, this.wordsCount.get(word)+1);
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading the file/s");
			e.printStackTrace();
		}
	    
	    //Close the file
	    try {
			fr.close();
		} catch (IOException e) {
			System.out.println("Error closing the file/s");
			e.printStackTrace();
		}
	}

	@Override
	public void readFiles() {
		try {
			List<Path> paths = Files.walk(this.rootLocation, 1)
	                .filter(path -> !path.equals(this.rootLocation))
	                .map(this.rootLocation::relativize).collect(Collectors.toList());
			for(Path path : paths) {
				System.out.println(path.toString());
				files.add(new File(this.rootLocation+"/" + path.toString()));
			}
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
	}

}
