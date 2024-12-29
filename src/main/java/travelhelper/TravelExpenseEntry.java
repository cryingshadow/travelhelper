package travelhelper;

import java.time.*;
import java.util.*;

import ocp.*;

public abstract class TravelExpenseEntry implements Comparable<TravelExpenseEntry> {

    private static final Comparator<TravelExpenseEntry> COMPARATOR =
        new LexicographicComparator<TravelExpenseEntry>(
            entry -> entry.start,
            entry -> entry.end,
            entry -> entry.destination
        );

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

    @Override
    public int compareTo(final TravelExpenseEntry other) {
        return TravelExpenseEntry.COMPARATOR.compare(this, other);
    }

}
