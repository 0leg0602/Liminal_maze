package project;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Maps {
    int[][] main;
    public Maps(){
        BufferedImage main_map_image;
        try {
            main_map_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/main_map.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        main = new int[main_map_image.getHeight()][main_map_image.getWidth()];
//        System.out.println(Arrays.deepToString(main));

        for (int y = 0; y < main.length; y++) {
            for (int x = 0; x < main[0].length; x++) {
                Color pixel_color = new Color(main_map_image.getRGB(x, y));
                if(pixel_color.equals(Color.WHITE)){
                main[y][x]=1;
                } else if (pixel_color.equals(Color.RED)){
                    main[y][x]=2;
                }
            }
        }

    }
}
