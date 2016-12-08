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
    
    // raiz generadora de numeros aleatorios
    static int seed = 1234567890;
    private static Random generador = new Random(seed);
    
    public static double getAleatorio()
    {
        return generador.nextDouble();
    }
}
