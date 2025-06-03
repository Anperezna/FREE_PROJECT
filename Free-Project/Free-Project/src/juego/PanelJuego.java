package juego;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PanelJuego extends JPanel implements ActionListener, KeyListener {
    private final int ANCHO = 800, ALTO = 600;
    private int segundos = 0;
    private int ticks = 0;
    private final int PUNTOS_PARA_GANAR = 5; // Puntos maximos para ganar

    private int pelotaX = ANCHO / 2; // Posicion de la pelota al iniciar
    private int pelotaY = ALTO / 2; // Posicion de la pelota al iniciar
    private int tamanoPelota = 20; // Tamaño de la pelota

    // Velocidad de la pelota
    private int velocidadX = 5;
    private int velocidadY = 5;

    private final int anchoPaleta = 20, altoPaleta = 100;

    // Posicion de la paleta del jugador
    private int jugador1Y = ALTO / 2 - altoPaleta / 2;
    private int jugador2Y = ALTO / 2 - altoPaleta / 2;

    // Velocidad paleta
    private final int velocidadPaleta = 6;

    // Que jugador no se mueva
    private boolean jugador1Arriba = false, jugador1Abajo = false;
    private boolean jugador2Arriba = false, jugador2Abajo = false;

    private int puntosJugador1 = 0;
    private int puntosJugador2 = 0;

    private Timer temporizador;
    private String nombre1;
    private String nombre2;
    private BufferedImage fondo;
    private Font fuente;

    public PanelJuego(String nombre1, String nombre2) {
        this.nombre1 = nombre1;
        this.nombre2 = nombre2;

        setPreferredSize(new Dimension(ANCHO, ALTO));
        setFocusable(true);
        addKeyListener(this);

        try {
            fondo = ImageIO.read(new File("Free-Project/src/resources/fondo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fuente = Font.createFont(Font.TRUETYPE_FONT, new File("Free-Project/src/resources/Poppins-Regular.ttf")).deriveFont(36f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        temporizador = new Timer(10, this); // 10 ms
        temporizador.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (fondo != null) {
            g.drawImage(fondo, 0, 0, ANCHO, ALTO, this);
        }

        g.setColor(Color.WHITE);
        g.fillOval(pelotaX, pelotaY, tamanoPelota, tamanoPelota);

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

        if (pelotaY <= 0 || pelotaY + tamanoPelota >= ALTO) {
            velocidadY *= -1;
        }

        ticks++;
        if (ticks >= 100) {
            segundos++;
            ticks = 0;
        }

        Rectangle rectPelota = new Rectangle(pelotaX, pelotaY, tamanoPelota, tamanoPelota);
        Rectangle rectJugador1 = new Rectangle(30, jugador1Y, anchoPaleta, altoPaleta);
        Rectangle rectJugador2 = new Rectangle(ANCHO - 50, jugador2Y, anchoPaleta, altoPaleta);

        if (rectPelota.intersects(rectJugador1) || rectPelota.intersects(rectJugador2)) {
            velocidadX *= -1;
        }

        if (pelotaX < 0) {
            puntosJugador2++;
            if (puntosJugador2 >= PUNTOS_PARA_GANAR) {
                terminarPartida();
            } else {
                reiniciarPelota();
            }
        }
        if (pelotaX > ANCHO) {
            puntosJugador1++;
            if (puntosJugador1 >= PUNTOS_PARA_GANAR) {
                terminarPartida();
            } else {
                reiniciarPelota();
            }
        }

        repaint();
    }

    private void reiniciarPelota() {
        pelotaX = ANCHO / 2;
        pelotaY = ALTO / 2;
        velocidadX = (Math.random() > 0.5) ? Math.abs(velocidadX) : -Math.abs(velocidadX);
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
    public void keyTyped(KeyEvent e) {
    }

    private void terminarPartida() {
        temporizador.stop(); // Detiene el juego

        // Pasa también los puntos al guardar en BD
        Database.insertarResultado(nombre1, nombre2, puntosJugador1, puntosJugador2);

        // Mostrar mensaje de fin de partida
        String ganador;
        if (puntosJugador1 > puntosJugador2) {
            ganador = nombre1;
        } else {
            ganador = nombre2;
        }
        JOptionPane.showMessageDialog(this, "¡Fin del juego! Ganador: " + ganador);

        // Aquí puedes pedir reiniciar partida o salir
        int opcion = JOptionPane.showConfirmDialog(this, "¿Quieres reiniciar la partida?", "Reiniciar", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            reiniciarJuego();
        } else {
            System.exit(0);
        }
    }

    private void reiniciarJuego() {
        // Reiniciar puntajes y pelota
        puntosJugador1 = 0;
        puntosJugador2 = 0;


        // Pedir nombres de nuevo
        nombre1 = JOptionPane.showInputDialog(this, "Ingrese el nombre del jugador 1:");
        nombre2 = JOptionPane.showInputDialog(this, "Ingrese el nombre del jugador 2:");

        //Reiniciar pelota
        reiniciarPelota();
        temporizador.start();
    }

}
