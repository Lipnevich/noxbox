package live.noxbox.debug;

import java.math.BigDecimal;

public class SandBox {


    public static void main(String[] args) {
        BigDecimal smallBigDecimal = new BigDecimal(1);
        BigDecimal bigBigDecimal = new BigDecimal(5);
        System.out.println(smallBigDecimal.compareTo(bigBigDecimal) > 0);
    }
}
