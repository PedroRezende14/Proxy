package Sock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BancodeDados {

    protected static String nameBanco = "Proxy";
    protected static String user = "root";
    protected static String password = "";
    protected static String host = "localhost";
    protected static int porta = 3306;

    public static Connection conectar() {
        Connection conexao = null;
        try {
            String url = "jdbc:mysql://" + host + ":" + porta + "/" + nameBanco;
            conexao = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados.");
            e.printStackTrace();
        }
        return conexao;
    }

    public static String consultarBD(int id) {
        String sql = "SELECT * FROM requisicoes WHERE id = ?";
        String resultado = null;

        try (Connection conexao = conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                resultado = rs.getString("dado");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultado;
    }
}
