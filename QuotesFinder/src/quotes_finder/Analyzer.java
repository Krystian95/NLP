package quotes_finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    javax.swing.JTextArea textAreaFragment;
    private String mainSubFolder;
    private String pathStopWords;
    private String pathFolderTexts;
    private final String tempFolderName = createTempFolderName();
    private final String tempFinalFolderName = createTempFinalFolderName();
    private final String[] punctuation = {".", ",", ";", ":", "·", "!", "?", "|", "%", "(", ")", "=", "'", "^", "*", "+", "°", "§", "@", "-", "_", "<", ">"};
    private List<String> listPunctuation;
    private String regularExpressionPunctuation;
    private ArrayList<String> stopWords;
    private ArrayList<String> contentOfText = new ArrayList<>();
    private ArrayList<String[]> quotes = new ArrayList<>();
    private int tokenCounter;
    private final String tokenSeparator = "_";
    private int nLastCharsToRemove = 2;
    private int lenghtOfPhrase = 3;

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
        File directory = new File(this.tempFolderName);
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

        File tempFolder = new File(this.tempFolderName);
        fileNames = listFilesForFolder(tempFolder);

        for (String tempFileName : fileNames) {
            createFinalFileWithoutStopWords(this.tempFolderName, tempFileName);
        }

        File finalFolder = new File(this.tempFinalFolderName);
        fileNames = listFilesForFolder(finalFolder);

        for (String finalFileName : fileNames) {
            contentOfText = initializeArrayListText(this.tempFinalFolderName, finalFileName);
            int shiftCount = 0;
            ArrayList<String> checkPhraseTemp = new ArrayList<>();
            ArrayList<String> checkPhraseTempOnlyWord = new ArrayList<>();
            ArrayList<String> checkPhraseTempOnlyTokenNumber = new ArrayList<>();
            
            for (int y = 0; y < this.contentOfText.size() - (this.lenghtOfPhrase - 1); y++) {
                checkPhraseTemp = initializeArrayListCheckPharase(contentOfText, shiftCount);
                checkPhraseTempOnlyWord = returnOnlyWordsCheckPhrase(checkPhraseTemp);
                checkPhraseTempOnlyTokenNumber = returnOnlyTokenNumberCheckPhrase(checkPhraseTemp);
                shiftCount++;
                checkRecurrentQuotes(this.tempFinalFolderName, finalFileName, lenghtOfPhrase, contentOfText, checkPhraseTempOnlyWord, finalFileName, checkPhraseTempOnlyTokenNumber);
            }
        }

        // Ordina quotes in base alle citazioni
        quotes.sort(Comparator.comparing(a -> a[2]));
        
        // Stampa ArrayList citazioni
        for (int i = 0; i < this.quotes.size(); i++) {
            this.textAreaFragment.append(quotes.get(i)[2] + "\t\t");
            this.textAreaFragment.append(quotes.get(i)[0] + "\t");
            this.textAreaFragment.append(quotes.get(i)[4]);
            this.textAreaFragment.append("\n");

            // A capo ad ogni gruppo di citazioni
            if (i + 1 < quotes.size() && !quotes.get(i)[2].equals(quotes.get(i + 1)[2])) {
                this.textAreaFragment.append("\n");
            }
        }
        this.textAreaFragment.append("\n");
    }

    public void setTextArea(javax.swing.JTextArea jTextArea1) {
        this.textAreaFragment = jTextArea1;
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

        try (PrintWriter writer = new PrintWriter(this.tempFolderName + "~" + fileName, "UTF-8")) {

            s = new Scanner(new File(path + fileName), "UTF-8");
            while (s.hasNext()) {
                
                System.setOut(new PrintStream(System.out, true, "UTF8"));
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

    public String fileToString(String location) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(location), "UTF-8");
        return readerToString(fr);
    }
    
    public String readerToString(InputStreamReader r) throws IOException {
        StringWriter sw = new StringWriter();
        char[] buf = new char[1024];
        int len;
        while ((len = r.read(buf)) > 0) {
            sw.write(buf, 0, len);
        }
        r.close();
        sw.close();
        return sw.toString();
    }

    private void createFinalFileWithoutStopWords(String path, String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        this.tokenCounter = 0;
        String fileInString = fileToString(path + fileName);
        String[] fileInArray = fileInString.split(" ");

        try (PrintWriter writer = new PrintWriter(this.tempFinalFolderName + fileName, "UTF-8")) {

            for (int i = 0; i < fileInArray.length; i++) {
                String word = fileInArray[i];
                String[] tokenNumberAndWord = separateTokenNumber(word);
                String wordToAnalyze = tokenNumberAndWord[1];

                if (!stringIsStopWordOrPunctuationCharacter(wordToAnalyze)) {
                    if (wordToAnalyze.length() > this.nLastCharsToRemove) {
                        wordToAnalyze = wordToAnalyze.substring(0, wordToAnalyze.length() - this.nLastCharsToRemove);
                    }
                    
                    String finalWorld = this.tokenSeparator + tokenNumberAndWord[0] + this.tokenSeparator + wordToAnalyze;
                    writeWordIntoFile(writer, finalWorld);
                }
            }

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

    private void checkRecurrentQuotes(String path, String originalFileName, int lenghtOfPhrase, ArrayList<String> contentOfText, ArrayList<String> checkPhrase, String finalFileName, ArrayList<String> checkPhraseTempOnlyTokenNumber) throws FileNotFoundException, IOException {

        ArrayList<Boolean> checkTemp = new ArrayList<>();
        ArrayList<Boolean> checkTempTokenNumber = new ArrayList<>();
        ArrayList<String> fileNames;
        File folder = new File(this.tempFinalFolderName);
        fileNames = listFilesForFolder(folder);
        int count = 0;

        for (String file : fileNames) {

            contentOfText = initializeArrayListText(path, file);

            for (int i = 0; i < contentOfText.size(); i++) {

                if (count < lenghtOfPhrase) {
                    String[] wordCleared = separateTokenNumber(contentOfText.get(i));

                    if (wordCleared[0].equals(checkPhraseTempOnlyTokenNumber.get(count))) {
                        checkTempTokenNumber.add(true);
                    } else {
                        checkTempTokenNumber.add(false);
                    }

                    if (wordCleared[1].equals(checkPhrase.get(count))) {
                        checkTemp.add(true);
                    } else {
                        checkTemp.add(false);
                    }
                    count++;
                }

                if (count == lenghtOfPhrase) {
                    if (areAllTrue(checkTemp) && ((file.equals(originalFileName) && !areAllTrue(checkTempTokenNumber)) || !file.equals(originalFileName))) {
                        String[] wordClearedTemp = separateTokenNumber(contentOfText.get((i + 1) - lenghtOfPhrase));
                        manageQuote(file, wordClearedTemp[0], checkPhrase);
                    }
                    i = i - (lenghtOfPhrase - 1);
                    count = 0;
                    checkTemp.clear();
                    checkTempTokenNumber.clear();
                }
            }
            count = 0;
            checkTemp.clear();
            checkTempTokenNumber.clear();
        }
    }

    private ArrayList<String> initializeArrayListText(String path, String fileName) throws FileNotFoundException, IOException {

        ArrayList<String> contentOfText = new ArrayList<String>();
        String fileInString = fileToString(path + fileName);
        String[] fileInArray = fileInString.split(" ");
        
        for (int i = 0; i < fileInArray.length; i++){
            String word = fileInArray[i];
            contentOfText.add(word);
        }
        return contentOfText;
    }

    private ArrayList<String> initializeArrayListCheckPharase(ArrayList<String> contentOfText, int shiftCount) throws FileNotFoundException {

        ArrayList<String> checkPhraseTemp = new ArrayList<>();

        for (int i = 0; i < this.lenghtOfPhrase; i++) {

            String word = contentOfText.get(shiftCount + i);
            checkPhraseTemp.add(word);
        }
        return checkPhraseTemp;
    }

    private ArrayList<String> returnOnlyWordsCheckPhrase(ArrayList<String> checkPhraseTemp) throws FileNotFoundException {

        ArrayList<String> checkPhraseTempOnlyWord = new ArrayList<>();

        for (int i = 0; i < checkPhraseTemp.size(); i++) {
            String[] wordTemp = separateTokenNumber(checkPhraseTemp.get(i));
            checkPhraseTempOnlyWord.add(wordTemp[1]);
        }
        return checkPhraseTempOnlyWord;
    }

    private ArrayList<String> returnOnlyTokenNumberCheckPhrase(ArrayList<String> checkPhraseTemp) throws FileNotFoundException {

        ArrayList<String> checkPhraseTempOnlyTokenNumber = new ArrayList<>();

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

    public boolean isInList(ArrayList<String[]> quotes, String[] quote) {

        for (String[] item : quotes) {
            if (Arrays.equals(item, quote)) {
                return true;
            }
        }
        return false;
    }

    private void manageQuote(String nomeFile, String indice, ArrayList<String> checkPhrase) throws FileNotFoundException, IOException {

        String paragraphOriginalText = recoverOriginalText(this.tempFolderName, nomeFile, Integer.parseInt(indice));
        String joined = String.join(" ", checkPhrase);
        String nomeFileOriginal = nomeFile;
        nomeFile = nomeFile.substring(1, nomeFile.length() - 4);
        
        if (!isInList(quotes, new String[]{nomeFile, indice, joined, nomeFileOriginal, paragraphOriginalText})) {
            quotes.add(new String[]{nomeFile, indice, joined, nomeFileOriginal, paragraphOriginalText});
        }
    }

    private String recoverOriginalText(String path, String nomeFile, Integer indice) throws FileNotFoundException, IOException {

        StringBuilder paragraphOriginalText = new StringBuilder();
        ArrayList<String> contentOfText = new ArrayList<>();
        contentOfText = returnOnlyWordsCheckPhrase(initializeArrayListText(path, nomeFile));

        for (int i = 0; i < contentOfText.size(); i++) {

            if (i >= (indice - 10) && i <= ((indice + lenghtOfPhrase) + 10)) {

                String content = contentOfText.get(i);

                if (i > 0 && !this.stopWords.contains(content) && !contentOfText.get(i - 1).equals("'")) {
                    paragraphOriginalText.append(" ");
                }

                paragraphOriginalText.append(content);
            }
        }
        return paragraphOriginalText.toString();
    }

    public void setlenghtOfPhrase(Integer lenghtOfPhrase) {
        this.lenghtOfPhrase = lenghtOfPhrase;
    }

    public void setnLastCharsToRemove(Integer nLastCharsToRemove) {
        this.nLastCharsToRemove = nLastCharsToRemove;
    }

    public String getOsName() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS;
    }

    public boolean isWindows() {
        return getOsName().contains("win");
    }

    public String createTempFolderName() {
        if (isWindows()) {
            return "temp_files\\";
        } else {
            return "temp_files/";
        }
    }

    public String createTempFinalFolderName() {
        if (isWindows()) {
            return "final\\";
        } else {
            return "final/";
        }
    }
}