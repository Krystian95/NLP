package nlp._class;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final String[] punctuation = {".", ",", ";", ":", "!", "?", "|", "%", "(", ")", "=", "'", "^", "*", "+", "°", "§", "@", "-", "_", "<", ">"};

    private ArrayList<String> stopWords;
    private ArrayList<String> fileNames;

    private int tokenCounter;
    private String regularExpressionPunctuation;

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
    }

    private void saveStopWords(String path) throws FileNotFoundException {

        try (Scanner s = new Scanner(new File(path))) {
            while (s.hasNext()) {
                this.stopWords.add(s.next());
            }
        } catch (Exception e) {
            System.out.println("ERROR! An error occours when trying to read the stop words file. Here some details:");
            System.out.println(e);
        }
    }

    private void createIntermediateFileWithEnumeratedTokens(String path, String fileName) throws FileNotFoundException, UnsupportedEncodingException {

        this.tokenCounter = 0;
        Scanner s;

        try (PrintWriter writer = new PrintWriter(this.mainSubFolder + "temp_files/~" + fileName, "UTF-8")) {

            s = new Scanner(new File(path + fileName));
            while (s.hasNext()) {

                String word = s.next();

                if (stringContainsItemFromList(word)) {

                    String[] wordSplitted = splitStringForPunctuation(word);

                    for (String singleWord : wordSplitted) {
                        writeWordToIntermediateFile(writer, attachTokenNumber(singleWord));
                    }
                } else {
                    writeWordToIntermediateFile(writer, attachTokenNumber(word));
                }
            }
            s.close();
        } catch (Exception e) {
            System.out.println("ERROR! An error occours when trying to write a temporany file inside the project folder. Here some details:");
            System.out.println(e);
        }
    }

    private void listFilesForFolder(File folder) {

        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                this.fileNames.add(fileEntry.getName());
            }
        }
    }

    private boolean stringContainsItemFromList(String word) {
        return Arrays.stream(this.punctuation).parallel().anyMatch(word::contains);
    }

    private String attachTokenNumber(String word) {

        this.tokenCounter++;
        return "[" + this.tokenCounter + "]" + word;
    }

    private String[] splitStringForPunctuation(String word) {
        return word.split(this.regularExpressionPunctuation);
    }

    private void writeWordToIntermediateFile(PrintWriter writer, String word) throws FileNotFoundException, UnsupportedEncodingException {

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

}
