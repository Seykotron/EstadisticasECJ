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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generacion {

	private String rawLog;
	private int generation;
	private String bestIndividual;
	private String evaluated;
	private float fitness;
	private float validationFitness;
	private String tree;
	private String lisp;
	private int idLisp = 0;
	private String lisp2 = "";
	private String fecha;
	
	private String lispFinal = "";
	private int deepFinal = 0;
	private int complejidadFinal = 0;
	
	private boolean bestOfTheRun = false;
	
	private int deep;
	private int complejidad;
	
	private int deep2;
	private int complejidad2;
	
	public Generacion(String rawLog){
		this.rawLog = rawLog;
		generarAtributos();                
                calcularCon2Lisp();
	}

        private enum Estados{
            AuxFinal,
            ADF
        }
    
        private enum EstadosADF{
            ARG0,
            ARG1
        }
        
        /*
            Se trata la cadena para obtener un array con cada "nodo" ya sea palabra o
            parentesis para despues tratarlo y obtener el LISP final de uno con ADF
        */
        private String[] tratarCadena( String cadena ){
            cadena = separarParentesis(cadena);
            cadena = cadena.trim();
            return cadena.split("\\s+");
        }

        /*
            Le añade espacio a los parentesis para que se pueda calcular bien el LISP con ADF
        */
        private String separarParentesis( String cadena ){
            cadena = cadena.replace("(", " ( ");
            cadena = cadena.replace(")", " ) ");
            return cadena;
        }
        
        /*
            Quita el primer y el ultimo parentesis del string que se le pase
        */
        private String quitarPrimerYUltimoParentesis(String texto) {
            String aux = texto.trim();
            aux = aux.substring(1,aux.length()-1);
            return aux;
        }
        
        /*
            Los arrays de string si se pasan por referencia, los String no.
        */
        private void escribir( String t, Estados estado, EstadosADF estadoADF, String[] auxFinal, String[] arg0, String[] arg1 ){
            if( estado == Estados.AuxFinal ){
                auxFinal[0] += t;
            }
            else if( estado == Estados.ADF ){
                switch( estadoADF ){
                    case ARG0:
                        arg0[0] += t;
                        break;
                    case ARG1:
                        arg1[0] += t;
                        break;
                }
            }
        }
        
        /*
            Calcula el LISP cuando tiene ADF
        */
        private void calcularLispConADF(){
            lisp2 = separarParentesis(lisp2);
            lisp2 = quitarPrimerYUltimoParentesis(lisp2);

            String lispPrimario = lisp;

            do{
                String[] buffer = tratarCadena(lisp);

                Estados estado = Estados.AuxFinal;
                EstadosADF estadoADF = EstadosADF.ARG0;

                int parentesisCuandoADF = 1;
                int parentesisArg = 0;

                String[] arg0 = { "" };
                String[] arg1 = { "" };
                String[] auxFinal = { "" };
                String auxLisp2 = "";

                for( String nodo : buffer ){
                    if( nodo.equals("(") ){
                        if( estado == Estados.ADF ){
                            parentesisCuandoADF++;
                            parentesisArg++;
                        }
                        escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                    }
                    else if( nodo.equals(")") ){
                        if( estado == Estados.ADF ){

                            parentesisCuandoADF--;
                            parentesisArg--;

                            if( parentesisCuandoADF == 0 ){
                                estado = Estados.AuxFinal;

                                auxLisp2 = auxLisp2.replaceAll("ARG0", arg0[0] );
                                auxLisp2 = auxLisp2.replaceAll("ARG1", arg1[0] );

                                escribir( auxLisp2, estado, estadoADF, auxFinal, arg0, arg1 );
                                /*
                                    Escribo el paréntesis despues del ADF
                                */
                                escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                            }
                            else if( parentesisArg == 0 ){
                                escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                                /*
                                    Le cambio de estado
                                */
                                if( estadoADF == EstadosADF.ARG0 ){
                                    estadoADF = EstadosADF.ARG1;
                                }
                                else{
                                    estadoADF = EstadosADF.ARG0;
                                }
                            }
                            else{
                                escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                            }
                        }
                        else{
                            escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                        }
                    }
                    else if( nodo.contains("ADF") && estado != Estados.ADF ){
                        estado = Estados.ADF;
                        estadoADF = EstadosADF.ARG0;
                        parentesisCuandoADF = 1;
                        parentesisArg = 0;
                        auxLisp2 = lisp2;
                        arg0[0] = "";
                        arg1[0] = "";
                    }
                    else{
                        escribir( " "+nodo, estado, estadoADF, auxFinal, arg0, arg1 );
                    }
                }
                lisp = auxFinal[0];

            }while( lisp.contains("ADF") );
            
            this.lispFinal = lisp;
            this.complejidadFinal = getCantidadNodos( this.lispFinal );
            this.deepFinal = getProfundidad( this.lispFinal );
            this.lisp = lispPrimario;
            
        }
        
        /*
            Calcula el LISP
        */
	private void calcularCon2Lisp(){
		if( this.idLisp != 0 && this.lisp.contains("ADF1") ){
                    calcularLispConADF();
		}
		else{
			this.lispFinal = this.lisp;
			this.complejidadFinal = this.complejidad;
			this.deepFinal = this.deep;
		}
	}
	
	/*
	 * Imprime los atributos
	 */
	public void imprimirPorPantalla(){
		System.out.println(this.toString());
	}
	
	/*
	 * sobrescribo el metodo to string
	 */
	
	@Override
	public String toString(){
		String aux = 	"---------------------------------------------------------------\n";
		aux += "Generacion: \n";
		aux += ""+this.getGeneration()+"\n";
		aux += "Mejor Individual: \n";
		aux += ""+this.getBestIndividual()+"\n";
		aux += "Evaluado: \n";
		aux += ""+this.getEvaluated()+"\n";
		aux += "Fitness: \n";
		aux += ""+this.getFitness()+"\n";
		aux += "ValidationFitness: \n";
		aux += ""+this.getValidationFitness()+"\n";
		aux += "Arbol: \n";
		aux += ""+this.getTree()+"\n";
		
		/**
		if( idLisp != 0 ){
                /**
			aux += "Lisp: \n";
			aux += ""+this.getLisp()+"\n";
			aux += "Profundidad: \n";
			aux += ""+this.getProfundidad()+"\n";
			aux += "Complejidad: \n";
			aux += ""+this.getComplejidad()+"\n";
		
		
			aux += "Lisp2: \n";
			aux += ""+this.getLisp2()+"\n";
			aux += "Profundidad: \n";
			aux += ""+this.getProfundidad2()+"\n";
			aux += "Complejidad: \n";
			aux += ""+this.getComplejidad2()+"\n";
		/**/
			
			//if( this.lisp.contains("ADF1") ){
				aux += "LispFinal: \n";
				aux += ""+this.getLispFinal()+"\n";
				aux += "ProfundidadFinal: \n";
				aux += ""+this.getProfundidadFinal()+"\n";
				aux += "ComplejidadFinal: \n";
				aux += ""+this.getComplejidadFinal()+"\n";
			//}
		//}/**
		//else if( idLisp == 0 ){
		//	aux += "Lisp: \n";
		//	aux += ""+this.getLisp()+"\n";
		//	aux += "Profundidad: \n";
		//	aux += ""+this.getProfundidad()+"\n";
		//	aux += "Complejidad: \n";
		//	aux += ""+this.getComplejidad()+"\n";
		//}/**/
		
		aux += "Fecha: \n";
		aux += ""+this.getFecha()+"\n";
		aux += 			"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
		return aux;
	}
	
	/*
	 * Obtengo la profundidad de una expresion LISP
	 */
	public int getProfundidad( String lisp ){
		int resultado = 0;
		
		int max = 0;
		int contador = 1;
		
		if( lisp == null ){
			return 0;
		}
		
		for( char c : lisp.toCharArray() ){
			if( c == '(' ){
				contador++;
			}
			else if( c == ')' ){
				if( contador > max ){
					max = contador;
				}
				contador--;
			}
		}
		
		if( max > 0 ){
			resultado = max;
		}
		
		return resultado;
	}
	
	/*
	 * Obtengo los nodos de una expresion Lisp
	 */
	public int getCantidadNodos(String lisp){
		int resultado = 0;
		
		String aux = lisp;
		
		/*
		 * Elimino los parentesis
		 */
		aux = aux.replaceAll("\\(", "" );
		aux = aux.replaceAll( "\\)", "" );
		
		/*
		 * Separo por espacios
		 */
		
		String[] palabras = aux.split(" ");
		
		/*
		 * Ahora lo hago con expresiones regulares, si la palabra es una word (de expresion regular \w) entonces
		 * cuenta como nodo
		 */

		 // Creo el objeto de pattern de expresion regular
		Pattern r = Pattern.compile("\\w");
		for( String palabra : palabras ){
			if(palabra != null){
				Matcher m = r.matcher(palabra);
				if (m.find()) {
					resultado++;
				}
			}
		}
		
		return resultado;
	}
	
	/*
	 * Genera los atributos de la clase a partir del rawlog que se le pasa
	 */
	private void generarAtributos(){
		String[] lineas = rawLog.split("\n");
		
		boolean esGraph = false;
		boolean esLisp = false;
		
		String aux = "";
		
		for( String linea : lineas ){
			if( !esGraph && !esLisp ){
				if( linea.toLowerCase().contains("generation") || linea.toLowerCase().contains("best individual of run") ){
					/*
					 * Obtengo los datos de la generacion
					 */
					String[] textos = linea.split(":");
					if( textos.length > 1 ){
						this.generation = Integer.valueOf(textos[1].trim());
					}
					
					/*
					 * Si contiene esta cadena, es el mejor
					 */
					if(linea.toLowerCase().contains("best individual of run")){
						bestOfTheRun = true;
					}
				}
				else if( linea.toLowerCase().contains("best individual") ){
					/*
					 * Obtengo los datos del best individual
					 * OJO puede estar vacio
					 */
					String[] textos = linea.split(":");
					if( textos.length > 1 ){
						this.bestIndividual = textos[1].trim();
					}
					else{
						this.bestIndividual = "";
					}
				}
				else if(linea.toLowerCase().contains("evaluated") ){
					/*
					 * Obtego los datos del evaluated
					 * OJO puede estar vacio
					 */
					String[] textos = linea.split(":");
					if( textos.length > 1 ){
						this.evaluated = textos[1].trim();
					}
					else{
						this.evaluated = "";
					}
				}
				else if(linea.contains("ValidationFitness") ){
					/*
					 * Obtego los datos del validationfitness
					 */
					String[] textos = linea.split(":");
					if( textos.length > 0 ){
						this.validationFitness = Float.valueOf( textos[1].trim() );
					}
				}
				else if(linea.toLowerCase().contains("fitness") ){
					/*
					 * Obtego los datos del fitness
					 */
					String[] textos = linea.split(":");
					if( textos.length > 1 ){
						this.fitness = Float.valueOf( textos[1].trim() );
					}
				}
				else if(linea.toLowerCase().contains("tree") ){
					/*
					 * Hemos llegado al tree
					 */
					esGraph = true;
				}
				else if(linea.toLowerCase().contains("fecha") ){
					/*
					 * Obtego los datos de la fecha
					 */
					this.fecha = linea.replace("Fecha:", "").trim();
				}
				
			}
			else if( esGraph ){
				/*
				 * Aqui obtengo los datos del Tree, el tree termina siempre con }
				 */
				if( linea.toLowerCase().contains("}") ){
					esGraph = false;
					aux+= linea;
					this.tree = aux;
					aux = "";
					esLisp = true;
				}
				else{
					aux += linea;
				}
			}
			else if( esLisp ){
				
				/*
				 * El lisp siempre termina antes de que aparezca la palabra ERCs:
				 * por lo tanto si esta la palabra es que se ha terminado el lisp
				 */
				if( linea.toLowerCase().contains("ercs") || linea.toLowerCase().contains("fecha") || linea.toLowerCase().contains("tree") ){
					esLisp = false;
					if( idLisp == 0 ){
						this.lisp = aux;
						this.deep = getProfundidad(this.lisp);
						this.complejidad = getCantidadNodos(this.lisp);
						aux = "";
					}
					else{
						this.lisp2 = aux;
						this.deep2 = getProfundidad(this.lisp2);
						this.complejidad2 = getCantidadNodos(this.lisp2);
						aux = "";
					}
					
					if( linea.toLowerCase().contains("tree") ){
						esGraph = true;
						this.idLisp = 1;
					}
					else if( linea.toLowerCase().contains("fecha") ){
						this.fecha = linea.replace("Fecha:", "").trim();
					}
				}
				else{
					aux += linea;
				}
			}
		}
	}

	
	public int getProfundidad() {
		return deep;
	}
	public void setProfundidad(int deep) {
		this.deep = deep;
	}
	public int getComplejidad() {
		return complejidad;
	}
	public void setComplejidad(int complejidad) {
		this.complejidad = complejidad;
	}
	public String getRawLog() {
		return rawLog;
	}
	public void setRawLog(String rawLog) {
		this.rawLog = rawLog;
	}
	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}
	public String getBestIndividual() {
		return bestIndividual;
	}
	public void setBestIndividual(String bestIndividual) {
		this.bestIndividual = bestIndividual;
	}
	public String getEvaluated() {
		return evaluated;
	}
	public void setEvaluated(String evaluated) {
		this.evaluated = evaluated;
	}
	public float getFitness() {
		return fitness;
	}
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}
	public float getValidationFitness() {
		return validationFitness;
	}
	public void setValidationFitness(float validationFitness) {
		this.validationFitness = validationFitness;
	}
	public String getTree() {
		return tree;
	}
	public void setTree(String tree) {
		this.tree = tree;
	}
	public String getLisp() {
		return lisp;
	}
	public void setLisp(String listp) {
		this.lisp = listp;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public boolean isBestOfTheRun() {
		return bestOfTheRun;
	}
	public void setBestOfTheRun(boolean bestOfTheRun) {
		this.bestOfTheRun = bestOfTheRun;
	}
	public String getLisp2() {
		return lisp2;
	}
	public void setLisp2(String lisp2) {
		this.lisp2 = lisp2;
	}
	public int getProfundidad2() {
		return deep2;
	}
	public void setProfundidad2(int deep2) {
		this.deep2 = deep2;
	}
	public int getComplejidad2() {
		return complejidad2;
	}
	public void setComplejidad2(int complejidad2) {
		this.complejidad2 = complejidad2;
	}
	public String getLispFinal() {
		return lispFinal;
	}
	public void setLispFinal(String lispFinal) {
		this.lispFinal = lispFinal;
	}
	public int getProfundidadFinal() {
		return deepFinal;
	}
	public void setProfundidadFinal(int deepFinal) {
		this.deepFinal = deepFinal;
	}
	public int getComplejidadFinal() {
		return complejidadFinal;
	}
	public void setComplejidadFinal(int complejidadFinal) {
		this.complejidadFinal = complejidadFinal;
	}
	
	
}
