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

    private static final List<Function<OCEntry, Optional<TravelExpenseEntry>>> CONVERTERS =
        List.of();

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 2) {
            System.out.println("Call with calendarExportFile and travelExpenseFile!");
            return;
        }
        final File calendarExportFile = new File(args[0]);
        final File travelExpenseFile = new File(args[1]);
        final List<OCEntry> calendarEntries = OCEntry.parse(calendarExportFile);
        final List<TravelExpenseEntry> travelExpenseEntries = Main.convertToTravelExpenseEntries(calendarEntries);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(travelExpenseFile))) {
            writer.write("\\documentclass{article}\n");
            writer.write("\n");
            writer.write("\\input{../../../templates/travel/packages.tex}\n");
            writer.write("\n");
            writer.write("\\newcommand{\\placedate}{\\E, ");
            writer.write(LocalDate.now().format(Main.DATE_FORMAT));
            writer.write("}\n");
            writer.write("\\newcommand{\\reportfrom}{");
            writer.write(Main.getMinDate(travelExpenseEntries).format(Main.DATE_FORMAT));
            writer.write("}\n");
            writer.write("\\newcommand{\\reportto}{");
            writer.write(Main.getMaxDate(travelExpenseEntries).format(Main.DATE_FORMAT));
            writer.write("}\n");
            writer.write("\\newcommand{\\travels}{%\n");
            for (final TravelExpenseEntry entry : travelExpenseEntries) {
                writer.write(entry.toString());
                writer.write("\n");
            }
            writer.write("}\n");
            writer.write("\n");
            writer.write("\\input{../../../templates/travel/travel.tex}\n");
        }
    }

    private static List<TravelExpenseEntry> convertToTravelExpenseEntries(final List<OCEntry> calendarEntries) {
        final List<TravelExpenseEntry> result = new LinkedList<TravelExpenseEntry>();
        for (final Function<OCEntry, Optional<TravelExpenseEntry>> converter : Main.CONVERTERS) {
            result.addAll(
                calendarEntries.stream().map(converter).filter(Optional::isPresent).map(Optional::get).toList()
            );
        }
        return result;
    }

    private static LocalDateTime getMaxDate(final List<TravelExpenseEntry> travelExpenseEntries) {
        return travelExpenseEntries.stream().map(entry -> entry.end).max(Comparator.naturalOrder()).get();
    }

    private static LocalDateTime getMinDate(final List<TravelExpenseEntry> travelExpenseEntries) {
        return travelExpenseEntries.stream().map(entry -> entry.start).min(Comparator.naturalOrder()).get();
    }

}
