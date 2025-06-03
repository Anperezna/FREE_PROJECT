package juego;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/free_project";
    private static final String USER = "root";
    private static final String PASS = "mysql";

    public static void insertarResultado(String nombre1, String nombre2, int puntosJugador1, int puntosJugador2) {
        Connection con = null;
        PreparedStatement psInsertUsuario = null;
        PreparedStatement psInsertPartida = null;

        try {
            con = DriverManager.getConnection(URL, USER, PASS);
            con.setAutoCommit(false);  // Para manejo transaccional

            // Insertar usuario 1
            String insertUsuario = "INSERT INTO usuario (nombre, fecha) VALUES (?, NOW())";
            psInsertUsuario = con.prepareStatement(insertUsuario, Statement.RETURN_GENERATED_KEYS);

            // Asignar un valor a un parámetro
            psInsertUsuario.setString(1, nombre1);

            // Ejecuta la instruccion para la base de datos
            psInsertUsuario.executeUpdate();

            // Recupera las claves primarias
            ResultSet generatedKeys1 = psInsertUsuario.getGeneratedKeys();

            int idUsuario1 = -1;
            if (generatedKeys1.next()) {
                idUsuario1 = generatedKeys1.getInt(1);
            }
            generatedKeys1.close();
            psInsertUsuario.close();

            // Insertar usuario 2
            psInsertUsuario = con.prepareStatement(insertUsuario, Statement.RETURN_GENERATED_KEYS);

            // Asignar un valor a un parámetro
            psInsertUsuario.setString(1, nombre2);

            // Ejecuta la instruccion para la base de datos
            psInsertUsuario.executeUpdate();

            // Recupera las claves primarias
            ResultSet generatedKeys2 = psInsertUsuario.getGeneratedKeys();
            int idUsuario2 = -1;
            if (generatedKeys2.next()) {
                idUsuario2 = generatedKeys2.getInt(1);
            }
            generatedKeys2.close();
            psInsertUsuario.close();

            System.out.println("ID de usuario 1 (nuevo): " + idUsuario1);
            System.out.println("ID de usuario 2 (nuevo): " + idUsuario2);

            // Calcular id del ganador (puede ser idUsuario1, idUsuario2 o null si empate)
            Integer idGanador = null;
            if (puntosJugador1 > puntosJugador2) {
                idGanador = idUsuario1;
            } else if (puntosJugador2 > puntosJugador1) {
                idGanador = idUsuario2;
            }

            // Insertar partida
            String insertPartida = "INSERT INTO partida (fecha, id_jugador_1, id_jugador_2, puntos_jugador_1, puntos_jugador_2, ganador) VALUES (NOW(), ?, ?, ?, ?, ?)";
            psInsertPartida = con.prepareStatement(insertPartida);
            psInsertPartida.setInt(1, idUsuario1);
            psInsertPartida.setInt(2, idUsuario2);
            psInsertPartida.setInt(3, puntosJugador1);
            psInsertPartida.setInt(4, puntosJugador2);

            if (idGanador != null) {
                psInsertPartida.setInt(5, idGanador);
            } else {
                psInsertPartida.setNull(5, Types.INTEGER);
            }

            psInsertPartida.executeUpdate();
            psInsertPartida.close();

            con.commit();

            System.out.println("Partida guardada exitosamente.");

        } catch (SQLException e) {

            //Dar rollback si da error
            System.out.println("Error de base de datos: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error rollback: " + ex.getMessage());
            }
        }
    }
}
