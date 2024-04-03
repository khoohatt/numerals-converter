import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class NumeralsConverter {

    private static final int MILLION = 1000000;
    private static final int THOUSAND = 1000;
    private static final int HUNDRED = 100;
    private static final int FIVE = 5;

    public NumeralsConverter() {
    }

    public String replaceNumber(List<String> wordsList) {
        long[] forms;  // падеж, род

        StringBuilder sb = new StringBuilder();

        String[] words = new String[wordsList.size()];
        wordsList.toArray(words);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            forms = new long[2];
            String word = words[i];

            if ((i > 0 && words[i - 1].equals("\n") || i == 0)
                    && words.length > i + 1 && word.matches("\\d+") && (words[i + 1].equals(".") || words[i + 1].equals(")"))) {  // нумерованный список
                result.append(word);
                sb.append(result);

            } else if (word.matches("(8|\\+7|7)[0-9]{7,10}")) {  // номера телефонов
                result.append(word);
                sb.append(result);

            } else if (words.length > i + 1 && words[i + 1].matches("(,\\d+)+")) {  // дробные числа через запятую: 34,9; 1341,0021 и валюта: 30,5 рублей, 43,23 доллара
                i++;
                word += words[i];
                while (words.length > i + 2 && words[i + 2].matches("(млн|тыс)")) {
                    if (words[i + 2].equals("млн")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * MILLION);
                    } else if (words[i + 2].equals("тыс")) {
                        word = String.valueOf(Double.parseDouble(word.replace(",", ".")) * THOUSAND);
                    }
                    i += 2;
                    if (words.length > i + 3 && words[i + 1].equals(".") && !words[i + 2].equals("\n")) {
                        i++;
                    }
                }

                forms = getCaseAndGender(words, i, (int) Double.parseDouble(word.replace(",", ".")));
                forms[1] = MorfologyParameters.Gender.FEMININ;

                if (word.contains(",") && words.length > i + 2 && words[i + 2].matches("(руб[а-я]*)|(дол[а-я]*)|(евр[а-я]*)")) {
                    result.append(convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms, words[i + 2]));
                    words[i + 1] = words[i + 2] = "";
                } else {
                    result.append(convertFractionalNumberToWords(Double.parseDouble(word.replace(",", ".")), forms));
                }

                changeFirstLetter(result.toString(), sb);
            } else if (words.length > i + 1 && words[i + 1].matches("/\\d+")) {  // дробные числа через слэш: 3/6, 929/2913
                i++;
                word += words[i];

                forms = getCaseAndGender(words, i, Integer.parseInt(word.split("/")[1]));
                forms[1] = MorfologyParameters.Gender.FEMININ;

                result.append(convertFractionalWithSlash(word, forms));

                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("\\d{1,9}")) {  // обычные числа: 6, 9992, 949913; числа с делением тысяч точками: 23.231.865, 5.246; числа с делением тысяч пробелами: 23 231 865, 5 246
                StringBuilder number = new StringBuilder(word);

                if (words.length > i + 1) {
                    while (words.length > i + 1 && words[i + 1].matches("(\\.\\d{3})+")) {
                        i++;
                        number.append(words[i]);
                    }
                    while (words.length > i + 2 && words[i + 1].matches("(\\s)") && words[i + 2].matches("\\d{3}")) {
                        i += 2;
                        number.append(words[i]);
                    }
                    while (words.length > i + 2 && words[i + 2].matches("(млн|тыс)")) {
                        if (words[i + 2].equals("млн")) {
                            number.append("000000");
                        } else if (words[i + 2].equals("тыс")) {
                            number.append("000");
                        }
                        i += 2;

                        if (words.length > i + 3 && words[i + 1].equals(".") && !words[i + 2].equals("\n")) {
                            i++;
                        }
                    }
                }

                if (words.length > i + 2 && (words[i + 2].matches("год|году|годам|годы|года|годов")
                        || words[i + 2].matches("(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)"))) {
                    forms = getForms(words, i, !words[i + 2].matches("годам|годах|годов|годы"));
                    String[] date = convertOrdinalToWords(word).split("(?<=( ))");
                    for (int j = 0; j < date.length - 1; j++) {
                        result.append(date[j]);
                    }
                    if (words[i + 2].equals("года")) {
                        forms[0] = MorfologyParameters.Case.GENITIVE;
                    }
                    result.append(convertDateToWords(date[date.length - 1], forms, !words[i + 2].matches("годам|годах|годов|годы")));
                } else {
                    if (words.length > i + 2) {
                        forms = getForms(words, i, Integer.parseInt(word) % 10 == 1);
                    } else if (i > 1) {
                        forms = getFormsWithPreposition(words, i);
                    }
                    result.append(convertNumberToWords(Integer.parseInt(number.toString().replaceAll("[ .]", "")), forms));
                }

                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("\\d+-((?:[тм]и)|(?:[её][мх]))")) {  // числа с наращениями: 5-ти, 4-ем
                word = word.replace("ем", "ём").replace("ех", "ёх");
                result.append(convertNumberToWords(Integer.parseInt(word.substring(0, word.indexOf("-"))), forms, word.substring(word.indexOf("-") + 1)));
                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("[вВ](о)*-(\\d)+")) {  // перечисления: во-1, в-23
                result.append(convertEnumToWords(word));
                changeFirstLetter(result.toString(), sb);

            } else if (word.matches("(\\d)+(-)*([ыо]?й|[ыо]?м|о?е|а?я|ы?х|у?ю|го)")) {  // числа с наращениями: 1980-м, 16-е, 4-ом, 21-х, 5-й, 5й
                result.append(convertOrdinalToWords(word));
                changeFirstLetter(result.toString(), sb);

            } else {
                sb.append(word);
            }
            result.setLength(0);
        }

        return sb.toString();
    }

    private void changeFirstLetter(String string, StringBuilder sb) {
        if ((sb.length() > 0 && string.length() > 0 && sb.charAt(sb.length() - 1) == '\n'
                || (sb.length() > 1 && sb.substring(sb.length() - 1).matches("[!.?]"))) || sb.length() == 0) {
            sb.append(string.substring(0, 1).toUpperCase()).append(string.substring(1));
        } else {
            sb.append(string);
        }
    }

    private long[] getCaseAndGender(String[] words, int i, int num) {
        if (words.length > i + 2) {
            return getForms(words, i, num % 10 == 1);
        }
        return new long[]{};
    }

    private long[] getForms(String[] words, int i, boolean single) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        long[] forms = new long[]{0, 0};
        ++i;

        if (i > 2) {
            forms = getFormsWithPreposition(words, i);
            System.out.println(forms[0] + " ааа " + forms[1]);
        }

        while (i < words.length && !words[i].matches("[.,;:!?]") && !words[i].matches("лет|метров|дней|месяцев|рублей|долларов|раз|километров|минут|часов")) {
            List<Long> list = new LinkedList<>();
            for (Form form : jMorfSdk.getOmoForms(words[i])) {
                if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                        && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.class) == MorfologyParameters.Numbers.PLURAL ^ single)) {
                    forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                    if (forms[0] == 0 && form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) != MorfologyParameters.Case.GENITIVE1
                            && !(form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE && words[i].matches("мая|марта|августа"))) {
                        list.add(form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER));
                        list.add(form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.IDENTIFIER));
                    }
                }
            }
            if (!list.isEmpty()) {
                if (list.contains(MorfologyParameters.Case.NOMINATIVE) || list.contains(MorfologyParameters.Case.GENITIVE1)) {
                    forms[0] = MorfologyParameters.Case.NOMINATIVE;
                } else {
                    forms[0] = list.get(0);
                }
                if (list.contains(MorfologyParameters.Gender.FEMININ) && forms[1] != MorfologyParameters.Gender.FEMININ) {
                    forms[1] = MorfologyParameters.Gender.FEMININ;
                }
                System.out.println(forms[0] + " ббб " + forms[1]);
                return forms;
            }
            if (forms[1] != 0 && forms[0] != 0) {
                System.out.println(forms[0] + " ввв " + forms[1]);
                return forms;
            }
            ++i;
        }
        return forms;
    }

    private long[] getFormsWithPreposition(String[] words, int i) {
        int j = 2;
        while (i - j >= 0 && (words[i - j + 2].matches("\\b\\S*\\d\\S*\\b") || words[i - j + 1].matches("\\b\\S*\\d\\S*\\b") || words[i - j].matches("\\b\\S*\\d\\S*\\b"))) {
            if (i + 1 < words.length && words[i + 1].matches("году|месяце") && words[i - j].matches("в|В")) {
                return new long[]{MorfologyParameters.Case.PREPOSITIONA, 0};
            } else if (words[i - j].matches("[п|П]о") || i + 1 < words.length && words[i + 1].matches("год|месяц") && words[i - j].matches("[з|З]а")) {
                return new long[]{MorfologyParameters.Case.NOMINATIVE, 0};
            } else if (words[i - j].matches("к|К|[к|К]о|[п|П]о|[б|Б]лагодаря|[в|В]опреки|[с|С]огласно")) {
                return new long[]{MorfologyParameters.Case.DATIVE, 0};
            } else if (words[i - j].matches("с|С|у|У|[о|О]т|[д|Д]о|[и|И]з|[б|Б]ез|[д|Д]ля|[в|В]округ|[о|О]коло|[в|В]озле|[к|К]роме|[б|Б]олее")) {
                return new long[]{MorfologyParameters.Case.GENITIVE, 0};
            } else if (words[i - j].matches("[п|П]од|[з|З]а|[п|П]ро|[ч|Ч]ерез|в|В|[н|Н]а|[в|В]о")) {
                return new long[]{MorfologyParameters.Case.ACCUSATIVE, 0};
            } else if (words[i - j].matches("с|С|[с|С]о|[з|З]а|[н|Н]ад|[п|П]од|[м|М]ежду|[п|П]еред")) {
                return new long[]{MorfologyParameters.Case.ABLTIVE, 0};
            } else if (words[i - j].matches("в|В|о|О|[о|О]б|[н|Н]а|[п|П]ри|[п|П]о")) {
                return new long[]{MorfologyParameters.Case.PREPOSITIONA, 0};
            }
            j += 1;
        }
        return new long[]{0, 0};
    }

    private String convertNumberToWords(int num, long[] forms, String... buildup) {
        String[] ones = {"ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"};
        String[] tens = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String[] hundreds = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};

        StringBuilder result = new StringBuilder();

        if (num == 0) {
            return result.append(getFormedNumber(forms, ones[0])).toString();
        }

        if (num / MILLION > 0) {
            result.append(convertNumberToWords(num / MILLION, forms)).append(" ").append(getFormedMillions(forms, (num / MILLION) % 10)).append(" ");
            num = num % MILLION;
        }

        if (num / THOUSAND > 0) {
            result.append(convertNumberToWords(num / THOUSAND, new long[]{forms[0] != 0 ? forms[0] : MorfologyParameters.Case.ACCUSATIVE, MorfologyParameters.Gender.FEMININ})).append(" ").append(getFormedThousand(forms, (num / THOUSAND) % 10)).append(" ");
            num = num % THOUSAND;
        }

        if (num / HUNDRED > 0) {
            result.append(getFormedNumber(forms, hundreds[num / HUNDRED])).append(" ");
            num = num % HUNDRED;
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

    private String convertDateToWords(String date, long[] forms, boolean isSingular) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(date, MorfologyParameters.TypeOfSpeech.ADJECTIVE_FULL)) {
            for (Form form : jMorfSdk.getOmoForms(s)) {
                if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0]) {
                    System.out.println(form.getMyString());
                    return form.getMyString();
                }
            }
        }
        return "";
    }

    private String convertFractionalWithSlash(String number, long[] forms) {
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

    private String convertEnumToWords(String enumer) {
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

        if (number < 20 || number % 10 == 0) {
            return result[0] + "-" + String.join(" ", words);
        } else {
            return result[0] + " " + String.join(" ", words);
        }
    }

    private String convertOrdinalToWords(String ordinal) {
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
            if ((number % HUNDRED < 20) && (number % HUNDRED > 9)) {
                forms = onesOrdinal[number % HUNDRED];
            } else {
                forms = onesOrdinal[number % 10];
            }
        } else {
            forms = tensOrdinal[(number % HUNDRED) / 10];
        }

        for (String form : forms) {
            if (form.endsWith(result[1])) {
                words[words.length - 1] = form;
                break;
            }
        }

        return String.join(" ", words);
    }

    private String convertFractionalNumberToWords(double number, long[] forms, String... currency) {  // добавить склонение (думаю)
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

    private String convertFractionalNumberToWordsWithoutCurrency(String fractionalStr, int integerPart, long[] forms) {
        String integerWords = integerPart != 0 ? convertNumberToWords(integerPart, forms) : "ноль";
        String fractionalWords = convertNumberToWords(!fractionalStr.equals("") ? Integer.parseInt(fractionalStr) : 0, forms);

        if (fractionalStr.length() > 5) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " миллионн" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
        } else if (fractionalStr.length() > 4) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " стотысячн" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
        } else if (fractionalStr.length() > 3) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " десятитысячн" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
        } else if (fractionalStr.length() > 2) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " тысячн" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
        } else if (fractionalStr.length() > 1) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " сот" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
        } else if (fractionalStr.length() == 1) {
            integerWords += " " + getFormedWholeParts(forms, integerPart % 10) + " " + fractionalWords + " десят" + getFormedParts(forms, Integer.parseInt(fractionalStr) % 10);
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
                fractionalWords = convertNumberToWords(fractionalPart, new long[]{forms[0], MorfologyParameters.Gender.FEMININ});
                break;

            case "дол":
                fractionalWords = convertNumberToWords(fractionalPart, new long[]{forms[0], 0});
                break;

            default:
                return convertFractionalNumberToWords(number, forms);
        }

        forms[1] = 0;
        String whole, fractional;

        boolean mainPart = (integerPart % HUNDRED < 21 && integerPart % HUNDRED > 10) || integerPart % 10 >= FIVE || integerPart % 10 == 0;
        boolean minorPart = (fractionalPart % HUNDRED < 21 && fractionalPart % HUNDRED > 10) || fractionalPart % 10 >= FIVE || fractionalPart % 10 == 0;
        switch (currency.substring(0, 3)) {

            case "руб":
                if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
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

                if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
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
                if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
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

                if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
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

    private String getFormedThousand(long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "тысяча";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "тысяче";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "тысячи";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "тысячу";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "тысячей";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "тысяче";
            }
        } else {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                if (num > 1 && num < FIVE) {
                    return "тысячи";
                } else {
                    return "тысяч";
                }
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "тысячам";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "тысяч";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "тысячи";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "тысячами";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "тысячах";
            }
        }
        return "тысячи";
    }

    private String getFormedMillions(long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "миллион";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "миллиону";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "миллиона";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "миллион";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "миллионом";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "миллионе";
            }
        } else {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                if (num > 1 && num < FIVE) {
                    return "миллиона";
                } else {
                    return "миллионов";
                }
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "миллионам";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                if (num > 1 && num < FIVE) {
                    return "миллиона";
                } else {
                    return "миллионов";
                }
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "миллионов";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "миллионами";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "миллионах";
            }
        }
        return "миллионов";
    }

    private String getFormedWholeParts (long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "целая";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "целой";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "целой";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "целую";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "целой";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "целой";
            }
        } else {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "целых";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "целым";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "целых";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "целых";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "целыми";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "целых";
            }
        }
        return "целых";
    }

    private String getFormedParts (long[] forms, int num) {
        if (num == 1) {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "ая";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "ой";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "ой";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "ую";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "ой";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "ой";
            }
        } else {
            if (forms[0] == MorfologyParameters.Case.NOMINATIVE || forms[0] == 0) {
                return "ых";
            } else if (forms[0] == MorfologyParameters.Case.DATIVE) {
                return "ым";
            } else if (forms[0] == MorfologyParameters.Case.GENITIVE) {
                return "ых";
            } else if (forms[0] == MorfologyParameters.Case.ACCUSATIVE) {
                return "ых";
            } else if (forms[0] == MorfologyParameters.Case.ABLTIVE) {
                return "ыми";
            } else if (forms[0] == MorfologyParameters.Case.VOATIVE || forms[0] == MorfologyParameters.Case.PREPOSITIONA) {
                return "ых";
            }
        }
        return "ых";
    }

    private String getFormedNumber(long[] forms, String num) {
        byte param;
        if (num.startsWith("ноль")) {
            param = MorfologyParameters.TypeOfSpeech.NOUN;
        } else {
            param = MorfologyParameters.TypeOfSpeech.NUMERAL;
        }

        if (forms[0] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

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

                    } else if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE ||
                            form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.ACCUSATIVE2) {
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

        return "";
    }
}