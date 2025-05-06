
package igu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javazoom.jl.player.Player;
import logica.reproductormusica.Log;
import logica.reproductormusica.ReproductorService;
import modelo.Cancion;
import modelo.Playlist;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import persistencia.CancionDAO;
import persistencia.PlaylistDAO;


public class Vista extends javax.swing.JFrame {
    
    private final String URL_CARPETA_CANCIONES = "./canciones";
    private File[] archivosCanciones;
    private Thread hiloCanciones;
    private int indiceCanciones;
    private volatile boolean isPlaying;
    private Player player;
    private CancionDAO cancioDAO;
    private PlaylistDAO playlistDAO;
    private ReproductorService reproductorService;
    
    /** Creates new form Vista */
    public Vista() {
        initComponents();
        Log.crearArchivoLog();
        iniciarAcciones();
        crearCarpetaCanciones();
        cargarCancionesLista();
        cargarPlaylistDesdeBD();
    }
    
    //METODOS QUE SE EJECUTAN AL ARRANCAR
    /**
     * Iniciar las acciones de todos los elementos 
     */
    private void iniciarAcciones(){
        this.reproductorService = new ReproductorService();
        try{
            this.playlistDAO = new PlaylistDAO();
            this.cancioDAO = new CancionDAO();
        }catch(SQLException e){
            Log.escribirLog("No se pudo crear la playlis dao");
        }
        listaCancionesTodo.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                int indice = listaCancionesTodo.locationToIndex(e.getPoint());
                if (indice != -1) {
                    String nombreCancion = listaCancionesTodo.getModel().getElementAt(indice);
                    textoTituloCancion.setText(nombreCancion);
                }
                Vista.this.indiceCanciones = indice;
            }
        });
        campoBuscarCancion.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                campoBuscarCancion.setText(" ");
            }
        });
        try{
            cancioDAO = new CancionDAO();
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null,"No se pudo conectar la base de datos");
        }
    }
    /**
     * Crea la carpeta que guarda las canciones
     */
    private void crearCarpetaCanciones(){
        File carpeta = new File(URL_CARPETA_CANCIONES);
        if(!carpeta.exists())
            carpeta.mkdir();
    }
    /**
     * Busca en al carpeta que guarda las canciones y las carga en al lista de canciones
     */
    private void cargarCancionesLista(){
        File archivosCancionesTemporal = new File(URL_CARPETA_CANCIONES);
        this.archivosCanciones = archivosCancionesTemporal.listFiles();
        this.listaCancionesTodo.setListData(arregloCanciones());
        cargarBD();
    }
    /**
     * Busca las playlist ya creadas en la base de datos y las cargue en la Vista
     */
    private void cargarPlaylistDesdeBD() {
        // 1. Obtener datos
        ArrayList<Playlist> listaPlaylistCreadas = playlistDAO.devolverPlaylist();

        // 2. Obtener modelo y raíz una sola vez
        DefaultTreeModel modelo = (DefaultTreeModel) arbolPlaylist.getModel();
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();

        // 3. Limpiar nodos existentes (opcional)
        raiz.removeAllChildren();

        // 4. Agregar nuevas playlists
        for (Playlist playlist : listaPlaylistCreadas) {
            DefaultMutableTreeNode nodoPlaylist = new DefaultMutableTreeNode(playlist.getNombre().trim());
            raiz.add(nodoPlaylist);
        }

        // 5. Actualizar el árbol una sola vez
        modelo.reload(raiz);
    }
    /**
     * Carga las canciones que hay en la lista y en la carpeta para meterla en la base de datos si no existe
     */
    private void cargarBD(){
        if (this.archivosCanciones!=null){
            for(File archivo: this.archivosCanciones){
                try{
                    AudioFile audioFile = AudioFileIO.read(archivo);
                    String nombre = audioFile.getTag().getFirst(FieldKey.TITLE);
                    String artista = audioFile.getTag().getFirst(FieldKey.ARTIST);
                    int duracionint = audioFile.getAudioHeader().getTrackLength(); // duracion en segundos
                    String duracion = convertirStringDuracion(duracionint);
                    Cancion cancion = new Cancion(nombre,artista,duracion);
                    this.cancioDAO.insertarCancion(cancion);
                }catch(CannotReadException | SQLException e){
                    Log.escribirLog("No se pudo leer los metadatos del archivo ");
                }catch(IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException i){
                    Log.escribirLog("No se pudo leer el archivo");
                }
            }
        }
    }
    /**
     * Convierte la duracion de las canciones que se extraen del mp3 como enteras y en segundos
     * y las devuelve como String y en minutos.
     * @param dI - La duracion en entero de la cancion
     * @return String - La representacion "00:00" de la cancion
     */
    private String convertirStringDuracion(int duracionEnSegundos){
        int minutos = duracionEnSegundos / 60;
        int segundos = duracionEnSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
    /**
     * Retorna los nombres del arreglo de archivos mp3
     * @return String[] - Los nombres de los archivos mp3
     */
    private String[] arregloCanciones(){
        String[] nombres = new String[this.archivosCanciones.length];
        for (int i = 0; i < this.archivosCanciones.length; i++) {
            File archivo = this.archivosCanciones[i];
            try {
                AudioFile audioFile = AudioFileIO.read(archivo);
                String titulo = audioFile.getTag().getFirst(FieldKey.TITLE);
                String artista = audioFile.getTag().getFirst(FieldKey.ARTIST);
                int duracionInt = audioFile.getAudioHeader().getTrackLength();
                String duracion = convertirStringDuracion(duracionInt);

                // Si falta título o artista, usar el nombre del archivo como fallback
                if (titulo == null || titulo.isEmpty()) titulo = archivo.getName();
                if (artista == null || artista.isEmpty()) artista = "Desconocido";

                nombres[i] = artista + " - " + titulo + " [" + duracion + "]";
            } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | KeyNotFoundException | TagException e) {
                nombres[i] = archivo.getName(); // fallback si falla
            }
        }
        return nombres;
    }
    
    /**
     * Cierra la ventana deteniendo a su vez el reproductor 
     */
    @Override
    public void dispose() {
        this.reproductorService.pausar();
        super.dispose();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupPlaylist = new javax.swing.JPopupMenu();
        agregarPlaylist = new javax.swing.JMenuItem();
        borrarPlaylist = new javax.swing.JMenuItem();
        pestañas = new javax.swing.JTabbedPane();
        pestañaCanciones = new javax.swing.JPanel();
        separador = new javax.swing.JSeparator();
        textoListaCanciones = new javax.swing.JLabel();
        campoBuscarCancion = new javax.swing.JTextField();
        textoTituloCancion = new javax.swing.JLabel();
        botonStop = new javax.swing.JButton();
        botonStart = new javax.swing.JButton();
        botonBuscar = new javax.swing.JButton();
        pestañasPlaylist = new javax.swing.JTabbedPane();
        panelScroll_Todo = new javax.swing.JScrollPane();
        listaCancionesTodo = new javax.swing.JList<>();
        botonActualizarLista = new javax.swing.JButton();
        botonStop1 = new javax.swing.JButton();
        pestañaPlaylist = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        arbolPlaylist = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jProgressBar1 = new javax.swing.JProgressBar();

        agregarPlaylist.setText("Agregar PlayList");
        agregarPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agregarPlaylistActionPerformed(evt);
            }
        });
        popupPlaylist.add(agregarPlaylist);

        borrarPlaylist.setText("Borrar PlayList");
        popupPlaylist.add(borrarPlaylist);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        textoListaCanciones.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        textoListaCanciones.setText("Lista Canciones");

        campoBuscarCancion.setForeground(new java.awt.Color(153, 153, 153));
        campoBuscarCancion.setText("buscar");
        campoBuscarCancion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoBuscarCancionActionPerformed(evt);
            }
        });

        textoTituloCancion.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        textoTituloCancion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textoTituloCancion.setText("Titulo:");
        textoTituloCancion.setMaximumSize(new java.awt.Dimension(189, 32));
        textoTituloCancion.setMinimumSize(new java.awt.Dimension(189, 32));

        botonStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaOriginal.png"))); // NOI18N
        botonStop.setContentAreaFilled(false);
        botonStop.setMaximumSize(new java.awt.Dimension(32, 32));
        botonStop.setMinimumSize(new java.awt.Dimension(32, 32));
        botonStop.setPreferredSize(new java.awt.Dimension(32, 32));
        botonStop.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaPresionado.png"))); // NOI18N
        botonStop.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaAdentro.png"))); // NOI18N
        botonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonStopActionPerformed(evt);
            }
        });

        botonStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reproducirOriginal.png"))); // NOI18N
        botonStart.setContentAreaFilled(false);
        botonStart.setMaximumSize(new java.awt.Dimension(32, 32));
        botonStart.setMinimumSize(new java.awt.Dimension(32, 32));
        botonStart.setPreferredSize(new java.awt.Dimension(32, 32));
        botonStart.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reproducirPresionado.png"))); // NOI18N
        botonStart.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reproducirAdentro.png"))); // NOI18N
        botonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonStartActionPerformed(evt);
            }
        });

        botonBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/busquedaOriginal.png"))); // NOI18N
        botonBuscar.setContentAreaFilled(false);
        botonBuscar.setMaximumSize(new java.awt.Dimension(32, 32));
        botonBuscar.setMinimumSize(new java.awt.Dimension(32, 32));
        botonBuscar.setPreferredSize(new java.awt.Dimension(32, 32));
        botonBuscar.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/busquedaPresionado.png"))); // NOI18N
        botonBuscar.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/busquedaAdentro.png"))); // NOI18N
        botonBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBuscarActionPerformed(evt);
            }
        });

        panelScroll_Todo.setViewportView(listaCancionesTodo);

        pestañasPlaylist.addTab("Todo", panelScroll_Todo);

        botonActualizarLista.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/actualizarOriginal.png"))); // NOI18N
        botonActualizarLista.setContentAreaFilled(false);
        botonActualizarLista.setMaximumSize(new java.awt.Dimension(24, 24));
        botonActualizarLista.setMinimumSize(new java.awt.Dimension(24, 24));
        botonActualizarLista.setPreferredSize(new java.awt.Dimension(24, 24));
        botonActualizarLista.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/actualizarPresionado.png"))); // NOI18N
        botonActualizarLista.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/actualizarAdentro.png"))); // NOI18N
        botonActualizarLista.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonActualizarListaActionPerformed(evt);
            }
        });

        botonStop1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaOriginal.png"))); // NOI18N
        botonStop1.setContentAreaFilled(false);
        botonStop1.setMaximumSize(new java.awt.Dimension(32, 32));
        botonStop1.setMinimumSize(new java.awt.Dimension(32, 32));
        botonStop1.setPreferredSize(new java.awt.Dimension(32, 32));
        botonStop1.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaPresionado.png"))); // NOI18N
        botonStop1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pausaAdentro.png"))); // NOI18N
        botonStop1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonStop1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pestañaCancionesLayout = new javax.swing.GroupLayout(pestañaCanciones);
        pestañaCanciones.setLayout(pestañaCancionesLayout);
        pestañaCancionesLayout.setHorizontalGroup(
            pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pestañaCancionesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pestañaCancionesLayout.createSequentialGroup()
                        .addComponent(textoListaCanciones, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                        .addComponent(botonBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campoBuscarCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pestañasPlaylist, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pestañaCancionesLayout.createSequentialGroup()
                        .addComponent(textoTituloCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(botonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botonStop1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pestañaCancionesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botonActualizarLista, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(separador))
                .addContainerGap())
        );
        pestañaCancionesLayout.setVerticalGroup(
            pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pestañaCancionesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(textoListaCanciones, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addComponent(campoBuscarCancion))
                    .addComponent(botonBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(botonActualizarLista, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pestañasPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separador, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(pestañaCancionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textoTituloCancion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonStop1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        pestañas.addTab("Canciones", pestañaCanciones);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("PLAYLIST");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("PlayLists");
        arbolPlaylist.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        arbolPlaylist.setComponentPopupMenu(popupPlaylist);
        jScrollPane1.setViewportView(arbolPlaylist);

        javax.swing.GroupLayout pestañaPlaylistLayout = new javax.swing.GroupLayout(pestañaPlaylist);
        pestañaPlaylist.setLayout(pestañaPlaylistLayout);
        pestañaPlaylistLayout.setHorizontalGroup(
            pestañaPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pestañaPlaylistLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pestañaPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        pestañaPlaylistLayout.setVerticalGroup(
            pestañaPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pestañaPlaylistLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addContainerGap())
        );

        pestañas.addTab("PlayLists", pestañaPlaylist);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pestañas)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pestañas, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonStartActionPerformed
        File file = this.archivosCanciones[indiceCanciones];
        this.reproductorService.reproducir(file);
    }//GEN-LAST:event_botonStartActionPerformed

    private void botonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarActionPerformed
        String texto = this.campoBuscarCancion.getText();
        Cancion cancion = this.cancioDAO.buscarCancion(texto);
    }//GEN-LAST:event_botonBuscarActionPerformed

    private void botonActualizarListaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonActualizarListaActionPerformed
        cargarCancionesLista();
    }//GEN-LAST:event_botonActualizarListaActionPerformed

    private void botonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonStopActionPerformed
        this.reproductorService.pausar();
    }//GEN-LAST:event_botonStopActionPerformed

    private void campoBuscarCancionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoBuscarCancionActionPerformed

    }//GEN-LAST:event_campoBuscarCancionActionPerformed

    private void agregarPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarPlaylistActionPerformed
        String nombrePlaylist = JOptionPane.showInputDialog("Ingrese el nombre de la playlist");
        if (nombrePlaylist != null && !nombrePlaylist.trim().isEmpty()) {
             nombrePlaylist = nombrePlaylist.trim(); // limpiar espacios
            if (this.playlistDAO.insertarPlaylist(nombrePlaylist)){
                 // Solo si se insertó correctamente en la base de datos, agregar al árbol
                DefaultTreeModel modelo = (DefaultTreeModel) arbolPlaylist.getModel();
                DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();
                DefaultMutableTreeNode nuevaPlaylist = new DefaultMutableTreeNode(nombrePlaylist.trim());
                raiz.add(nuevaPlaylist);
                modelo.reload(); // actualiza el árbol visualmente
            }
        }
    }//GEN-LAST:event_agregarPlaylistActionPerformed

    private void botonStop1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonStop1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_botonStop1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem agregarPlaylist;
    private javax.swing.JTree arbolPlaylist;
    private javax.swing.JMenuItem borrarPlaylist;
    private javax.swing.JButton botonActualizarLista;
    private javax.swing.JButton botonBuscar;
    private javax.swing.JButton botonStart;
    private javax.swing.JButton botonStop;
    private javax.swing.JButton botonStop1;
    private javax.swing.JTextField campoBuscarCancion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JList<String> listaCancionesTodo;
    private javax.swing.JScrollPane panelScroll_Todo;
    private javax.swing.JPanel pestañaCanciones;
    private javax.swing.JPanel pestañaPlaylist;
    private javax.swing.JTabbedPane pestañas;
    private javax.swing.JTabbedPane pestañasPlaylist;
    private javax.swing.JPopupMenu popupPlaylist;
    private javax.swing.JSeparator separador;
    private javax.swing.JLabel textoListaCanciones;
    private javax.swing.JLabel textoTituloCancion;
    // End of variables declaration//GEN-END:variables

}
