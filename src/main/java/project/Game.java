package project;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    private static JFrame main_frame;
    private static JPanel control_panel;
    static final int[] DEFAULT_SIZE = {1024, 512};
    static boolean is_fullscreen = false;
    static boolean is_paused = true;

    static GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static GraphicsDevice device = graphics.getDefaultScreenDevice();


    public static void main(String[] args) {
        prepareGUI();
    }

    private static void prepareGUI() {
        main_frame = new JFrame("Liminal maze");

        main_frame.pack();

        main_frame.setSize(DEFAULT_SIZE[0], DEFAULT_SIZE[1]);

        main_frame.setBackground(Color.BLACK);

        main_frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        main_frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (control_panel != null) {
                    int window_width = control_panel.getWidth();
                    int window_height = control_panel.getHeight();
                    if (window_width / 2 > window_height) {
                        control_panel.setBounds((window_width / 2 - window_height), 0, window_height * 2, window_height);
                    } else if (window_width * 2 > window_height) {
                        control_panel.setBounds(0, (window_height / 2 - window_width / 4), window_width, window_width / 2);
                    }
                }
            }
        });

        control_panel = new MyPanel();
        control_panel.setFocusable(true);
        main_frame.setContentPane(control_panel);


        main_frame.setVisible(true);
    }

    static class MyPanel extends JPanel implements KeyListener, MouseMotionListener, MouseInputListener {

        Robot robot;

        {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }


        public int scale = 64;

        public static double rays_multiplier = 3;
        public static double prev_rays_multiplier = 3;

        public static Textures textures = new Textures();

        public static Maps maps = new Maps();

        public static Render render = new Render();




        public static class Player {
            String horizontal_direction = "none";
            String vertical_direction = "none";
            double rotation = 0;
            double x = 120 - 32;
            double y = 120 - 32;
            double delta_x = 0;
            double delta_y = 0;
            double angle = Math.PI / 2;
            boolean capture_mouse = false;

            int speed = 1;
        }

        Player player = new Player();

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

            timer.schedule(task, 1000 / 30, 1000 / 30);

            addKeyListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
        }

        @Override
        public void paint(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();

            Graphics2D g2;
            g2 = (Graphics2D) g;


            g2.setColor(Color.GRAY);
            g2.fillRect(0, 0, width, height);

            render.draw_rays(g, player, scale, width, height, rays_multiplier);

            render.draw_menu(g, width, height);

//            render.draw_map(g, scale);
//
//            render.draw_player(g, player);


        }


        @Override
        public void keyTyped(KeyEvent e) {

            int keyChar = e.getKeyChar();


            if (keyChar == KeyEvent.VK_ENTER) {
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
                    is_fullscreen = true;
                }
            }

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
//            System.out.println("Key Pressed: " + KeyEvent.getKeyText(keyCode));


            if (keyCode == KeyEvent.VK_ESCAPE) {
                player.capture_mouse = false;
                main_frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            if (keyCode == KeyEvent.VK_EQUALS) {
                rays_multiplier++;
            }
            if (keyCode == KeyEvent.VK_MINUS) {
                if (rays_multiplier > 1) {
                    rays_multiplier--;
                }
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


            } else if (keyCode == KeyEvent.VK_RIGHT && !player.capture_mouse) {
                player.rotation = -0.05;
            } else if (keyCode == KeyEvent.VK_LEFT && !player.capture_mouse) {
                player.rotation = 0.05;
            } else if (keyCode == KeyEvent.VK_K) {
                System.exit(0);
            }

            if (keyCode == KeyEvent.VK_E) {

                open_door();

            }
        }

        private void open_door() {
            int offset = 50;

            if (maps.main_map[(int) (player.y - offset) / scale][(int) (player.x) / scale] == 2) {
                System.out.println("test1");
//                maps.main[(int) (player.y - offset) / scale][(int) (player.x) / scale] = 0;
            } else if (maps.main_map[(int) (player.y + offset) / scale][(int) (player.x) / scale] == 2) {
                System.out.println("test2");
//                maps.main[(int) (player.y + offset) / scale][(int) (player.x) / scale] = 0;
            } else if (maps.main_map[(int) (player.y) / scale][(int) (player.x - offset) / scale] == 2) {
                System.out.println("test3");
//                maps.main[(int) (player.y) / scale][(int) (player.x - offset) / scale] = 0;
            } else if (maps.main_map[(int) (player.y) / scale][(int) (player.x + offset) / scale] == 2) {
                System.out.println("test4");
//                maps.main[(int) (player.y) / scale][(int) (player.x + offset) / scale] = 0;

            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
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

            if (!player.capture_mouse) {
                if (player.rotation < 0 && keyCode == KeyEvent.VK_RIGHT) {
                    player.rotation = 0;
                } else if (player.rotation > 0 && keyCode == KeyEvent.VK_LEFT) {
                    player.rotation = 0;
                }
            }


        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!player.capture_mouse && !is_paused) {
                player.capture_mouse = true;
                BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
                main_frame.getContentPane().setCursor(blankCursor);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

            int delta_x;

            Point pos = control_panel.getLocationOnScreen();

            int oldX = pos.x + getWidth() / 2;

            if (player.capture_mouse) {
                int mouse_x = MouseInfo.getPointerInfo().getLocation().x;

                delta_x = oldX - mouse_x;
                player.rotation = (double) delta_x / 800;

            }
        }



        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            is_paused = false;
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

        class Update extends TimerTask {
            public void run() {
                repaint();
                if (!is_paused) {


                    double prev_pos_x = player.x;
                    double prev_pos_y = player.y;


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
        }


    }


}