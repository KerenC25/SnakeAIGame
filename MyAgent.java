import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import za.ac.wits.snake.DevelopmentAgent;

public class MyAgent extends DevelopmentAgent {
    private static final int BOARD_SIZE = 51;
    private static final int[] DIRECTIONS_X = { 0, 0, 1, -1 };
    private static final int[] DIRECTIONS_Y = { 1, -1, 0, 0 };
    private static final int MOVE_DOWN = 1;
    private static final int MOVE_UP = 0;
    private static final int MOVE_RIGHT = 3;
    private static final int MOVE_LEFT = 2;

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
            int numObstacles = 3;

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                String apple1 = line;
                String[] appleCoOr = apple1.split(" ");
                int appleX = Integer.parseInt(appleCoOr[0]);
                int appleY = Integer.parseInt(appleCoOr[1]);
                int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
                board[appleX][appleY] = 1;

                for (int j = 0; j < numObstacles; j++) {
                    String obsLine = br.readLine();
                    drawObstacles(board, obsLine);
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                int mySnakeX = 0, mySnakeY = 0;
                List<String> otherSnakeCoOr = new ArrayList<>();
                List<Double> distances = new ArrayList<>();
                List<String> mySnakeCoOr = new ArrayList<>(); // New list to store my snake's body coordinates

                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (i == mySnakeNum) {
                        String[] snakeInfo = snakeLine.split(" ");
                        if (snakeInfo.length > 3) {
                            for (int j = 4; j < snakeInfo.length; j++) {
                                String[] prevSnakeCoOr = snakeInfo[j - 1].split(",");
                                String[] snakeCoOr = snakeInfo[j].split(",");
                                drawSnake(prevSnakeCoOr, snakeCoOr, board);
                                if (j == 4) {
                                    mySnakeX = Integer.parseInt(prevSnakeCoOr[0]);
                                    mySnakeY = Integer.parseInt(prevSnakeCoOr[1]);
                                    mySnakeCoOr.add(prevSnakeCoOr[0] + "," + prevSnakeCoOr[1]); // Store the coordinates of the snake's body
                                    distances.add(Dist(mySnakeX, mySnakeY, appleX, appleY));
                                }
                            }
                        }
                    } else {
                        String[] snakeInfo = snakeLine.split(" ");
                        if (snakeInfo.length > 3) {
                            for (int j = 4; j < snakeInfo.length; j++) {
                                String[] prevSnakeCoOr = snakeInfo[j - 1].split(",");
                                drawSnake(prevSnakeCoOr, snakeInfo[j].split(","), board);
                                if (j == 4) {
                                    distances.add(Dist(Integer.parseInt(prevSnakeCoOr[0]), Integer.parseInt(prevSnakeCoOr[1]), appleX, appleY));
                                    otherSnakeCoOr.add(prevSnakeCoOr[0]);
                                    otherSnakeCoOr.add(prevSnakeCoOr[1]);
                                }
                            }
                        }
                    }
                }

                int move = findNextMove(mySnakeX, mySnakeY, appleX, appleY, board, otherSnakeCoOr, distances, mySnakeCoOr);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    private double Dist(int startX, int startY, int endX, int endY) {
        return Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
    }

    private int findNextMove(int snakeX, int snakeY, int appleX, int appleY, int[][] board, List<String> otherSnakeCoOr, List<Double> distances, List<String> mySnakeCoOr) {
        int[][] pathfindingBoard = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                pathfindingBoard[i][j] = board[i][j];
            }
        }
        pathfindingBoard[appleX][appleY] = 0;
        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();
        openList.add(new Node(snakeX, snakeY, 0, Math.abs(appleX - snakeX) + Math.abs(appleY - snakeY), null));

        while (!openList.isEmpty()) {
            Node current = openList.get(0);
            int currentIndex = 0;
            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).getF() < current.getF()) {
                    current = openList.get(i);
                    currentIndex = i;
                }
            }

            openList.remove(currentIndex);
            closedList.add(current);

            if (current.getX() == appleX && current.getY() == appleY) {
                while (current.getParent() != null && current.getParent().getParent() != null) {
                    current = current.getParent();
                }
                int nextX = current.getX();
                int nextY = current.getY();
                if (nextX == snakeX) {
                    if (nextY == snakeY - 1) {
                        return MOVE_UP;
                    } else {
                        return MOVE_DOWN;
                    }
                } else {
                    if (nextX == snakeX - 1) {
                        return MOVE_LEFT;
                    } else {
                        return MOVE_RIGHT;
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                int nextX = current.getX() + DIRECTIONS_X[i];
                int nextY = current.getY() + DIRECTIONS_Y[i];

                if (nextX >= 0 && nextX < BOARD_SIZE && nextY >= 0 && nextY < BOARD_SIZE
                        && pathfindingBoard[nextX][nextY] != -1) {
                    Node successor = new Node(nextX, nextY, current.getG() + 1,
                            Math.abs(appleX - nextX) + Math.abs(appleY - nextY), current);

                    if (containsNode(closedList, successor) && successor.getG() >= current.getG()) {
                        continue;
                    }

                    if (!containsNode(openList, successor) || successor.getG() < current.getG()) {
                        openList.add(successor);
                    }
                }
            }
        }

        // If no path is found, fall back to the original logic
        int dx = appleX - snakeX;
        int dy = appleY - snakeY;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                if (snakeX < BOARD_SIZE - 1 && board[snakeX + 1][snakeY] != -1
                        && !mySnakeCoOr.contains((snakeX + 1) + "," + snakeY)) {
                    return MOVE_RIGHT;
                } else if (snakeY < BOARD_SIZE - 1 && board[snakeX][snakeY + 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY + 1))) {
                    return MOVE_DOWN;
                } else if (snakeY > 0 && board[snakeX][snakeY - 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY - 1))) {
                    return MOVE_UP;
                }
            } else {
                if (snakeX > 0 && board[snakeX - 1][snakeY] != -1 && !mySnakeCoOr.contains((snakeX - 1) + "," + snakeY)) {
                    return MOVE_LEFT;
                } else if (snakeY > 0 && board[snakeX][snakeY - 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY - 1))) {
                    return MOVE_UP;
                } else if (snakeY < BOARD_SIZE - 1 && board[snakeX][snakeY + 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY + 1))) {
                    return MOVE_DOWN;
                }
            }
        } else {
            if (dy > 0) {
                if (snakeY < BOARD_SIZE - 1 && board[snakeX][snakeY + 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY + 1))) {
                    return MOVE_DOWN;
                } else if (snakeX < BOARD_SIZE - 1 && board[snakeX + 1][snakeY] != -1
                        && !mySnakeCoOr.contains((snakeX + 1) + "," + snakeY)) {
                    return MOVE_RIGHT;
                } else if (snakeX > 0 && board[snakeX - 1][snakeY] != -1
                        && !mySnakeCoOr.contains((snakeX - 1) + "," + snakeY)) {
                    return MOVE_LEFT;
                }
            } else {
                if (snakeY > 0 && board[snakeX][snakeY - 1] != -1
                        && !mySnakeCoOr.contains(snakeX + "," + (snakeY - 1))) {
                    return MOVE_UP;
                } else if (snakeX > 0 && board[snakeX - 1][snakeY] != -1
                        && !mySnakeCoOr.contains((snakeX - 1) + "," + snakeY)) {
                    return MOVE_LEFT;
                } else if (snakeX < BOARD_SIZE - 1 && board[snakeX + 1][snakeY] != -1
                        && !mySnakeCoOr.contains((snakeX + 1) + "," + snakeY)) {
                    return MOVE_RIGHT;
                }
            }
        }

        return -1; // No suitable move found
    }

    private boolean containsNode(List<Node> list, Node node) {
        for (Node n : list) {
            if (n.getX() == node.getX() && n.getY() == node.getY()) {
                return true;
            }
        }
        return false;
    }

    private class Node {
        private int x;
        private int y;
        private int g;
        private int h;
        private Node parent;

        public Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getG() {
            return g;
        }

        public int getH() {
            return h;
        }

        public int getF() {
            return g + h;
        }

        public Node getParent() {
            return parent;
        }
    }


    private int avoidHeadOnCollision(int snakeX, int snakeY, int otherSnakeHeadX, int otherSnakeHeadY, int[][] board) {
        if (snakeX == otherSnakeHeadX) {
            if (snakeY < otherSnakeHeadY && snakeY < BOARD_SIZE - 1 && board[snakeX][snakeY + 1] != -1) {
                return MOVE_DOWN;
            } else if (snakeY > 0 && board[snakeX][snakeY - 1] != -1) {
                return MOVE_UP;
            }
        } else if (snakeY == otherSnakeHeadY) {
            if (snakeX < otherSnakeHeadX && snakeX < BOARD_SIZE - 1 && board[snakeX + 1][snakeY] != -1) {
                return MOVE_RIGHT;
            } else if (snakeX > 0 && board[snakeX - 1][snakeY] != -1) {
                return MOVE_LEFT;
            }
        }
        return -1; 
    }

    private void drawObstacles(int[][] board, String coOr) {
        String[] values = coOr.split(" ");
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        for (String value : values) {
            String[] xy = value.split(",");
            x.add(Integer.parseInt(xy[0]));
            y.add(Integer.parseInt(xy[1]));
        }
        if (x.get(0).equals(x.get(1))) {
            for (Integer yi : y) {
                board[x.get(0)][yi] = -1;
            }
        } else if (y.get(0).equals(y.get(1))) {
            for (Integer xi : x) {
                board[xi][y.get(0)] = -1;
            }
        }
    }

    private void drawSnake(String[] xy1, String[] xy2, int[][] board) {
        int minx = Math.min(Integer.parseInt(xy1[0]), Integer.parseInt(xy2[0]));
        int maxx = Math.max(Integer.parseInt(xy1[0]), Integer.parseInt(xy2[0]));
        int miny = Math.min(Integer.parseInt(xy1[1]), Integer.parseInt(xy2[1]));
        int maxy = Math.max(Integer.parseInt(xy1[1]), Integer.parseInt(xy2[1]));

        for (int i = minx; i <= maxx; i++) {
            for (int j = miny; j <= maxy; j++) {
                board[i][j] = -1;
            }
        }
    }
}
