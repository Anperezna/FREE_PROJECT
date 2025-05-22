package juego;
import javax.swing.*;
import java.awt.*;

public class Principal {
    private JPanel panelMain;

    public Principal() {
        panelMain.setPreferredSize(new Dimension(800, 600));
    }

    public static void main(String[] args) {
        // Solicitar los nombres de los jugadores
        String nombre1 = JOptionPane.showInputDialog("Nombre del Jugador 1:");
        String nombre2;

        do {
            nombre2 = JOptionPane.showInputDialog("Nombre del Jugador 2 (no puede ser igual a Jugador 1):");
            if (nombre2 != null && nombre2.equalsIgnoreCase(nombre1)) {
                JOptionPane.showMessageDialog(null, "Error: ¡Ambos jugadores no pueden tener el mismo nombre!");
            }
        } while (nombre2 != null && nombre2.equalsIgnoreCase(nombre1));


        JFrame ventana = new JFrame("PONG 2 Jugadores");
        PanelJuego juego = new PanelJuego(nombre1, nombre2); // Pasar los nombres al PanelJuego
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar programa al presionar X
        ventana.setResizable(false); // Que no se pueda redimensionar
        ventana.add(juego); // Añadir el PanelJuego al JFrame
        ventana.pack(); // Ajusta automaticamente el tamaño del JFrame
        ventana.setLocationRelativeTo(null); // Si es null, siempre se centrará en el medio de la pantalla
        ventana.setVisible(true); // Que la ventana sea visible siempre, se sobreponga
    }
}
