/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author cristian
 */
public class NLP {

    private static ArrayList<String> stopWords = new ArrayList<>();
    private static ArrayList<String> fileNames = new ArrayList<>();
    private static String[] punctuation = {".", ",", ";", ":", "!", "?", "|", "%", "(", ")", "=", "'", "^", "*", "+", "°", "§", "@", "-", "_", "<", ">"};
    private static int tokenCounter = 0;
    private static String regularExpressionPunctuation;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        generateRegularExpressionForPunctuation(punctuation);
        saveStopWords("texts/stop_words.txt");
        //System.out.println(stopWords);

        final File folder = new File("texts/original_texts");
        listFilesForFolder(folder);

        //System.out.println(fileNames);
        for (String fileName : fileNames) {
            createIntermediateFileWithEnumeratedTokens("texts/original_texts/", fileName);
        }
    }

    private static void saveStopWords(String path) throws FileNotFoundException {

        Scanner s = new Scanner(new File(path));
        while (s.hasNext()) {
            stopWords.add(s.next());
        }

        s.close();
    }

    private static void createIntermediateFileWithEnumeratedTokens(String path, String fileName) throws FileNotFoundException, UnsupportedEncodingException {

        tokenCounter = 0;
        Scanner s;
        
        try (PrintWriter writer = new PrintWriter("texts/temp_files/~" + fileName, "UTF-8")) {
            
            s = new Scanner(new File(path + fileName));
            while (s.hasNext()) {

                String word = s.next();

                if (stringContainsItemFromList(word)) {
                    
                    String[] wordSplitted = splitStringForPunctuation(word);

                    for (int i = 0; i < wordSplitted.length; i++) {
                        writeWordToIntermediateFile(writer, attachTokenNumber(wordSplitted[i]));
                    }
                } else {
                    writeWordToIntermediateFile(writer, attachTokenNumber(word));
                }
            }
            s.close();
        } catch (Exception e) {
            
            System.out.println("Impossibile scrivere sul file!");
        }
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

    public static boolean stringContainsItemFromList(String word) {

        return Arrays.stream(punctuation).parallel().anyMatch(word::contains);
    }

    public static String attachTokenNumber(String word) {

        tokenCounter++;
        return "[" + tokenCounter + "]" + word;
    }

    public static String[] splitStringForPunctuation(String word) {
   
        return word.split(regularExpressionPunctuation);
        
        //return word.split("((?<=\\,|\\'|\\.|\\;|\\·|\\)|(?=\\,|\\'|\\.|\\;|\\·))");
        //return word.split("((?<=\\.|\\,|\\;|\\:|\\!|\\?|\\||\\%|\\(|\\)|\\=|\\'|\\^|\\*|\\+|\\°|\\§|\\@|\\-|\\_|\\<|\\>)|(?=\\.|\\,|\\;|\\:|\\!|\\?|\\||\\%|\\(|\\)|\\=|\\'|\\^|\\*|\\+|\\°|\\§|\\@|\\-|\\_|\\<|\\>))");
    }

    public static void writeWordToIntermediateFile(PrintWriter writer, String word) throws FileNotFoundException, UnsupportedEncodingException {

        writer.print(word + " ");
    }
    
    public static void generateRegularExpressionForPunctuation(String[] punctuation) {
        
        StringBuilder regularExpression = new StringBuilder();
        regularExpression.append("((?<=");
                
        for (int i = 0; i < punctuation.length; i++) {
            regularExpression.append("\\" + punctuation[i] + "|");
        }
        
        regularExpression.deleteCharAt(regularExpression.length() - 1); 
        regularExpression.append(")|(?=");
        
        for (int i = 0; i < punctuation.length; i++) {
            regularExpression.append("\\" + punctuation[i] + "|");
        }
        
        regularExpression.deleteCharAt(regularExpression.length() - 1); 
        regularExpression.append("))");
        regularExpressionPunctuation = regularExpression.toString();
        
        System.out.println(regularExpressionPunctuation);
    }
}