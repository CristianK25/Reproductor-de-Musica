
package logica.reproductormusica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class Log {
    private static final String RUTA = "./log.txt";
    private static final File ARCHIVO = new File(RUTA);
    
    /**
     * Crea un archivo txt en donde van a estar guardados las excepciones que se produzcan
     */
    public static void crearArchivoLog(){
        try{
            if(!ARCHIVO.exists()){
                ARCHIVO.createNewFile();
                ARCHIVO.setWritable(false);
            }
        }catch(IOException i){
            System.out.println();
        }
    }
    
    /**
     * Escribe en el archivo txt el mensaje que se le manda por parametro
     * @param mensaje 
     */
    public static void escribirLog(String mensaje){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = timestamp + " - " + mensaje + "\n";
        try{
            if(ARCHIVO.exists()){
                ARCHIVO.setWritable(true);
                try (FileWriter ARCHIVOEditable = new FileWriter(ARCHIVO, true)) {
                    ARCHIVOEditable.write(logEntry + "\n");
                }
                ARCHIVO.setWritable(false);
            }
        }catch(IOException e){
            
        }
    }

}
