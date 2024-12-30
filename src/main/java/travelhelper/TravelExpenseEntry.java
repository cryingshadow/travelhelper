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

    public static LocalDateTime max(final LocalDateTime dateTime1, final LocalDateTime dateTime2) {
        return dateTime1.compareTo(dateTime2) < 0 ? dateTime2 : dateTime1;
    }

    public static LocalDateTime min(final LocalDateTime dateTime1, final LocalDateTime dateTime2) {
        return dateTime1.compareTo(dateTime2) < 0 ? dateTime1 : dateTime2;
    }

    private static int combineKilometers(final TravelExpenseEntry entry1, final TravelExpenseEntry entry2) {
        if (entry1 instanceof CarTravelEntry && entry2 instanceof CarTravelEntry) {
            final CarTravelEntry carEntry1 = (CarTravelEntry)entry1;
            final CarTravelEntry carEntry2 = (CarTravelEntry)entry2;
            if (carEntry1.kilometers == carEntry2.kilometers) {
                return carEntry1.kilometers;
            }
        }
        return 0;
    }

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

    public TravelExpenseEntry combine(final TravelExpenseEntry travelExpenseEntry) {
        return new CarTravelEntry(
            TravelExpenseEntry.min(this.start, travelExpenseEntry.start),
            TravelExpenseEntry.max(this.end, travelExpenseEntry.end),
            this.destination.equals(travelExpenseEntry.destination) ? this.destination : "?",
            this.route.equals(travelExpenseEntry.route) ? this.route : "?",
            this.reason.equals(travelExpenseEntry.reason) ?
                this.reason :
                    String.format("%s/%s", this.reason, travelExpenseEntry.reason),
            TravelExpenseEntry.combineKilometers(this, travelExpenseEntry)
        );
    }

    @Override
    public int compareTo(final TravelExpenseEntry other) {
        return TravelExpenseEntry.COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

    public abstract String toString(boolean comment);

}
