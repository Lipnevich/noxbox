package live.noxbox.tools.exceptions;

/**
 * Created by Vladislaw Kravchenok on 15.05.2019.
 */
public class AcceptanceException extends Exception {
    private String acceptanceContent;
    public AcceptanceException(String acceptanceContent) {
        super(acceptanceContent);
        this.acceptanceContent = acceptanceContent;
    }

    @Override
    public String getMessage() {
        return acceptanceContent;
    }
}
