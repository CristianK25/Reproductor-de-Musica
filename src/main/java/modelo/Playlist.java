
package modelo;

import java.util.ArrayList;


public class Playlist {
    private String genero;
    private ArrayList<Cancion> canciones;

    public Playlist(String genero) {
        this.canciones = new ArrayList<>();
        this.genero = genero;
    }
    
    public void agregarCancion(Cancion c){
        this.canciones.add(c);
    }
    public void eliminarCancion(Cancion c){
        this.canciones.remove(c);
    }
    public boolean contieneCancion(Cancion c){
        return canciones.contains(c);
    }

    /*
    Getters and Setters
    */
    
    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        if(genero == null || genero.trim().isEmpty()){
            throw new IllegalArgumentException("EL genero no puede estar vacio");
        }
        this.genero = genero;
    }

    public ArrayList<Cancion> getCanciones() {
        return canciones;
    }

    public void setCanciones(ArrayList<Cancion> canciones) {
        if(canciones == null) throw new IllegalArgumentException("No hay canciones puestas");
        this.canciones = canciones;
    }  
}
