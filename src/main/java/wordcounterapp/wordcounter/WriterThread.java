package wordcounterapp.wordcounter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class WriterThread extends Thread {
	
	private static Set<Map.Entry<String, Integer>> wordsCount;
	private static String file;
	
	WriterThread(Set<Map.Entry<String, Integer>> entries, String file){
		wordsCount = entries;
		WriterThread.file = file;
	}
	
	public void run() {
		try {
			FileWriter currentFile = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(currentFile);
			
			//Write
			for(Map.Entry entry : wordsCount) {
				out.write(entry.getKey()+ " " + entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
