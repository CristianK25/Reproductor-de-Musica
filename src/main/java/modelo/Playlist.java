
package modelo;

public class Playlist {
    private String nombre;

    public Playlist(String nombre) {
        this.nombre = nombre;
    }
 
    /*
    Getters and Setters
    */

    public String getNombre() {
        return nombre;
    }
    
    public void setGenero(String genero) {
        if(genero == null || genero.trim().isEmpty()){
            throw new IllegalArgumentException("EL genero no puede estar vacio");
        }
        this.nombre = genero;
    }

}
