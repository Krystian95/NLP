package nlp._class;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * The main class of the program.
 *
 *
 *
 * @author cristian
 */
public class Analyzer {

    private String mainSubFolder;
    private String pathStopWords;
    private String pathFolderTexts;
    private final String tempFolderName = "temp_files/";
    private final String tempFinalFolderName = "final/";

    private final String[] punctuation = {".", ",", ";", ":", "·", "!", "?", "|", "%", "(", ")", "=", "'", "^", "*", "+", "°", "§", "@", "-", "_", "<", ">"};
    private List<String> listPunctuation;
    private String regularExpressionPunctuation;
    
    private ArrayList<String> stopWords;
    private ArrayList<String> fileNames;

    private int tokenCounter;
    private final String tokenSeparator = "_";

    /**
     * Constructor for the Analyzer class.
     *
     * @param pathStopWords the path to the stop words file
     * @param pathFolderTexts the path to folder containing the texts
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public Analyzer(String mainSubFolder, String pathStopWords, String pathFolderTexts) throws FileNotFoundException, UnsupportedEncodingException {

        this.mainSubFolder = mainSubFolder;
        this.pathStopWords = pathStopWords;
        this.pathFolderTexts = pathFolderTexts;
        this.tokenCounter = 0;
        this.fileNames = new ArrayList<>();
        this.stopWords = new ArrayList<>();
        this.listPunctuation = Arrays.asList(this.punctuation);
        
        File directory = new File(this.mainSubFolder + this.tempFolderName);
        deleteRecursivelyOnlyFilesFromDirectory(directory);
    }

    /**
     * Main mathod that execute the analyzation.
     *
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void analyze() throws FileNotFoundException, UnsupportedEncodingException {

        generateRegularExpressionForPunctuation(this.punctuation);
        saveStopWords(this.pathStopWords);

        File folder = new File(this.pathFolderTexts);
        listFilesForFolder(folder);

        for (String fileName : this.fileNames) {
            createIntermediateFileWithEnumeratedTokens(this.pathFolderTexts, fileName);
        }
        
        File tempFolder = new File(this.mainSubFolder + this.tempFolderName);
        listFilesForFolder(tempFolder);
        
        for (String tempFileName : this.fileNames) {
            createFinalFileWithoutStopWords(this.mainSubFolder + this.tempFolderName, tempFileName);
        }
    }

    private void saveStopWords(String path) throws FileNotFoundException {

        try (Scanner s = new Scanner(new File(path))) {
            while (s.hasNext()) {
                this.stopWords.add(s.next());
            }
        } catch (Exception e) {
            System.err.println("ERROR! An error occours when trying to read the stop words file. Here some details:");
            System.err.println(e);
        }
    }

    private void createIntermediateFileWithEnumeratedTokens(String path, String fileName) throws FileNotFoundException, UnsupportedEncodingException {

        this.tokenCounter = 0;
        Scanner s;

        try (PrintWriter writer = new PrintWriter(this.mainSubFolder + this.tempFolderName + "~" + fileName, "UTF-8")) {

            s = new Scanner(new File(path + fileName));
            while (s.hasNext()) {

                String word = s.next();

                if (stringContainsPunctuationCharacters(word)) {

                    String[] wordSplitted = splitStringForPunctuation(word);

                    for (String singleWord : wordSplitted) {
                        writeWordIntoFile(writer, attachTokenNumber(singleWord));
                    }
                } else {
                    writeWordIntoFile(writer, attachTokenNumber(word));
                }
            }
            s.close();
        } catch (Exception e) {
            System.err.println("ERROR! An error occours when trying to write a temporany file inside the project folder. Here some details:");
            System.err.println(e);
        }
    }
    
    private void createFinalFileWithoutStopWords(String path, String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        
        this.tokenCounter = 0;
        Scanner s;

        try (PrintWriter writer = new PrintWriter(this.mainSubFolder + this.tempFolderName + this.tempFinalFolderName + fileName, "UTF-8")) {
            
            s = new Scanner(new File(path + fileName));
            while (s.hasNext()) {
                
                String word = s.next();
                
                String[] tokenNumberAndWord = separateTokenNumber(word);
                
                if(!stringIsStopWordOrPunctuationCharacter(tokenNumberAndWord[1])){
                    String finalWorld = this.tokenSeparator + tokenNumberAndWord[0] + this.tokenSeparator + tokenNumberAndWord[1];
                    writeWordIntoFile(writer, finalWorld);
                }
            }
            s.close();
        } catch (Exception e) {
            System.err.println("ERROR! An error occours when trying to write a temporany FINAL file inside the project folder. Here some details:");
            System.err.println(e);
        }
    }
    
    private String[] separateTokenNumber (String word) {
        
        String[] wordSplitted = word.split(tokenSeparator);
        String[] tokenNumberAndWord = {wordSplitted[1],wordSplitted[2]};
        
        return tokenNumberAndWord;
    }
    
    private boolean stringIsStopWordOrPunctuationCharacter (String word) {
        
        return stringIsStopWord(word) || stringIsPuncruationCharacter(word);
    }

    private void listFilesForFolder(File folder) {
        
        this.fileNames = new ArrayList<>();
        
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else if (!fileEntry.getName().equals(".DS_Store")){
                this.fileNames.add(fileEntry.getName());
            }
        }
    }

    private boolean stringContainsPunctuationCharacters(String word) {
        return Arrays.stream(this.punctuation).parallel().anyMatch(word::contains);
    }
    
    private boolean stringIsStopWord(String word) {
        return this.stopWords.contains(word);
    }
    
    private boolean stringIsPuncruationCharacter(String word) {
        return this.listPunctuation.contains(word);
    }

    private String attachTokenNumber(String word) {

        this.tokenCounter++;
        return this.tokenSeparator + this.tokenCounter + this.tokenSeparator + word;
    }

    private String[] splitStringForPunctuation(String word) {
        return word.split(this.regularExpressionPunctuation);
    }

    private void writeWordIntoFile(PrintWriter writer, String word) throws FileNotFoundException, UnsupportedEncodingException {

        writer.print(word + " ");
    }

    private void generateRegularExpressionForPunctuation(String[] punctuation) {

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
        this.regularExpressionPunctuation = regularExpression.toString();
    }
    
    private void deleteRecursivelyOnlyFilesFromDirectory(File directory) {
        
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            } else {
                deleteRecursivelyOnlyFilesFromDirectory(file);
            }
        }
    }
}
