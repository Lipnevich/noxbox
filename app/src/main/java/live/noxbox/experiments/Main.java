package live.noxbox.experiments;

public class Main {
    public static void main(String[] args) {
//        Account<Object> objectAccount = new Account<Object>(new Object());
//        Account<? super Number> numbAccount = new Account<Number>(2);
//        Account<? extends Number> numbAccount1 = new Account<Number>(2);
//        Account<Integer> intAccount = new Account<Integer>(1);

        Account<? super Integer> intAccount = new Account<Number>(3.0);


    }

    public static <T, Team extends String, Steps> T getT(T unknowType, Team unknowTeam, Steps unknowSteps) {
        System.out.println(unknowType.getClass().getName());
        return unknowType;
    }
}

class Account<T extends Object> {
    public T id;

    public Account(T id) {
        this.id = id;
    }

}
