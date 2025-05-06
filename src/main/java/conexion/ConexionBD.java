
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import logica.reproductormusica.Log;


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
                            + "nombre VARCHAR(30) UNIQUE,"
                            + "PRIMARY KEY(idPlaylist));");
            ps.execute(
                    "CREATE TABLE IF NOT EXISTS Cancion("
                            + "idCancion INT AUTO_INCREMENT,"
                            + "nombre VARCHAR(60) NOT NULL,"
                            + "artista VARCHAR(60),"
                            + "duracion VARCHAR(5) NOT NULL,"
                            + "idPlaylist INT,"
                            + "PRIMARY KEY(idCancion),"
                            + "FOREIGN KEY(idPlaylist) REFERENCES PlayList(idPlaylist));");
            
        }catch(SQLException e){
            Log.escribirLog("No se puedo crear las tablas " + e.getMessage());
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
