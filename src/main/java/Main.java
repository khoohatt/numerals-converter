import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {
/*
987654321
ыгр, 76676! вора. ыв - 666, 88, ооыова.
Есть заявки на 2 конфедерации и 1 мусульманское государство.
Есть заявки на 2 чудесные конфедерации и 32 мусульманских государства.
1. Чтобы обойти Антарктиду, ледоколу понадобилось не больше 62 суток.
как-то 5-ти раз-таки, около 11-ти, 35-й час или 45й год
2) примерно 34,8 млн.
3/4, 3,2, 5/2, 3.095 часов, 5 тыс. кораблей
23 455 235 кошкам
1. Человек делает за день около 20 тысяч шагов, за год – до 7 миллионов, а за 70 лет – почти 500 миллионов
шагов. Это значит, что за всю свою жизнь человек мог бы 9 раз обойти земной шар по экватору или
преодолеть расстояние от Земли до Луны.
2. В Мировом океане обитает 18 тысяч видов рыб. Наибольшая глубина Балтийского моря – 459 метров,
Азовского – 14 метров. Самой длинной рекой в мире считается Нил, его длина – 6672 километра.
Наибольшая глубина Байкала – 1637 метров.
Во-2, в-20, в-21, в-19, в-97, в-11...
4-ём или 4-ем стенам, в 1945-м, около 2007-го, у 215-го
деньги: 3,4 рубля, а еще 5792,34 дол. или 9,3345 долларов;
62873,2952 руб, 4,2 руб...
2 334 232 рубля, 666 666 666 рублей
мне 0 лет, у меня 0 рублей...
Если бы 2 химика, свободно владеющие 30 языками, начали с 1 января 1964 года читать все выходящие в
этом году публикации, представляющие для них профессиональный интерес, и читали бы их по 40 часов в
неделю со скоростью 4 публикации в час, то к 31 декабря 1964 года они прочитали бы лишь 2/10 части этих
публикаций.
Самое долговечное из домашних животных – осёл, он доживает до 50 лет, лошадь и верблюд живут до 30 лет, корова – до 25, собака и кошка живут до 15 лет.
Спасская башня московского Кремля была сооружена в 1491 году в период княжения Ивана III.
*/

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