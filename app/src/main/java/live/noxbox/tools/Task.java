package live.noxbox.tools;

/**
 * Created by nicolay.lipnevich on 14/05/2017.
 */
public interface Task<T> {

    void execute(T object);
}
