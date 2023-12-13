package tests;


public class Rekurze {

    public static void main(String[] args) {
        napis0(0);
    }

    public static void napis0(int i){
        System.out.println(""+i);
        if (i<20){
            napis0(i+1);
        }
        System.out.println(""+i);
    }
}
