package live.noxbox.debug;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SandBox {
    public static void main(String[] args) {
        Set<String> strings = new HashSet<>();
        strings.add("asd");
        strings.add("asdd");
        strings.add("asdf");
        strings.add("asdg");
        strings.add("asdh");
        System.out.println(strings);
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            strings.remove("asdh");
        }
        System.out.println(strings);
    }
}
