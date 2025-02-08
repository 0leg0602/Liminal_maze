package project;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static project.Game.DEFAULT_SIZE;
import static project.Game.MyPanel.Ray;
import static project.Game.MyPanel.fix_angle;
import static project.Game.MyPanel.maps;
import static project.Game.MyPanel.textures;
import static project.Game.endless_maze_prev_rand_shape;
import static project.Game.endless_maze_rand_shape;
import static project.Game.player;
import static project.Game.screen_scale;
import static project.Game.window_height;
import static project.Game.window_width;
import static project.Game.settings;

public class Render {
    public static final int FOV = 60;
    static Font text_font;

    BufferedImage image_to_draw;

    {
        try {
            text_font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResource("/fonts/TurretRoad-Regular.ttf")).openStream());
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void draw_rays(Graphics g, int scale, double rays_multiplier) {
        // Draw the rays

        int width = DEFAULT_SIZE[0];
        int height = DEFAULT_SIZE[1];

        image_to_draw = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);


        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.scale(screen_scale, screen_scale);

        ArrayList<int[]> rendered_wall_cells = new ArrayList<int[]>();
        ArrayList<int[]> rendered_floor_cells = new ArrayList<int[]>();

        int number_of_ray;
        int number_of_rays_limit = (int) (FOV * Game.MyPanel.rays_multiplier);
        int map_x;
        int map_y;
        int depth_of_field;

        int depth_of_field_limit = 50;


        double deg = Math.PI / 180;

        // Horizontal ray
        Ray hr = new Ray();
        Ray vr = new Ray();


        hr.angle = player.angle - deg * FOV / 2;
        vr.angle = player.angle - deg * FOV / 2;


        if (hr.angle < 0 || vr.angle < 0) {
            hr.angle += Math.PI * 2;
            vr.angle += Math.PI * 2;
        }

        if (hr.angle > Math.PI * 2 || vr.angle > Math.PI * 2) {
            hr.angle -= Math.PI * 2;
            vr.angle -= Math.PI * 2;
        }


        for (number_of_ray = 0; number_of_ray <= number_of_rays_limit; number_of_ray++) {

            int[] current_map_cell;
            boolean shade = false;

            int[] vertical_current_map_cell = new int[]{0, 0};
            int[] horizontal_current_map_cell = new int[]{0, 0};

            // Check horizontal lines
            depth_of_field = 0;
            double aTan = -1 / (Math.tan(hr.angle));

            if (hr.angle > Math.PI) { //Looking up
                hr.y = (((int) (player.y / scale)) * scale) - 0.0001;
                hr.x = (player.y - hr.y) * aTan + player.x;
                hr.offset_y = -scale;
                hr.offset_x = -hr.offset_y * aTan;
            } else if (hr.angle < Math.PI) { //Looking down
                hr.y = (((int) (player.y / scale)) * scale) + scale;
                hr.x = (player.y - hr.y) * aTan + player.x;
                hr.offset_y = scale;
                hr.offset_x = -hr.offset_y * aTan;
            } else if (hr.angle == 0 || hr.angle == Math.PI) { //Looking right or left
                hr.x = player.x;
                hr.y = player.y;
                depth_of_field = depth_of_field_limit;
            }
            while (depth_of_field < depth_of_field_limit) {
                map_x = (int) (hr.x) / scale;
                map_y = (int) (hr.y) / scale;

                if (map_y < maps.main_map.length && map_x < maps.main_map[0].length && map_y >= 0 && map_x >= 0 && maps.main_map[map_y][map_x] != 0) {

                    vertical_current_map_cell[0] = map_y;
                    vertical_current_map_cell[1] = map_x;

                    depth_of_field = depth_of_field_limit;
                } else {
                    hr.x += hr.offset_x;
                    hr.y += hr.offset_y;
                    depth_of_field += 1;
                }

            }

            // Check vertical lines

            double TOTC = Math.PI / 2; //Top of the circle
            double BOTC = 3 * Math.PI / 2; //Bottom of the circle

            depth_of_field = 0;
            double nTan = -1 * Math.tan(vr.angle);
            if (vr.angle > TOTC && vr.angle < BOTC) { //Looking left

                vr.x = (((int) (player.x / scale)) * scale) - 0.0001;
                vr.y = (player.x - vr.x) * nTan + player.y;
                vr.offset_x = -scale;
                vr.offset_y = -vr.offset_x * nTan;

            } else if (vr.angle < TOTC || vr.angle > BOTC) { //Looking right

                vr.x = (((int) (player.x / scale)) * scale) + scale;
                vr.y = (player.x - vr.x) * nTan + player.y;
                vr.offset_x = scale;
                vr.offset_y = -vr.offset_x * nTan;

            } else if (vr.angle == 0 || vr.angle == Math.PI) { //Looking up or down

                vr.x = player.x;
                vr.y = player.y;
                depth_of_field = depth_of_field_limit;

            }
            while (depth_of_field < depth_of_field_limit) {
                map_x = (int) (vr.x) / scale;
                map_y = (int) (vr.y) / scale;

                if (map_y < maps.main_map.length && map_x < maps.main_map[0].length && map_y >= 0 && map_x >= 0 && maps.main_map[map_y][map_x] != 0) {

                    horizontal_current_map_cell[0] = map_y;
                    horizontal_current_map_cell[1] = map_x;

                    depth_of_field = depth_of_field_limit;
                } else {
                    vr.x += vr.offset_x;
                    vr.y += vr.offset_y;
                    depth_of_field += 1;
                }


            }

            double shortest_ray_x;
            double shortest_ray_y;
            double shortest_ray_angle;
            double shortest_ray_distance;

            if (Math.sqrt(Math.pow(hr.x - player.x, 2) + Math.pow(hr.y - player.y, 2)) < Math.sqrt(Math.pow(vr.x - player.x, 2) + Math.pow(vr.y - player.y, 2))) {
                shortest_ray_x = hr.x;
                shortest_ray_y = hr.y;
                shortest_ray_angle = hr.angle;
                shortest_ray_distance = Math.sqrt(Math.pow(hr.x - player.x, 2) + Math.pow(hr.y - player.y, 2));

                shade = true;
                current_map_cell = vertical_current_map_cell;

            } else {
                shortest_ray_x = vr.x;
                shortest_ray_y = vr.y;
                shortest_ray_angle = vr.angle;
                shortest_ray_distance = Math.sqrt(Math.pow(vr.x - player.x, 2) + Math.pow(vr.y - player.y, 2));


                current_map_cell = horizontal_current_map_cell;

            }


            //Draw walls


            double player_to_ray_angle = player.angle - shortest_ray_angle;

            if (player_to_ray_angle < 0) {
                player_to_ray_angle += Math.PI * 2;
            }

            if (player_to_ray_angle > Math.PI * 2) {
                player_to_ray_angle -= Math.PI * 2;
            }

//            Fixing fish eye
            shortest_ray_distance = shortest_ray_distance * Math.cos(player_to_ray_angle);


            double lineH = height / (shortest_ray_distance / scale);


            double segment_length = lineH / 32;

            int wall_x = number_of_ray * width / number_of_rays_limit;

            Color pixel_color = Color.BLACK;
            Color prev_floor_pixel_color = Color.BLACK;
            Color prev_ceil_pixel_color = Color.BLACK;


            for (int segment = 0; segment < 32; segment++) {

                int texture_x;

                if (shade) {
                    texture_x = (int) ((shortest_ray_x / 2) % 32);

                    if (shortest_ray_angle < Math.PI) {
                        texture_x = 31 - texture_x;
                    }

                } else {
                    texture_x = (int) ((shortest_ray_y / 2) % 32);

                    if (shortest_ray_angle > TOTC && shortest_ray_angle < BOTC) {
                        texture_x = 31 - texture_x;
                    }

                }

                int current_map_element = maps.main_map[current_map_cell[0]][current_map_cell[1]];

                rendered_wall_cells.add(current_map_cell);


                if (current_map_element != -1) {


                    BufferedImage wallTexture = get_tile_image("wall", current_map_element);
                    pixel_color = new Color(wallTexture.getRGB(texture_x, segment));

                    if (current_map_cell[0] == 11 && (current_map_cell[1] == 3 || current_map_cell[1] == 9)) {
                        BufferedImage shape_image;
                        if (current_map_cell[1] == 3) {
                            shape_image = get_shape_image(endless_maze_rand_shape);
                        } else {
                            shape_image = get_shape_image(endless_maze_prev_rand_shape);
                        }
                        if (shape_image != null) {

                            int shape_pixel_color = shape_image.getRGB(texture_x, segment);
                            if (shape_pixel_color != 0) {

                                pixel_color = new Color(shape_pixel_color);

                            }
                        }
                    }

//                    Add shading to the game, after I added torch, shading was no longer necessary

//                    if (shade) {
//                        pixel_color = pixel_color.darker();
//                    }

                    double distance_factor = shortest_ray_distance / 100;

                    if (distance_factor > 1) {
                        if (current_map_element != 5) {
                            pixel_color = get_color_darker(pixel_color, 0.97 / distance_factor);
                        }
                    }

                    if (current_map_cell[0] > 39) {
                        int count_down = 54 - current_map_cell[0];
                        if (count_down > -1) {
                            int white_factor = 255 - (count_down * 9);
                            pixel_color = new Color(white_factor, white_factor, white_factor);
                        }
                    }

                    int wall_y1 = ((int) ((double) height / 2 - (lineH / 2) + segment_length * segment));
                    int wall_y2 = (int) (wall_y1 + segment_length);
                    if (wall_y1 < image_to_draw.getHeight() && wall_x < image_to_draw.getWidth()) {
                        while (wall_y1 < 0) {
                            wall_y1++;
                        }


                        for (int j = 0; j < (width / number_of_rays_limit + 1); j++) {
                            for (int i = 0; i <= wall_y2 - wall_y1; i++) {
                                if (wall_y1 + i < image_to_draw.getHeight()) {
                                    image_to_draw.setRGB(wall_x + j, wall_y1 + i, pixel_color.getRGB());
                                } else {
                                    break;
                                }
                            }


                        }


                    }
                }
            }


//            Draw floors
            for (int y = (int) (height / 2 + lineH / 2); y < height; y++) {
                double dy = y - (height / 2.0);
                double raFix = Math.cos(fix_angle(player.angle - shortest_ray_angle));

                int floor_scale = height * scale / 2;

                double tx = player.x / 2 + Math.cos(shortest_ray_angle) * floor_scale / 2 / dy / raFix + 1;
                double ty = -player.y / 2 - Math.sin(shortest_ray_angle) * floor_scale / 2 / dy / raFix - 1;


                int floor_map_x = (int) ((tx - 1) / (scale / 2));
                int floor_map_y = (int) (-(ty + 1) / (scale / 2));

                if (floor_map_x == -1 || floor_map_y == -1 || floor_map_y >= maps.floor_map.length || floor_map_x >= maps.floor_map[0].length) {
                    continue;
                }

                BufferedImage floor_texture = get_tile_image("floor", maps.floor_map[floor_map_y][floor_map_x]);
                BufferedImage ceil_texture = textures.stone_ceil_tile_image;

                int textureWidth = floor_texture.getWidth();
                int textureHeight = floor_texture.getHeight();


                rendered_floor_cells.add(new int[]{floor_map_y, floor_map_x});


                int txn = (int) (-tx % textureWidth);
                if (txn < 0) {
                    txn += textureWidth;
                }
                int tyn = (int) (ty % textureHeight);
                if (tyn < 0) {
                    tyn += textureHeight;
                }


                double distance_factor = 100 / dy;

                Color floor_pixel_color;

                if (maps.floor_map[floor_map_y][floor_map_x] != 0) {
                    floor_pixel_color = new Color(floor_texture.getRGB(txn, tyn));

                    if (distance_factor > 1) {
                        floor_pixel_color = get_color_darker(floor_pixel_color, 0.97 / distance_factor);
                    }
                } else {
                    floor_pixel_color = prev_floor_pixel_color;
                }

                if (current_map_cell[0] > 39) {
                    int count_down = 54 - current_map_cell[0];
                    if (count_down > -1) {
                        int white_factor = 255 - (count_down * 9);
                        floor_pixel_color = new Color(white_factor, white_factor, white_factor);
                    } else {
                        floor_pixel_color = Color.WHITE;
                    }
                }


                prev_floor_pixel_color = floor_pixel_color;


                if (wall_x < image_to_draw.getWidth()) {
                    for (int j = 0; j < (width / number_of_rays_limit + 1); j++) {
                        image_to_draw.setRGB(wall_x + j, y, floor_pixel_color.getRGB());
                    }
                }

                Color ceil_pixel_color;

                if (maps.ceil_map[floor_map_y][floor_map_x] != 0) {
                    ceil_pixel_color = new Color(ceil_texture.getRGB(txn, tyn));

                    if (distance_factor > 1) {
                        ceil_pixel_color = get_color_darker(ceil_pixel_color, 0.97 / distance_factor);
                    }
                } else {
                    ceil_pixel_color = prev_ceil_pixel_color;
                }

                if (current_map_cell[0] > 39) {
                    int count_down = 54 - current_map_cell[0];
                    if (count_down > -1) {
                        int white_factor = 255 - (count_down * 9);
                        ceil_pixel_color = new Color(white_factor, white_factor, white_factor);
                    } else {
                        ceil_pixel_color = Color.WHITE;
                    }
                }

                prev_ceil_pixel_color = ceil_pixel_color;

                if (wall_x < image_to_draw.getWidth()) {
                    for (int j = 0; j < (width / number_of_rays_limit + 1); j++) {
                        image_to_draw.setRGB(wall_x + j, height - y, ceil_pixel_color.getRGB());
                    }
                }


            }

            hr.angle += deg / rays_multiplier;
            vr.angle += deg / rays_multiplier;

            hr.angle = fix_angle(hr.angle);
            vr.angle = fix_angle(vr.angle);

//            Debugging for player rays collision

//                g2.setStroke(new BasicStroke(1));
//                g2.setColor(Color.GREEN);
//                g2.drawLine((int) player.x, (int) player.y, (int) shortest_ray_x, (int) shortest_ray_y);


        }

        boolean player_looked_away1 = true;
        for (int[] cell : rendered_floor_cells) {

            if (cell[0] == 1 && cell[1] == 16) {
                player_looked_away1 = false;
                break;
            }

        }

        if (player_looked_away1 && player.x / scale > 17) {
            maps.main_map[1][16] = 1;
        }

        int offset = (int) (((double) window_height / 2) - ((height * screen_scale) / 2));
        g2.drawImage(image_to_draw, 0, (int) (offset / screen_scale), null);
    }

    private BufferedImage get_shape_image(int i) {
        BufferedImage texture = null;
        if (i == 1) {
            texture = textures.shape1_image;
        } else if (i == 2) {
            texture = textures.shape2_image;
        } else if (i == 3) {
            texture = textures.shape3_image;
        } else if (i == 4) {
            texture = textures.shape4_image;
        } else if (i == -1) {
            texture = textures.shape_wrong_image;
        }
        return texture;
    }

    private static BufferedImage get_tile_image(String tile_set, int current_map_element) {
        BufferedImage texture = textures.stone_floor_image;
        if (tile_set.equals("wall")) {
            if (current_map_element == 1) {
                texture = textures.brick_wall_image;
            } else if (current_map_element == 2) {
                texture = textures.brick_wall_door_image;
            } else if (current_map_element == 3) {
                texture = textures.brick_wall_key_image;
            } else if (current_map_element == 4) {
                texture = textures.brick_wall_watch_image;
            } else if (current_map_element == 5) {
                texture = textures.last_door_image;
            }
        }
        if (tile_set.equals("floor")) {
            if (current_map_element == 1) {
                texture = textures.stone_floor_image;
            } else if (current_map_element == 2) {
                texture = textures.stone_floor_tile_image;
            } else if (current_map_element == 3) {
                texture = textures.stone_floor_one_tile_image;
            } else if (current_map_element == 4) {
                texture = textures.stone_floor_one_tile_fall_image;
            }
        }
        return texture;
    }

    public Color get_color_darker(Color color, double factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0),
                color.getAlpha());
    }

    public void draw_map(Graphics g, int scale) {

        Graphics2D g2;
        g2 = (Graphics2D) g;

        //            Drawing the map
        int x, y;
        for (y = 0; y < maps.main_map.length; y++) {
            for (x = 0; x < maps.main_map[0].length; x++) {
                if (maps.main_map[y][x] == 0) {
                    g2.setColor(Color.BLACK);
                } else {
                    g2.setColor(Color.WHITE);

                }
                g2.fillRect(x * scale / 4, y * scale / 4, scale / 4 - 1, scale / 4 - 1);


            }

        }
    }

    public void draw_player(Graphics g) {

        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setColor(Color.YELLOW);

        g2.setStroke(new BasicStroke(1));

        g2.fillRect((int) player.x / 4 - 5 / 4, (int) player.y / 4 - 5 / 4, 10 / 4, 10 / 4);
        g2.drawLine((int) player.x / 4, (int) player.y / 4, (int) (player.x / 4 + player.delta_x * 10 / 4), (int) (player.y / 4 + player.delta_y * 10 / 4));

    }

    public void draw_menu(Graphics g, ArrayList<Game.MyPanel.Button> buttons, boolean is_started) {

        if (Game.is_paused) {


            Graphics2D g2;
            g2 = (Graphics2D) g;

            g2.setColor(new Color(0, 0, 0, 90));

            g2.fillRect(0, 0, window_width, window_height);

//            remove all buttons

            for (int i = 0; i <= 4; i++) {
                draw_ui_element(g, buttons, " ", 1, -100, i);
            }

            if (!Game.gameover) {
                if(settings){
                    draw_ui_element(g, buttons, "Settings", window_width / 15, 0, 0);

                    draw_ui_element(g, buttons, "Fullscreen", window_width / 15, 20, 4);
                    draw_ui_element(g, buttons, "\"-\" \"+\" to change resolution", window_width / 20, 40, 0);
                    draw_ui_element(g, buttons, "\"[\" \"]\" to change mouse sensitivity", window_width / 20, 50, 0);
                    draw_ui_element(g, buttons, "Back", window_width / 15, 70, 2);
//                    draw_ui_element(g, buttons, "EXIT", window_width / 15, 70, 3);
                } else {
                    draw_ui_element(g, buttons, "Liminal maze", window_width / 10, 0, 0);
                    String second_button_text = "PLAY";
                    if (is_started) {
                        second_button_text = "CONTINUE";
                    }
                    draw_ui_element(g, buttons, second_button_text, window_width / 15, 30, 1);
                    draw_ui_element(g, buttons, "Settings", window_width / 15, 50, 2);
                    draw_ui_element(g, buttons, "EXIT", window_width / 15, 70, 3);
                }

            } else {
                draw_ui_element(g, buttons, "YOU ESCAPED", window_width / 8, 0, 0);
                draw_ui_element(g, buttons, "it took you " + Game.end_time / 1000 + "s to escape", window_width / 20, 40, 0);
                draw_ui_element(g, buttons, "EXIT", window_width / 15, 70, 3);
            }


        }

    }

    public Color get_black_or_white(boolean bool) {
        if (bool) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    public void draw_ui_element(Graphics g, ArrayList<Game.MyPanel.Button> buttons, String text, int font_size, int start, int index) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke((float) (3 * screen_scale)));

        Font font = text_font.deriveFont(Font.PLAIN, font_size);
        FontMetrics font_metrics = g2.getFontMetrics(font);

        g2.setColor(Color.BLACK);
        g2.setFont(font);


        int text_x = window_width / 2 - font_metrics.stringWidth(text) / 2;
        double text_y = font_metrics.getHeight() + ((double) window_height / 100) * start;

        int offset = window_width / 100;

        Rectangle2D text_rect = font_metrics.getStringBounds(text, g);

        Rectangle button_rect = new Rectangle(text_x - offset, (int) (text_y - (int) text_rect.getHeight() + offset), (int) text_rect.getWidth() + offset * 2, (int) text_rect.getHeight() + offset);

        Game.MyPanel.Button button;

        if (buttons.size() <= index) {
            String action = switch (index) {
                case 1 -> "play";
                case 2 -> "settings";
                case 3 -> "exit";
                case 4 -> "fullscreen";
                default -> "none";
            };

            buttons.add(new Game.MyPanel.Button(button_rect, action));

        }

        button = buttons.get(index);
        button.rect = button_rect;


        if (index != 0) {

            g2.setColor(get_black_or_white(!button.is_hovered));

            g2.draw(button_rect);

            g2.setColor(get_black_or_white(button.is_hovered));

            g2.fill(button_rect);

            g2.setColor(get_black_or_white(!button.is_hovered));

        } else {
            g2.setColor(Color.BLACK);
            g2.fill(button_rect);
            g2.setColor(Color.WHITE);
        }

        g2.drawString(text, text_x, (int) text_y);

    }

    public void draw_torch(Graphics g, int torch_state) {


        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.scale(1 / screen_scale, 1 / screen_scale);

        BufferedImage torch;

        switch (torch_state) {
            case 1 -> torch = textures.torch2_image;
            case 2 -> torch = textures.torch3_image;
            default -> torch = textures.torch1_image;

        }


        int torch_scale = 10;

        int torch_width = (int) (torch.getWidth() * torch_scale * screen_scale);
        int torch_height = (int) (torch.getHeight() * torch_scale * screen_scale);

        int torch_y = (int) ((double) (window_height / 2) + (((double) DEFAULT_SIZE[1] / 2) * screen_scale)) - torch_height;


        g2.drawImage(torch, 0, torch_y, torch_width, torch_height, null);
    }

    public void draw_cursor(Graphics g, boolean can_interact) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        if (!can_interact) {
            g2.setColor(Color.WHITE);
            g2.fillOval(window_width / 2 - 5, window_height / 2 - 5, 10, 10);
        } else {
            g2.drawImage(textures.hand_image, window_width / 2 - (50 / 2), window_height / 2 - (50 / 2), 50, 50, null);
        }


    }

    public void draw_watch(Graphics g, int golden_watch) {

        Graphics2D g2;
        g2 = (Graphics2D) g;

        boolean watch_red = true;

        if (golden_watch > 360) {
            golden_watch -= 360;
            watch_red = false;
        }

        BufferedImage watch;

        if (watch_red) {
            watch = textures.watch_red_image;
        } else {
            watch = textures.watch_green_image;
        }


        int watch_scale = 1;

        int watch_width = (int) (watch.getWidth() * watch_scale * screen_scale);
        int watch_height = (int) (watch.getHeight() * watch_scale * screen_scale);

        int arrow_length = (int) (50 * screen_scale);

        double arrow_delta_x = Math.cos(Math.toRadians(golden_watch - 90)) * arrow_length;
        double arrow_delta_y = Math.sin(Math.toRadians(golden_watch - 90)) * arrow_length;

        int watch_x = window_width - watch_width;
        int watch_y = (int) ((double) (window_height / 2) + (((double) DEFAULT_SIZE[1] / 2) * screen_scale)) - watch_height;

        g2.drawImage(watch, watch_x, watch_y, watch_width, watch_height, null);

        g2.setStroke(new BasicStroke((float) (3 * screen_scale)));

        int watch_center_x = watch_x + watch_width / 2;
        int watch_center_y = (int) ((watch_y + (double) watch_height / 2) + 11 * screen_scale);

        g2.drawLine((int) (watch_center_x + arrow_delta_x), (int) (watch_center_y + arrow_delta_y), watch_center_x, watch_center_y);
    }

    public void draw_message(Graphics g, String message, long message_timer) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        double alpha = (double) (System.currentTimeMillis() - message_timer) / 10;

        if (alpha < 255) {

            int alpha_color = (int) (255 - alpha);

            g2.setStroke(new BasicStroke((float) (3 * screen_scale)));

            Font font = text_font.deriveFont(Font.PLAIN, (float) (70 * screen_scale));
            FontMetrics font_metrics = g2.getFontMetrics(font);

            g2.setFont(font);


            int text_x = window_width / 2 - font_metrics.stringWidth(message) / 2;
            double text_y = (double) window_height / 2 + (double) font_metrics.getHeight() / 2;

            Rectangle2D text_rect = font_metrics.getStringBounds(message, g);

            int offset = window_width / 100;

            Rectangle button_rect = new Rectangle(text_x - offset, (int) (text_y - (int) text_rect.getHeight() + offset), (int) text_rect.getWidth() + offset * 2, (int) text_rect.getHeight() + offset);


            g2.setColor(new Color(0, 0, 0, alpha_color));

            g2.draw(button_rect);


            g2.setColor(new Color(255, 255, 255, alpha_color));

            g2.fill(button_rect);

            g2.setColor(new Color(0, 0, 0, alpha_color));

            g2.drawString(message, text_x, (int) text_y);
        }
    }
}
