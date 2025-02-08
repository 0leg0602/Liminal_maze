package project;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Textures {

    BufferedImage brick_wall_image;
    BufferedImage brick_wall_door_image;
    BufferedImage brick_wall_key_image;
    BufferedImage brick_wall_watch_image;

    BufferedImage stone_floor_tile_image;
    BufferedImage stone_floor_one_tile_image;
    BufferedImage stone_floor_one_tile_fall_image;
    BufferedImage stone_floor_image;
    BufferedImage stone_ceil_tile_image;


    BufferedImage torch1_image;
    BufferedImage torch2_image;
    BufferedImage torch3_image;


    BufferedImage shape1_image;
    BufferedImage shape2_image;
    BufferedImage shape3_image;
    BufferedImage shape4_image;
    BufferedImage shape_wrong_image;

    BufferedImage hand_image;

    BufferedImage watch_red_image;
    BufferedImage watch_green_image;


    BufferedImage last_door_image;



    public Textures() {
        try {
            brick_wall_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall.png")));
            brick_wall_door_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall_door.png")));
            brick_wall_key_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall_key.png")));
            brick_wall_watch_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/brick_wall_watch.png")));

            stone_floor_tile_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/stone_floor_tile.png")));
            stone_floor_one_tile_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/stone_floor_one_tile.png")));
            stone_floor_one_tile_fall_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/stone_floor_one_tile_fall.png")));
            stone_floor_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/stone_floor.png")));
            stone_ceil_tile_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/stone_ceiling_tile.png")));

            torch1_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/torches/torch1.png")));
            torch2_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/torches/torch2.png")));
            torch3_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/torches/torch3.png")));

            shape1_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/shape1.png")));
            shape2_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/shape2.png")));
            shape3_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/shape3.png")));
            shape4_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/shape4.png")));
            shape_wrong_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/shape_wrong.png")));

            hand_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/hand.png")));


            watch_green_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/watch_green.png")));
            watch_red_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/watch_red.png")));


            last_door_image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/last_door.png")));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
