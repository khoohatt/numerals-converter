import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class NumeralsConverter {

    public NumeralsConverter() {
    }

    public String replaceNumber(List<String> wordsList) {
        long[] forms;  // падеж, род

        StringBuilder sb = new StringBuilder();

        String[] words = new String[wordsList.size()];
        wordsList.toArray(words);
        StringBuilder result;

        for (int i = 0; i < words.length; i++) {
            forms = new long[2];
            String word = words[i];

            if ((((i > 0) && (words[i - 1].equals("\n"))) || (i == 0))
                    && ((words.length > i + 1) && (word.matches("\\d+") && (words[i + 1].equals(".") || words[i + 1].equals(")"))))) {  // нумерованный список
                result = new StringBuilder(word);
                sb.append(result);

            } else if ((word.matches("(8|\\+7|7)[0-9]{7,10}"))) {  // номера телефонов
                result = new StringBuilder(word);
                sb.append(result);

            } else if ((words.length > i + 1) && (words[i + 1].matches("(,\\d+)+"))) {  // дробные числа через запятую: 34,9; 1341,0021 и валюта: 30,5 рублей, 43,23 доллара
                i++;
                word += words[i];
                while ((words.length > i + 2) && (words[i + 2].matches("(млн|тыс)"))) {
                    if (words[i + 2].equals("млн")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * 1000000);
                    } else if (words[i + 2].equals("тыс")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * 1000);
                    }
                    i += 2;
                    if (words.length > i + 3 && (words[i + 1].equals(".") && !words[i + 2].equals("\n"))) {
                        i++;
                    }
                }
                if (words.length > i + 2) {
                    forms = getForms(words, i, (int) Double.parseDouble(word.replace(",", ".")) % 10 == 1);
                }
                forms[1] = 8;
                if (word.contains(",") && (words.length > i + 2) && (words[i + 2].matches("(руб[а-я]*)|(дол[а-я]*)|(евр[а-я]*)"))) {
                    result = new StringBuilder(convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms, words[i + 2]));
                    words[i + 1] = words[i + 2] = "";
                } else {
                    result = new StringBuilder(convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms));
                }

                changeFirstLetter(result.toString(), sb);

            } else if ((words.length > i + 1) && (words[i + 1].matches("/\\d+"))) {  // дробные числа через слэш: 3/6, 929/2913
                i++;
                word += words[i];

                if (words.length > i + 2) {
                    forms = getForms(words, i, Integer.parseInt(word.split("/")[1]) % 10 == 1);
                }

                forms[1] = 8L;
                result = new StringBuilder(convertFractionalWithSlash(word, forms));

                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("\\d{1,9}")) {  // обычные числа: 6, 9992, 949913; числа с делением тысяч точками: 23.231.865, 5.246; числа с делением тысяч пробелами: 23 231 865, 5 246
                StringBuilder number = new StringBuilder(word);

                if (words.length > i + 1) {
                    while ((words.length > i + 1) && (words[i + 1].matches("(\\.\\d{3})+"))) {
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
                }

                if ((words.length > i + 2) && (words[i + 2].matches("год|году|годах|годам|годы|года|годов")
                        || words[i + 2].matches("(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)"))) {
                    forms = getForms(words, i, !words[i + 2].matches("годам|годах|годов|годы"));
                    String[] date = convertOrdinalToWords(word).split("(?<=( ))");
                    result = new StringBuilder();
                    for (int j = 0; j < date.length - 1; j++) {
                        result.append(date[j]);
                    }
                    if (words[i + 2].equals("года")) {
                        forms[0] = 128L;
                    }
                    result.append(convertDateToWords(date[date.length - 1], forms, !words[i + 2].matches("годам|годах|годов|годы")));
                } else {
                    if (words.length > i + 2) {
                        forms = getForms(words, i, Integer.parseInt(word) % 10 == 1);
                    } else if (i > 1) {
                        forms = getFormsWithPreposition(words, i + 1);
                    }
                    result = new StringBuilder(convertNumberToWords(Integer.parseInt(number.toString().replaceAll("[ .]", "")), forms));
                }

                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("\\d+-((?:[тм]и)|(?:[её]м))")) {  // числа с наращениями: 5-ти, 4-ем
                word = word.replace("ем", "ём");
                result = new StringBuilder(convertNumberToWords(Integer.parseInt(word.substring(0, word.indexOf("-"))), forms, word.substring(word.indexOf("-") + 1)));
                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("[вВ](о)*-(\\d)+")) {  // перечисления: во-1, в-23
                result = new StringBuilder(convertEnumToWords(word));
                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("(\\d)+(-)*([ыо]?й|[ыо]?м|о?е|а?я|ы?х|у?ю|го)")) {  // числа с наращениями: 1980-м, 16-е, 4-ом, 21-х, 5-й, 5й
                result = new StringBuilder(convertOrdinalToWords(word));
                changeFirstLetter(result.toString(), sb);

            } else {
                sb.append(word);
            }
        }

        return sb.toString();
    }

    private void changeFirstLetter(String string, StringBuilder sb) {
        if ((sb.length() > 0 && string.length() > 0 && (sb.charAt(sb.length() - 1) == '\n')
                || ((sb.length() > 1) && (sb.substring(sb.length() - 1).matches("[!.?]")))) || (sb.length() == 0)) {
            sb.append(string.substring(0, 1).toUpperCase()).append(string.substring(1));
        } else {
            sb.append(string);
        }
    }

    public long[] getForms(String[] words, int i, boolean single) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        long[] forms = new long[]{0, 0};
        ++i;

        if (i > 2) {
            forms = getFormsWithPreposition(words, i);
        }


        while (i < words.length && !words[i].matches("[.,;:!?]") && !words[i].matches("лет|метров|дней|месяцев|рублей|долларов|раз|километров|минут|часов")) {
            List<Long> list = new LinkedList<>();
            for (Form form : jMorfSdk.getOmoForms(words[i])) {
                if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                        && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.class) == MorfologyParameters.Numbers.PLURAL ^ single)
                ) {
//                    System.out.println("существительное: " + form + " " + form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER));
                    forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                    if (forms[0] == 0 && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) != MorfologyParameters.Case.GENITIVE1)
                            && !((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) && words[i].matches("мая|марта"))) {
                        list.add(form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER));
                    }
                }
            }
            if (!list.isEmpty()) {
                if (list.contains(64L) || list.contains(640L)) {
                    forms[0] = 64L;
                } else {
                    forms[0] = list.get(0);
                }
                return forms;
            }
            if (forms[1] != 0) {
                return forms;
            }
            ++i;
        }
        return forms;
    }

    public long[] getFormsWithPreposition(String[] words, int i) {
        int j = 2;
        while ((i - j >= 0) && (words[i - j + 2].matches("\\b\\S*\\d\\S*\\b") || words[i - j + 1].matches("\\b\\S*\\d\\S*\\b") || words[i - j].matches("\\b\\S*\\d\\S*\\b"))) {
            if (words[i + 1].matches("году|месяце") && words[i - j].matches("в")) {
//                System.out.println("предложный");
                return new long[]{448L, 0};
            } else if (words[i - j].matches("по")) {
//                System.out.println("именительный для числительного?");
                return new long[]{64L, 0};
            } else if (words[i - j].matches("к|ко|по|благодаря|вопреки|согласно")) {
//                System.out.println("дательный");
                return new long[]{192L, 0};
            } else if (words[i - j].matches("с|у|от|до|из|без|для|вокруг|около|возле|кроме|более")) {
//                System.out.println("родительный");
                return new long[]{128L, 0};
            } else if (words[i - j].matches("под|за|про|через|в|на|во")) {
//                System.out.println("винительный");
                return new long[]{512L, 0};
            } else if (words[i - j].matches("с|со|за|над|под|между|перед")) {
//                System.out.println("творительный");
                return new long[]{320L, 0};
            } else if (words[i - j].matches("в|о|об|на|при|по")) {
//                System.out.println("предложный");
                return new long[]{576L, 0};
            }
            j += 1;
        }
        return new long[]{0, 0};
    }

    public String convertNumberToWords(int num, long[] forms, String... buildup) {
        String[] ones = {"ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"};
        String[] tens = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String[] hundreds = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};

        StringBuilder result = new StringBuilder();

        if (num == 0) {
            return result.append(getFormedNumber(forms, ones[0])).toString();
        }

        if (num / 1000000 > 0) {
            result.append(convertNumberToWords(num / 1000000, forms)).append(" ").append(getFormedMillions(forms, (num / 1000000) % 10)).append(" ");
            num = num % 1000000;
        }

        if (num / 1000 > 0) {
            result.append(convertNumberToWords(num / 1000, new long[]{forms[0] != 0 ? forms[0] : 512, 8})).append(" ").append(getFormedThousand(forms, (num / 1000) % 10)).append(" ");
            num = num % 1000;
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

    public String convertDateToWords(String date, long[] forms, boolean isSingular) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(date, MorfologyParameters.TypeOfSpeech.ADJECTIVE_FULL)) {
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0]) {
                    return form.getMyString();
                }
            }
        }
        return "";
    }

    public String convertFractionalWithSlash(String number, long[] forms) {
        String[] onesOrdinal = {"нулевых", "первых", "вторых", "третьих", "четвертых", "пятых", "шестых", "седьмых", "восьмых", "девятых", "десятых", "одиннадцатых", "двенадцатых", "тринадцатых", "четырнадцатых", "пятнадцатых", "шестнадцатых", "семнадцатых", "восемнадцатых", "девятнадцатых"};
        String[] tensOrdinal = {"", "", "двадцатых", "тридцатых", "сороковых", "пятидесятых", "шестидесятых", "семидесятых", "восьмидесятых", "девяностых"};

        String[] result = number.split("/");
        result[0] = convertNumberToWords(Integer.parseInt(result[0]), forms);
        String word = convertNumberToWords(Integer.parseInt(result[1]), new long[]{0, 0});

        String[] words = word.split(" ");
        int secondNumber = Integer.parseInt(result[1]);

        if (secondNumber < 20) {
            words[words.length - 1] = onesOrdinal[secondNumber];
        } else if (secondNumber % 10 != 0) {
            words[words.length - 1] = onesOrdinal[secondNumber % 10];
        } else {
            words[words.length - 1] = tensOrdinal[secondNumber / 10];
        }

        return result[0] + " " + String.join(" ", words);
    }

    public String convertEnumToWords(String enumer) {
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

    public String convertOrdinalToWords(String ordinal) {
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
                {"двадцатый", "двадцатого", "двадцатому", "двадцатом", "двадцатым", "двадцатая", "двадцатой", "двадцатую", "двадцатое", "двадцатые", "двадцатых", "двадцатым", "двадцатыми"},
                {"тридцатый", "тридцатого", "тридцатому", "тридцатом", "тридцатым", "тридцатая", "тридцатой", "тридцатую", "тридцатое", "тридцатые", "тридцатых", "тридцатым", "тридцатыми"},
                {"сороковой", "сорокового", "сороковому", "сороковом", "сороковым", "сороковая", "сороковой", "сороковую", "сороковое", "сороковые", "сороковых", "сороковым", "сороковыми"},
                {"пятидесятый", "пятидесятого", "пятидесятому", "пятидесятом", "пятидесятым", "пятидесятая", "пятидесятой", "пятидесятую", "пятидесятое", "пятидесятые", "пятидесятых", "пятидесятым", "пятидесятыми"},
                {"шестидесятый", "шестидесятого", "шестидесятому", "шестидесятом", "шестидесятым", "шестидесятая", "шестидесятой", "шестидесятую", "шестидесятое", "шестидесятые", "шестидесятых", "шестидесятым", "шестидесятыми"},
                {"семидесятый", "семидесятого", "семидесятому", "семидесятом", "семидесятым", "семидесятая", "семидесятой", "семидесятую", "семидесятое", "семидесятые", "семидесятых", "семидесятым", "семидесятыми"},
                {"восьмидесятый", "восьмидесятого", "восьмидесятому", "восьмидесятом", "восьмидесятым", "восьмидесятая", "восьмидесятой", "восьмидесятую", "восьмидесятое", "восьмидесятые", "восьмидесятых", "восьмидесятым", "восьмидесятыми"},
                {"девяностый", "девяностого", "девяностому", "девяностом", "девяностым", "девяностая", "девяностой", "девяностую", "девяностое", "девяностые", "девяностых", "девяностым", "девяностыми"},
                {"сотый", "сотого", "сотому", "сотом", "сотым", "сотая", "сотой", "сотую", "сотое", "сотые", "сотых", "сотым", "сотыми"}};

        String[] result = new String[2];
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

    public String convertFractionalNumberToWords(double number, long[] forms, String... currency) {  // добавить склонение (думаю)
        number = ((number * 1e6)) / 1e6;
        int integerPart = (int) number;

        DecimalFormat df = new DecimalFormat("0.000000000");
        String numberStr = df.format(number);
        String fractionalStr = numberStr.substring(numberStr.indexOf(',') + 1).replaceAll("0*$", "");

        if (currency.length != 0) {
            return convertFractionalCurrencyToWords(fractionalStr, integerPart, forms, currency[0], number);
        } else {
            return convertFractionalNumberToWordsWithoutCurrency(fractionalStr, integerPart, forms);
        }
    }

    public String convertFractionalNumberToWordsWithoutCurrency(String fractionalStr, int integerPart, long[] forms) {
        String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, forms) : "ноль";
        String fractionalWords = convertNumberToWords(!fractionalStr.equals("") ? Integer.parseInt(fractionalStr) : 0, forms);

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

    private String convertFractionalCurrencyToWords(String fractionalStr, int integerPart, long[] forms, String currency, double number) {
        int fractionalPart = !fractionalStr.equals("") ? Integer.parseInt(fractionalStr) : 0;

        if (fractionalPart < 10) {
            fractionalPart *= 10;
        } else if (fractionalPart > 99) {
            fractionalPart = fractionalPart / (int) (Math.pow(10, (int) (Math.log10(fractionalPart) + 1) - 2));
        }

        String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, new long[]{forms[0], 0}) : "ноль";
        String fractionalWords;

        switch (currency.substring(0, 3)) {
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

        boolean mainPart = (integerPart % 100 < 21 && integerPart % 100 > 10) || integerPart % 10 >= 5 || integerPart % 10 == 0;
        boolean minorPart = (fractionalPart % 100 < 21 && fractionalPart % 100 > 10) || fractionalPart % 10 >= 5 || fractionalPart % 10 == 0;
        switch (currency.substring(0, 3)) {

            case "руб":
                if (forms[0] == 64 || forms[0] == 0) {
                    if (mainPart) {
                        whole = "рублей";
                    } else if (integerPart % 10 == 1) {
                        whole = "рубль";
                    } else {
                        whole = "рубля";
                    }
                } else {
                    whole = getFormedNoun(forms[0], "рубль");
                }

                if (forms[0] == 64 || forms[0] == 0) {
                    if (minorPart) {
                        fractional = "копеек";
                    } else if (fractionalPart % 10 == 1) {
                        fractional = "копейка";
                    } else {
                        fractional = "копейки";
                    }
                } else {
                    fractional = getFormedNoun(forms[0], "копейка");
                }
                break;

            case "дол":
                if (forms[0] == 64 || forms[0] == 0) {
                    if (mainPart) {
                        whole = "долларов";
                    } else if (integerPart % 10 == 1) {
                        whole = "доллар";
                    } else {
                        whole = "доллара";
                    }
                } else {
                    whole = getFormedNoun(forms[0], "доллар");
                }

                if (forms[0] == 64 || forms[0] == 0) {
                    if (minorPart) {
                        fractional = "центов";
                    } else if (fractionalPart % 10 == 1) {
                        fractional = "цент";
                    } else {
                        fractional = "цента";
                    }
                } else {
                    fractional = getFormedNoun(forms[0], "цент");
                }
                break;

            default:
                return convertFractionalNumberToWords(number, forms);
        }
        integerWords += " " + whole + " " + fractionalWords + " " + fractional;

        return integerWords;
    }

    private String getFormedNoun(long forms, String noun) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(noun, MorfologyParameters.TypeOfSpeech.NOUN)) {
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms) {
                    if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.IDENTIFIER) == MorfologyParameters.Numbers.PLURAL) {
                        return form.getMyString();
                    }
                }
            }
        }
        return "";
    }

    private String getFractionalForm(String word, long[] forms) {  // пока совсем не работает...

//        ЧАСТЬ РЕЧИ - [17, 17, 18, 18, 18]
//        13:12:40.495 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: целых
//        13:12:40.496 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: целых с частью речи: 18
//        11:16:32.507 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: целые
//        11:16:32.509 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: целые с частью речи: 17
//        ЧАСТЬ РЕЧИ - [18, 18, 18]
//        13:12:40.497 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре начальные формы для литерала: десятых
//        13:12:40.497 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: десятых с частью речи: 18

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

    private String getFormedThousand(long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == 64L || forms[0] == 0) {
//                System.out.println("именительный");
                return "тысяча";
            } else if (forms[0] == 192L) {
//                System.out.println("дательный");
                return "тысяче";
            } else if (forms[0] == 128L) {
//                System.out.println("родительный");
                return "тысячи";
            } else if (forms[0] == 512L) {
//                System.out.println("винительный");
                return "тысячу";
            } else if (forms[0] == 320L) {
//                System.out.println("творительный");
                return "тысячей";
            } else if (forms[0] == 576L || forms[0] == 448L) {
//                System.out.println("предложный");
                return "тысяче";
            }
        } else {
            if (forms[0] == 64L || forms[0] == 0) {
                if (num > 1 && num < 5) {
                    return "тысячи";
                } else {
                    return "тысяч";
                }
            } else if (forms[0] == 192L) {
                return "тысячам";
            } else if (forms[0] == 128L) {
                if (num > 1 && num < 5) {
                    return "тысячи";
                } else {
                    return "тысяч";
                }
            } else if (forms[0] == 512L) {
                return "тысячи";
            } else if (forms[0] == 320L) {
                return "тысячами";
            } else if (forms[0] == 576L || forms[0] == 448L) {
                return "тысячах";
            }
        }
        return "тысячи";
    }

    private String getFormedMillions(long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == 64L || forms[0] == 0) {
                return "миллион";
            } else if (forms[0] == 192L) {
                return "миллиону";
            } else if (forms[0] == 128L) {
                return "миллиона";
            } else if (forms[0] == 512L) {
                return "миллион";
            } else if (forms[0] == 320L) {
                return "миллионом";
            } else if (forms[0] == 576L || forms[0] == 448L) {
                return "миллионе";
            }
        } else {
            if (forms[0] == 64L || forms[0] == 0) {
                if (num > 1 && num < 5) {
                    return "миллиона";
                } else {
                    return "миллионов";
                }
            } else if (forms[0] == 192L) {
                return "миллионам";
            } else if (forms[0] == 512L) {
                if (num > 1 && num < 5) {
                    return "миллиона";
                } else {
                    return "миллионов";
                }
            } else if (forms[0] == 128L) {
                return "миллионов";
            } else if (forms[0] == 320L) {
                return "миллионами";
            } else if (forms[0] == 576L || forms[0] == 448L) {
                return "миллионах";
            }
        }
        return "тысячи";
    }

    private String getFormedNumber(long[] forms, String num) {  // todo рефакторинг :(
        byte param;
        if (num.startsWith("ноль")) {
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
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0] && form.getMorphCharacteristics() != 514)) {
                        if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == 0)
                                || (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1])
                                || (s.equals("ноль"))) {
                            return form.getMyString();
                        }
                    }
                }
            }
            return "";
        } else if (forms[1] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
                        if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1]) {
                            return form.getMyString();
                        }

                    } else if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE) ||
                            (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2)) {
                        if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1]) {
                            return form.getMyString();
                        }
                    }
                }
            }
        }
        return num;
    }

    private String getFormedNumberWithBuildup(String num, String buildup) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.NUMERAL)) {
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    return form.getMyString();
                }
            }
        }

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.COLLECTIVE_NUMERAL)) {  // без результата
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    return form.getMyString();
                }
            }
        }

        //  порядковые не нашлись в библиотеке, есть только собирательные, а в прилагательных тоже ничего, образованного от числительных
        //  однако собирательные от числительных тоже не находятся (через getDerivativeFormLiterals)
        return "";
    }
}
