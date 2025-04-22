
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public final class ConexionBD {
    private static final String URL = "jdbc:h2:./db/ReproductorDB";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    
    /**
     * Inicia las tablas de las bases de datos
     */
    public static void initDB() throws SQLException{
        try (Connection con = getConnection();
                Statement ps = con.createStatement()){
            ps.execute(
                    "CREATE TABLE IF NOT EXISTS PlayList("
                            + "idPlaylist INT AUTO_INCREMENT,"
                            + "genero VARCHAR(30),"
                            + "PRIMARY KEY(idPlaylist);");
            ps.execute(
                    "CREATE TABLE IF NOT EXISTS Cancion("
                            + "idCancion INT AUTO_INCREMENT,"
                            + "nombre VARCHAR(60),"
                            + "artista VARCHAR(60),"
                            + "duracion VARCHAR(5),"
                            + "idPlaylist INT"
                            + "PRIMARY KEY(idCancion)"
                            + "FOREIGN KEY(idPlaylist) REFERENCES PlayList(idPlaylist);");
            
        }catch(SQLException e){
            System.out.println("No se pudo crear las tablas: "+ e.getMessage());
            throw e;
        }
    }
    
    /**
     * Devuelve un objeto con la conexion a la base de datos 
     * @return Connection
     * @throws SQLException 
     */
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
