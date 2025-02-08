package project;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/*
        Author: Oleg Poliakov

        Features:

        Player has vertical and horizontal direction which allows him to move in 2 directions at the same time
        (for example forward and left)

        Player has an angle which he can move on not just up down right left

        Player's angle can be changed using arrows keys or mouse
        to be able to properly detect mouses movement and convert them to players rotation
        Robot is moving mouse to the center of the window

        Game has a menu, it is opened when esc key is pressed
        Robot stops moving the mouse when menu is open

        In the menu there are settings
        you can change the window to be fullscreen or windowed
        you can change game's resolution
        you can change mouse's sensitivity which simultaneity effects rotation speed when player rotates via arrow keys

        form the menu you can continue or unpause the game

        The game uses raytracing to make an illusion of a 3d game from a 2d game

        The game will scale for any screen size no matter how big or small it is

        The game has 3 2d maps representing walls, floors, and ceilings

        All the maps are in csv format so that they can be edited in any csv editor such as Tiled

        All images used in the game are created by me, but inspired by assets from the Internet

        The game has a timer which counts how much time it took for you to beat the game

        There are many interactive objects in the game (keys, doors), all of which can be interacted with using the mouse or keyboard

        There is lighting in the game

        The player has a torch in his hand, everything that is far from the player will appear darker, and closer - brighter

                Endless maze
        In endless maze player will see strange shapes on the walls
        and depending on which shape player sees he has to guess which way to go right or left
        each time the game is launched this patter of shape and what direction you need to go is randomly generated

        Player have to guess correctly 8 times before he will be able to reach the end of the maze

                Bridge
        Player needs to cross the bridge
        This bridge consists of tiles with blue dots and tiles with red dots
        If a player steps outside the tile, he falls and has to cross the bridge again from the beginning
        It is almost impossible to cross the bridge without the golden watch
        When the red light on the clock flashes, the player will fall from the tile with the red dot
        Therefore, the player needs to time his movement precisely
        so that he is on the tiles with the red dot only when the green light on the clock flashes.

        After completing the bridge, the player will come across a door that he does not have a key for,
        but once he tries to open it, a key will appear behind him, allowing him to open the door

        Upon opening the door player reached the end of the game
*/

public class Game {

//    global variables

    private static JFrame main_frame;
    private static JPanel control_panel;
    public static final int[] DEFAULT_SIZE = {1024, 512};
    public static boolean is_fullscreen = false;
    public static boolean is_paused = true;

    public static boolean is_started = false;

    public static double screen_scale = 1;

    public static int scale = 64;

    public static long torch_timer = 0;
    public static int torch_state = 0;

    public static int endless_maze_rand_shape = 0;
    public static int endless_maze_prev_rand_shape = 0;
    public static String[] endless_maze_shapes_correct_direction = new String[4];
    public static int endless_maze_depth = 0;

    public static int golden_watch = 0;
    public static int golden_watch_speed = 2;

    public static long golden_watch_timer = System.currentTimeMillis();
    public static long death_animation_timer = System.currentTimeMillis();

    public static boolean was_door_in_tunnel_open = false;

    public static String message = "";
    public static long message_timer = 0;

    public static long global_timer;

    public static boolean gameover = false;

    public static boolean settings = false;

    public static int end_time = 0;

    public static int window_height = DEFAULT_SIZE[1];
    public static int window_width = DEFAULT_SIZE[0];

    public static class Player {
        String horizontal_direction = "none";
        String vertical_direction = "none";
        double rotation = 0;
        double x = 120 - 32;
        double y = 120 - 32;
        double delta_x = 0;
        double delta_y = 0;
        double angle = Math.PI / 2;

        boolean watch = false;
        boolean dead = false;
        int death_animation = 0;

        final int interact_offset = 50;
        boolean capture_mouse = false;
        int mouse_sense = 800;
        boolean can_interact = false;

        boolean arrow_keys_pressed = false;

        int keys = 0;

        int speed = 1;

//            for debug

//        Player() {
//            x = 14 * scale + (double) scale / 2;
//            y = 11 * scale + (double) scale / 2;
//        }
    }

    public static Player player = new Player();


    public static Random random = new Random();

//    I will use GraphicsDevice to make window fullscreen

    public static GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
    public static GraphicsDevice device = graphics.getDefaultScreenDevice();


    public static void main(String[] args) {
//        generating random pattern for endless_maze
        Arrays.fill(endless_maze_shapes_correct_direction, "left");

        for (int i = 0; i < endless_maze_shapes_correct_direction.length / 2; i++) {
            int rand_int = random.nextInt(endless_maze_shapes_correct_direction.length);
            while (endless_maze_shapes_correct_direction[rand_int].equals("right")) {
                rand_int = random.nextInt(endless_maze_shapes_correct_direction.length);
            }
            endless_maze_shapes_correct_direction[rand_int] = "right";
        }
        prepareGUI();
    }

    private static void prepareGUI() {
//        Creating and making window visible
        main_frame = new JFrame("Liminal maze");

        main_frame.pack();

        main_frame.setSize(DEFAULT_SIZE[0], DEFAULT_SIZE[1]+100);

        main_frame.setBackground(Color.BLACK);

        main_frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });


        control_panel = new MyPanel();
        control_panel.setFocusable(true);
        main_frame.setContentPane(control_panel);


        main_frame.setVisible(true);
    }

    static class MyPanel extends JPanel implements KeyListener, MouseMotionListener, MouseInputListener {

//        Robot for moving the mouse
        Robot robot;

        {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }


        public static double rays_multiplier = 3;
        public static double prev_rays_multiplier = 3;

        public static Textures textures = new Textures();

        public static Maps maps = new Maps();

        public static Render render = new Render();

        public static class Button {
            Rectangle rect;
            String action;
            boolean is_hovered = false;

            public Button(Rectangle rectangle, String button_action) {
                rect = rectangle;
                action = button_action;
            }
        }

        ArrayList<Button> buttons = new ArrayList<>();


        public static class Ray {
            double x = 0;
            double y = 0;
            double angle = 0;
            double offset_x = 0;
            double offset_y = 0;
        }

        public static double fix_angle(double angle) {
            while (angle < 0) {
                angle += Math.PI * 2;
            }

            while (angle > Math.PI * 2) {
                angle -= Math.PI * 2;
            }
            return angle;
        }


        public MyPanel() {
            Timer timer = new Timer();
            TimerTask task = new Update();

//            Game will run at max 30 frames per second

            timer.schedule(task, 1000 / 30, 1000 / 30);

            addKeyListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
        }

        @Override
        public void paint(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2;
            g2 = (Graphics2D) g;


            g2.setColor(Color.BLACK);
            window_height = getHeight();
            window_width = getWidth();
            g2.fillRect(0, 0, window_width, window_height);

//            Move screen when the player falls into a pit

            double percent = (double) player.death_animation / 100;

            g2.translate(0, (int) ((double) (window_height / 2) + (((double) DEFAULT_SIZE[1] / 2) * screen_scale)) * percent);

            if (!gameover) {
                render.draw_rays(g, scale, rays_multiplier);

                render.draw_torch(g, torch_state);

                if (player.watch) {
                    render.draw_watch(g, golden_watch);
                }

                render.draw_menu(g, buttons, is_started);

                if (!is_paused) {
                    render.draw_cursor(g, player.can_interact);
                }

                render.draw_message(g, message, message_timer);
            } else {
                render.draw_menu(g, buttons, is_started);
            }


        }


        @Override
        public void keyTyped(KeyEvent e) {

            int keyChar = e.getKeyChar();


            if (keyChar == KeyEvent.VK_ENTER) {
                fullscreen();
            }

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!gameover) {
                int keyCode = e.getKeyCode();


                if (keyCode == KeyEvent.VK_ESCAPE) {
                    is_paused = true;
                    player.capture_mouse = false;
                    main_frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                if (keyCode == KeyEvent.VK_EQUALS) {
                    rays_multiplier++;
                    message = "resolution is " + rays_multiplier;
                    message_timer = System.currentTimeMillis();
                }
                if (keyCode == KeyEvent.VK_MINUS) {
                    if (rays_multiplier > 1) {
                        rays_multiplier--;
                    }
                    message = "resolution is " + rays_multiplier;
                    message_timer = System.currentTimeMillis();
                }

                if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
                    player.mouse_sense -= 100;
                    if (player.mouse_sense < 100) {
                        player.mouse_sense = 100;
                    }
                    message = "mouse sense is " + player.mouse_sense;
                    message_timer = System.currentTimeMillis();
                }

                if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
                    player.mouse_sense += 100;
                    message = "mouse sense is " + player.mouse_sense;
                    message_timer = System.currentTimeMillis();
                }

                if (keyCode == KeyEvent.VK_MINUS) {
                    if (rays_multiplier > 1) {
                        rays_multiplier--;
                    }
                }

                if (keyCode == KeyEvent.VK_NUMPAD1) {
                    screen_scale++;
                }


                if (keyCode == KeyEvent.VK_NUMPAD0) {
                    if (rays_multiplier != 20) {
                        prev_rays_multiplier = rays_multiplier;
                        rays_multiplier = 20;
                    }
                }


//
                if (keyCode == KeyEvent.VK_W) {
                    player.vertical_direction = "forward";
                } else if (keyCode == KeyEvent.VK_S) {
                    player.vertical_direction = "backward";
                } else if (keyCode == KeyEvent.VK_D) {
                    player.horizontal_direction = "right";
                } else if (keyCode == KeyEvent.VK_A) {
                    player.horizontal_direction = "left";


                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    player.rotation = -0.05 / ((double) player.mouse_sense /800);
                    player.arrow_keys_pressed = true;
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    player.rotation = 0.05 / ((double) player.mouse_sense /800);
                    player.arrow_keys_pressed = true;
                } else if (keyCode == KeyEvent.VK_K) {
                    System.exit(0);
                }

                if (keyCode == KeyEvent.VK_E) {

                    interact();

                }
            }
        }

        private void interact() {
            if (!gameover) {

                int looking_at = get_tile_front_of_player();


                if (looking_at == 2) {
                    if (player.keys > 0) {
                        set_tile_front_of_player(0);
                        was_door_in_tunnel_open = true;
                        player.keys--;
                    } else {
                        if (is_player_y_in(38)) {
                            maps.main_map[32][19] = 3;
                        }
                        message = "the door is locked";
                        message_timer = System.currentTimeMillis();
                    }


                } else if (looking_at == 3) {

                    set_tile_front_of_player(1);
                    player.keys += 1;

                    message = "picked up a key";
                    message_timer = System.currentTimeMillis();

                } else if (looking_at == 4) {

                    set_tile_front_of_player(1);

                    message = "picked up a watch";
                    message_timer = System.currentTimeMillis();

                } else if (looking_at == 5) {
                    gameover = true;
                    is_paused = true;
                    end_time = (int) (System.currentTimeMillis() - global_timer);
                    player.capture_mouse = false;
                    main_frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (!gameover) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_NUMPAD0) {
                    rays_multiplier = prev_rays_multiplier;
                }

                if (player.vertical_direction.equals("forward") && keyCode == KeyEvent.VK_W) {
                    player.vertical_direction = "none";
                } else if (player.vertical_direction.equals("backward") && keyCode == KeyEvent.VK_S) {
                    player.vertical_direction = "none";
                }

                if (player.horizontal_direction.equals("right") && keyCode == KeyEvent.VK_D) {
                    player.horizontal_direction = "none";
                } else if (player.horizontal_direction.equals("left") && keyCode == KeyEvent.VK_A) {
                    player.horizontal_direction = "none";
                }

                if (player.rotation < 0 && keyCode == KeyEvent.VK_RIGHT) {
                    player.rotation = 0;
                    player.arrow_keys_pressed = false;
                } else if (player.rotation > 0 && keyCode == KeyEvent.VK_LEFT) {
                    player.rotation = 0;
                    player.arrow_keys_pressed = false;
                }

            }

        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point mouse_point = new Point(e.getX(), e.getY());

            for (Button button : buttons) {
                button.is_hovered = false;
                if (button.rect.contains(mouse_point)) {
                    button.is_hovered = true;
                }
            }

            int delta_x;

            Point pos = control_panel.getLocationOnScreen();

            int oldX = pos.x + getWidth() / 2;

            if (player.capture_mouse && !player.arrow_keys_pressed) {
                int mouse_x = MouseInfo.getPointerInfo().getLocation().x;

                delta_x = oldX - mouse_x;
                player.rotation = (double) delta_x / player.mouse_sense;

            }
        }


        @Override
        public void mousePressed(MouseEvent e) {

            interact();

            Point mouse_point = new Point(e.getX(), e.getY());

            for (Button button : buttons) {
                if (button.rect.contains(mouse_point)) {
                    switch (button.action) {
                        case "exit" -> System.exit(0);
                        case "play" -> {
                            is_paused = false;
                            player.capture_mouse = true;
                            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
                            main_frame.getContentPane().setCursor(blankCursor);

                            if (!is_started) {
                                is_started = true;
                                global_timer = System.currentTimeMillis();
                            }

                        }
                        case "settings" -> settings = !settings;

                        case "fullscreen" -> fullscreen();

                    }
                }
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        public int get_tile_front_of_player() {
            for (int i = 0; i < scale; i++) {

                double front_player_x = player.x + Math.cos(player.angle) * i;
                double front_player_y = player.y + Math.sin(player.angle) * i;

                int looking_at = maps.main_map[(int) (front_player_y / scale)][(int) (front_player_x / scale)];

                if (looking_at == 2 || looking_at == 3 || looking_at == 4 || looking_at == 5) {

                    return looking_at;

                }
            }
            return 0;
        }

        public void set_tile_front_of_player(int tile) {
            for (int i = 0; i < scale; i++) {

                double front_player_x = player.x + Math.cos(player.angle) * i;
                double front_player_y = player.y + Math.sin(player.angle) * i;

                int looking_at = maps.main_map[(int) (front_player_y / scale)][(int) (front_player_x / scale)];

                if (looking_at == 2 || looking_at == 3 || looking_at == 4) {

                    maps.main_map[(int) (front_player_y / scale)][(int) (front_player_x / scale)] = tile;

                }
            }
        }

        class Update extends TimerTask {
            public void run() {

//                Make the gold clock rotate depending on the time that has passed.

                if (!is_paused) {
                    golden_watch = (int) ((System.currentTimeMillis() - golden_watch_timer) / 10) * golden_watch_speed;

                    if (golden_watch > 360 * 2) {
                        golden_watch_timer = System.currentTimeMillis();
                        golden_watch = 0;
                    }
                } else {
                    golden_watch_timer = System.currentTimeMillis();
                }


                if (player.dead) {
                    player.death_animation = (int) (((double) (System.currentTimeMillis() - death_animation_timer) / 10) * 0.5);

                    if (player.death_animation > 100) {
                        player.x = 18 * scale + (double) scale / 2;
                        player.y = 8 * scale + (double) scale / 2;
                        player.angle = Math.PI / 2;
                        player.dead = false;
                        player.death_animation = -100;
                        death_animation_timer = System.currentTimeMillis();
                    }
                } else {
                    if (player.death_animation < 0) {
                        player.death_animation = -100 + (int) (((double) (System.currentTimeMillis() - death_animation_timer) / 10) * 3);
                    } else {
                        player.death_animation = 0;
                    }
                }

                logic();

                player.can_interact = get_tile_front_of_player() != 0;

                repaint();

//                Scale the game depending on the window size
                screen_scale = (double) getWidth() / DEFAULT_SIZE[0];
                if (!is_paused && player.death_animation == 0) {

//                    torch animation

                    long torch_time_elapsed = System.currentTimeMillis() - torch_timer;

                    if (torch_time_elapsed > 200) {
                        torch_state++;
                        if (torch_state > 2) {
                            torch_state = 0;
                        }
                        torch_timer = System.currentTimeMillis();
                    }


                    double prev_pos_x = player.x;
                    double prev_pos_y = player.y;

//                    move the player depending on the input

                    if (player.vertical_direction.equals("forward")) {
                        player.x += player.delta_x * player.speed;
                        player.y += player.delta_y * player.speed;

                    } else if (player.vertical_direction.equals("backward")) {
                        player.x -= player.delta_x * player.speed;
                        player.y -= player.delta_y * player.speed;
                    }

                    if (player.horizontal_direction.equals("right")) {
                        player.x -= player.delta_y * player.speed;
                        player.y += player.delta_x * player.speed;
                    } else if (player.horizontal_direction.equals("left")) {
                        player.x += player.delta_y * player.speed;
                        player.y -= player.delta_x * player.speed;
                    }

                    int offset = 5;

//                    First collision check (makes player slide along the wall)

                    if (
                            maps.main_map[(int) (player.y - offset) / scale][(int) (player.x) / scale] != 0 ||
                                    maps.main_map[(int) (player.y + offset) / scale][(int) (player.x) / scale] != 0
                    ) {
                        player.y = prev_pos_y;
                    }

                    if (
                            maps.main_map[(int) (player.y) / scale][(int) (player.x - offset) / scale] != 0 ||
                                    maps.main_map[(int) (player.y) / scale][(int) (player.x + offset) / scale] != 0
                    ) {
                        player.x = prev_pos_x;
                    }

//                    Second collision check
//                     sometimes the player can walk through walls,
//                     I'm too lazy to figure out where the error is,
//                     so I added a second collision check

                    if (maps.main_map[(int) (player.y) / scale][(int) (player.x) / scale] != 0) {
                        player.y = prev_pos_y;
                        player.x = prev_pos_x;
                    }


                    if (maps.floor_map[(int) (player.y) / scale][(int) (player.x) / scale] == 0 || maps.floor_map[(int) (player.y) / scale][(int) (player.x) / scale] == 4 && golden_watch < 360) {
                        if (!player.dead) {
                            death_animation_timer = System.currentTimeMillis();
                            player.dead = true;
                            if (maps.main_map[32][19] == 3) {
                                for (int i = 18; i <= 20; i++) {
                                    for (int j = 31; j <= 33; j++) {
                                        maps.floor_map[j][i] = 3;
                                    }
                                }
                            }
                        }
                    }

                    player.angle -= player.rotation;
//
                    if (player.angle > Math.PI * 2) {
                        player.angle -= Math.PI * 2;
                    } else if (player.angle < 0) {
                        player.angle += Math.PI * 2;
                    }

                    player.delta_x = Math.cos(player.angle) * 5;
                    player.delta_y = Math.sin(player.angle) * 5;


                    if (player.capture_mouse) {

                        try {

                            Point pos = control_panel.getLocationOnScreen();

                            int oldX = pos.x + getWidth() / 2;
                            int oldY = pos.y + getHeight() / 2;


                            if (player.rotation != 0) {
                                robot.mouseMove(oldX, oldY);
                            }
                        } catch (Exception e) {
                            System.out.println("control_panel is not visible");
                        }

                    }
                }

            }


            private void logic() {
                if (maps.main_map[7][18] == 1) {

                    if (is_player_x_in(20, 22)) {
                        if (is_player_y_in(1) || is_player_y_in(15)) {
                            maps.main_map[3][21] = 0;
                            maps.main_map[7][21] = (was_door_in_tunnel_open) ? 0 : 2;
                            maps.main_map[13][21] = 0;

                            for (int i = 4; i <= 12; i++) {
                                maps.main_map[i][20] = 1;
                                maps.main_map[i][22] = 1;
                            }

                        }
                    } else if (is_player_x_in(19) || is_player_x_in(23)) {

                        if (is_player_y_in(1) || is_player_y_in(15)) {
                            maps.main_map[3][21] = 1;
                            maps.main_map[7][21] = 1;
                            maps.main_map[13][21] = 1;

                            for (int i = 4; i <= 12; i++) {
                                if (i == 7) {
                                    i++;
                                }
                                maps.main_map[i][20] = 0;
                                maps.main_map[i][22] = 0;
                            }

                        }
                    }

                }

                if (!player.watch && maps.main_map[10][45] != 4) {
                    player.x -= 18 * scale;
                    maps.main_map[9][26] = 1;
                    player.watch = true;

                }


                if (is_player_x_in(4, 5) && endless_maze_depth < 8) {
                    if (is_player_y_in(9) || is_player_y_in(13)) {
                        if (endless_maze_rand_shape == 0) {
                            endless_maze_rand_shape = random.nextInt(1, 5);
                        }else if (endless_maze_rand_shape == -1) {
                            endless_maze_rand_shape = random.nextInt(1, 5);
                            endless_maze_prev_rand_shape = -1;
                        } else {
                            endless_maze_prev_rand_shape = endless_maze_rand_shape;
                            if (is_player_y_in(9) && endless_maze_shapes_correct_direction[endless_maze_rand_shape - 1].equals("right") || is_player_y_in(13) && endless_maze_shapes_correct_direction[endless_maze_rand_shape - 1].equals("left")) {
                                endless_maze_depth += 1;
                                if (endless_maze_depth >= 8) {
                                    maps.main_map[11][0] = 3;
                                }

                                endless_maze_rand_shape = random.nextInt(1, 5);
                            } else {
                                endless_maze_rand_shape = -1;
                                endless_maze_depth = 0;
                            }
                        }

                        player.x += 6 * scale;

                    }
                }


            }


        }

        public boolean is_player_x_in(int x1, int x2) {
            return (int) player.x / scale > x1 - 1 && (int) player.x / scale < x2 + 1;
        }

        public boolean is_player_x_in(int x) {
            return (int) player.x / scale == x;
        }

        public boolean is_player_y_in(int y) {
            return (int) player.y / scale == y;
        }


    }

    public static void fullscreen() {
        if (is_fullscreen) {
            main_frame.dispose();
            main_frame.setUndecorated(false);
            device.setFullScreenWindow(null);
            main_frame.setVisible(true);
            is_fullscreen = false;
        } else {
            main_frame.dispose();
            device.setFullScreenWindow(main_frame);
            main_frame.setVisible(true);
            control_panel.requestFocus();
            is_fullscreen = true;
        }
    }


}