import java.util.*;
import java.time.LocalDate;

public class run {

    static class BookingEvent {
        LocalDate date;
        int change;

        BookingEvent(LocalDate date, int change) {
            this.date = date;
            this.change = change;
        }
    }

    private static boolean isCapacityEnough(int capacity, List<Map<String, String>> bookings) {
        List<BookingEvent> timeline = new ArrayList<>();

        for (Map<String, String> guest : bookings) {
            LocalDate arrival = LocalDate.parse(guest.get("check-in"));
            LocalDate departure = LocalDate.parse(guest.get("check-out"));
            timeline.add(new BookingEvent(arrival, 1));
            timeline.add(new BookingEvent(departure, -1));
        }

        timeline.sort((a, b) -> {
            if (!a.date.equals(b.date)) {
                return a.date.compareTo(b.date);
            }
            return Integer.compare(a.change, b.change);
        });

        int currentOccupancy = 0;
        for (BookingEvent event : timeline) {
            currentOccupancy += event.change;
            if (currentOccupancy > capacity) {
                return false;
            }
        }

        return true;
    }

    private static Map<String, String> parseJsonLine(String line) {
        Map<String, String> guestInfo = new HashMap<>();
        line = line.trim().substring(1, line.length() - 1);
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String field : fields) {
            String[] pair = field.split(":", 2);
            String key = pair[0].trim().replaceAll("\"", "");
            String value = pair[1].trim().replaceAll("\"", "");
            guestInfo.put(key, value);
        }

        return guestInfo;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int maxRooms = 0;
        int guestCount = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                maxRooms = Integer.parseInt(line);
                break;
            }
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                guestCount = Integer.parseInt(line);
                break;
            }
        }

        List<Map<String, String>> allGuests = new ArrayList<>();

        int readCount = 0;
        while (readCount < guestCount && scanner.hasNextLine()) {
            String json = scanner.nextLine().trim();
            if (!json.isEmpty()) {
                allGuests.add(parseJsonLine(json));
                readCount++;
            }
        }

        boolean canAccommodate = isCapacityEnough(maxRooms, allGuests);
        System.out.println(canAccommodate ? "True" : "False");

        scanner.close();
    }
}
