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
1. Ледокол обошёл Антарктиду за 62 суток.
как-то 5-ти раз-таки
2) примерно 34,8 млн.
3/4, 23 455 235 кошкам, 3.095, 2 тыс. кораблей
1. Человек делает за день около 20 тысяч шагов, за год – до 7 миллионов, а за 70 лет – почти 500 миллионов
шагов. Это значит, что за всю свою жизнь человек мог бы 9 раз обойти земной шар по экватору или
преодолеть расстояние от Земли до Луны.
2. В Мировом океане обитает 18 тысяч видов рыб. Наибольшая глубина Балтийского моря – 459 метров,
Азовского – 14 метров. Самой длинной рекой в мире считается Нил, его длина – 6671 километр.
Наибольшая глубина Байкала – 1637 метров.
*/

    public static void main(String[] args) {

        String input = " ";


//        while (!scanner.nextLine().equals("")) {
//            string += scanner.nextLine();
//
//        }
//        scanner.forEachRemaining(string:: string += s);
        StringBuilder sb = new StringBuilder();

        while (!input.equals("")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n\nтекст: ");
            while (!input.equals("")) {
                input = scanner.nextLine();
                sb.append(input);
                sb.append("\n");
            }
            input = sb.toString().trim();

            if (!input.equals("")) {

//            string = scanner.nextLine();
                System.out.print("результат: " + replaceNumber(input));
                sb.setLength(0);
                input = " ";
            }
        }
    }

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
                    forms = getForms(words[i + 2], (int) Double.parseDouble(word.replace(",", ".")) % 10 == 1);
                }
                forms[1] = 8;
                result[i] = convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms);

            } else if ((words.length > i + 1) && (words[i + 1].matches("/\\d+"))) {  // дробные числа через слэш: 3/6, 929/2913
                i++;
                word += words[i];
                double number = Double.parseDouble(word.split("/")[0]) / Double.parseDouble(word.split("/")[1]);
                if (words.length > i + 2) {
                    forms = getForms(words[i + 2], (int) number % 10 == 1);
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
                        forms = getForms(words[i + 2], Integer.parseInt(word) % 10 == 1);
                    }
                }
                result[i] = convertNumberToWords(Integer.parseInt(number.toString().replaceAll("[ .]", "")), forms);

            } else if (word.matches("\\d+-(?:[йяе]|(?:[тм]и)|(?:[ыое]?[м]))")) {  // числа с наращениями: 1980-м, 5-ти, 16-е
                result[i] = convertNumberToWords(Integer.parseInt(word.substring(0, word.indexOf("-"))), forms, word.substring(word.indexOf("-") + 1));

            } else if (word.matches("в(о)*-(\\d)+")) {
                // нужны порядковые
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

    public static long[] getForms(String word, boolean single) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        long[] forms = new long[2];

        for (Form form : jMorfSdk.getOmoForms(word)) {
            System.out.println("слово: " + form);

            if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                    && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.class) == MorfologyParameters.Numbers.PLURAL ^ single)) {
                System.out.println("существительное: " + form);
                forms[0] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER);
                forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
                    break;
                } else if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2) {
                    break;
                }
            }
        }
        //        jMorfSdk.finish();
        return forms;
    }

    public static String convertNumberToWords(int num, long[] forms, String... buildup) {
        String[] ones = {"ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"};
        String[] tens = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String[] hundreds = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};
        String[] thousands = {"тысяча", "тысячи", "тысяч"};
        String[] millions = {"миллион", "миллиона", "миллионов"};

        String[] onesOrdinal = {"нулевой", "первый", "второй", "третий", "четвертый", "пятый", "шестой", "седьмой", "восьмой", "девятый", "десятый", "одиннадцатый", "двенадцатый", "тринадцатый", "четырнадцатый", "пятнадцатый", "шестнадцатый", "семнадцатый", "восемнадцатый", "девятнадцатый"};
        String[] tensOrdinal = {"", "", "двадцатый", "тридцатый", "сороковой", "пятидесятый", "шестидесятый", "семидесятый", "восьмидесятый", "девяностый"};
        String[] hundredsOrdinal = {"", "сотый", "двухсотый"};

        StringBuilder result = new StringBuilder();

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

    public static String convertFractionalNumberToWords(double number, long[] forms) {
        number = ((number * 1e6)) / 1e6;
        int integerPart = (int) number;

        DecimalFormat df = new DecimalFormat("0.000000000");
        String numberStr = df.format(number);
        String fractionalStr = numberStr.substring(numberStr.indexOf(',') + 1).replaceAll("0*$", "");
        int fractionalPart = !fractionalStr.equals("") ? Integer.parseInt(fractionalStr) : 0;

        String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, forms) : "ноль";
        String fractionalWords = convertNumberToWords(fractionalPart, forms);

        if (fractionalStr.length() > 5) {
            return integerWords + " целых " + fractionalWords + " миллионных";
        } else if (fractionalStr.length() > 4) {
            return integerWords + " целых " + fractionalWords + " стотысячных";
        } else if (fractionalStr.length() > 3) {
            return integerWords + " целых " + fractionalWords + " десятитысячных";
        } else if (fractionalStr.length() > 2) {
            return integerWords + " целых " + fractionalWords + " тысячных";
        } else if (fractionalStr.length() > 1) {
            return integerWords + " целых " + fractionalWords + " сотых";
        } else if (fractionalStr.length() == 1) {
            return integerWords + " целых " + fractionalWords + " десятых";
        } else {
            return integerWords;
        }
    }

    private static String getWordForms(int value, String[] forms, long identifier) {
        String result = "";
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

        if (identifier != 0) {
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
//            if (number[0].length() > 0) {
//                System.out.println("форма: " + number[0]);
                return number[0];
//            }
        }
        return result;
    }

    private static String getFormedNumber(long[] forms, String num) {
        if (forms[0] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

//            18:03:36.505 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 17 - существительное
//            18:03:36.507 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 28 - числительное
//            19:03:14.942 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: восемь с частью речи: 29 - собирательное
            byte param;
            if (num.equals("один")) {
                param = MorfologyParameters.TypeOfSpeech.NOUN;
            } else {
                param = MorfologyParameters.TypeOfSpeech.NUMERAL;
            }

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                System.out.println(s);
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0])) {
                        if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == 0)
                                || (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1])) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }
                    }
                }
            }
            return "";
        } else if (forms[1] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

            byte param;
            if (num.equals("один")) {
                param = MorfologyParameters.TypeOfSpeech.NOUN;
            } else {
                param = MorfologyParameters.TypeOfSpeech.NUMERAL;
            }

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                System.out.println(s);
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
                        if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1]) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }
                    } else if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2) {
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
                System.out.println("наращение в методе: " + form.getMyString().substring(buildup.length()));
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    System.out.println("число: " + form);
                    return form.getMyString();
                }
            }
        }

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.COLLECTIVE_NUMERAL)) {  // без результата
            System.out.println(s);
            for (Form form : jMorfSdk.getOmoForms(s)) {
                System.out.println("наращение в методе: " + form.getMyString().substring(buildup.length()));
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    System.out.println("число: " + form);
                    return form.getMyString();
                }
            }
        }

        //  порядковые не нашлись в библиотеке, есть только собирательные, а в прилагательных тоже ничего, образованного от числительных.
        //  однако собирательные от числительных тоже не находятся (через getDerivativeFormLiterals)
        return "";
    }
}