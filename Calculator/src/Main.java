public class Main {
    public static void main(String[] args){
        CalculatorImpl c = new CalculatorImpl();
        System.out.println(c.evaluate("26+(13-15.34*2)"));
        System.out.println(CalculatorImpl.subStrings.toString());
        System.out.println("Calculation successful");
    }
}
