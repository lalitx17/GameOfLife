package org.example.gameoflife;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.layout.Region;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameOfLife {
    @FXML private ScrollPane gameScrollPane;
    @FXML private StackPane gamePane;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;

    private Canvas gameCanvas;
    private static final int CELL_SIZE = 10;
    private static final double MOVE_SPEED = 1.0;

    private double targetX = 0;
    private double targetY = 0;
    private double currentX = 0;
    private double currentY = 0;
    private double zoomFactor = 1.0;
    private double targetZoom = 1.0;
    private double smoothingFactor = 0.5;

    private Set<Point2D> aliveCells = new HashSet<>();
    private Map<Point2D, Boolean> cellBuffer = new HashMap<>();

    public void initialize() {
        setupCanvas();
        setupControls();
        initializeGrid();
        startGameLoop();
        startSmoothRenderLoop();
    }

    private void setupCanvas() {
        gameCanvas = new Canvas();
        gamePane.getChildren().add(gameCanvas);

        gamePane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        gamePane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        gameCanvas.widthProperty().bind(gamePane.widthProperty());
        gameCanvas.heightProperty().bind(gamePane.heightProperty());

        gameScrollPane.setFitToWidth(true);
        gameScrollPane.setFitToHeight(true);

        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);

        zoomInButton.setOnAction(e -> zoomCanvas(1.2));
        zoomOutButton.setOnAction(e -> zoomCanvas(1/1.2));
    }

    private void setupControls() {
        gameScrollPane.setPannable(false);
        gameScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gameScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void initializeGrid() {
        int[][] gliderGun = {
                {0, 4}, {0, 5}, {1, 4}, {1, 5}, {10, 4}, {10, 5}, {10, 6},
                {11, 3}, {11, 7}, {12, 2}, {12, 8}, {13, 2}, {13, 8}, {14, 5},
                {15, 3}, {15, 7}, {16, 4}, {16, 5}, {16, 6}, {17, 5},
                {20, 2}, {20, 3}, {20, 4}, {21, 2}, {21, 3}, {21, 4},
                {22, 1}, {22, 5}, {24, 0}, {24, 1}, {24, 5}, {24, 6}
        };

        for (int[] coord : gliderGun) {
            aliveCells.add(new Point2D(coord[0], coord[1]));
        }
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                targetY -= MOVE_SPEED;
                break;
            case DOWN:
                targetY += MOVE_SPEED;
                break;
            case LEFT:
                targetX -= MOVE_SPEED;
                break;
            case RIGHT:
                targetX += MOVE_SPEED;
                break;
        }
        event.consume();
    }

    private void zoomTowardPoint(double factor, double pivotX, double pivotY) {
        double oldZoom = targetZoom;
        targetZoom *= factor;
        targetZoom = Math.max(0.1, Math.min(targetZoom, 50.0));

        double mouseWorldX = (pivotX / (CELL_SIZE * oldZoom)) + targetX;
        double mouseWorldY = (pivotY / (CELL_SIZE * oldZoom)) + targetY;

        targetX = mouseWorldX - (pivotX / (CELL_SIZE * targetZoom));
        targetY = mouseWorldY - (pivotY / (CELL_SIZE * targetZoom));
    }

    private void zoomCanvas(double factor) {
        zoomTowardPoint(factor, gameCanvas.getWidth() / 2, gameCanvas.getHeight() / 2);
    }

    private void updateGrid() {
        cellBuffer.clear();
        Set<Point2D> cellsToCheck = new HashSet<>();

        // Add all alive cells and their neighbors to check
        for (Point2D cell : aliveCells) {
            cellsToCheck.add(cell);
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    cellsToCheck.add(new Point2D(cell.getX() + dx, cell.getY() + dy));
                }
            }
        }

        // Check all relevant cells
        for (Point2D cell : cellsToCheck) {
            int neighbors = countLiveNeighbors(cell);
            boolean isAlive = aliveCells.contains(cell);

            if (isAlive && (neighbors == 2 || neighbors == 3)) {
                cellBuffer.put(cell, true);
            } else if (!isAlive && neighbors == 3) {
                cellBuffer.put(cell, true);
            }
        }

        aliveCells.clear();
        aliveCells.addAll(cellBuffer.keySet());
    }

    private int countLiveNeighbors(Point2D cell) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                Point2D neighbor = new Point2D(cell.getX() + dx, cell.getY() + dy);
                if (aliveCells.contains(neighbor)) count++;
            }
        }
        return count;
    }

    private void drawGrid() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        double viewportWidth = gameCanvas.getWidth() / (CELL_SIZE * zoomFactor);
        double viewportHeight = gameCanvas.getHeight() / (CELL_SIZE * zoomFactor);

        int minX = (int)(currentX - viewportWidth/2 - 1);
        int maxX = (int)(currentX + viewportWidth/2 + 1);
        int minY = (int)(currentY - viewportHeight/2 - 1);
        int maxY = (int)(currentY + viewportHeight/2 + 1);

        gc.setFill(Color.BLACK);

        for (Point2D cell : aliveCells) {
            if (cell.getX() >= minX && cell.getX() <= maxX &&
                    cell.getY() >= minY && cell.getY() <= maxY) {

                double screenX = (cell.getX() - currentX) * CELL_SIZE * zoomFactor + gameCanvas.getWidth()/2;
                double screenY = (cell.getY() - currentY) * CELL_SIZE * zoomFactor + gameCanvas.getHeight()/2;

                gc.fillRect(screenX, screenY,
                        CELL_SIZE * zoomFactor,
                        CELL_SIZE * zoomFactor);
            }
        }

        if (CELL_SIZE * zoomFactor > 5) {
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(0.5);

            double offsetX = (-(currentX % 1.0)) * CELL_SIZE * zoomFactor;
            double offsetY = (-(currentY % 1.0)) * CELL_SIZE * zoomFactor;

            for (double x = offsetX; x <= gameCanvas.getWidth(); x += CELL_SIZE * zoomFactor) {
                gc.strokeLine(x + gameCanvas.getWidth()/2 % (CELL_SIZE * zoomFactor), 0,
                        x + gameCanvas.getWidth()/2 % (CELL_SIZE * zoomFactor), gameCanvas.getHeight());
            }

            for (double y = offsetY; y <= gameCanvas.getHeight(); y += CELL_SIZE * zoomFactor) {
                gc.strokeLine(0, y + gameCanvas.getHeight()/2 % (CELL_SIZE * zoomFactor),
                        gameCanvas.getWidth(), y + gameCanvas.getHeight()/2 % (CELL_SIZE * zoomFactor));
            }
        }
    }

    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> {
            updateGrid();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void startSmoothRenderLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                currentX += (targetX - currentX) * smoothingFactor;
                currentY += (targetY - currentY) * smoothingFactor;
                zoomFactor += (targetZoom - zoomFactor) * smoothingFactor;

                drawGrid();
            }
        }.start();
    }
}