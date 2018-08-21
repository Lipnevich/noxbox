package live.noxbox.model;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum EventType {
    // request types
    balance,
    refund,

    request,
    accept,
    decline,
    timeout,
    qr,
    payerCancel,
    performerCancel,
    complete,
    dislike,

    // notification types
    sync,
    story,
    move,
    payment,

    uploadingProgress,

}
