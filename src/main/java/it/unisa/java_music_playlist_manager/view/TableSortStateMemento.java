package it.unisa.java_music_playlist_manager.view;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern Memento per salvare e ripristinare lo stato di ordinamento di una TableView.
 * Evita di esporre la complessità del salvataggio nel controller principale.
 */
public class TableSortStateMemento {
    private final List<TableColumn<?, ?>> sortColumns;

    public TableSortStateMemento(TableView<?> table) {
        if (table != null) {
            this.sortColumns = new ArrayList<>(table.getSortOrder());
        } else {
            this.sortColumns = new ArrayList<>();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void restore(TableView<?> table) {
        if (table != null && !sortColumns.isEmpty()) {
            table.getSortOrder().setAll((List) sortColumns);
        }
    }
}
