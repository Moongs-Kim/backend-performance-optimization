package predawn.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class TimeFormatter {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
    private static final DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy/MM");

    public static String format(LocalDateTime createdTime) {
        return createdTime.format(formatter);
    }

    public static String fileNameFormat(LocalDate createdTime) {
        return createdTime.format(fileNameFormatter);
    }
}
