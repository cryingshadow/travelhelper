package travelhelper;

import java.time.*;

public class CarTravelEntry extends TravelExpenseEntry {

    public final int kilometers;

    public CarTravelEntry(
        final LocalDateTime start,
        final LocalDateTime end,
        final String destination,
        final String route,
        final String reason,
        final int kilometers
    ) {
        super(start, end, destination, route, reason);
        this.kilometers = kilometers;
    }

    @Override
    public String toString(final boolean comment) {
        return String.format(
            "%s\\cartravel{%s}{%s}{%s}{%s}{%s}{%s}{%s}{%d}",
            comment ? "%" : "",
            this.start.format(Main.DATE_FORMAT),
            this.start.format(Main.TIME_FORMAT),
            this.end.format(Main.DATE_FORMAT),
            this.end.format(Main.TIME_FORMAT),
            this.destination,
            this.route,
            this.reason,
            this.kilometers
        );
    }

}
