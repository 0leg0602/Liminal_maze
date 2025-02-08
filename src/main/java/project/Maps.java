package project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class Maps {
    int[][] main_map;
    int[][] floor_map;
    int[][] ceil_map;


    public Maps(){

        main_map = int_array_from_tile_set(Objects.requireNonNull(getClass().getResourceAsStream("/Tile_sets/_main.csv")));
        floor_map = int_array_from_tile_set(Objects.requireNonNull(getClass().getResourceAsStream("/Tile_sets/_floor.csv")));
        ceil_map = int_array_from_tile_set(Objects.requireNonNull(getClass().getResourceAsStream("/Tile_sets/_ceiling.csv")));
    }

    public int[][] int_array_from_tile_set(InputStream input_stream){

        String tile_set_csv = get_string(input_stream);

        String[] rows = tile_set_csv.split("\\r?\\n");

        int width = rows[0].split(",").length;

        int[][] cells = new int[rows.length][width];

        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = Integer.parseInt(rows[i].split(",")[j])+1;
            }
        }
        return cells;
    }

    private static String get_string(InputStream input_stream) {
        String tile_set_csv = "";

        try(BufferedReader br = new BufferedReader(new InputStreamReader(input_stream))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            tile_set_csv = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tile_set_csv;
    }
}
