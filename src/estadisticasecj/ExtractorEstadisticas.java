package estadisticasecj;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractorEstadisticas {

	private HashMap<String,Object> logs = new HashMap<>();
	
	/*
	 * Devuelve la lista de logs
	 */
	public HashMap<String,Object> getLogs(){
		return logs;
	}
	
	/*
	 * Se le pasa la ruta de la carpeta de los logs para que haga su magia.
	 * Rellena el HashMap log con clave el nombre del fichero y dentro va el hashmap
	 * generado por el metodo getDatosFichero
	 */
	public ExtractorEstadisticas(String rutaCarpetaLogs){
		try {

			File carpeta = new File(rutaCarpetaLogs);
			
			ArrayList<File> archivos = getArchivosEnCarpeta(carpeta);
			
			for( File archivo : archivos ){
				logs.put( archivo.getName(), getDatosFichero(archivo) );
			}
			
                } catch (Exception ex) {
                    /*
                        Si no quieres que te pete, aquí deberías manejar la excepcion
                        e imprimirla por pantalla, yo dejo que pete para que si te da
                        algun error puedas saberlo.
                    */
                    ex.printStackTrace();
                }
	}
	
	public static final String NOMBRE_FICHERO       = "NombreFichero";
	public static final String JOBN                 = "JobN";
	public static final String TIPO                 = "Tipo";
	public static final String LISTA_GENERACIONES	= "listaGeneraciones";
	public static final String BEST_OF_THE_RUN      = "bestOfTheRun";
	
	/*
	 * Devuelve un hashmap con los datos del nombre del fichero y un arraylist para agregar los
	 * datos de las generaciones y una ultima key con el mejor del fichero
	 * 
	 * Claves:
	 * 		NombreFichero
	 * 		JobN
	 * 		Tipo
	 * 		listaGeneraciones
	 * 		besOfTheRun
	 * Tipos:
	 * 		NombreFichero 		- String
	 * 		JobN				- Integer (ojo no int, porque hace falta que sea hijo de Object)
	 * 		Tipo				- String
	 * 		listaGeneraciones	- ArrayList<Genearcion>
	 * 		bestOfTheRun		- Generacion
	 */
	public final HashMap<String,Object> getDatosFichero(File archivo) throws Exception{
		HashMap<String,Object> datos = new HashMap<>();
		/*
		 * Parte 1 - Obtengo los datos del nombre del fichero
		 */
		String nombre = archivo.getName();
		Integer jobN = getJobN( nombre );
		String tipo = getTipo( nombre );
		
		datos.put( ExtractorEstadisticas.NOMBRE_FICHERO , nombre );
		datos.put( ExtractorEstadisticas.JOBN , jobN);
		datos.put( ExtractorEstadisticas.TIPO , tipo);
		
		ArrayList<Generacion> generaciones = new ArrayList<>();
		
		/*
		 * Parte 2 - Obtengo los datos del fichero
		 */
		Scanner input;
		
        input = new Scanner( archivo );

        boolean enGeneracion = false;
        String rawLog = "";
        
        /*
         * Magia, prefiero no explicarlo que tardo m�s xD
         */
        while (input.hasNextLine()) {
            String line = input.nextLine();
            
            if( !enGeneracion && esInicioGeneracion(line) ){
            	enGeneracion = true;
            	rawLog += line+"\n";
            }
            else if( enGeneracion && esFinGeneracion(line) ){
            	enGeneracion = false;
            	rawLog += line+"\n";
            	generaciones.add( new Generacion(rawLog) );
            	rawLog = "";
            }
            else if( enGeneracion ){
            	rawLog += line+"\n";
            }
        }
        input.close();
		
        datos.put( ExtractorEstadisticas.LISTA_GENERACIONES , generaciones);
        
        /*
         * La ultima generacion es la best individual of the run, por lo tanto la �ltima del array
         */
        if( generaciones.size() > 0 ){
	        Generacion best = generaciones.get( generaciones.size()-1 );
	        if( best.isBestOfTheRun() ){
	        	datos.put( ExtractorEstadisticas.BEST_OF_THE_RUN , best);
	        }
	        else{
	        	datos.put( ExtractorEstadisticas.BEST_OF_THE_RUN , null );
	        }
        }
        
		return datos;
	}
	
	/*
	 * Esta expresion regular saca tanto el numero (en posicion 1)
	 * como el tipo (en posicion 2)
	 * Si no encuentra nada devuelve null OJO
	 */
	public Matcher getDatosNombreFichero(String nombre){
		Pattern p = Pattern.compile("^job\\.(\\d+)\\.(\\w+)-out\\.stat");
		Matcher m = p.matcher( nombre );

		if (m.find()) {
		    return m;
		}
		return null;
	}
	
	/*
	 * Devuelve un Integer con el numero de job del nombre del fichero
	 */
	public Integer getJobN( String nombre ){
		Integer salida = 0;
		
		/*
		 * Este metodo devuelve el numero (en posicion 1) y
		 * el tipo (en posicion 2)
		 */
		Matcher m = getDatosNombreFichero(nombre);
		
		if( m != null ){
			String numero = m.group(1);
			salida = Integer.valueOf(numero);
		}
		
		return salida;
	}
	
	/*
	 * Devuelve un String con el tipo de ejecucion sacado del nombre del fichero
	 */
	public String getTipo( String nombre ){
		String salida = "";
		
		/*
		 * Este metodo devuelve el numero (en posicion 1) y
		 * el tipo (en posicion 2)
		 */
		Matcher m = getDatosNombreFichero(nombre);
		
		if( m != null ){
			salida = m.group(2);
		}
		
		return salida;
	}

	/*
	 * Devuelve una lista de los ficheros que hay en la carpeta que se le pasa por referencia
	 * en caso de que sea una carpeta
	 */
	public final ArrayList<File> getArchivosEnCarpeta(File carpeta){
      
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:"+carpeta.getAbsolutePath().replace("\\", "\\\\")+"\\\\job\\.(\\d+)\\.(\\w+)-out\\.stat");
      
		ArrayList<File> archivos = new ArrayList<>();
		
		if(carpeta.isDirectory()){
			/*
			 * Itero por los archivos, y los agrego a la lista de archivos que devolver�
			 */
			for( File archivo : carpeta.listFiles() ){
				if((archivo.isFile()) && matcher.matches(archivo.toPath())){
               archivos.add(archivo);
               //System.out.println(archivo.toPath());
				}
			}
		}
		
		return archivos;
	}

	/*
	 * Compruebo si la linea es el inicio de una generacion
	 */
	public boolean esInicioGeneracion(String linea){
		return linea.toLowerCase().contains("generation") || linea.toLowerCase().contains("best individual of run");
	}
	
	/*
	 * Compruebo si la linea es el fin de una generacion
	 */
	public boolean esFinGeneracion(String linea){
		return linea.toLowerCase().contains("fecha");
	}
}
