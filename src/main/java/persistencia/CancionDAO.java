
package persistencia;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import logica.reproductormusica.Log;
import modelo.Cancion;


public class CancionDAO {
    private final Connection conexion;
    private final String CANCIONES_SQL = "SELECT * FROM Cancion";
    private final String CANCION_SQL = "SELECT COUNT(*) FROM Cancion WHERE nombre = ? AND artista = ? AND duracion = ?";
    private final String ACTUALIZAR_PLAYLIST_SQL = "UPDATE Cancion SET idPlaylist = ? WHERE nombre = ? AND artista = ? AND duracion = ?";
    private final String INSERTAR_SQL = 
            "INSERT INTO Cancion(nombre,artista,duracion) VALUES (?,?,?)";
    private final String CANCION_BUSCA_SQL = "SELECT nombre, artista, duracion FROM Cancion WHERE nombre LIKE ? ";

    public CancionDAO() throws SQLException{
        this.conexion = ConexionBD.getConnection();
    }
    
    public void insertarCancion(Cancion cancion) throws SQLException{
        try (PreparedStatement ps = conexion.prepareStatement(this.INSERTAR_SQL)){
            if (!existeCancion(cancion)){
                ps.setString(1, cancion.getNombre());
                ps.setString(2, cancion.getArtista());
                ps.setString(3, cancion.getDuracion());
                ps.executeUpdate();
            }
        }
    }
    
    private boolean existeCancion(Cancion cancion){
        try (PreparedStatement ps = conexion.prepareStatement(CANCION_SQL)){
            ps.setString(1,cancion.getNombre());
            ps.setString(2,cancion.getArtista());
            ps.setString(3,cancion.getDuracion());
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    int cantidad = rs.getInt(1);
                    return cantidad > 0;
                }
            }
        }catch(SQLException e){
            Log.escribirLog("public boolean existeCancion();\t"
                    + "Error al preparar el statement");
        }
        return false;
    }
    
    public Cancion buscarCancion(String texto){
        try (PreparedStatement ps = conexion.prepareStatement(CANCION_BUSCA_SQL)){
            ps.setString(1,texto + '%');
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    String nombreCancion = rs.getString(1);
                    String artistaCancion = rs.getString(2);
                    String duracionCancion = rs.getString(3);
                    return new Cancion(nombreCancion, artistaCancion, duracionCancion);
                }
            }
        }catch(SQLException e){
            Log.escribirLog("No se puede buscar la cancion");
        }
        return new Cancion();
    }
    
    
}
