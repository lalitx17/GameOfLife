package org.example.gameoflife;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;



public class GameOfLife {
    @FXML
    private Canvas gameCanvas;

    private static final int GRID_SIZE = 50;
    private static final int CELL_SIZE = 10;             //10*10 pixels each

    private boolean[][] grid = new boolean[GRID_SIZE][GRID_SIZE];

    public void initializeGrid(){
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                grid[i][j] = Math.random() < 0.5;
            }
        }
    }

    public void updateGrid(){
        boolean[][] newGrid = new boolean[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++){
            for(int j = 0; j < GRID_SIZE; j++){

                int liveNeighbor = countLiveNeighbors(i, j);

                if (grid[i][j]){
                    newGrid[i][j] = (liveNeighbor == 2 || liveNeighbor == 3);
                }else{
                    newGrid[i][j] = (liveNeighbor == 3);
                }
            }
        }

        grid = newGrid;
    }

    private int countLiveNeighbors(int row, int col){
        int liveNeighbors = 0;

        for (int i = -1; i <= 1; i++){
            for (int j = -1; j <= 1; j++){
                if (i == 0 && j == 0) continue;

                int newRow = row + i;
                int newCol = col + j;

                if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE){
                    if (grid[newRow][newCol]){
                        liveNeighbors++;
                    }
                }
            }
        }

        return liveNeighbors;
    };

    private void drawGrid(){
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                if (grid[i][j]){
                    gc.setFill(Color.BLACK);
                }else{
                    gc.setFill(Color.WHITE);
                }

                gc.fillRect(j*CELL_SIZE, i*CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    };

    @FXML
    protected void StartGame() {
        initializeGrid();
        drawGrid();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            updateGrid();
            drawGrid();
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    };
}