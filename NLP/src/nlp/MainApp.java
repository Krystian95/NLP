package nlp;

import nlp._class.Analyzer;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author cristian
 */
public class MainApp {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        String mainSubFolder = "texts/";
        String pathStopWords = mainSubFolder + "stop_words.txt";
        String pathFolderTexts = mainSubFolder + "original_texts/";

        Analyzer analyzer = new Analyzer(mainSubFolder, pathStopWords, pathFolderTexts);
        analyzer.analyze();
    }
}
