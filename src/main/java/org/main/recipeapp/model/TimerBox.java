package org.main.recipeapp.model;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TimerBox extends HBox {

    private final String name;
    private int secondsLeft;
    private boolean isRunning = true;
    private Thread timerThread;

    public TimerBox(String name, int totalSeconds, VBox parentContainer) {
        this.name = name;
        this.secondsLeft = totalSeconds;

        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-background-color: #2D2D44; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #555570; -fx-border-radius: 5;");

        // elementy ui
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #BB86FC; -fx-font-weight: bold;");

        Label timeLabel = new Label(formatTime(secondsLeft));
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button stopButton = new Button("X");
        stopButton.setStyle("-fx-background-color: #CF6679; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        stopButton.setOnAction(e -> {
            stopTimer();
            parentContainer.getChildren().remove(this);
        });

        this.getChildren().addAll(nameLabel, timeLabel, stopButton);

        // zaczynamy wątek
        startTimerThread(timeLabel);
    }

    // wątki
    private void startTimerThread(Label timeLabel) {
        timerThread = new Thread(() -> {
            while (secondsLeft > 0 && isRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                secondsLeft--;

                Platform.runLater(() -> {
                    if (!isRunning) { return; }
                    timeLabel.setText(formatTime(secondsLeft));
                });
            }

            // koniec czasu
            if (secondsLeft <= 0 && isRunning) {
                Platform.runLater(() -> {
                    timeLabel.setStyle("-fx-text-fill: #03DAC6; -fx-font-size: 14px;");
                    timeLabel.setText("JUŻ!");
                });
            }
        });

        // wątek demon żeby się zamknął z aplikacją
        timerThread.setDaemon(true);
        timerThread.start();
    }

    public void stopTimer() {
        isRunning = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    // formatowanie sekund
    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}