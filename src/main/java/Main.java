import ru.textanalysis.tawt.gama.parser.GamaParserDefault;
import ru.textanalysis.tawt.graphematic.parser.exception.NotParserTextException;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;

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
Азовского – 14 метров. Самой длинной рекой в мире считается Нил, его длина – 6671 километр.
Наибольшая глубина Байкала – 1637 метров.
Во-2, в-20, в-21, в-19, в-97, в-11...
4-ём или 4-ем стенам, в 1945-м, около 2007-го, у 215-го
деньги: 3,4 рубля, а еще 5792,34 дол. или 9,3345 долларов;
62873,2952 руб, 4,2 руб...
2 334 232 рубля, 666 666 666 рублей
мне 0 лет, у меня 0 рублей...
Если бы 2 химиков, свободно владеющих 30 языками, начали с 1 января 1964 года читать все выходящие в
этом году публикации, представляющие для них профессиональный интерес, и читали бы их по 40 часов в
неделю со скоростью 4 публикации в час, то к 31 декабря 1964 года они прочитали бы лишь 1/10 часть этих
публикаций.
Самое долговечное из домашних животных – осёл, он доживает до 50 лет, лошадь и верблюд живут до 30 лет, корова – до 25, собака и кошка живут до 15 лет.
*/

    public static void main(String[] args) {
        String input = " ";
        StringBuilder sb = new StringBuilder();

        /*JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        GamaParserDefault gamaParserDefault = new GamaParserDefault();
        gamaParserDefault.init();*/

//        List<List<String>> actual = gamaParserDefault.getParserSentence("Человек делает за день около 20 тысяч шагов, за год – до 7 миллионов, а за 70 лет – почти 500 миллионов");

        /*for (List<String> list : actual) {
            for (String string : list) {
                System.out.println(string);
            }
        }*/


        /*for (Form form : jMorfSdk.getOmoForms(".")) {
            System.out.println("слово: " + form);
            if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.PUNCTUATION) {
                System.out.println(form.getTypeOfSpeech() + " знак");
            }
        }*/

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
                List<List<String>> parsedText = parserSentenceWithPunctuation(input);
                System.out.print("\n\nрезультат: " + nc.replaceNumber(parsedText) + "\n\n");
                sb.setLength(0);
                input = " ";
            }
        }
    }

    public static List<List<String>> parserSentenceWithPunctuation(String sentence) throws NotParserTextException {
        List<List<String>> sentenceList = new LinkedList<>();

        for (String basicsPhase : sentence.split("((?=(\n))|((?<=[,.!?–;:])(?!(\\p{N})))|((?=[,.!?–;:\n])(?<!(\\p{N}))))")) {
            System.out.println(basicsPhase);
            sentenceList.add(parserPhraseWithPunctuation(basicsPhase));
        }

        return sentenceList;
    }

    public static List<String> parserPhraseWithPunctuation(String basicsPhase) throws NotParserTextException {
        return new LinkedList<>(Arrays.asList(basicsPhase.split("(?<=(\n))|((?<![\\p{L}\\p{N}_-])|(?![\\p{L}\\p{N}_-]))((?<![,./])|(?!(\\p{N})))")));
    }
}