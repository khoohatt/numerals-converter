import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String input = " ";
        StringBuilder sb = new StringBuilder();
        NumeralsConverter nc = new NumeralsConverter();

        while (!input.equals("")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("текст: ");

            while (!input.equals("")) {
                input = scanner.nextLine();
                sb.append(input);
                sb.append("\n");
            }

            input = sb.toString().trim();

            if (!input.equals("")) {
                List<String> parsedText = getParserBearingPhraseWithPunctuation(input);
                System.out.print("\n\nрезультат: ");
                System.out.println(nc.replaceNumber(parsedText) + "\n\n");
                sb.setLength(0);
                input = " ";
            }
        }
    }

    // эти методы будут в gama
    public static List<String> getParserBearingPhraseWithPunctuation(String bearingPhrase) {
        return new LinkedList<>(Arrays.asList(bearingPhrase.split("(?<=(\n))|((?<![\\p{L}\\p{N}_-])|(?![\\p{L}\\p{N}_-]))((?<![,./])|(?!(\\p{N})))")));
    }

    public static List<List<String>> getParserSentenceWithPunctuation(String sentence) {
        List<List<String>> phraseList = new LinkedList<>();

        for (String basicsPhase : sentence.split("((?=(\n))|((?<=[,.!?–;:])(?!(\\p{N})))|((?=[,.!?–;:\n])(?<!(\\p{N}))))")) {
            phraseList.add(getParserBearingPhraseWithPunctuation(basicsPhase));
        }

        return phraseList;
    }

    public static List<List<List<String>>> getParserParagraphWithPunctuation(String paragraph) {
        List<List<List<String>>> sentenceList = new LinkedList<>();

        for (String sentence : paragraph.split("((?=(\n))|((?<=[.!?])(?!(\\p{N})))|((?=[.!?\n])(?<!(\\p{N}))))")) {
            sentenceList.add(getParserSentenceWithPunctuation(sentence));
        }

        return sentenceList;
    }

    public static List<List<List<List<String>>>> getParserTextWithPunctuation(String text) {
        List<List<List<List<String>>>> paragraphList = new LinkedList<>();

        for (String paragraph : text.split("(?=(\n))")) {
            paragraphList.add(getParserParagraphWithPunctuation(paragraph));
        }

        return paragraphList;
    }
}