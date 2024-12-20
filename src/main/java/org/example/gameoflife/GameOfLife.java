package org.example.gameoflife;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameOfLife {

    @FXML
    private ScrollPane gameScrollPane;

    @FXML
    private Pane gamePane;

    @FXML
    private Button zoomInButton;

    @FXML
    private Button zoomOutButton;

    private Canvas gameCanvas;

    private static final int CELL_SIZE = 10; // Base size of each cell
    private static final int GRID_SIZE = 500; // Initial large grid size
    private boolean[][] grid = new boolean[GRID_SIZE][GRID_SIZE]; // Large grid
    private double zoomFactor = 1.0; // Initial zoom level

    // Initialize the game
    public void initialize() {
        // Create and set up the canvas
        gameCanvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        gamePane.getChildren().add(gameCanvas);

        // Bind the canvas size to the zoom level
        gameCanvas.setScaleX(zoomFactor);
        gameCanvas.setScaleY(zoomFactor);

        // Initialize the grid and draw it
        initializeGrid();
        drawGrid();

        // Set up zooming with buttons
        zoomInButton.setOnAction(e -> zoomCanvas(1.1)); // Zoom in by 10%
        zoomOutButton.setOnAction(e -> zoomCanvas(1 / 1.1)); // Zoom out by 10%

        // Set up zooming behavior with the scroll wheel
        gameScrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);

        // Start the game loop
        startGameLoop();
    }

    // Initialize the grid with random values
    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = Math.random() < 0.5;
            }
        }
    }

    // Update the grid to the next generation
    private void updateGrid() {
        boolean[][] newGrid = new boolean[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int liveNeighbors = countLiveNeighbors(i, j);

                // Apply rules of the Game of Life
                if (grid[i][j]) {
                    newGrid[i][j] = (liveNeighbors == 2 || liveNeighbors == 3);
                } else {
                    newGrid[i][j] = (liveNeighbors == 3);
                }
            }
        }

        grid = newGrid;
    }

    // Count live neighbors for a cell
    private int countLiveNeighbors(int row, int col) {
        int liveNeighbors = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the current cell
                int newRow = (row + i + GRID_SIZE) % GRID_SIZE; // Wrap around the edges
                int newCol = (col + j + GRID_SIZE) % GRID_SIZE; // Wrap around the edges

                if (grid[newRow][newCol]) {
                    liveNeighbors++;
                }
            }
        }

        return liveNeighbors;
    }

    // Draw the grid on the canvas
    private void drawGrid() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j]) {
                    gc.setFill(Color.BLACK); // Alive cells
                } else {
                    gc.setFill(Color.WHITE); // Dead cells
                }

                gc.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    // Handle zooming via the mouse wheel
    private void handleZoom(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            zoomCanvas(1.1); // Zoom in
        } else {
            zoomCanvas(1 / 1.1); // Zoom out
        }

        event.consume(); // Consume the event to prevent default behavior
    }

    // Zoom the canvas by a given factor
    private void zoomCanvas(double factor) {
        zoomFactor *= factor;

        // Apply the zoom factor to the canvas
        gameCanvas.setScaleX(zoomFactor);
        gameCanvas.setScaleY(zoomFactor);
    }

    // Start the game loop with a timeline
    private void startGameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            updateGrid();
            drawGrid();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
