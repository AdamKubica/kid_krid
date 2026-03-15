package lab;

public class Levels {

    public static boolean[][] getLevelMap(int level, int cols, int rows) {
        boolean[][] map = new boolean[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = true;
            }
        }

        switch (level) {
            case 1:
                break;

            case 2:
                for (int r = 1; r < rows - 1; r++) {
                    for (int c = 1; c < cols - 1; c++) {
                        map[r][c] = false;
                    }
                }
                break;

            case 3:
                if (rows > 2) {
                    int midR = rows / 2;
                    for (int c = 1; c < cols - 1; c++) {
                        map[midR][c] = false;
                    }
                }
                break;

            case 4:
                for (int c = 1; c < cols; c += 2) {
                    for (int r = 1; r < rows - 1; r++) {
                        map[r][c] = false;
                    }
                }
                break;

            case 5:
                map[0][0] = false;
                map[0][1] = false;
                map[1][0] = false;

                map[0][cols - 1] = false;
                map[0][cols - 2] = false;
                map[1][cols - 1] = false;

                map[rows - 1][0] = false;
                map[rows - 1][1] = false;
                map[rows - 2][0] = false;

                map[rows - 1][cols - 1] = false;
                map[rows - 1][cols - 2] = false;
                map[rows - 2][cols - 1] = false;
                break;

            default:
                if (level > 5) {
                    map[rows / 2][cols / 2] = false;
                }
                break;
        }

        return map;
    }
}
