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
        
        BufferedReader Fbr,Kbr;
        Kbr = new BufferedReader(new InputStreamReader(System.in));
        
        // raiz generadora de numeros aleatorios
        int seed = 1234567890;
        Aleatorios.setSeed(seed); // para pesos de red neuronal
        
        // IMPRIME ABABABAB, basado en una maquina con cintas en 0's
        String maquina = "60015201280109103900370144104000180133014501151062015501481160114111141003004411160012116100391158106000421020101310020048112400110107103701550151105001490034104401441140105610151020103300110026100000490114010301051139002200621101016011270132003511450142113910490128111500291152101701401138112300440117110001220153004611100116014510350109105601391049113601430025016011320008010501441019113410351015114110631054000210410108004510540129100511081102003700140062001001630105116100300147112200351152111010210160015401"; 
        // validar ejecucion MT con oraculo
        String maquinaOraculo = "6001391052014901280128110910150039002911370152104410170140004011180138113301230045014401151017116201000155012201481153006011461141111001141016010300451044113501160009101211560161003910391149115810360160004300421025012010601113103200020008014811050124004410110119110710341037013510550115115110411050016310490054003410021044014101441108004010451056105401151029102010051133000811110002002610370000001400490162001401100103016301051105113900610022003001621147110101220060113511270152113200101035112101450160014211540160013910520149012801281109101500390029113701521044101701400040111801381133012300450144011510171162010001550122014811530060114611411110011410160103004510441135011600091012115601610039103911491158103601600043004210250120106011131032000200080148110501240044101101191107103410370135105501151151104110500163104900540034100210440141014411080040104510561054011510291020100511330008111100020026103700000014004901620014011001030163010511051139006100220030016211471101012200601135112701521132001010351121014501600142115401";
        int ventana = 10;
        int ciclosMax = 1000;
        int noEstados = 64;
        int[] cinta = new int[1000];
        
        int[] neurons = new int[3];
        int noIndependientes = 9;
        int noDependientes = 1;

        neurons[0] = 9;
        neurons[1] = 5; // neuronas de la primer capa
        neurons[2] = 1; // neuronas de la segunda capa

        double learnRate = 0.1;
        double moment = 0.9;
        double minError = 0.01;
        int epocas = 5000;
        
        int cintaAleatoria = 1;
        
        String cad = "";
        System.out.println("Ejecutar Maquina de Turing con Oráculo");
        do{
            System.out.println("");
            System.out.println("Parametros: ");
            System.out.println("1) Raiz generadora de numeros aleatorios: " + seed);
            System.out.println("2) Maquina de Turing: " + maquina);
            System.out.println("3) Maquina de Turing con oraculo: " + maquinaOraculo);
            System.out.println("4) Tasa de aprendizaje: " + learnRate);
            System.out.println("5) Momento: " + moment);
            System.out.println("6) Epocas: " + epocas);
            System.out.println("7) Neuronas capa intermedia: " + neurons[1]);
            System.out.println("8) Error minimo entrenamiento: " + minError);
            
            System.out.print("Presiona ENTER o @Parametro NuevoValor: ");
            try{
                cad = Kbr.readLine();
                if(cad.contains("@")){
                    String[] valores = cad.split(" ");
                    int num = Integer.parseInt(valores[0].substring(1));
                    switch(num){
                        case 1:
                            seed = Integer.parseInt(valores[1]);
                            Aleatorios.setSeed(seed);
                            break;
                        case 2:
                            maquina = valores[1];
                            break;
                        case 3:
                            maquinaOraculo = valores[1];
                            break;
                        case 4:
                            learnRate = Double.parseDouble(valores[1]);
                            break;
                        case 5:
                            moment = Double.parseDouble(valores[1]);
                            break;
                        case 6:
                            epocas = Integer.parseInt(valores[1]); 
                            break;
                        case 7:
                            neurons[1] = Integer.parseInt(valores[1]);
                            break;
                        case 8:
                            minError = Double.parseDouble(valores[1]);
                            break;
                    }
                    // double val = Double.parseDouble(valores[1]);
                }
            }catch(Exception ex){
                System.out.println("Teclado: " + ex.getMessage());
            }
        }while(cad.contains("@"));
        
        Random gen = new Random(seed);
        int numCintas = 1;
         // bandera para identificar si generar aleatorios en la cinta base
        
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
            ArrayList<double[]> ejecucion = ejecutarMaquina(maquina, noEstados, cinta, ventana, ciclosMax, null);
            System.out.println("Ejecucion de la máquina y base de entrenamiento: " + c);
            for(int i=0;i<ejecucion.size();i++){
                ejecucionTotal.add(ejecucion.get(i));
                for(int j=0;j<ejecucion.get(i).length;j++)
                    System.out.print("\t" + ejecucion.get(i)[j]);
                System.out.println("");
            }
            
            // datos para entrenamiento
            AleatoriosSinRepeticion gensinrep = new AleatoriosSinRepeticion(0, ejecucion.size()-1, seed);
            int[] indicesAleatorios = new int[ejecucion.size()];
            for(int i=0;i<ejecucion.size();i++)
                indicesAleatorios[i]=gensinrep.generar();
            
            
            int incremento = ejecucion.size()/5; // 20% de los datos
            
            ArrayList<double[]> train = new ArrayList();
            ArrayList<double[]> test = new ArrayList();
            //Validación cruzada
            // 20% de los datos
            int inicio = 0;
            int fin = incremento;
            for(int j=0;j<ejecucion.size();j++){
                if(j>=inicio && j<fin)
                    test.add(ejecucion.get(indicesAleatorios[j]));
                else
                    train.add(ejecucion.get(indicesAleatorios[j]));
            }
            BackPropagation red = entrenarRedNeuronal(train, test, neurons,learnRate,moment,minError,epocas,noIndependientes,noDependientes);
            
            
            
            ArrayList<double[]> ejecucionOraculo = ejecutarMaquina(maquinaOraculo, noEstados, cinta, ventana, ciclosMax, red);
            System.out.println("Ejecucion de la máquina y base de entrenamiento: " + c);
            for(int i=0;i<ejecucion.size();i++){
                ejecucionTotal.add(ejecucion.get(i));
                for(int j=0;j<ejecucion.get(i).length;j++)
                    System.out.print("\t" + ejecucion.get(i)[j]);
                System.out.println("");
            }
            
            System.out.println("La MT original ejecuto: " + ejecucion.size() + " ciclos, la MT con oraculo ejecuto: " + ejecucionOraculo.size() + " ciclos");
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
    
    public static ArrayList<double[]> ejecutarMaquina(String maquina, int noEstadosMaquina, 
            int[] cintaInicial, int ventana, int ciclosMax, BackPropagation oraculo) {
        ArrayList<double[]> ordenEjecucion = new ArrayList();
        Stack st = new Stack();
        
        int[] estadoVisitado = new int[noEstadosMaquina];
        for(int i=0;i<noEstadosMaquina;i++){
            estadoVisitado[i] = 0;
        }
        
        int cols = 2;
        int factorCadena = 8;
        if(oraculo != null) {
            cols = 4;
            factorCadena = 16;
        }
        
        int [][] estados = new int [noEstadosMaquina][3*cols];
        // decodificar estados de la maquina de turing
        for(int i=0;i<noEstadosMaquina;i++) {
            int inicio = i*factorCadena;
            int fin = inicio + factorCadena;
            String substring = maquina.substring(inicio, fin);
            // caso 0: estado_siguiente, 0/1, Movimiento
            estados[i][0] = Integer.parseInt(substring.substring(0, 2)); // estado
            estados[i][1] = Integer.parseInt(substring.substring(2, 3)); // reemplazo
            estados[i][2] = Integer.parseInt(substring.substring(3, 4)); // left 0 / right 1
            // caso 1:
            estados[i][3] = Integer.parseInt(substring.substring(4, 6));
            estados[i][4] = Integer.parseInt(substring.substring(6, 7));
            estados[i][5] = Integer.parseInt(substring.substring(7, 8)); // left 0 / right 1
            
            if(oraculo != null)
            {
                estados[i][6] = Integer.parseInt(substring.substring(8, 10)); // estado
                estados[i][7] = Integer.parseInt(substring.substring(10, 11)); // reemplazo
                estados[i][8] = Integer.parseInt(substring.substring(11, 12)); // left 0 / right 1
                // caso 1:
                estados[i][9] = Integer.parseInt(substring.substring(12, 14));
                estados[i][10] = Integer.parseInt(substring.substring(14, 15));
                estados[i][11] = Integer.parseInt(substring.substring(15, 16)); // left 0 / right 1
            }
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
            
            int respuestaOraculo = -1;
            Object[] ob = st.toArray();
            double[] elementosPila = new double[ob.length];
            for(int i=0;i<ob.length;i++){
                elementosPila[i] = (int)ob[i];
            }
            
            if(oraculo!=null){
                // pregunta a oraculo
                double[] respuesta = oraculo.test(elementosPila);
                respuestaOraculo = respuesta[0]>0.5 ? 1 : 0;
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
            if(oraculo != null) {
                int suma = (respuestaOraculo == 1) ? 3 : 0;
                lado = cintaInicial[indiceCinta] == 1 ? 6 : 0;
                lado = lado + suma;
            }
            
            int reemplazo = estados[estado][lado+1];
            cintaInicial[indiceCinta] = reemplazo;
            
            //ALMACENAR ESCRITURA
            st.push(reemplazo);
            st.remove(st.firstElement());
            // System.out.println("Pila: " + st);
            
            // almacenar la pila en la orden de ejecucion
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
    
    
    // regresa la red neuronal
    public static BackPropagation entrenarRedNeuronal(ArrayList<double[]> train, ArrayList<double[]> test,
            int[] neurons, double learnRate, double moment, double minError, int epocas,
            int noIndependientes, int noDependientes){

        // ArrayList<double[]> train = leerArchivo("train.txt");
        System.out.println("Archivo entrenamiento: " + train.size());
        
        BackPropagation net = new BackPropagation(neurons, train, 
                        learnRate, moment,
                        minError, epocas, noIndependientes, noDependientes);
        net.test = test;
        net.train = train;
        // System.out.println("Inicia entrenamiento");
        net.TrainNetwork();
        // System.out.print("Fin entrenamiento");
        
        // ArrayList<double[]> test = leerArchivo("test.txt");
        System.out.println("Archivo prueba: " + test.size());
        
        return net;
    }
}
