import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;

import java.text.DecimalFormat;
import java.util.Scanner;

public class Var5 {
/*
ыгр, 76676! вора. ыв - 666, 88, ооыова.
Есть заявки на 2 конфедерации и 1 мусульманское государство.
Есть заявки на 2 чудесные конфедерации и 32 мусульманских государства.
1. Чтобы обойти Антарктиду, ледоколу понадобилось не больше 62 суток.
как-то 5-ти раз-таки, около 11-ти, 35-й час или 45й год
2) примерно 34,8 млн.
3/4, 3,2, 5/2, 3.095 часов, 2 тыс. кораблей
23 455 235 кошкам
1. Человек делает за день около 20 тысяч шагов, за год – до 7 миллионов, а за 70 лет – почти 500 миллионов
шагов. Это значит, что за всю свою жизнь человек мог бы 9 раз обойти земной шар по экватору или
преодолеть расстояние от Земли до Луны.
2. В Мировом океане обитает 18 тысяч видов рыб. Наибольшая глубина Балтийского моря – 459 метров,
Азовского – 14 метров. Самой длинной рекой в мире считается Нил, его длина – 6671 километр.
Наибольшая глубина Байкала – 1637 метров.
Во-2, в-20, в-21, в-19, в-97, в-11...
4-ём или 4-ем стенам, в 1945-м, около 2007-го
деньги: 3,4 рубля, а еще 5792,34 дол. или 9,3345 долларов; 62873,2952 руб, 4,2 руб.
*/

    // исправить: ноль, номера телефонов, годы без наращений, деньги, 10-19- в годах, наращения не по правилам,
    // цифры перед тысячами: 2 334 232 рубля, 666 666 666 рублей, "2" в деньгах, склонение в дробях,
    // сложить все в отдельный класс

/*    public static void main(String[] args) {
        String input = " ";
        StringBuilder sb = new StringBuilder();

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

                System.out.print("результат: " + replaceNumber(input) + "\n\n");
                sb.setLength(0);
                input = " ";
            }
        }
    }*/

    public static String replaceNumber(String text) {
        long[] forms;  // падеж, род

        String[] words = text.split("((?<![\\p{L}\\p{N}_-])|(?![\\p{L}\\p{N}_-]))((?<![,./])|(?!(\\p{N})))");  // (?=!(\\p{N}+[,.]\\p{N}+))  |((?<![.,])|(?![\\p{N}.,]))|((?<![\\p{N}.,])|(?![.,]))");  // (?<![\p{L}\p{N}_-])|(?![\p{L}\p{N}_-])|(?<=\d)[,.](?=\d)  text.split("(?<!\\d),(?!\\d)|(?<!-)\s+");  // text.split("(?<![\\p{L}\\p{N}_-])|(?![\\p{L}\\p{N}_-])");
        String[] result = new String[words.length];

        for (int i = 0; i < words.length; i++) {
            forms = new long[2];
            String word = words[i];

            if ((words.length > i + 1) && (word.matches("^\\d+") && (words[i + 1].equals(".") || words[i + 1].equals(")")))) {  // нумерованный список
                System.out.println("список");
                result[i] = word;

            } else if ((word.matches("^(\\+)?(?:[0-9] ?){6,14}[0-9]$"))) {  // номера телефонов (пока не всех)
                result[i] = word;

            } else if ((words.length > i + 1) && (words[i + 1].matches("(,\\d+)+"))) {  // дробные числа через запятую: 34,9; 1341,0021
                i++;
                word += words[i];
                while ((words.length > i + 2) && (words[i + 2].matches("(млн|тыс)"))) {
                    if (words[i + 2].equals("млн")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * 1000000);
                    } else if (words[i + 2].equals("тыс")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * 1000);
                    }
                    i += 2;
                    if (words.length > i + 3 && (words[i + 1].equals(".")&& !words[i + 2].equals("\n"))) {
                        i++;
                    }
                }
                if (words.length > i + 2) {
                    forms = getForms(words, i, (int) Double.parseDouble(word.replace(",", ".")) % 10 == 1);
                }
                forms[1] = 8;
                if (word.contains(",") && (words.length > i + 2) && (words[i + 2].matches("(руб[а-я]*)|(дол[а-я]*)|(евр[а-я]*)"))) {
                    result[i] = convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms, words[i + 2]);
                    words[i + 1] = words[i + 2] = "";
                } else {
                    result[i] = convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms);
                }

            } else if ((words.length > i + 1) && (words[i + 1].matches("/\\d+"))) {  // дробные числа через слэш: 3/6, 929/2913
                i++;
                word += words[i];
                double number = Double.parseDouble(word.split("/")[0]) / Double.parseDouble(word.split("/")[1]);
                if (words.length > i + 2) {
                    forms = getForms(words, i, (int) number % 10 == 1);
                }
                forms[1] = 8;
                result[i] = convertFractionalNumberToWords(number, forms);

            } else if (word.matches("\\d+")) {  // обычные числа: 6, 9992, 949913; числа с делением тысяч точками: 23.231.865, 5.246; числа с делением тысяч пробелами: 23 231 865, 5 246
                StringBuilder number = new StringBuilder(word);
                if (words.length > i + 1) {

                    while ((words.length > i + 1) && (words[i + 1].matches("(\\.\\d+)+"))) {
                        i++;
                        number.append(words[i]);
                    }
                    while ((words.length > i + 2) && (words[i + 1].matches("(\\s)") && (words[i + 2].matches("\\d{3}")))) {
                        i += 2;
                        number.append(words[i]);
                    }
                    while ((words.length > i + 2) && (words[i + 2].matches("(млн|тыс)"))) {
                        if (words[i + 2].equals("млн")) {
                            number.append("000000");
                        } else if (words[i + 2].equals("тыс")) {
                            number.append("000");
                        }
                        i += 2;
                        if ((words.length > i + 3) && (words[i + 1].equals(".") && !words[i + 2].equals("\n"))) {
                            i++;
                        }
                    }
                    if (words.length > i + 2) {
                        forms = getForms(words, i, Integer.parseInt(word) % 10 == 1);
                    }
                }
                result[i] = convertNumberToWords(Integer.parseInt(number.toString().replaceAll("[ .]", "")), forms);

            } else if (word.matches("\\d+-((?:[тм]и)|(?:[её]м))")) {  // числа с наращениями: 5-ти, 4-ем
                word = word.replace("ем", "ём");
                result[i] = convertNumberToWords(Integer.parseInt(word.substring(0, word.indexOf("-"))), forms, word.substring(word.indexOf("-") + 1));

            } else if (word.matches("[вВ](о)*-(\\d)+")) {
                result[i] = convertEnumToWords(word);

            } else if (word.matches("(\\d)+(-)*([ыо]?й|[ыо]?м|о?е|а?я|ы?х|у?ю|го)")) {  // числа с наращениями: 1980-м, 16-е, 4-ом, 21-х, 5-й, 5й
                result[i] = convertOrdinalToWords(word);

            } else {
                result[i] = word;
            }
        }

        StringBuilder finalText = new StringBuilder();
        for (String s : result) {
            if (s != null) {
                finalText.append(s);
            }
        }

        return finalText.toString();
    }

    public static long[] getForms(String[] words, int i, boolean single) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        long[] forms = new long[2];

        ++i;
        while (i < words.length && !words[i].equals(".")) {

            for (Form form : jMorfSdk.getOmoForms(words[i])) {
                System.out.println("слово: " + form);

                if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                        && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.class) == MorfologyParameters.Numbers.PLURAL ^ single)) {
                    System.out.println("существительное: " + form);
                    forms[0] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER);
                    forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                    if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
                        break;
                    } else if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE) ||
                            (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2)) {
                        break;
                    }
                }
            }
            ++i;
        }
        //        jMorfSdk.finish();
        return forms;
    }

    public static String convertNumberToWords(int num, long[] forms, String... buildup) {  // ноль добавить
        String[] ones = {"ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"};
        String[] tens = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String[] hundreds = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};
        String[] thousands = {"тысяча", "тысячи", "тысяч"};
        String[] millions = {"миллион", "миллиона", "миллионов"};

        StringBuilder result = new StringBuilder();

        if (num == 0) {
            return result.append(getFormedNumber(forms, ones[0])).toString();
        }

        if (num / 1000000 > 0) {
            result.append(convertNumberToWords(num / 1000000, forms)).append(" ").append(getWordForms(num / 1000000, millions, forms[0])).append(" ");
            num = num % 1000000;
        }

        if (num / 1000 > 0) {
            result.append(convertNumberToWords(num / 1000, new long[]{forms[0] != 0 ? forms[0] : 512, 8})).append(" ").append(getWordForms(num / 1000, thousands, forms[0])).append(" ");
            num = num % 1000;  // getWordForms(num / 1000, thousands, forms[1])  // thousands[(num / 1000) % 10])
        }

        if (num / 100 > 0) {
            result.append(getFormedNumber(forms, hundreds[num / 100])).append(" ");
            num = num % 100;
        }

        if (num > 0) {
            if (num < 20) {
                if (buildup.length == 0) {
                    result.append(getFormedNumber(forms, ones[num])).append(" ");
                } else {
                    result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num]), buildup[0]));
                }
            } else {
                if (buildup.length == 0) {
                    result.append(getFormedNumber(forms, tens[num / 10])).append(" ");
                } else {
                    result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num / 10]), buildup[0]));
                }

                if (num % 10 > 0) {
                    if (buildup.length == 0) {
                        result.append(getFormedNumber(forms, ones[num % 10])).append(" ");
                    } else {
                        result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num % 10]), buildup[0]));
                    }
                }
            }
        }

        return result.toString().trim();
    }

    public static String convertEnumToWords(String enumer) {
        String[] onesOrdinal = {"нулевых", "первых", "вторых", "третьих", "четвертых", "пятых", "шестых", "седьмых", "восьмых", "девятых", "десятых", "одиннадцатых", "двенадцатых", "тринадцатых", "четырнадцатых", "пятнадцатых", "шестнадцатых", "семнадцатых", "восемнадцатых", "девятнадцатых"};
        String[] tensOrdinal = {"", "", "двадцатых", "тридцатых", "сороковых", "пятидесятых", "шестидесятых", "семидесятых", "восьмидесятых", "девяностых"};

        String[] result = enumer.split("-");
        String word = convertNumberToWords(Integer.parseInt(result[1]), new long[]{0, 0});
        String[] words = word.split(" ");
        int number = Integer.parseInt(result[1]);

        if (number < 20) {
            words[words.length - 1] = onesOrdinal[number];
        } else if (number % 10 != 0) {
            words[words.length - 1] = onesOrdinal[number % 10];
        } else {
            words[words.length - 1] = tensOrdinal[number / 10];
        }

        if ((number < 20) || (number % 10 == 0)) {
            return result[0] + "-" + String.join(" ", words);
        } else {
            return result[0] + " " + String.join(" ", words);
        }
    }

    public static String convertOrdinalToWords(String ordinal) {
        String[][] onesOrdinal = {{"нулевой", "нулевого", "нулевому", "нулевого", "нулевым", "нулевом", "нулевая", "нулевой", "нулевой", "нулевую", "нулевой", "нулевой", "нулевое", "нулевого", "нулевому", "нулевое", "нулевым", "нулевом", "нулевые", "нулевых", "нулевым", "нулевых", "нулевыми", "нулевых"},
                {"первый", "первого", "первому", "первом", "первым", "первая", "первой", "первую", "первое", "первые", "первых", "первым", "первыми"},
                {"второй", "второго", "второму", "втором", "вторым", "вторая", "второй", "вторую", "второе", "вторые", "вторых", "вторым", "вторыми"},
                {"третий", "третьего", "третьему", "третьем", "третьим", "третья", "третьей", "третью", "третье", "третьи", "третьих", "третьим", "третьими"},
                {"четвертый", "четвертого", "четвертому", "четвертом", "четвертым", "четвертая", "четвертой", "четвертую", "четвертое", "четвертые", "четвертых", "четвертым", "четвертыми"},
                {"пятый", "пятого", "пятому", "пятом", "пятым", "пятая", "пятой", "пятую", "пятое", "пятые", "пятых", "пятым", "пятыми"},
                {"шестой", "шестого", "шестому", "шестом", "шестым", "шестая", "шестой", "шестую", "шестое", "шестые", "шестых", "шестым", "шестыми"},
                {"седьмой", "седьмого", "седьмому", "седьмом", "седьмым", "седьмая", "седьмой", "седьмую", "седьмое", "седьмые", "седьмых", "седьмым", "седьмыми"},
                {"восьмой", "восьмого", "восьмому", "восьмом", "восьмым", "восьмая", "восьмой", "восьмую", "восьмое", "восьмые", "восьмых", "восьмым", "восьмыми"},
                {"девятый", "девятого", "девятому", "девятом", "девятым", "девятая", "девятой", "девятую", "девятое", "девятые", "девятых", "девятым", "девятыми"},
                {},
                {"одиннадцатый", "одиннадцатого", "одиннадцатому", "одиннадцатом", "одиннадцатым", "одиннадцатая", "одиннадцатой", "одиннадцатую", "одиннадцатое", "одиннадцатые", "одиннадцатых", "одиннадцатым", "одиннадцатыми"},
                {"двенадцатый", "двенадцатого", "двенадцатому", "двенадцатом", "двенадцатым", "двенадцатая", "двенадцатой", "двенадцатую", "двенадцатое", "двенадцатые", "двенадцатых", "двенадцатым", "двенадцатыми"},
                {"тринадцатый", "тринадцатого", "тринадцатому", "тринадцатом", "тринадцатым", "тринадцатая", "тринадцатой", "тринадцатую", "тринадцатое", "тринадцатые", "тринадцатых", "тринадцатым", "тринадцатыми"},
                {"четырнадцатый", "четырнадцатого", "четырнадцатому", "четырнадцатом", "четырнадцатым", "четырнадцатая", "четырнадцатой", "четырнадцатую", "четырнадцатое", "четырнадцатые", "четырнадцатых", "четырнадцатым", "четырнадцатыми"},
                {"пятнадцатый", "пятнадцатого", "пятнадцатому", "пятнадцатом", "пятнадцатым", "пятнадцатая", "пятнадцатой", "пятнадцатую", "пятнадцатое", "пятнадцатые", "пятнадцатых", "пятнадцатым", "пятнадцатыми"},
                {"шестнадцатый", "шестнадцатого", "шестнадцатому", "шестнадцатым", "шестнадцатом", "шестнадцатая", "шестнадцатой", "шестнадцатую", "шестнадцатое", "шестнадцатые", "шестнадцатых", "шестнадцатым", "шестнадцатыми"},
                {"семнадцатый", "семнадцатого", "семнадцатому", "семнадцатым", "семнадцатом", "семнадцатая", "семнадцатой", "семнадцатую", "семнадцатое", "семнадцатые", "семнадцатых", "семнадцатым", "семнадцатыми"},
                {"восемнадцатый", "восемнадцатого", "восемнадцатому", "восемнадцатым", "восемнадцатом", "восемнадцатая", "восемнадцатой", "восемнадцатую", "восемнадцатое", "восемнадцатые", "восемнадцатых", "восемнадцатым", "восемнадцатыми"},
                {"девятнадцатый", "девятнадцатого", "девятнадцатому", "девятнадцатым", "девятнадцатом", "девятнадцатая", "девятнадцатой", "девятнадцатую", "девятнадцатое", "девятнадцатые", "девятнадцатых", "девятнадцатым", "девятнадцатыми"}};

        String[][] tensOrdinal = {{}, {"десятый", "десятого", "десятому", "десятом", "десятым", "десятая", "десятой", "десятую", "десятое", "десятые", "десятых", "десятым", "десятыми"},
                {"двадцатый", "двадцатого", "двадцатому", "двадцатом", "двадцатым", "двадцатая", "двадцатой", "двадцатую", "двадцатое", "двадцатые", "двадцатых", "двадцатым", "двадцатыми"}};

        String result[] = new String[2];
        if (ordinal.contains("-")) {
            result = ordinal.split("-");
        } else {
            result[0] = ordinal.replaceAll("\\D+", "");
            result[1] = ordinal.replaceAll("\\d+", "");
        }
        String word = convertNumberToWords(Integer.parseInt(result[0]), new long[]{0, 0});
        String[] words = word.split(" ");
        int number = Integer.parseInt(result[0]);

        String[] forms;
        if (number < 20) {
            forms = onesOrdinal[number];
        } else if (number % 10 != 0) {
            if ((number % 100 < 20) && (number % 100 > 9)) {
                forms = onesOrdinal[number % 100];
            } else {
                forms = onesOrdinal[number % 10];
            }
        } else {
            forms = tensOrdinal[(number % 100) / 10];
        }

        for (String form : forms) {
            if (form.endsWith(result[1])) {
                words[words.length - 1] = form;
                break;
            }
        }

        return String.join(" ", words);
    }

    public static String convertFractionalNumberToWords(double number, long[] forms, String... currency) {  // добавить склонение (думаю)
        number = ((number * 1e6)) / 1e6;
        int integerPart = (int) number;

        DecimalFormat df = new DecimalFormat("0.000000000");
        String numberStr = df.format(number);
        String fractionalStr = numberStr.substring(numberStr.indexOf(',') + 1).replaceAll("0*$", "");
        int fractionalPart = !fractionalStr.equals("") ? Integer.parseInt(fractionalStr) : 0;

        if (currency.length != 0) {
            if (fractionalPart < 10) {
                fractionalPart *= 10;
            } else if (fractionalPart > 99) {
                fractionalPart = fractionalPart / (int) (Math.pow(10, (int) (Math.log10(fractionalPart) + 1) - 2));
            }

            String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, new long[]{forms[0], 0}) : "ноль";
            String fractionalWords;

            switch (currency[0].substring(0, 3)) {
                case "руб":
                    fractionalWords = convertNumberToWords(fractionalPart, new long[]{forms[0], 8});
                    break;

                case "дол":
                    fractionalWords = convertNumberToWords(fractionalPart, new long[]{forms[0], 0});
                    break;

                default:
                    return convertFractionalNumberToWords(number, forms);
            }

            forms[1] = 0;
            String whole, fractional;

            switch (currency[0].substring(0, 3)) {
                case "руб":
                    if ((integerPart % 100 < 21 && integerPart % 100 > 10) || integerPart % 10 == 5 || integerPart % 10 == 6 || integerPart % 10 == 7 || integerPart % 10 == 8 || integerPart % 10 == 9 || integerPart % 10 == 0) {
                        whole = " рублей ";
                    } else if (integerPart % 10 == 1) {
                        whole = " рубль ";
                    } else {
                        whole = " рубля ";
                    }

                    if ((fractionalPart % 100 < 21 && fractionalPart % 100 > 10) || fractionalPart % 10 == 5 || fractionalPart % 10 == 6 || fractionalPart % 10 == 7 || fractionalPart % 10 == 8 || fractionalPart % 10 == 9 || fractionalPart % 10 == 0) {
                        fractional = " копеек";
                    } else if (fractionalPart % 10 == 1) {
                        fractional = " копейка";
                    } else {
                        fractional = " копейки";
                    }
                    break;

                case "дол":
                    if ((integerPart % 100 < 21 && integerPart % 100 > 10) || integerPart % 10 == 5 || integerPart % 10 == 6 || integerPart % 10 == 7 || integerPart % 10 == 8 || integerPart % 10 == 9 || integerPart % 10 == 0) {
                        whole = " долларов ";
                    } else if (integerPart % 10 == 1) {
                        whole = " доллар ";
                    } else {
                        whole = " доллара ";
                    }

                    if ((fractionalPart % 100 < 21 && fractionalPart % 100 > 10) || fractionalPart % 10 == 5 || fractionalPart % 10 == 6 || fractionalPart % 10 == 7 || fractionalPart % 10 == 8 || fractionalPart % 10 == 9 || fractionalPart % 10 == 0) {
                        fractional = " центов";
                    } else if (fractionalPart % 10 == 1) {
                        fractional = " цент";
                    } else {
                        fractional = " цента";
                    }
                    break;

                default:
                    return convertFractionalNumberToWords(number, forms);
            }
            integerWords += whole + fractionalWords + fractional;

            return integerWords;
        } else {
            String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, forms) : "ноль";
            String fractionalWords = convertNumberToWords(fractionalPart, forms);

            if (fractionalStr.length() > 5) {
                integerWords += " целых " + fractionalWords + " миллионных";
            } else if (fractionalStr.length() > 4) {
                integerWords += " целых " + fractionalWords + " стотысячных";
            } else if (fractionalStr.length() > 3) {
                integerWords += " целых " + fractionalWords + " десятитысячных";
            } else if (fractionalStr.length() > 2) {
                integerWords += " целых " + fractionalWords + " тысячных";
            } else if (fractionalStr.length() > 1) {
                integerWords += " целых " + fractionalWords + " сотых";
            } else if (fractionalStr.length() == 1) {
                integerWords += " целых " + fractionalWords + " десятых";
//           integerWords += " " + getFractionalForm("целых", forms) + " " + fractionalWords + " " + getFractionalForm("десятых", forms);
            }

            return integerWords;
        }
    }

    private static String getFractionalForm(String word, long[] forms) {  // пока совсем не работает...

//        ЧАСТЬ РЕЧИ - [17, 17, 18, 18, 18]
//        13:12:40.495 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: целых
//        13:12:40.496 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: целых с частью речи: 18
//        ЧАСТЬ РЕЧИ - [18, 18, 18]
//        13:12:40.497 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: десятых
//        13:12:40.497 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: десятых с частью речи: 18
//        ???

        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        System.out.println("ЧАСТЬ РЕЧИ - " + jMorfSdk.getTypeOfSpeeches(word));
        for (String s : jMorfSdk.getDerivativeFormLiterals(word, MorfologyParameters.TypeOfSpeech.ADJECTIVE_FULL)) {
            System.out.println(s);
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0])
                && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.PLURAL) == MorfologyParameters.Numbers.PLURAL)) {
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == 0)
                            || (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1])) {
                        System.out.println("прилагательное: " + form);
                        return form.getMyString();
                    }
                }
            }
        }
        return "";
    }

    private static String getWordForms(int value, String[] forms, long identifier) {
        String result;
        if (identifier == 0 || identifier == 512) {
            if (value % 100 >= 11 && value % 100 <= 19) {
                result = forms[2];
            } else if (value % 10 == 1) {
                result = forms[0];
            } else if (value % 10 >= 2 && value % 10 <= 4) {
                result = forms[1];
            } else {
                result = forms[2];
            }

//        13:08:29.213 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: тысяч
//        13:08:29.213 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: тысяч с частью речи: 28 - числительное
//        13:09:24.624 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: тысячи
//        13:09:24.624 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: тысячи с частью речи: 17 - существительное

        } else {
            result = forms[0];
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
            final String[] number = {""};
            for (String s : jMorfSdk.getDerivativeFormLiterals(result, MorfologyParameters.TypeOfSpeech.NOUN)) {
                System.out.println("форма: " + s);
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == identifier)) {
                        System.out.println("порядок: " + form);
                        number[0] = form.getMyString();
                    }
                }
            }
            return number[0];
        }
        return result;
    }

    private static String getFormedNumber(long[] forms, String num) {

        byte param;
        if (num.startsWith("тысяч") || num.startsWith("ноль")) {
            param = MorfologyParameters.TypeOfSpeech.NOUN;
        } else {
            param = MorfologyParameters.TypeOfSpeech.NUMERAL;
        }

        if (forms[0] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

//            18:03:36.505 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 17 - существительное
//            18:03:36.507 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 28 - числительное
//            19:03:14.942 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: восемь с частью речи: 29 - собирательное
//            интересная ситуация с "тысячей" и "миллионом": "тысяча" есть в библиотеке только как существительное, а "миллион" - только как числительное

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0])) {
                        if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == 0)
                                || (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1])
                                || (s.equals("ноль"))) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }
                    }
                }
            }
            return "";
        } else if (forms[1] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                System.out.println(s);
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
                        if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1]) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }

                    } else if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE) ||
                            (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2)) {
                        if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1]) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }
                    }
                }
            }
        }
        return num;
    }

    private static String getFormedNumberWithBuildup(String num, String buildup) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.NUMERAL)) {
            System.out.println(s);
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    System.out.println("число: " + form);
                    return form.getMyString();
                }
            }
        }

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.COLLECTIVE_NUMERAL)) {  // без результата
            System.out.println(s);
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    System.out.println("число: " + form);
                    return form.getMyString();
                }
            }
        }

        //  порядковые не нашлись в библиотеке, есть только собирательные, а в прилагательных тоже ничего, образованного от числительных
        //  однако собирательные от числительных тоже не находятся (через getDerivativeFormLiterals)
        return "";
    }
}