/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backpropagation;

import java.util.Random;

/**
 *
 * @author eduardomartinez
 */
public class Aleatorios {
    // raiz generadora de numeros aleatorios
    
    private static Random generador;
    
    public static double getAleatorio()
    {
        return generador.nextDouble();
    }
    
    public static void setSeed(int seed){
        generador = new Random(seed);
    }
}
