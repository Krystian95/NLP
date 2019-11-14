/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp;

import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author cristian
 */
public class NLP {

    private static ArrayList<String> stopWords = new ArrayList<>();
    private static ArrayList<String> fileNames = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        saveStopWords("texts/stop_words.txt");
        //System.out.println(stopWords);

        final File folder = new File("texts/original_texts");
        listFilesForFolder(folder);

        //System.out.println(fileNames);
        for (String fileName : fileNames) {
            createIntermediateFileWithEnumeratedTokens("texts/original_texts/" + fileName);
        }

        //PrintWriter writer = new PrintWriter("texts/temp_file.txt", "UTF-8");
        //writer.println(s.next());   
        //writer.close();
    }

    private static void saveStopWords(String path) throws FileNotFoundException {

        Scanner s = new Scanner(new File(path));
        while (s.hasNext()) {
            stopWords.add(s.next());
        }

        s.close();
    }

    private static void createIntermediateFileWithEnumeratedTokens(String path) throws FileNotFoundException {

        Scanner s = new Scanner(new File(path));
        while (s.hasNext()) {
            //stopWords.add(s.next());
            System.out.print(s.next() + " ");
        }

        System.out.println();

        s.close();
    }

    public static void listFilesForFolder(final File folder) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                fileNames.add(fileEntry.getName());
            }
        }
    }

    private static void checkStopWords(String word) {

        String[] stopWordsArr = new String[stopWords.size()];
        stopWordsArr = stopWords.toArray(stopWordsArr);
        
     
        StringUtils.indexOfAny(word, stopWordsArr);
        StringUtils.indexOfAny("ciao", new String[]{"ciao", "ciao", "ciao"});
        
    }
}
