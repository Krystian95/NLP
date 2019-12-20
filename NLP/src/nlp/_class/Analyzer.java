package nlp._class;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    private ArrayList<String> contentOfText = new ArrayList<String>();
    private ArrayList<String[]> quotes = new ArrayList<String[]>();

    private int tokenCounter;
    private final String tokenSeparator = "_";
    private int nLastCharsToRemove = 2;
    private int lenghtOfPhrase = 5;

    /**
     * Constructor for the Analyzer class.
     *
     * @param mainSubFolder the name of the main subfolder
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
    public void analyze() throws FileNotFoundException, UnsupportedEncodingException, IOException {

        generateRegularExpressionForPunctuation(this.punctuation);
        saveStopWords(this.pathStopWords);

        ArrayList<String> fileNames;

        File folder = new File(this.pathFolderTexts);
        fileNames = listFilesForFolder(folder);

        for (String fileName : fileNames) {
            createIntermediateFileWithEnumeratedTokens(this.pathFolderTexts, fileName);
        }

        File tempFolder = new File(this.mainSubFolder + this.tempFolderName);
        fileNames = listFilesForFolder(tempFolder);

        for (String tempFileName : fileNames) {
            createFinalFileWithoutStopWords(this.mainSubFolder + this.tempFolderName, tempFileName);
        }

        File finalFolder = new File(this.mainSubFolder + this.tempFolderName + this.tempFinalFolderName);
        fileNames = listFilesForFolder(finalFolder);

        for (String finalFileName : fileNames) {

            contentOfText = initializeArrayListText(this.mainSubFolder + this.tempFolderName + this.tempFinalFolderName, finalFileName);

            int shiftCount = 0;
            ArrayList<String> checkPhraseTemp = new ArrayList<String>();
            ArrayList<String> checkPhraseTempOnlyWord = new ArrayList<String>();
            ArrayList<String> checkPhraseTempOnlyTokenNumber = new ArrayList<String>();
            
            for (int y = 0; y < contentOfText.size() - (this.lenghtOfPhrase - 1); y++) {
                
                // Come determinare il file di origine? é finalFileName?
                // Se il nome del file che si sta analizzando è uguale a quello di origine e i numeri dei token sono identici, la cinquina non va aggiunta alle quotes
                // Nel caso in cui venga aggiunta una cinquina diversa da quella di origine allora va aggiunta anche quella di origine
                
                checkPhraseTemp = initializeArrayListCheckPharase(contentOfText, shiftCount);
                
                checkPhraseTempOnlyWord = returnOnlyWordsCheckPhrase(checkPhraseTemp);
                
                checkPhraseTempOnlyTokenNumber = returnOnlyTokenNumberCheckPhrase(checkPhraseTemp);
                
                //System.out.println(finalFileName + " " + checkPhraseTemp);
                
                shiftCount++;

                checkRecurrentQuotes(this.mainSubFolder + this.tempFolderName + this.tempFinalFolderName, finalFileName, lenghtOfPhrase, contentOfText, checkPhraseTempOnlyWord, finalFileName, checkPhraseTempOnlyTokenNumber);

                // Stampa ArrayList citazioni
                System.out.println();

                for (int i = 0; i < quotes.size(); i++) {

                    //if (quotes.size() > 1) {
                        for (int j = 0; j < quotes.get(i).length; j++) {
                            System.out.print(quotes.get(i)[j] + " ");
                        }

                        System.out.println();
                    //}
                }
                
                System.out.println();
                System.out.println();

                quotes.clear();
            }
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

                String wordToAnalyze = tokenNumberAndWord[1];

                if (!stringIsStopWordOrPunctuationCharacter(wordToAnalyze)) {

                    //System.out.print(wordToAnalyze + " ");
                    if (wordToAnalyze.length() > this.nLastCharsToRemove) {
                        wordToAnalyze = wordToAnalyze.substring(0, wordToAnalyze.length() - this.nLastCharsToRemove);
                    }

                    String finalWorld = this.tokenSeparator + tokenNumberAndWord[0] + this.tokenSeparator + wordToAnalyze;
                    writeWordIntoFile(writer, finalWorld);

                    //System.out.println(wordToAnalyze);
                }
            }
            s.close();
        } catch (Exception e) {
            System.err.println("ERROR! An error occours when trying to write a temporany FINAL file inside the project folder. Here some details:");
            System.err.println(e);
        }
    }

    private String[] separateTokenNumber(String word) {
        String[] wordSplitted = word.split(this.tokenSeparator);
        String[] tokenNumberAndWord = {wordSplitted[1], wordSplitted[2]};

        return tokenNumberAndWord;
    }

    private boolean stringIsStopWordOrPunctuationCharacter(String word) {
        return stringIsStopWord(word) || stringIsPunctuationCharacter(word);
    }

    private ArrayList<String> listFilesForFolder(File folder) {

        ArrayList<String> fileNames = new ArrayList<>();

        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else if (!fileEntry.getName().equals(".DS_Store")) {
                fileNames.add(fileEntry.getName());
            }
        }

        return fileNames;
    }

    private boolean stringContainsPunctuationCharacters(String word) {
        return Arrays.stream(this.punctuation).parallel().anyMatch(word::contains);
    }

    private boolean stringIsStopWord(String word) {
        return this.stopWords.contains(word);
    }

    private boolean stringIsPunctuationCharacter(String word) {
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

    private void checkRecurrentQuotes(String path, String fileName, int lenghtOfPhrase, ArrayList<String> contentOfText, ArrayList<String> checkPhrase, String finalFileName, ArrayList<String> checkPhraseTempOnlyTokenNumber) throws FileNotFoundException {

        //System.out.println("AAA: " + Arrays.toString(checkPhrase.toArray()));
        
        ArrayList<Boolean> checkTemp = new ArrayList<Boolean>();
        ArrayList<Boolean> checkTempTokenNumber = new ArrayList<Boolean>();

        int count = 0;

        ArrayList<String> fileNames;

        File folder = new File(this.mainSubFolder + this.tempFolderName + this.tempFinalFolderName);
        fileNames = listFilesForFolder(folder);

        for (String file : fileNames) {
            
            contentOfText = initializeArrayListText(path, file);

            //System.out.println("Inizializzo testo: " + file);
            for (int i = 0; i < contentOfText.size(); i++) {
                
                if (count < lenghtOfPhrase) {
                    
                    String[] wordCleared = separateTokenNumber(contentOfText.get(i));
                    
                    if(wordCleared[0].equals(checkPhraseTempOnlyTokenNumber.get(count)))
                    {
                        checkTempTokenNumber.add(true);
                    } else {
                        checkTempTokenNumber.add(false);
                    }
                
                    // Stampe test
                    //System.out.println(wordCleared[1] + "\tVS\t" + checkPhrase.get(count));
                    //System.out.println(contentOfText.get(i));
                    if (wordCleared[1].equals(checkPhrase.get(count))) {
                        checkTemp.add(true);
                    } else {
                        checkTemp.add(false);
                    }
                    
                    count++;
                } 
                
                if (count == lenghtOfPhrase) {
                    // il confronto con il nome del file va fatto solo quando tutti i numeri dei token sono uguali
                    if (areAllTrue(checkTemp) && !areAllTrue(checkTempTokenNumber)) {
                        System.out.println(Arrays.toString(checkPhrase.toArray()));
                        System.out.println(Arrays.toString(checkPhraseTempOnlyTokenNumber.toArray()));
                        System.out.println(Arrays.toString(checkTemp.toArray()));
                        System.out.println(Arrays.toString(checkTempTokenNumber.toArray()));
                        System.out.println(finalFileName + "   " + file);
                        System.out.println();
                    //if (areAllTrue(checkTemp) && !areAllTrue(checkTempTokenNumber) && !finalFileName.equals(file)) {
                    //if (areAllTrue(checkTemp) && !areAllTrue(checkTempTokenNumber)) {
                        String[] wordClearedTemp = separateTokenNumber(contentOfText.get((i + 1) - lenghtOfPhrase));
                        manageQuote(file, wordClearedTemp[0]);
                    }

                    i = i - (lenghtOfPhrase - 1);
                    count = 0;

                    // Stampe test
                    //System.out.println(Arrays.toString(checkTemp.toArray()));
                    checkTemp.clear();
                    checkTempTokenNumber.clear();
                }
            }

            count = 0;
            checkTemp.clear();
            checkTempTokenNumber.clear();
        }
    }

    private ArrayList<String> initializeArrayListText(String path, String fileName) throws FileNotFoundException {

        ArrayList<String> contentOfText = new ArrayList<String>();
        Scanner s = new Scanner(new File(path + fileName));

        while (s.hasNext()) {

            String word = s.next();
            contentOfText.add(word);
        }

        // Stampa tutto il testo contenuto in contentOfText
        //System.out.println(Arrays.toString(contentOfText.toArray()));
        //System.out.println();
        //System.out.println();
        return contentOfText;
    }

    private ArrayList<String> initializeArrayListCheckPharase(ArrayList<String> contentOfText, int shiftCount) throws FileNotFoundException {

        ArrayList<String> checkPhraseTemp = new ArrayList<String>();

        for (int i = 0; i < this.lenghtOfPhrase; i++) {

            String word = contentOfText.get(shiftCount + i);
            checkPhraseTemp.add(word);
        }

        return checkPhraseTemp;
    }
    
    private ArrayList<String> returnOnlyWordsCheckPhrase(ArrayList<String> checkPhraseTemp) throws FileNotFoundException {

        ArrayList<String> checkPhraseTempOnlyWord = new ArrayList<String>();
        
        for (int i = 0; i < checkPhraseTemp.size(); i++) {
            String[] wordTemp = separateTokenNumber(checkPhraseTemp.get(i));
            checkPhraseTempOnlyWord.add(wordTemp[1]);
        }
        
        return checkPhraseTempOnlyWord;
    }
    
    private ArrayList<String> returnOnlyTokenNumberCheckPhrase(ArrayList<String> checkPhraseTemp) throws FileNotFoundException {

        ArrayList<String> checkPhraseTempOnlyTokenNumber = new ArrayList<String>();
        
        for (int i = 0; i < checkPhraseTemp.size(); i++) {
            String[] wordTemp = separateTokenNumber(checkPhraseTemp.get(i));
            checkPhraseTempOnlyTokenNumber.add(wordTemp[0]);
        }
        
        return checkPhraseTempOnlyTokenNumber;
    }
    
    private boolean areAllTrue(ArrayList<Boolean> checkTemp) {

        for (boolean element : checkTemp) {
            if (!element) {
                return false;
            }
        }
        return true;
    }

    private void manageQuote(String nomeFile, String indice) {

        // Aggiungere if che fa quotes.add solo se la coppia da aggiungere non esiste già, nel checkRecurrentQuotes
        // aggiungere la condizione del nome del file (caso in cui ci sia la stessa cinquina iniziale in più file)
        
        quotes.add(new String[]{nomeFile, indice});
    }
}
