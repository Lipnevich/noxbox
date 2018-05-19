package by.nicolay.lipnevich.noxbox.model;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum MessageType {
    // Payer say
    ping, cancel,

    // Performer say
    pong, gnop, move, complete, qr,

    // Both say
    dislike, story,

    // Server say
    balanceUpdated, sync,
}
