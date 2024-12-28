package travelhelper;

import java.time.*;

public abstract class TravelExpenseEntry {

    public final String destination;

    public final LocalDateTime end;

    public final String reason;

    public final String route;

    public final LocalDateTime start;

    protected TravelExpenseEntry(
        final LocalDateTime start,
        final LocalDateTime end,
        final String destination,
        final String route,
        final String reason
    ) {
        this.start = start;
        this.end = end;
        this.destination = destination;
        this.route = route;
        this.reason = reason;
    }

}
