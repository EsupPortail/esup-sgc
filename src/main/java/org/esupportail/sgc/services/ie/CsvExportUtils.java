package org.esupportail.sgc.services.ie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Utility helpers to export beans to CSV using Apache Commons CSV.
 * <p>
 * Replaces SuperCSV's {@code CsvDozerBeanWriter} (nested bean property mapping via
 * dot notation, e.g. "userAccount.eppn") and {@code CellProcessor}s (null to "" conversion,
 * date formatting) which are no longer available since SuperCSV is unmaintained.
 */
public final class CsvExportUtils {

    /**
     * CSV format equivalent to SuperCSV's {@code CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE}
     * (semicolon delimiter, double quote, CRLF line ending).
     */
    public static final CSVFormat EXCEL_NORTH_EUROPE = CSVFormat.Builder.create(CSVFormat.EXCEL)
            .setDelimiter(';')
            .setRecordSeparator("\r\n")
            .get();

    private CsvExportUtils() {
    }

    /**
     * Extracts a CSV row from a bean, resolving (possibly nested, dot notation) properties.
     * Null values are converted to an empty string. Date values whose index is flagged in
     * {@code isDateField} are formatted with {@code datePattern}.
     */
    public static Object[] extractRow(Object bean, List<String> fieldMapping, boolean[] isDateField, String datePattern) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        SimpleDateFormat sdf = datePattern != null ? new SimpleDateFormat(datePattern) : null;
        Object[] row = new Object[fieldMapping.size()];
        for (int i = 0; i < fieldMapping.size(); i++) {
            String path = fieldMapping.get(i);
            Object value = wrapper.isReadableProperty(path) ? wrapper.getPropertyValue(path) : null;
            if (value instanceof Date && sdf != null && isDateField != null && isDateField[i]) {
                row[i] = sdf.format((Date) value);
            } else {
                row[i] = (value == null) ? "" : value;
            }
        }
        return row;
    }

    /**
     * Extracts a CSV row from a bean with no date formatting (all values converted to "" if null).
     */
    public static Object[] extractRow(Object bean, List<String> fieldMapping) {
        return extractRow(bean, fieldMapping, null, null);
    }

    public static Object[] extractRow(Object bean, String[] fieldMapping) {
        return extractRow(bean, List.of(fieldMapping), null, null);
    }
}

