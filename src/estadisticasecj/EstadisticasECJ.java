/*
 * Copyright (C) 2016 Seykotron
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package estadisticasecj;

import static estadisticasecj.ExtractorEstadisticas.BEST_OF_THE_RUN;
import static estadisticasecj.ExtractorEstadisticas.JOBN;
import static estadisticasecj.ExtractorEstadisticas.LISTA_GENERACIONES;
import static estadisticasecj.ExtractorEstadisticas.NOMBRE_FICHERO;
import static estadisticasecj.ExtractorEstadisticas.TIPO;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Seykotron
 */
public class EstadisticasECJ {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Ejemplo de uso
        
        ExtractorEstadisticas e = new ExtractorEstadisticas("C:\\Users\\Miguel Angel\\Documents\\NetBeansProjects\\EstadisticasECJ\\logs\\");
		
        HashMap<String,Object> logs = e.getLogs();

        for( String key : logs.keySet() ){
                System.out.println(key);
                HashMap<String,Object> estadisticas = (HashMap<String,Object>) logs.get(key);

                System.out.println("Imprimiendo datos Fichero");
                System.out.println( "NombreFichero: "+estadisticas.get(NOMBRE_FICHERO) );
                System.out.println( "JobN:          "+estadisticas.get(JOBN) );
                System.out.println( "Tipo:          "+estadisticas.get(TIPO) );
                System.out.println( "Generaciones");
                System.out.println( "**********************************************************");
                ArrayList<Generacion> generaciones = (ArrayList<Generacion>) estadisticas.get(LISTA_GENERACIONES);
                for( Generacion generacion : generaciones ){
                        generacion.imprimirPorPantalla();
                }
                System.out.println( "Best of the Run");
                System.out.println( "**********************************************************");
                Generacion best = (Generacion) estadisticas.get(BEST_OF_THE_RUN);
                if( best != null ){
                        best.imprimirPorPantalla();
                }
                else{
                        System.out.println("No tiene best.");
                }
        }
    }
    
}
