import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class run2 {

    static class Coord {
        int row, col;
        Coord(int r, int c) {
            this.row = r;
            this.col = c;
        }
    }

    static class GameState {
        int[] bots;
        int keys;

        GameState(int[] positions, int keys) {
            this.bots = Arrays.copyOf(positions, 4);
            this.keys = keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GameState)) return false;
            GameState other = (GameState) obj;
            return Arrays.equals(this.bots, other.bots) && this.keys == other.keys;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bots) * 31 + keys;
        }
    }

    private static char[][] readMap() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<char[]> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            lines.add(line.toCharArray());
        }
        return lines.toArray(new char[0][]);
    }

    private static List<int[]> discoverKeys(char[][] grid, int row, int col, int keyMask, Map<Character, Integer> keyIndexMap, int rows, int cols) {
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> q = new ArrayDeque<>();
        List<int[]> found = new ArrayList<>();

        q.add(new int[]{row, col, 0});
        visited[row][col] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1], steps = cur[2];
            char ch = grid[r][c];

            if (ch >= 'a' && ch <= 'z') {
                int keyBit = 1 << keyIndexMap.get(ch);
                if ((keyMask & keyBit) == 0) {
                    found.add(new int[]{ch, keyIndexMap.get(ch), steps});
                    continue;
                }
            }

            for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                if (visited[nr][nc] || grid[nr][nc] == '#') continue;

                char next = grid[nr][nc];
                if (next >= 'A' && next <= 'Z') {
                    int doorBit = 1 << (next - 'A');
                    if ((keyMask & doorBit) == 0) continue;
                }

                visited[nr][nc] = true;
                q.add(new int[]{nr, nc, steps + 1});
            }
        }

        return found;
    }

    public static int min_steps_to_collect_all_keys(char[][] grid) {
        int rows = grid.length, cols = grid[0].length;

        List<Coord> starts = new ArrayList<>();
        Map<Character, Coord> keyLocs = new HashMap<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = grid[r][c];
                if (ch == '@') {
                    starts.add(new Coord(r, c));
                } else if (ch >= 'a' && ch <= 'z') {
                    keyLocs.put(ch, new Coord(r, c));
                }
            }
        }

        List<Character> keyList = new ArrayList<>(keyLocs.keySet());
        Collections.sort(keyList);
        Map<Character, Integer> keyBitMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            keyBitMap.put(keyList.get(i), i);
        }

        int goalMask = (1 << keyList.size()) - 1;
        int[] initBots = new int[4];
        for (int i = 0; i < 4; i++) {
            Coord pos = (i < starts.size()) ? starts.get(i) : new Coord(-1, -1);
            initBots[i] = pos.row * cols + pos.col;
        }

        GameState start = new GameState(initBots, 0);
        Map<GameState, Integer> seen = new HashMap<>();
        PriorityQueue<GameState> pq = new PriorityQueue<>(Comparator.comparingInt(seen::get));
        seen.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            GameState state = pq.poll();
            int currentSteps = seen.get(state);
            if (state.keys == goalMask) return currentSteps;

            for (int i = 0; i < 4; i++) {
                int pos = state.bots[i];
                if (pos < 0) continue;
                int r = pos / cols, c = pos % cols;
                List<int[]> keys = discoverKeys(grid, r, c, state.keys, keyBitMap, rows, cols);

                for (int[] k : keys) {
                    char keyChar = (char) k[0];
                    int keyBit = 1 << k[1];
                    int stepsToKey = k[2];

                    int[] newBots = Arrays.copyOf(state.bots, 4);
                    Coord to = keyLocs.get(keyChar);
                    newBots[i] = to.row * cols + to.col;

                    GameState next = new GameState(newBots, state.keys | keyBit);
                    int totalSteps = currentSteps + stepsToKey;

                    if (!seen.containsKey(next) || seen.get(next) > totalSteps) {
                        seen.put(next, totalSteps);
                        pq.add(next);
                    }
                }
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {
        char[][] grid = readMap();
        int answer = min_steps_to_collect_all_keys(grid);
        System.out.println(answer == -1 ? "No solution found" : answer);
    }
}
