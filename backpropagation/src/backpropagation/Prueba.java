/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backpropagation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;


/**
 *
 * @author eduardomartinez
 */
public class Prueba {
    
    private BackPropagation net = null;
    final static Charset ENCODING = StandardCharsets.UTF_8;
    // private int ciclosMax = 1000;
    
    /**
     * @param args the command line arguments
    */
    public static void main(String[] args) {
        // TODO code application logic here
        
        BufferedReader Fbr,Kbr;
        Kbr = new BufferedReader(new InputStreamReader(System.in));
        
        String maquina = "111110010200411310311001"; //castor 4 estados 13 1's en 107 pasos
        int ventana = 5;
        int ciclosMax = 1000;
        int noEstados = 4;
        int[] cinta = new int[1000];
        for(int i=0;i<cinta.length;i++){
            cinta[i]=0;
        }
        
        System.out.println("Cinta inicial:");
        for(int i=0;i<cinta.length;i++){
            System.out.print(cinta[i]);
        }
        System.out.println("");
        
        ArrayList<int[]> ejecucion = ejecutarMaquina(maquina, noEstados, cinta, ventana, ciclosMax);
        System.out.println("Ejecucion: ");
        for(int i=0;i<ejecucion.size();i++){
            for(int j=0;j<ejecucion.get(i).length;j++)
                System.out.print("\t" + ejecucion.get(i)[j]);
            System.out.println("");
        }
    }
    
    
    public static ArrayList<double[]> leerArchivo(String archivo){
        ArrayList<double[]> entradas = new ArrayList();
        Path path = Paths.get(archivo);
        try (BufferedReader reader = Files.newBufferedReader(path, ENCODING)){
            String line = null;
            while ((line = reader.readLine()) != null) {
                //process each line in some way
                String[] indep = line.split(",");
                
                double[] datos = new double[indep.length];
                for(int i=0;i<indep.length;i++){
                    datos[i]=Double.parseDouble(indep[i]);
                }
                entradas.add(datos);
                // imprime contenido del archivo
                // System.out.println(line);
            }
        }
        catch(Exception ex){
            System.out.println("Excepción: " + ex.getMessage());
        }
        return entradas;
    }
    
    public static ArrayList<int[]> ejecutarMaquina(String maquina, int noEstadosMaquina, int[] cintaInicial, int ventana, int ciclosMax) {
        ArrayList<int[]> ordenEjecucion = new ArrayList();
        Stack st = new Stack();
        
        int[] estadoVisitado = new int[noEstadosMaquina];
        for(int i=0;i<noEstadosMaquina;i++){
            estadoVisitado[i] = 0;
        }
        
        int [][] estados = new int [noEstadosMaquina][6];
        // decodificar estados de la maquina de turing
        for(int i=0;i<noEstadosMaquina;i++) {
            int inicio = i*6;
            int fin = inicio + 6;
            String substring = maquina.substring(inicio, fin);
            // caso 0: estado_siguiente, 0/1, Movimiento
            estados[i][0] = Integer.parseInt(substring.substring(0, 1)); // estado
            estados[i][1] = Integer.parseInt(substring.substring(1, 2)); // reemplazo
            estados[i][2] = Integer.parseInt(substring.substring(2, 3)); // left 0 / right 1
            // caso 1:
            estados[i][3] = Integer.parseInt(substring.substring(3, 4));
            estados[i][4] = Integer.parseInt(substring.substring(4, 5));
            estados[i][5] = Integer.parseInt(substring.substring(5, 6)); // left 0 / right 1
        }
        int estado = 0;
        int indiceCinta = cintaInicial.length/2;
        
        for(int i=0;i<ventana;i++){
            st.push(0);
        }
        
        // almacenar ordenEjecucion
        for(int c=0;c<ciclosMax;c++){
            // esto funciona solo para la impresion de los estados
            // impresion de la maquina
            
            // ultimo estado es halt
            if(estado == noEstadosMaquina || indiceCinta >= cintaInicial.length || indiceCinta < 0) {
                System.out.println("Halt!");
                break;
            }
            
            if(estadoVisitado[estado]==0){
                estadoVisitado[estado]=1;
                String mov0 = estados[estado][2] == 0 ? "I" : "D";
                String mov1 = estados[estado][5] == 0 ? "I" : "D";
                System.out.println("Estado: " + estado + ", Valor cinta: 0, Escribe: " + estados[estado][1] + ", Mueve: " + mov0 + ", Sig. estado: " + estados[estado][0] + 
                    ", Valor cinta: 1, Escribe: " + estados[estado][4] + ", Mueve: " + mov1 + ", Sig. estado: " + estados[estado][3]);
            }
            
            int lado = cintaInicial[indiceCinta] == 1 ? 3 : 0; // indice si es estado 0 o 1
            int reemplazo = estados[estado][lado+1];
            cintaInicial[indiceCinta] = reemplazo;
            
            //ALMACENAR ESCRITURA
            st.push(reemplazo);
            st.remove(st.firstElement());
            // System.out.println("Pila: " + st);
            
            // almacenar la pila en la orden de ejecucion
            Object[] ob = st.toArray();
            int[] elementosPila = new int[ob.length];
            for(int i=0;i<ob.length;i++){
                elementosPila[i] = (int)ob[i];
            }
            ordenEjecucion.add(elementosPila);
            
            if(estados[estado][lado+2] == 0){ // izquierda
                indiceCinta--;
            }
            else { // derecha
                indiceCinta++;
            }
            
            // System.out.println("Estado: " + estado + ", pasa a: " + estados[estado][lado] + ", escribe: " + reemplazo + ", indice cinta: " + indiceCinta);
            estado = estados[estado][lado];
        }
        
        System.out.println("Cinta final:");
        for(int i=0;i<cintaInicial.length;i++){
            System.out.print(cintaInicial[i]);
        }
        System.out.println("");
            
        return ordenEjecucion;
    }
    
    private int booleanStringToInt(String cadena){
        int valor = 0;
	int i = 0;
        for (int j=cadena.length();j>0;j--){
            if (cadena.substring(j-1, j).equals("1"))
            {
                valor += Math.pow(2, i);
            }
            i++;
        }
        return valor;
    }
    
    public BackPropagation entrenarRedNeuronal(ArrayList<double[]> train, ArrayList<double[]> test){
        int[] neurons = new int[3];
        
        int noIndependientes = 13;
        int noDependientes = 3;
        
        neurons[0] = 13;
        neurons[1] = 3; // neuronas de la primer capa
        neurons[2] = 3; // neuronas de la segunda capa
        
        double learnRate = 0.1;
        double moment = 0.9;
        double minError = 0.01;
        long epocas = 40000;

        // ArrayList<double[]> train = leerArchivo("train.txt");
        System.out.println("Archivo entrenamiento: " + train.size());
        
        BackPropagation net = new BackPropagation(neurons, train, 
                        learnRate, moment,
                        minError, epocas, noIndependientes, noDependientes);
        System.out.println("Inicia entrenamiento");
        net.TrainNetwork();
        System.out.print("Fin entrenamiento");
        
        // ArrayList<double[]> test = leerArchivo("test.txt");
        System.out.println("Archivo prueba: " + test.size());
        
        double rms = 0.0;
        int malEtiquetadas = 0;
        for(int i=0;i<test.size();i++)
        {
            double[] ind = new double[noIndependientes];
            double[] dep = new double[noDependientes];
            for(int j=0;j<test.get(i).length;j++){
                if(j<noIndependientes)
                    ind[j]=test.get(i)[j];
                else
                    dep[j-noIndependientes]=test.get(i)[j];
            }
            
            double[] res = net.test(ind);
            for(int j=0;j<noIndependientes+noDependientes;j++){
                if(j<noIndependientes)
                    System.out.print("" + ind[j] + " ");
                else
                    System.out.print("" + res[j-noIndependientes] + " ");
            }
            System.out.println("");
            
            for(int j=0;j<res.length;j++)
            {
                double error = Math.abs(res[j]-dep[j]);
                if(error > 0.5)
                    malEtiquetadas++;
                rms = rms + error;
            }
        }
        System.out.println("Mal etiquetadas: " + malEtiquetadas + ", ECM: " + rms);
        
        return net;
    }
}
