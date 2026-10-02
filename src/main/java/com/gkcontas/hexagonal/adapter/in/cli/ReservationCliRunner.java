package com.gkcontas.hexagonal.adapter.in.cli;

import com.gkcontas.hexagonal.application.port.in.BookRoomCommand;
import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.port.in.CancelReservationUseCase;
import com.gkcontas.hexagonal.application.port.in.FindReservationsUseCase;
import com.gkcontas.hexagonal.application.port.in.ListRoomsUseCase;
import com.gkcontas.hexagonal.domain.exception.DomainException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * The second inbound adapter, and the one that proves the first was not special.
 *
 * <p>It calls exactly the same use cases as the REST controller — same interfaces, same
 * commands, same exceptions. Nothing in the application layer knows which one is calling,
 * and that is the whole claim of ports and adapters, stated as code rather than as a
 * diagram.
 *
 * <p>Note what had to be duplicated to add it: argument parsing and output formatting.
 * Nothing else. No rule, no validation of business meaning, no second copy of "is the room
 * free?".
 */
public class ReservationCliRunner implements ApplicationRunner {

    private final BookRoomUseCase bookRoom;
    private final CancelReservationUseCase cancelReservation;
    private final FindReservationsUseCase findReservations;
    private final ListRoomsUseCase listRooms;
    private final PrintStream out;

    /**
     * One constructor, and the stream is a parameter.
     *
     * <p>A second, convenience constructor defaulting to {@code System.out} looks
     * harmless and leaves Spring with two candidates and no way to choose — it then looks
     * for a no-arg one and fails at startup. Where the output goes is decided in
     * {@link CliConfiguration}, which is also what lets the test point it at a buffer.
     */
    public ReservationCliRunner(BookRoomUseCase bookRoom,
                                CancelReservationUseCase cancelReservation,
                                FindReservationsUseCase findReservations,
                                ListRoomsUseCase listRooms,
                                PrintStream out) {
        this.bookRoom = bookRoom;
        this.cancelReservation = cancelReservation;
        this.findReservations = findReservations;
        this.listRooms = listRooms;
        this.out = out;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> commands = args.getNonOptionArgs();
        if (commands.isEmpty()) {
            printUsage();
            return;
        }
        try {
            switch (commands.getFirst()) {
                case "rooms" -> printRooms();
                case "book" -> printReservation(book(args));
                case "cancel" -> printReservation(
                        cancelReservation.cancel(ReservationId.of(required(args, "id"))));
                case "agenda" -> printAgenda(args);
                default -> printUsage();
            }
        } catch (DomainException | IllegalArgumentException exception) {
            // The same failures the HTTP adapter turns into 4xx, here turned into a line
            // of text. The decision is the adapter's; the rule was never duplicated.
            out.println("error: " + exception.getMessage());
        }
    }

    private Reservation book(ApplicationArguments args) {
        return bookRoom.book(new BookRoomCommand(
                required(args, "room"),
                required(args, "organizer"),
                Instant.parse(required(args, "from")),
                Instant.parse(required(args, "to")),
                Integer.parseInt(required(args, "attendees"))));
    }

    private void printAgenda(ApplicationArguments args) {
        RoomId roomId = new RoomId(required(args, "room"));
        LocalDate date = LocalDate.parse(required(args, "date"));
        ZoneId zone = ZoneId.of(args.containsOption("zone")
                ? args.getOptionValues("zone").getFirst() : "UTC");

        List<Reservation> reservations = findReservations.byRoomAndDay(roomId, date, zone);
        if (reservations.isEmpty()) {
            out.println("no reservations for %s on %s".formatted(roomId, date));
            return;
        }
        reservations.forEach(this::printReservation);
    }

    private void printRooms() {
        listRooms.listAll().forEach(room ->
                out.printf("%-12s %-14s capacity %d%n", room.id(), room.name(), room.capacity()));
    }

    private void printReservation(Reservation reservation) {
        out.printf("%s  %-12s %-10s %s  %d attendees  %s%n",
                reservation.id(), reservation.roomId(), reservation.organizer(),
                reservation.slot(), reservation.attendees(), reservation.status());
    }

    private void printUsage() {
        out.println("""
                usage:
                  rooms
                  book   --room=ID --organizer=NAME --from=ISO_INSTANT --to=ISO_INSTANT --attendees=N
                  cancel --id=UUID
                  agenda --room=ID --date=YYYY-MM-DD [--zone=ZONE_ID]""");
    }

    private static String required(ApplicationArguments args, String option) {
        List<String> values = args.getOptionValues(option);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("missing required option --" + option);
        }
        return values.getFirst();
    }
}
