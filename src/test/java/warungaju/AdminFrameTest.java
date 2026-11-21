package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.Test;

class AdminFrameTest {
    @Test
    void formatsSupabaseTimestampsInWib() {
        assertEquals("21-11-2026 08:42:45 WIB",
            AdminFrame.formatWib("2026-11-21T01:42:45Z"));
        assertEquals("", AdminFrame.formatWib(null));
        assertThrows(IllegalArgumentException.class,
            () -> AdminFrame.formatWib("not-a-timestamp"));
    }

    @Test
    void sortChoicesOrderNamesAndActivityDates() {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "Nomor", "Email", "Nama", "Role", "Status", "Terakhir aktif",
            "Dibuat", "Diverifikasi", "ID internal"
        }, 0);
        model.addRow(new Object[]{1, "b@example.com", "Budi", "umkm", "Aktif",
            Instant.parse("2026-11-21T05:00:00Z"),
            Instant.parse("2026-11-21T02:00:00Z"), null, "id-b"});
        model.addRow(new Object[]{2, "a@example.com", "Ayu", "umkm", "Aktif",
            Instant.parse("2026-11-21T04:00:00Z"),
            Instant.parse("2026-11-21T01:00:00Z"), null, "id-a"});
        model.addRow(new Object[]{3, "c@example.com", "Citra", "umkm", "Aktif",
            Instant.parse("2026-11-21T03:00:00Z"),
            Instant.parse("2026-11-21T03:00:00Z"), null, "id-c"});
        TableRowSorter<DefaultTableModel> sorter = AdminFrame.createTableSorter(model);

        AdminFrame.applySortChoice(sorter, 0);
        assertEquals("Ayu", nameAt(model, sorter, 0));
        assertEquals("Budi", nameAt(model, sorter, 1));

        AdminFrame.applySortChoice(sorter, 2);
        assertEquals("Citra", nameAt(model, sorter, 0));
        assertEquals("Budi", nameAt(model, sorter, 1));

        AdminFrame.applySortChoice(sorter, 4);
        assertEquals("Budi", nameAt(model, sorter, 0));
        assertEquals("Ayu", nameAt(model, sorter, 1));
    }

    private String nameAt(DefaultTableModel model, TableRowSorter<DefaultTableModel> sorter,
                          int viewRow) {
        int modelRow = sorter.convertRowIndexToModel(viewRow);
        return String.valueOf(model.getValueAt(modelRow, 2));
    }
}
