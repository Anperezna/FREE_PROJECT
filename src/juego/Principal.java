package juego;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Principal {
    private JPanel panelMain;
    public Principal() {
        panelMain.setPreferredSize(new Dimension(800, 600));
    }

    public static void main(String[] args) {
        String nombre1 = JOptionPane.showInputDialog("Introduzca el nombre del Jugador 1");
        String nombre2 = JOptionPane.showInputDialog("Introduzca el nombre del Jugador 2");

        JFrame ventana = new JFrame("PONG 2 Jugadores");
        PanelJuego juego = new PanelJuego(nombre1, nombre2);

        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.add(juego);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        // Guardar resultado al cerrar la ventana
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                juego.guardarResultado();
            }
        });
    }

    public static class PanelJuego extends JPanel implements ActionListener, KeyListener {
        private final int ANCHO = 800, ALTO = 600;
        private int segundos = 0;
        private int ticks = 0;

        private int pelotaX = ANCHO / 2;
        private int pelotaY = ALTO / 2;
        private int tamañoPelota = 20;
        private int velocidadX = 4;
        private int velocidadY = 4;

        private final int anchoPaleta = 20, altoPaleta = 100;
        private int jugador1Y = ALTO / 2 - altoPaleta / 2;
        private int jugador2Y = ALTO / 2 - altoPaleta / 2;
        private final int velocidadPaleta = 6;

        private boolean jugador1Arriba = false, jugador1Abajo = false;
        private boolean jugador2Arriba = false, jugador2Abajo = false;

        private int puntosJugador1 = 0;
        private int puntosJugador2 = 0;

        private Timer temporizador;
        private String nombre1;
        private String nombre2;
        private BufferedImage fondo;
        private Font fuente;

        private int idJugador1;
        private int idJugador2;

        public PanelJuego(String nombre1, String nombre2) {
            this.nombre1 = nombre1;
            this.nombre2 = nombre2;

            setPreferredSize(new Dimension(ANCHO, ALTO));
            setFocusable(true);
            addKeyListener(this);

            try {
                fondo = ImageIO.read(new File("src/fondo_pong.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fuente = Font.createFont(Font.TRUETYPE_FONT, new File("src/Poppins/Poppins-Regular.ttf")).deriveFont(36f);
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
            }

            // Conexión a base de datos: obtener o insertar jugadores
            idJugador1 = Database.obtenerOInsertarUsuario(nombre1);
            idJugador2 = Database.obtenerOInsertarUsuario(nombre2);

            temporizador = new Timer(10, this);
            temporizador.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (fondo != null) {
                g.drawImage(fondo, 0, 0, ANCHO, ALTO, this);
            }

            g.setColor(Color.WHITE);
            g.fillOval(pelotaX, pelotaY, tamañoPelota, tamañoPelota);

            g.fillRect(30, jugador1Y, anchoPaleta, altoPaleta);
            g.fillRect(ANCHO - 50, jugador2Y, anchoPaleta, altoPaleta);

            g.setFont(fuente);
            g.drawString(nombre1 + ": " + puntosJugador1, 50, 50);
            g.drawString(nombre2 + ": " + puntosJugador2, ANCHO - 150, 50);
            g.setFont(new Font("Comic Sans", Font.BOLD, 20));
            g.drawString("Tiempo: " + segundos + "s", ANCHO / 2 - 80, 50);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (jugador1Arriba && jugador1Y > 0) jugador1Y -= velocidadPaleta;
            if (jugador1Abajo && jugador1Y + altoPaleta < ALTO) jugador1Y += velocidadPaleta;
            if (jugador2Arriba && jugador2Y > 0) jugador2Y -= velocidadPaleta;
            if (jugador2Abajo && jugador2Y + altoPaleta < ALTO) jugador2Y += velocidadPaleta;

            pelotaX += velocidadX;
            pelotaY += velocidadY;

            if (pelotaY <= 0 || pelotaY + tamañoPelota >= ALTO) {
                velocidadY *= -1;
            }

            ticks++;
            if (ticks >= 100) {
                segundos++;
                ticks = 0;
            }

            Rectangle rectPelota = new Rectangle(pelotaX, pelotaY, tamañoPelota, tamañoPelota);
            Rectangle rectJugador1 = new Rectangle(30, jugador1Y, anchoPaleta, altoPaleta);
            Rectangle rectJugador2 = new Rectangle(ANCHO - 50, jugador2Y, anchoPaleta, altoPaleta);

            if (rectPelota.intersects(rectJugador1) || rectPelota.intersects(rectJugador2)) {
                velocidadX *= -1;
            }

            if (pelotaX < 0) {
                puntosJugador2++;
                reiniciarPelota();
            }
            if (pelotaX > ANCHO) {
                puntosJugador1++;
                reiniciarPelota();
            }

            repaint();
        }

        private void reiniciarPelota() {
            pelotaX = ANCHO / 2;
            pelotaY = ALTO / 2;
            velocidadX = (Math.random() > 0.5) ? Math.abs(velocidadX) : -Math.abs(velocidadX);
        }

        public void guardarResultado() {
            Database.guardarPartida(idJugador1, idJugador2, puntosJugador1, puntosJugador2);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W) jugador1Arriba = true;
            if (e.getKeyCode() == KeyEvent.VK_S) jugador1Abajo = true;
            if (e.getKeyCode() == KeyEvent.VK_UP) jugador2Arriba = true;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) jugador2Abajo = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W) jugador1Arriba = false;
            if (e.getKeyCode() == KeyEvent.VK_S) jugador1Abajo = false;
            if (e.getKeyCode() == KeyEvent.VK_UP) jugador2Arriba = false;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) jugador2Abajo = false;
        }

        @Override
        public void keyTyped(KeyEvent e) {}
    }

    public static class Database {
        private static final String URL = "jdbc:mysql://localhost:3306/free-project";
        private static final String USER = "root";
        private static final String PASS = "196465200206";

        public static Connection conectar() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASS);
        }

        public static int obtenerOInsertarUsuario(String nombre) {
            try (Connection con = conectar()) {
                var psBuscar = con.prepareStatement("SELECT id_usuario FROM usuario WHERE nombre = ?");
                psBuscar.setString(1, nombre);
                var rs = psBuscar.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }

                var psInsertar = con.prepareStatement("INSERT INTO usuario (nombre, fecha) VALUES (?, NOW())", Statement.RETURN_GENERATED_KEYS);
                psInsertar.setString(1, nombre);
                psInsertar.executeUpdate();
                var keys = psInsertar.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public static void guardarPartida(int id1, int id2, int puntos1, int puntos2) {
            try (Connection con = conectar()) {
                int ganador;
                if (puntos1 > puntos2) ganador = id1;
                else if (puntos2 > puntos1) ganador = id2;
                else ganador = 0;

                var ps = con.prepareStatement("""
                    INSERT INTO partida (fecha, id_jugador_1, id_jugador_2, puntos_jugador_1, puntos_jugador_2, ganador)
                    VALUES (NOW(), ?, ?, ?, ?, ?)
                """);

                ps.setInt(1, id1);
                ps.setInt(2, id2);
                ps.setInt(3, puntos1);
                ps.setInt(4, puntos2);
                if (ganador == 0) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, ganador);

                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
