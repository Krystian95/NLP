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

        String pathStopWords = "texts/stop_words.txt";
        String pathFolderTexts = "texts/original_texts/";

        Analyzer analyzer = new Analyzer(pathStopWords, pathFolderTexts);
        analyzer.analyze();
    }
}
