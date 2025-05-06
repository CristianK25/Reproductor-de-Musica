
package logica.reproductormusica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public class ReproductorService {
    private volatile boolean isPlaying;
    private Player player;
    private Thread hiloCanciones;
    
    public void reproducir(File archivoMP3){
        pausar();
        hiloCanciones = new Thread(() -> {
            try {
                this.isPlaying = true;
                FileInputStream fis = new FileInputStream(archivoMP3);
                player = new Player(fis);
                player.play();
                this.isPlaying = false;
            } catch (FileNotFoundException | JavaLayerException ex) {
                this.isPlaying = false;
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(null, 
                        "Error al reproducir: " + ex.getMessage(),
                        "Error", 
                        JOptionPane.ERROR_MESSAGE));
            }
        });
        hiloCanciones.start();        
    }
    
    public void pausar(){
        if (player != null) {
        player.close();
        player = null;
        }
        if (hiloCanciones != null && hiloCanciones.isAlive()) {
            hiloCanciones.interrupt();
        }
        isPlaying = false;        
    }
    
}
