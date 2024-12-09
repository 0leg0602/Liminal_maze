package project;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Textures {

    BufferedImage brick_wall_image;
    BufferedImage brick_wall_door_image;

    public Textures(){
        try {
            brick_wall_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall.png")));
            brick_wall_door_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall_door.png")));
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

}
