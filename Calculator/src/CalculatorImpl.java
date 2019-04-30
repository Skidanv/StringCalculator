import java.util.ArrayList;
import java.util.HashMap;

/**
 * Строка делится на подстроки в соответствии с очерёдностью вычисления. Сначала отделяются члены выражения в скобках,
 * потом умножение/деление, потом сложение/вычитание. Деление происходит до тех пор, пока в каждой подстроке не
 * останется простое выражение с одним оператором и двумя числами. Отделенная часть заменяется "s" + номер подстроки
 * по порядку.
 *
 * Вычисление начинается с подстрок, не имеющих s-замен, т.е. состоящих только из чисел. Их результаты подставляются в
 * соответствующие выражения вместо s. Вычисление повторяется, пока не будут вычислен результат заданного выражения
 * (s-замен не останется)
 */


public class CalculatorImpl implements Calculator {
    private static int j = 0; //счётчик s-замен
    static HashMap<String, String> subStrings = new HashMap<>(); //выражения для вычисления, ключ - s и номер по счётчику

    @Override
    public String evaluate(String statement) {
        String s = splitParentheses(statement); //отделение первоочерёдно вычисляемых выражений в скобках
        if(s==null)
            return null;
        s = splitMultDiv(s); //отделение второй очереди, умножение и деление
        processMap(); //обрезка скобок в строках-выражениях и деление многооператорных выражений на однооператорные
        calculateMap(); //вычисление простых выражений

        for(String i : subStrings.keySet()){ //замена s в выражениях на числа-результаты
            if(s.contains(i))
                s = s.replace(i, subStrings.get(i));
        }

        s = calculateAddSub(s); //вычисление последнего выражения, содержащего только сложение и вычитание
        s = String.valueOf((Math.round(Double.parseDouble(s)*10000))/10000.0); //округление результата до четвертого знака
        return s;
    }

    private String splitParentheses(String statement) {
        int open;
        int close;

        for (int i = 0; i < statement.length(); i++) {
            if (statement.charAt(i) == '(') { //поиск первой скобки
                open = i;
                if (statement.indexOf(')', i) < statement.indexOf('(', i + 1)
                        || statement.indexOf('(', i + 1) == -1) { //поиск соответствующей закрывающей скобки
                    close = statement.indexOf(')', i) + 1;
                    subStrings.put("s" + j, statement.substring(open, close)); //добавление выражения и его ключа
                    j++; //инкремент счётчика ключей
                }
            }
        }


        for (String i : subStrings.keySet())
            statement = statement.replace(subStrings.get(i), i); //замена отделённого выражения на его ключ

        if (statement.contains("(") && statement.contains(")"))//проверка наличия парных скобок в оставшемся выражении
        statement = splitParentheses(statement);
        else if ((statement.contains("(") && !statement.contains(")")) ||
                (!statement.contains("(") && statement.contains(")"))) //проверка на наличие непарных скобок
            return null;

        return statement;
    }

    private String splitMultDiv(String statement) {
        int open; //координаты начала и конца строки с умножением/делением
        int close = statement.length();

        for (int i = 0; i < statement.length(); i++) { //если строка между плюсами/минусами содержит умножение/деление, она добавляется в коллекцию
            if (statement.charAt(i) == '+' || statement.charAt(i) == '-') {
                open = i+1;

                String statement1 = statement.substring(open);
                if((statement1.contains("+") && ((statement1.indexOf("+")<statement1.indexOf("-"))) || //проверка наличия плюса и его первой очереди перед минусом либо отсутствия минуча
                        (statement1.contains("+") && !statement1.contains("-"))))
                    close = statement1.indexOf('+', open);
                else if((statement1.contains("-") && ((statement1.indexOf("-")<statement1.indexOf("+"))) ||
                        (statement1.contains("-") && !statement1.contains("+"))))
                    close = statement1.indexOf('-', open);

                if(statement.substring(open, close).contains("/")||
                        statement.substring(open, close).contains("*")) {
                    subStrings.put("s" + j, statement.substring(open, close));
                    j++;
                }
            }
        }

        for (String i : subStrings.keySet()) { //замена выражений с умножением/делением на ключи из коллекции в исходной строке
            if (!subStrings.get(i).equals(statement))
                statement = statement.replace(subStrings.get(i), i);
        }

        return statement;
    }

    private void processMap() {
        ArrayList<String> keys = new ArrayList<>(); //список для хранения ключей скобочных выражений, которые содержат одновременно операторы разной очерёдности

        for (String i : subStrings.keySet()) {
            subStrings.put(i, subStrings.get(i).replace("(", "")); //обрезка скобок в выражениях в коллекции
            subStrings.put(i, subStrings.get(i).replace(")", ""));

            if((subStrings.get(i).contains("*")||subStrings.get(i).contains("/"))&&
                    (subStrings.get(i).contains("+")||subStrings.get(i).contains("-"))) //поиск умножения/деления в скобочных выражениях коллекции
                keys.add(i);
        }

        for (String i : keys) { //дополнительная сепарация умножения/деления в выражениях коллекции
            subStrings.put(i, splitMultDiv(subStrings.get(i)));
        }
    }

    private String calculateAddSub(String s){

        if(s.contains("--")) //замены операторов для корректного вычисления выражений с отрицательными числами
            s = s.replace("--", "+");
        if(s.contains("+-"))
            s = s.replace("+-", "-");

        String[] members = s.split("[+-]"); //разделение выражения на подстроки между операторами
        Double[] nums = new Double[members.length]; //массив для хранения чисел выражения

        for(int i = 0; i<nums.length; i++) //парсинг строк для получения чисел выражения
            nums[i] = Double.parseDouble(members[i]);

        if((s.contains("+") && ((s.indexOf("+")<s.indexOf("-"))) || //проверка наличия плюса и его первой очереди перед минусом либо отсутствия минуча
                (s.contains("+") && !s.contains("-"))))
            s = s.replace(members[0]+ "+" + members[1], String.valueOf(nums[0]+nums[1])); //выполнение сложения
        else
            s = s.replace(members[0]+ "-" + members[1], String.valueOf(nums[0]-nums[1])); //выполнение вычитания

        if((s.contains("-") || s.contains("+")) && s.indexOf("-")!=0) //проверка наличия невычисленных выражений
            s = calculateAddSub(s);


        return s;
    }

    private String calculateMultDiv(String s){ //метод действует аналогично методу вычисления сложения/вычитания
        String[] members = s.split("[*/]");
        Double[] nums = new Double[members.length];

        for(int i = 0; i<nums.length; i++)
            nums[i] = Double.parseDouble(members[i]);

        if((s.indexOf("/")<s.indexOf("*") && s.contains("/")) ||
                s.contains("/") && !s.contains("*"))
            s = s.replace(members[0]+ "/" + members[1], String.valueOf(nums[0]/nums[1]));
        else
            s = s.replace(members[0]+ "*" + members[1], String.valueOf(nums[0]*nums[1]));

        if(s.contains("/") || s.contains("*"))
            s = calculateMultDiv(s);


        return s;
    }

    private void calculateMap(){
        ArrayList<String> keys = new ArrayList<>(); //список ключей вычисленных выражений

        for (String i : subStrings.keySet()) {
            if(!subStrings.get(i).contains("s")) { //проверка отсутствия в вычисляемом выражении s-ключей для замены
                if (subStrings.get(i).contains("/") || subStrings.get(i).contains("*")) //проверка на наличие умножения/деления
                    subStrings.put(i, calculateMultDiv(subStrings.get(i))); //замена выражения в коллекции на вычисленный результат
                else if (subStrings.get(i).contains("+") || subStrings.get(i).contains("-")) //проверка на наличие сложения/вычитания
                    subStrings.put(i, calculateAddSub(subStrings.get(i)));
                keys.add(i); //добавление ключа после вычисления
            }
        }

        for (String i : keys) { // цикл для поиска s-ключей в выражениях и замены их на уже вычисленные результаты
            for(String n : subStrings.keySet()){
                if(subStrings.get(n).contains(i)){
                    subStrings.put(n, subStrings.get(n).replace(i, subStrings.get(i)));
                }
            }
        }

        String values = subStrings.values().toString(); //проверка наличия невычисленных выражений
        if((values.contains("+")||values.contains("-")||values.contains("*")||values.contains("/"))
        && values.charAt(values.indexOf("-")-1)!='[' && values.charAt(values.indexOf("-")-1)!=' ')
            calculateMap();
    }
}
