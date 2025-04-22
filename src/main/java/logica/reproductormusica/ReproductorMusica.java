package logica.reproductormusica;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import igu.Vista;
import javax.swing.SwingUtilities;

public class ReproductorMusica {

    public static void main(String[] args) {
        FlatSolarizedLightIJTheme.setup();
        SwingUtilities.invokeLater(() ->{
            Vista vista = new Vista();
            vista.setVisible(true);
            vista.setLocationRelativeTo(null);
        });
    }
}
