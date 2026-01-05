package org.main.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutoCompleteListener<T> implements EventHandler<KeyEvent> {

    private ComboBox<T> comboBox;
    private ObservableList<T> data;

    public AutoCompleteListener(final ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        this.data = comboBox.getItems();

        this.comboBox.setEditable(true);

        // ukrywanie listy po wciśnięciu klawisza
        this.comboBox.setOnKeyPressed(t -> comboBox.hide());

        // logika przy puszczeniu klawisza
        this.comboBox.setOnKeyReleased(AutoCompleteListener.this);
    }

    //funkcja co wywołuje się podczas klikania w przyciski
    @Override
    public void handle(KeyEvent event) {
        // ignorujemy strzałki itd
        if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN
                || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                || event.getCode() == KeyCode.HOME
                || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
            return;
        }

        // zapamiętujemy pozycję kursora i tekst
        String currentText = comboBox.getEditor().getText();
        int caretPosition = comboBox.getEditor().getCaretPosition();

        // filtrujemy listę
        ObservableList<T> list = FXCollections.observableArrayList();
        for (T datum : data) {
            if(datum.toString().toLowerCase().contains(currentText.toLowerCase())) {
                list.add(datum);
            }
        }

        // podmieniamy listę na nową przefiltrowaną
        comboBox.setItems(list);

        // przywracamy tekst
        comboBox.getEditor().setText(currentText);

        // obsługa błędu (przerzucania kursora na początek)
        if (event.getCode() != KeyCode.BACK_SPACE && event.getCode() != KeyCode.DELETE) {
            comboBox.getEditor().positionCaret(currentText.length());
        } else {
            // przy kasowaniu zostajemy przy tej samej pozycji
            try {
                comboBox.getEditor().positionCaret(caretPosition);
            } catch (Exception e) {
                // ustaw na koniec w razie błędu
                comboBox.getEditor().positionCaret(currentText.length());
            }
        }

        // pokazywanie listy
        if(!list.isEmpty()) {
            comboBox.show();
        }
    }
}