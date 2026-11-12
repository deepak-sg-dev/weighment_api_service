package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDcRegisteredOfficeDto;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses {@code sug_fnd_pkg.get_reg_off_address} into DC print fields. */
public final class RegisteredOfficeAddressSupport {

    private static final Pattern OFFICE =
            Pattern.compile("(?is)Registered office:\\s*([^,]+),\\s*(.+?)(?=\\s*Ph No:|$)");
    private static final Pattern PHONE = Pattern.compile("(?i)Ph No:\\s*([^,]+)");
    private static final Pattern FAX = Pattern.compile("(?i)Fax No:\\s*([^,]+)");
    private static final Pattern CIN = Pattern.compile("(?i)CIN No:\\s*([^,\\s]+)");
    private static final Pattern PAN = Pattern.compile("(?i)PAN:\\s*([A-Z0-9]+)");
    private static final Pattern EMAIL = Pattern.compile("(?i)Email:\\s*([^,\\s]+)");
    private static final Pattern WEBSITE = Pattern.compile("(?i)Website:\\s*([^,\\s]+)");
    private static final Pattern CARE = Pattern.compile("(?i)Customer Care No:\\s*(.+)");

    private RegisteredOfficeAddressSupport() {}

    public static ScheduleDcRegisteredOfficeDto parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim().replace("\r\n", "\n").replace('\r', '\n');
        ScheduleDcRegisteredOfficeDto dto = ScheduleDcRegisteredOfficeDto.builder().text(text).build();
        Matcher office = OFFICE.matcher(text);
        if (office.find()) {
            dto.setName(trimToNull(office.group(1)));
            dto.setAddress(trimToNull(office.group(2)));
        }
        dto.setPhone(group(PHONE, text));
        dto.setFax(group(FAX, text));
        dto.setCinNo(group(CIN, text));
        dto.setPan(group(PAN, text));
        dto.setEmail(group(EMAIL, text));
        dto.setWebsite(group(WEBSITE, text));
        dto.setCustomerCareNo(group(CARE, text));
        return dto;
    }

    private static String group(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? trimToNull(m.group(1)) : null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
