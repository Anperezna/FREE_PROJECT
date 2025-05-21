package juego;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Principal {
    private JPanel panelMain;
    public Principal() {
        panelMain.setPreferredSize(new Dimension(800, 600));
    }

    public static void main(String[] args) {
        // Solicitar los nombres de los jugadores
        String nombre1 = JOptionPane.showInputDialog("Introduzca el nombre del Jugador 1");
        String nombre2 = JOptionPane.showInputDialog("Introduzca el nombre del Jugador 2");

        JFrame ventana = new JFrame("PONG 2 Jugadores");
        PanelJuego juego = new PanelJuego(nombre1, nombre2); // Pasar los nombres al PanelJuego
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar programa al presionar X
        ventana.setResizable(false); // Que no se pueda redimensionar
        ventana.add(juego); // Añadir el PanelJuego al JFrame
        ventana.pack(); // Ajusta automaticamente el tamaño del JFrame
        ventana.setLocationRelativeTo(null); // Si es null, siempre se centrará en el medio de la pantalla
        ventana.setVisible(true); // Que la ventana sea visible siempre, se sobreponga
    }

    public static class PanelJuego extends JPanel implements ActionListener, KeyListener {
        // Dimensiones del juego
        private final int ANCHO = 800, ALTO = 600;
        private int segundos = 0;
        private int ticks = 0;

        // Pelota
        private int pelotaX = ANCHO / 2;
        private int pelotaY = ALTO / 2;
        private int tamañoPelota = 20;

        // Velocidad pelota
        private int velocidadX = 4;
        private int velocidadY = 4;

        // Paletas de jugador
        private final int anchoPaleta = 20, altoPaleta = 100;

        // Posicion inicial de las paletas
        private int jugador1Y = ALTO / 2 - altoPaleta / 2;
        private int jugador2Y = ALTO / 2 - altoPaleta / 2;

        // Velocidad de la paleta
        private final int velocidadPaleta = 6;

        // Teclas presionadas
        private boolean jugador1Arriba = false, jugador1Abajo = false;
        private boolean jugador2Arriba = false, jugador2Abajo = false;

        // Contadores de puntaje
        private int puntosJugador1 = 0;
        private int puntosJugador2 = 0;

        private Timer temporizador;
        private String nombre1;
        private String nombre2;
        private BufferedImage fondo;
        private Font fuente;

        public PanelJuego(String nombre1, String nombre2) {
            this.nombre1 = nombre1; // Guardar el nombre del jugador 1
            this.nombre2 = nombre2; // Guardar el nombre del jugador 2
            int segundos = 0;
            int ticks = 0;
            setPreferredSize(new Dimension(ANCHO, ALTO)); // Dimension de la pantalla de juego
            setFocusable(true); // Que se sobreponga a las demás ventanas
            addKeyListener(this); // El propio lee los key presionados

            // Cargar imagen
            try {
                fondo = ImageIO.read(new File("src/fondo_pong.png"));
            } catch (IOException e) {
                e.printStackTrace(); // Manejar errores de carga de la imagen
            }

            // Cargar fuente
            try {
                fuente = Font.createFont(Font.TRUETYPE_FONT, new File("src/Poppins/Poppins-Regular.ttf")).deriveFont(36f); // Cambia el tamaño según sea necesario
            } catch (FontFormatException | IOException e) {
                e.printStackTrace(); // Manejar errores de carga de la fuente
            }


            temporizador = new Timer(10, this);
            temporizador.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Dibujar la imagen de fondo
            if (fondo != null) {
                g.drawImage(fondo, 0, 0, ANCHO, ALTO, this); // Ajustar la imagen al tamaño del panel
            }

            // Pelota
            g.setColor(Color.WHITE);
            g.fillOval(pelotaX, pelotaY, tamañoPelota, tamañoPelota);

            // Paletas
            g.fillRect(30, jugador1Y, anchoPaleta, altoPaleta); // Jugador 1 (izquierda)
            g.fillRect(ANCHO - 50, jugador2Y, anchoPaleta, altoPaleta); // Jugador 2 (derecha)

            // Puntajes
            g.setFont(fuente);
            g.drawString(nombre1 + ": " + puntosJugador1, 50, 50);
            g.drawString(nombre2 + ": " + puntosJugador2, ANCHO - 150, 50);
            g.setFont(new Font("Comic Sans", Font.BOLD, 20));

            g.drawString("Tiempo: " + segundos + "s", ANCHO / 2 - 80, 50); // En medio de la pantalla a 50 pixeles desde arriba
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Movimiento del jugador 1 (W y S)
            if (jugador1Arriba && jugador1Y > 0) jugador1Y -= velocidadPaleta;
            if (jugador1Abajo && jugador1Y + altoPaleta < ALTO) jugador1Y += velocidadPaleta;

            // Movimiento del jugador 2 (flechas arriba/abajo)
            if (jugador2Arriba && jugador2Y > 0) jugador2Y -= velocidadPaleta;
            if (jugador2Abajo && jugador2Y + altoPaleta < ALTO) jugador2Y += velocidadPaleta;

            // Movimiento de la pelota
            pelotaX += velocidadX;
            pelotaY += velocidadY;

            // Rebote con bordes superior/inferior
            if (pelotaY <= 0 || pelotaY + tamañoPelota >= ALTO) {
                velocidadY *= -1;
            }

            // Temporizador de partida
            ticks++;
            if (ticks>= 100) { // Cada tick son 10ms, si es igual o superior a 100, cuenta como 1s
                segundos++;
                ticks = 0;
            }

            // Colisión con paletas
            Rectangle rectPelota = new Rectangle(pelotaX, pelotaY, tamañoPelota, tamañoPelota);
            Rectangle rectJugador1 = new Rectangle(30, jugador1Y, anchoPaleta, altoPaleta);
            Rectangle rectJugador2 = new Rectangle(ANCHO - 50, jugador2Y, anchoPaleta, altoPaleta);

            if (rectPelota.intersects(rectJugador1) || rectPelota.intersects(rectJugador2)) {
                velocidadX *= -1;
            }

            // Puntos
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
            // Alterna la dirección inicial para que no siempre vaya hacia el mismo lado
            velocidadX = (Math.random() > 0.5) ? Math.abs(velocidadX) : -Math.abs(velocidadX);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Controles jugador 1 (W y S)
            if (e.getKeyCode() == KeyEvent.VK_W) jugador1Arriba = true;
            if (e.getKeyCode() == KeyEvent.VK_S) jugador1Abajo = true;

            // Controles jugador 2 (flechas)
            if (e.getKeyCode() == KeyEvent.VK_UP) jugador2Arriba = true;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) jugador2Abajo = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Controles jugador 1 (W y S)
            if (e.getKeyCode() == KeyEvent.VK_W) jugador1Arriba = false;
            if (e.getKeyCode() == KeyEvent.VK_S) jugador1Abajo = false;

            // Controles jugador 2 (flechas)
            if (e.getKeyCode() == KeyEvent.VK_UP) jugador2Arriba = false;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) jugador2Abajo = false;
        }

        @Override
        public void keyTyped(KeyEvent e) {}
    }

    public class Database {
        private static final String URL = "jdbc:mysql://localhost:3306/free-project";
        private static final String USER = "root";
        private static final String PASS = "196465200206";
        public static Connection conectar() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASS);
        }
    }
}
