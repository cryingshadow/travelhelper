package travelhelper;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;

import ocp.*;

public class Main {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String BG_ROOM_PATTERN = "B-D\\d\\d";

    private static final List<Function<OCEntry, Optional<? extends TravelExpenseEntry>>> CONVERTERS =
        List.of(
            Main::travelEntryByRoomInBG,
            Main::travelEntryByPlace
        );

    private static final int NUMBER_OF_TRAVEL_ENTRIES = 8;

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 3) {
            System.out.println("Call with calendarExportFile, fromDate, and travelExpenseFile!");
            return;
        }
        final File calendarExportFile = new File(args[0]);
        final LocalDateTime fromDate = LocalDate.parse(args[1], Main.DATE_FORMAT).atStartOfDay();
        final File travelExpenseFile = new File(args[2]);
        final List<OCEntry> calendarEntries = OCEntry.parse(calendarExportFile);
        final List<TravelExpenseEntry> travelExpenseEntries =
            Main.convertToTravelExpenseEntries(
                calendarEntries.stream().filter(entry -> entry.start().compareTo(fromDate) >= 0).toList()
            );
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(travelExpenseFile))) {
            writer.write("\\documentclass{article}\n");
            writer.write("\n");
            writer.write("\\input{../../../templates/travel/packages.tex}\n");
            writer.write("\n");
            writer.write("\\newcommand{\\reportfrom}{");
            writer.write(Main.getMinDate(travelExpenseEntries).format(Main.DATE_FORMAT));
            writer.write("}\n");
            writer.write("\\newcommand{\\reportto}{");
            writer.write(Main.getMaxDate(travelExpenseEntries).format(Main.DATE_FORMAT));
            writer.write("}\n");
            writer.write("\\newcommand{\\travels}{%\n");
            int numberOfEntries = 0;
            for (final TravelExpenseEntry entry : travelExpenseEntries) {
                writer.write(entry.toString(numberOfEntries >= Main.NUMBER_OF_TRAVEL_ENTRIES));
                writer.write("\n");
                numberOfEntries++;
            }
            final int numberOfEmptyEntries = Main.NUMBER_OF_TRAVEL_ENTRIES - numberOfEntries;
            for (int i = 0; i < numberOfEmptyEntries; i++) {
                writer.write("\\emptytravel{}");
                writer.write("\n");
            }
            writer.write("}\n");
            writer.write("\n");
            writer.write("\\input{../../../templates/travel/travel.tex}\n");
        }
    }

    private static List<TravelExpenseEntry> convertToTravelExpenseEntries(final List<OCEntry> calendarEntries) {
        final List<TravelExpenseEntry> travelExpenseEntries = new ArrayList<TravelExpenseEntry>();
        for (final Function<OCEntry, Optional<? extends TravelExpenseEntry>> converter : Main.CONVERTERS) {
            travelExpenseEntries.addAll(
                calendarEntries.stream().map(converter).filter(Optional::isPresent).map(Optional::get).toList()
            );
        }
        Collections.sort(travelExpenseEntries);
        final List<TravelExpenseEntry> result = new LinkedList<TravelExpenseEntry>();
        TravelExpenseEntry current = null;
        for (final TravelExpenseEntry travelExpenseEntry : travelExpenseEntries) {
            if (current == null) {
                current = travelExpenseEntry;
            } else if (current.start.toLocalDate().equals(travelExpenseEntry.start.toLocalDate())) {
                current = current.combine(travelExpenseEntry);
            } else {
                result.add(current);
                current = travelExpenseEntry;
            }
        }
        result.add(current);
        return result;
    }

    private static LocalDateTime getMaxDate(final List<TravelExpenseEntry> travelExpenseEntries) {
        return travelExpenseEntries.stream().map(entry -> entry.end).max(Comparator.naturalOrder()).get();
    }

    private static LocalDateTime getMinDate(final List<TravelExpenseEntry> travelExpenseEntries) {
        return travelExpenseEntries.stream().map(entry -> entry.start).min(Comparator.naturalOrder()).get();
    }

    private static Optional<CarTravelEntry> travelEntryByPlace(final OCEntry calendarEntry) {
        if (calendarEntry.additionalInformation().meetingInformation().place().isEmpty()) {
            return Optional.empty();
        }
        final String place = calendarEntry.additionalInformation().meetingInformation().place().get();
        if (
            place.isBlank()
            || place.matches(Main.BG_ROOM_PATTERN)
            || place.matches(".*M-\\d\\d\\d.*")
            || "Microsoft Teams Meeting".equals(place)
            || "Microsoft Teams-Besprechung".equals(place)
        ) {
            return Optional.empty();
        }
        return Optional.of(
            new CarTravelEntry(
                calendarEntry.start(),
                calendarEntry.end(),
                place,
                "\\E{} -- " + place,
                "?",
                calendarEntry.additionalInformation().travelKilometers().orElse(0)
            )
        );
    }

    private static Optional<CarTravelEntry> travelEntryByRoomInBG(final OCEntry calendarEntry) {
        if (
            calendarEntry.additionalInformation().meetingInformation().place().orElse("").matches(Main.BG_ROOM_PATTERN)
        ) {
            return Optional.of(
                new CarTravelEntry(
                    calendarEntry.start().minusMinutes(105),
                    calendarEntry.end().plusMinutes(120),
                    "\\BG",
                    "\\E{} -- \\BG",
                    calendarEntry.subject().contains("Vorlesung") ? "Vorlesung" : "?",
                    140
                )
            );
        }
        return Optional.empty();
    }

}
