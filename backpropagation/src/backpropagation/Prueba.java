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
import java.util.Random;
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
        // raiz generadora de numeros aleatorios
        int seed = 1234567890;
        Aleatorios.setSeed(seed); // para pesos de red neuronal
        
        BufferedReader Fbr,Kbr;
        Kbr = new BufferedReader(new InputStreamReader(System.in));
        
        // IMPRIME ABABABAB, basado en una maquina con cintas en 0's
        String maquina = "60015201280109103900370144104000180133014501151062015501481160114111141003004411160012116100391158106000421020101310020048112400110107103701550151105001490034104401441140105610151020103300110026100000490114010301051139002200621101016011270132003511450142113910490128111500291152101701401138112300440117110001220153004611100116014510350109105601391049113601430025016011320008010501441019113410351015114110631054000210410108004510540129100511081102003700140062001001630105116100300147112200351152111010210160015401"; 
        int ventana = 10;
        int ciclosMax = 1000;
        int noEstados = 64;
        int[] cinta = new int[1000];
        
        Random gen = new Random(seed);
        int numCintas = 1;
        int cintaAleatoria = 1; // bandera para identificar si generar aleatorios en la cinta base
        
        ArrayList<double[]> ejecucionTotal = new ArrayList();
        int mitadCinta = cinta.length/2;
        for(int c=0;c<numCintas;c++){
            // iniciar aleatoriamente cinta de la maquina de turing
            for(int i=0;i<cinta.length;i++){
                if(i<=mitadCinta)
                    cinta[i]=0;
                else{
                    if(cintaAleatoria == 1)
                        cinta[i] = gen.nextBoolean() ? 1 : 0;
                    else
                        cinta[i] = 0;
                }
            }
            System.out.println("Cinta inicial:");
            for(int i=0;i<cinta.length;i++){
                System.out.print(cinta[i]);
            }
            System.out.println("");
            
            // Ejecutar máquina de turing y obtener los datos de ejecucion
            ArrayList<double[]> ejecucion = ejecutarMaquina(maquina, noEstados, cinta, ventana, ciclosMax);
            System.out.println("Ejecucion de la máquina y base de entrenamiento: " + c);
            for(int i=0;i<ejecucion.size();i++){
                ejecucionTotal.add(ejecucion.get(i));
                for(int j=0;j<ejecucion.get(i).length;j++)
                    System.out.print("\t" + ejecucion.get(i)[j]);
                System.out.println("");
            }
            
            //Validación cruzada
            AleatoriosSinRepeticion gensinrep = new AleatoriosSinRepeticion(0, ejecucion.size()-1, seed);
            int[] indicesAleatorios = new int[ejecucion.size()];
            for(int i=0;i<ejecucion.size();i++)
                indicesAleatorios[i]=gensinrep.generar();
            
            int[] neurons = new int[3];
            int noIndependientes = 9;
            int noDependientes = 1;

            neurons[0] = 9;
            neurons[1] = 5; // neuronas de la primer capa
            neurons[2] = 1; // neuronas de la segunda capa

            double learnRate = 0.1;
            double moment = 0.9;
            double minError = 0.01;
            long epocas = 40000;
            
            int incremento = ejecucion.size()/5; // 20% de los datos
            
            // entrenar red neuronal
            for(int i=0;i<epocas;i++) {
                if((i%1000)==0)
                {
                    ArrayList<double[]> train = new ArrayList();
                    ArrayList<double[]> test = new ArrayList();
                    // 20% de los datos
                    int inicio = 0;
                    int fin = incremento;
                    for(int j=0;j<ejecucion.size();j++){
                        if(j>=inicio && j<fin)
                            test.add(ejecucion.get(indicesAleatorios[j]));
                        else
                            train.add(ejecucion.get(indicesAleatorios[j]));
                    }
                    BackPropagation red = entrenarRedNeuronal(train, test, neurons,learnRate,moment,minError,i,noIndependientes,noDependientes);
                    System.out.println("Epocas: " + i );
                    System.out.println("Error entrenamiento: " + red.ErrorEntrenamiento + ", error prueba: " + red.ErrorPrueba);
                    System.out.println("Error entrenamiento: " + Math.round((double)red.maletiquetadasEntrenamiento/train.size() * 100) + ", error prueba: " + Math.round((double)red.maletiquetadasPrueba/test.size() * 100));
                }
            }
            
            
            
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
    
    public static ArrayList<double[]> ejecutarMaquina(String maquina, int noEstadosMaquina, int[] cintaInicial, int ventana, int ciclosMax) {
        ArrayList<double[]> ordenEjecucion = new ArrayList();
        Stack st = new Stack();
        
        int[] estadoVisitado = new int[noEstadosMaquina];
        for(int i=0;i<noEstadosMaquina;i++){
            estadoVisitado[i] = 0;
        }
        
        int [][] estados = new int [noEstadosMaquina][6];
        // decodificar estados de la maquina de turing
        for(int i=0;i<noEstadosMaquina;i++) {
            int inicio = i*8;
            int fin = inicio + 8;
            String substring = maquina.substring(inicio, fin);
            // caso 0: estado_siguiente, 0/1, Movimiento
            estados[i][0] = Integer.parseInt(substring.substring(0, 2)); // estado
            estados[i][1] = Integer.parseInt(substring.substring(2, 3)); // reemplazo
            estados[i][2] = Integer.parseInt(substring.substring(3, 4)); // left 0 / right 1
            // caso 1:
            estados[i][3] = Integer.parseInt(substring.substring(4, 6));
            estados[i][4] = Integer.parseInt(substring.substring(6, 7));
            estados[i][5] = Integer.parseInt(substring.substring(7, 8)); // left 0 / right 1
        }
        int estado = 0;
        int indiceCinta = cintaInicial.length/2;
        
        for(int i=0;i<ventana;i++){
            st.push(0);
        }
        
        // almacenar ordenEjecucion
        for(int c=0;c<ciclosMax;c++){
            // ultimo estado es halt
            if(estado == noEstadosMaquina || indiceCinta >= cintaInicial.length || indiceCinta < 0) {
                System.out.println("Halt!");
                break;
            }
            
            // esto funciona solo para la impresion de los estados
            // impresion de la maquina
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
            double[] elementosPila = new double[ob.length];
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
    
    // regresa el error 
    public static BackPropagation entrenarRedNeuronal(ArrayList<double[]> train, ArrayList<double[]> test,
            int[] neurons, double learnRate, double moment, double minError, int epocas,
            int noIndependientes, int noDependientes){

        // ArrayList<double[]> train = leerArchivo("train.txt");
        System.out.println("Archivo entrenamiento: " + train.size());
        
        BackPropagation net = new BackPropagation(neurons, train, 
                        learnRate, moment,
                        minError, epocas, noIndependientes, noDependientes);
        // System.out.println("Inicia entrenamiento");
        net.TrainNetwork();
        // System.out.print("Fin entrenamiento");
        
        // ArrayList<double[]> test = leerArchivo("test.txt");
        System.out.println("Archivo prueba: " + test.size());
        
        double rmsPrueba = 0.0;
        int malEtiquetadasPrueba = 0;
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
            // imprimir clasificacion de la red para entrenamiento
            /*
            for(int j=0;j<noIndependientes+noDependientes;j++){
                if(j<noIndependientes)
                    System.out.print("" + ind[j] + " ");
                else
                    System.out.print("" + res[j-noIndependientes] + " ");
            }
            System.out.println("");
            */
            
            for(int j=0;j<res.length;j++)
            {
                double error = Math.abs(res[j]-dep[j]);
                if(error > 0.5)
                    malEtiquetadasPrueba++;
                rmsPrueba = rmsPrueba + error;
            }
        }
        //System.out.println("Mal etiquetadas: " + malEtiquetadas + ", ECM: " + rmsPrueba);
        
        double rmsEntrenamiento = 0.0;
        int malEtiquetadasEntrenamiento = 0;
        for(int i=0;i<train.size();i++)
        {
            double[] ind = new double[noIndependientes];
            double[] dep = new double[noDependientes];
            for(int j=0;j<train.get(i).length;j++){
                if(j<noIndependientes)
                    ind[j]=train.get(i)[j];
                else
                    dep[j-noIndependientes]=train.get(i)[j];
            }
            
            double[] res = net.test(ind);
            
            // imprimir clasificacion de la red para prueba
            /*
            for(int j=0;j<noIndependientes+noDependientes;j++){
                if(j<noIndependientes)
                    System.out.print("" + ind[j] + " ");
                else
                    System.out.print("" + res[j-noIndependientes] + " ");
            }
            System.out.println("");
            */
            
            for(int j=0;j<res.length;j++)
            {
                double error = Math.abs(res[j]-dep[j]);
                if(error > 0.5)
                    malEtiquetadasEntrenamiento++;
                rmsEntrenamiento = rmsEntrenamiento + error;
            }
        }
        
        net.ErrorEntrenamiento = rmsEntrenamiento;
        net.maletiquetadasEntrenamiento = malEtiquetadasEntrenamiento;
        net.ErrorPrueba = rmsPrueba;
        net.maletiquetadasPrueba = malEtiquetadasPrueba;
        
        return net;
    }
}
