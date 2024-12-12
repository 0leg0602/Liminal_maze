package project;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static project.Game.MyPanel.*;

public class Render {

    //    static Font text_font = new Font("Arial", Font.PLAIN, 46);
    static Font text_font;

    {
        try {
            text_font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResource("/fonts/TurretRoad-Regular.ttf")).openStream());
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void draw_rays(Graphics g, Game.MyPanel.Player player, int scale, int width, int height, double rays_multiplier) {
        // Draw the rays

        Graphics2D g2;
        g2 = (Graphics2D) g;

        ArrayList<int[]> rendered_map_cells = new ArrayList<int[]>();

        int number_of_ray;
        int number_of_rays_limit = (int) (60 * Game.MyPanel.rays_multiplier);
        int map_x;
        int map_y;
        int depth_of_field;

        int depth_of_field_limit = 50;


        double deg = Math.PI / 180;

        // Horizontal ray
        Game.MyPanel.Ray hr = new Game.MyPanel.Ray();
        Game.MyPanel.Ray vr = new Game.MyPanel.Ray();


        hr.angle = player.angle - deg * 30;
        vr.angle = player.angle - deg * 30;


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
            shortest_ray_distance = shortest_ray_distance * Math.cos(player_to_ray_angle);


            double lineH = height / (shortest_ray_distance / scale);


            g2.setStroke(new BasicStroke(((float) width / number_of_rays_limit) + 1));

            double segment_length = lineH / 32;

            int wall_x = number_of_ray * width / number_of_rays_limit;


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

                rendered_map_cells.add(current_map_cell);

                if (current_map_element != 0) {

                    Color pixel_color;

                    BufferedImage texture = get_buffered_image(current_map_element);
                    pixel_color = new Color(texture.getRGB(texture_x, segment));

                    if (shade) {
                        pixel_color = pixel_color.darker();
                    }
                    g2.setColor(pixel_color);

                    int wall_y1 = ((int) ((double) height / 2 - (lineH / 2) + segment_length * segment));
                    int wall_y2 = (int) (wall_y1 + segment_length);
                    g2.drawLine(wall_x, wall_y1, wall_x, wall_y2);
                }
            }

//            g2.setColor(Color.green);
//            g2.drawLine(wall_x, (int) ((double) height /2+lineH/2), wall_x, height);
//
//            g2.setColor(Color.blue);
//            g2.drawLine(wall_x, (int) ((double) height /2-lineH/2), wall_x, 0);
//            Draw floors

//            System.out.println(shortest_ray_angle);

//            for(int y = (int) ((double) height /2+lineH/2); y < height; y++)
//            {
//                double dy=y-(height/2.0);
//                double raFix =Math.cos(fix_angle(player.angle-shortest_ray_angle));
////                double tx=player.x/2 + Math.cos(shortest_ray_angle)*158*scale/2/dy/raFix;
////                double ty=player.y/2 - Math.sin(shortest_ray_angle)*158*scale/2/dy/raFix;
//
//
//                double tx=player.x/2 + Math.cos(shortest_ray_angle)*158*scale/2/dy/raFix;
//                double ty=player.y/2 - Math.sin(shortest_ray_angle)*158*scale/2/dy/raFix;
//
////                int mp=mapF[(int)(ty/(scale/2))*maps.main_map[0].length+(int)(tx/(scale/2))]*scale/2*scale/2;
//                int mp = 0;
//
////                float c=All_Textures[((int)(ty)&(scale/2-1))*scale/2 + ((int)(tx)&(scale/2-1))+mp]*0.7;
//
//                System.out.println(tx + " | " + ty);
//
//                Color pixel_color = new Color(textures.brick_wall_image.getRGB((int) tx, (int) ty));
//
//                g2.setColor(pixel_color);
//
//                g2.setStroke(new BasicStroke(((float) width / number_of_rays_limit) + 1));
//
//                int floor_x = number_of_ray * width / number_of_rays_limit;
//
//                g2.drawLine(floor_x, (int) ((double) height /2-lineH/2), floor_x, 0);
//
////                glColor3f(c/1.3,c/1.3,c);glPointSize(8);glBegin(GL_POINTS);glVertex2i(r*8+530,y);glEnd();
//
//                //---draw ceiling---
////                mp=mapC[(int)(ty/32.0)*mapX+(int)(tx/32.0)]*32*32;
////                c=All_Textures[((int)(ty)&31)*32 + ((int)(tx)&31)+mp]*0.7;
////                glColor3f(c/2.0,c/1.2,c/2.0);glPointSize(8);glBegin(GL_POINTS);glVertex2i(r*8+530,height-y);glEnd();
//            }

//            for (){
//
//            }


            hr.angle += deg / rays_multiplier;
            vr.angle += deg / rays_multiplier;

            hr.angle = fix_angle(hr.angle);
            vr.angle = fix_angle(vr.angle);

//            g2.setStroke(new BasicStroke(1));
//            g2.setColor(Color.GREEN);
//            g2.drawLine((int) player.x, (int) player.y, (int) shortest_ray_x, (int) shortest_ray_y);


        }

        boolean test_bool = true;
        for (int[] cell : rendered_map_cells) {

            if (cell[0] == 2 && cell[1] == 16 || cell[0] == 0 && cell[1] == 17) {
                test_bool = false;
                break;
            }

        }
        if (test_bool && player.x / scale > 19) {
            maps.main_map[1][16] = 1;
        }

    }

    private static BufferedImage get_buffered_image(int current_map_element) {
        BufferedImage texture = null;
        if (current_map_element == 1) {
            texture = textures.brick_wall_image;
        } else if (current_map_element == 2) {
            texture = textures.brick_wall_door_image;
        }
        return texture;
    }

    public void draw_map(Graphics g, int scale) {

        Graphics2D g2;
        g2 = (Graphics2D) g;

        //            Drawing the map
        int x, y;
        for (y = 0; y < maps.main_map.length; y++) {
            for (x = 0; x < maps.main_map[0].length; x++) {
                if (maps.main_map[y][x] == 1) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.BLACK);

                }
                g2.fillRect(x * scale / 4, y * scale / 4, scale / 4 - 1, scale / 4 - 1);


            }

        }
    }

    public void draw_player(Graphics g, Game.MyPanel.Player player) {

        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setColor(Color.YELLOW);

        g2.setStroke(new BasicStroke(1));

        g2.fillRect((int) player.x / 4 - 5 / 4, (int) player.y / 4 - 5 / 4, 10 / 4, 10 / 4);
        g2.drawLine((int) player.x / 4, (int) player.y / 4, (int) (player.x / 4 + player.delta_x * 10 / 4), (int) (player.y / 4 + player.delta_y * 10 / 4));

    }

    public void draw_menu(Graphics g, int width, int height) {

        if (Game.is_paused) {


        Graphics2D g2;
        g2 = (Graphics2D) g;

//        Rectangle text_bounds = text_font_metrics.getStringBounds("test text for testing");

        g2.setColor(new Color(0, 0, 0, 90));

        g2.fillRect(0, 0, width, height);


        draw_ui_element(g, width, height, "Liminal maze", width / 10, 0);
        draw_ui_element(g, width, height, "PLAY", width / 15, 30);
        draw_ui_element(g, width, height, "Settings", width / 15, 50);
        draw_ui_element(g, width, height, "EXIT", width / 15, 70);
    }

    }

    public int draw_ui_element(Graphics g, int width, int height, String text, int font_size, int start) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        Font font = text_font.deriveFont(Font.PLAIN, font_size);
        FontMetrics font_metrics = g2.getFontMetrics(font);

        g2.setColor(Color.BLACK);
        g2.setFont(font);


        int text_x = width / 2 - font_metrics.stringWidth(text) / 2;
        double text_y = font_metrics.getHeight() + ((double) height /100)*start;

        int offset = width/100;

        Rectangle2D text_rect = font_metrics.getStringBounds(text,g);

        g2.setColor(Color.BLACK);

        g2.fillRect(text_x-offset, (int) (text_y- (int) text_rect.getHeight()+offset), (int) text_rect.getWidth()+offset*2, (int) text_rect.getHeight()+offset);

        g2.setColor(Color.WHITE);

        g2.drawString(text, text_x, (int) text_y);

        return 0;

    }
}
