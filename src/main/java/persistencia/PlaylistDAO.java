
package persistencia;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import logica.reproductormusica.Log;
import modelo.Playlist;


public class PlaylistDAO {
    private final Connection conexion;
    private final String INSERTAR_SQL = "INSERT INTO Playlist(nombre) VALUES (?)";
    private final String PLAYLIST_SQL = "SELECT * FROM Playlist WHERE nombre = ?";
    private final String PLAYLIST_TODOS_SQL = "SELECT * FROM Playlist";

    public PlaylistDAO() throws SQLException{
        this.conexion = ConexionBD.getConnection();
    }
    
    /**
     * Busca en la base de datos la playlist 
     * @return 
     */
    public ArrayList<Playlist> devolverPlaylist(){
        ArrayList<Playlist> playlist = new ArrayList<>();
        try(PreparedStatement ps = conexion.prepareStatement(PLAYLIST_TODOS_SQL)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    String nombre = rs.getString(2);
                    Playlist p = new Playlist(nombre);
                    playlist.add(p);
                }
            }
        }catch(SQLException e){
            Log.escribirLog("No se pudo encontrar Playlist en Base de datos");
        }
        return playlist;
    }
    /**
     * Inserta en la base de datos una playlist si es que no existe en la base de datos
     * segun el nombre de la playlist 
     * @param nombrePlaylist El nombre de la playlist puesta en el arbol
     * @return 1 (si se ejecuto bien) o 0 (si la playlis ya existe)
     */
    public boolean insertarPlaylist(String nombrePlaylist){
        if (!existePlaylist(nombrePlaylist)){
            try (PreparedStatement ps = conexion.prepareStatement(INSERTAR_SQL)){
                ps.setString(1, nombrePlaylist);
                int ejecuto = ps.executeUpdate();
                return ejecuto == 1;
            }catch (SQLException e){
                Log.escribirLog("No se pudo actualizar la playlist :" + e.getMessage());
            }            
        }else
            JOptionPane.showMessageDialog(null, "Esa playlist ya existe");
        return false;
    }
    /**
     * Se fija en al base de datos si el nombre que recibe como parametro coincide
     * con alguno en la base de datos
     * @param nombrePlaylist El nombre de la playlist
     * @return true (si existe la playlist), false (si no existe)
     */
    private boolean existePlaylist(String nombrePlaylist){
        try(PreparedStatement ps = conexion.prepareStatement(PLAYLIST_SQL)){
            ps.setString(1,nombrePlaylist);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }catch(SQLException e){
            Log.escribirLog("No se pudo reconocer si existe o no la Playlist");
        }
        return false;
    }
}
