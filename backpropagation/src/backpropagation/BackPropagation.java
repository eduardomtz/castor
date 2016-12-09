package backpropagation;

import java.util.*;
import java.io.*;

/**
 *
 * @author eduardomartinez
 */
public class BackPropagation {

    public double ErrorEntrenamiento;
    public double ErrorPrueba;
    public int maletiquetadasEntrenamiento;
    public int maletiquetadasPrueba;
    
    // Error Function variable that is calculated using the CalculateOverallError() function
    private double	OverallError;

    // The minimum Error Function defined by the user
    private double	MinimumError;

    // The user-defined expected output pattern for a set of samples
    private double	ExpectedOutput[][];

    // The user-defined input pattern for a set of samples
    private double	Input[][];

    // User defined learning rate - used for updating the network weights
    private double	LearningRate;

    // Users defined momentum - used for updating the network weights
    private double	Momentum;

    // Number of layers in the network - includes the input, output and hidden layers
    private  int	NumberOfLayers;

    // Number of training sets
    private  int	NumberOfSamples;

    // Current training set/sample that is used to train network
    private  int	SampleNumber;

    // Maximum number of Epochs before the traing stops training - user defined
    private long	MaximumNumberOfIterations;

    // Public Variables
    public LAYER	Layer[];
    public  double	ActualOutput[][];


    // Calculate the node activations
    public void FeedForward(){

        int i;

        // Since no weights contribute to the output 
        // vector from the input layer,
        // assign the input vector from the input layer 
        // to all the node in the first hidden layer
        for (i = 0; i < Layer[0].Node.length; i++)
                        Layer[0].Node[i].Output = Layer[0].Input[i];

        Layer[1].Input = Layer[0].Input;
        for (i = 1; i < NumberOfLayers; i++) {
                Layer[i].FeedForward();

                // Unless we have reached the last layer, assign the layer i's output vector
                // to the (i+1) layer's input vector
                if (i != NumberOfLayers-1)
                        Layer[i+1].Input = Layer[i].OutputVector();
        }

    } // FeedForward()

    // Back propagated the network outputy error through 
    // the network to update the weight values
    public void UpdateWeights() {

            CalculateSignalErrors();
            BackPropagateError();

    }


    private void CalculateSignalErrors() {

            int i,j,k,OutputLayer;
            double Sum;

            OutputLayer = NumberOfLayers-1;

        // Calculate all output signal error
            for (i = 0; i < Layer[OutputLayer].Node.length; i++) 
                    Layer[OutputLayer].Node[i].SignalError 
                            = (ExpectedOutput[SampleNumber][i] - 
                                    Layer[OutputLayer].Node[i].Output) * 
                                    Layer[OutputLayer].Node[i].Output * 
                                    (1-Layer[OutputLayer].Node[i].Output);

        // Calculate signal error for all nodes in the hidden layer
            // (back propagate the errors)
            for (i = NumberOfLayers-2; i > 0; i--) {
                    for (j = 0; j < Layer[i].Node.length; j++) {
                            Sum = 0;

                            for (k = 0; k < Layer[i+1].Node.length; k++)
                                    Sum = Sum + Layer[i+1].Node[k].Weight[j] * 
                                            Layer[i+1].Node[k].SignalError;

                            Layer[i].Node[j].SignalError 
                                    = Layer[i].Node[j].Output*(1 - 
                                            Layer[i].Node[j].Output)*Sum;
                    }
            }
    }


    private void BackPropagateError() {

            int i,j,k;

            // Update Weights
            for (i = NumberOfLayers-1; i > 0; i--) {
                    for (j = 0; j < Layer[i].Node.length; j++) {
                            // Calculate Bias weight difference to node j
                            Layer[i].Node[j].ThresholdDiff 
                                    = LearningRate * 
                                    Layer[i].Node[j].SignalError + 
                                    Momentum*Layer[i].Node[j].ThresholdDiff;

                            // Update Bias weight to node j
                            Layer[i].Node[j].Threshold = 
                                    Layer[i].Node[j].Threshold + 
                                    Layer[i].Node[j].ThresholdDiff;

                            // Update Weights
                            for (k = 0; k < Layer[i].Input.length; k++) {
                                    // Calculate weight difference between node j and k
                                    Layer[i].Node[j].WeightDiff[k] = 
                                            LearningRate * 
                                            Layer[i].Node[j].SignalError*Layer[i-1].Node[k].Output +
                                            Momentum*Layer[i].Node[j].WeightDiff[k];

                                    // Update weight between node j and k
                                    Layer[i].Node[j].Weight[k] = 
                                            Layer[i].Node[j].Weight[k] + 
                                            Layer[i].Node[j].WeightDiff[k];
                            }
                    }
            }
    }

    private void CalculateOverallError() {

            int i,j;
            OverallError = 0;
            for (i = 0; i < NumberOfSamples; i++)
                    for (j = 0; j < Layer[NumberOfLayers-1].Node.length; j++) {
                            OverallError = 
                                    OverallError + 
                                    0.5*( Math.pow(ExpectedOutput[i][j] - ActualOutput[i][j],2) );
            }
    }


    public  BackPropagation(int NumberOfNodes[], 
                                    ArrayList<double[]> InputSamples, 
                                    double LearnRate, 
                                    double Moment, 
                                    double MinError, 
                                    long MaxIter, 
                                    int noIndependientes, 
                                    int noDependientes) {

            int i,j;

            // Initiate variables
            NumberOfSamples = InputSamples.size();
            MinimumError = MinError;
            LearningRate = LearnRate;
            Momentum = Moment;
            NumberOfLayers = NumberOfNodes.length;
            MaximumNumberOfIterations = MaxIter;

            // Create network layers
            Layer = new LAYER[NumberOfLayers];

            // Assign the number of node to the input layer
            Layer[0] = new LAYER(NumberOfNodes[0],NumberOfNodes[0]);

            // Assign number of nodes to each layer
            for (i = 1; i < NumberOfLayers; i++) 
                    Layer[i] = new LAYER(NumberOfNodes[i],NumberOfNodes[i-1]);

            Input = new double[NumberOfSamples][Layer[0].Node.length];
            ExpectedOutput = new double[NumberOfSamples][Layer[NumberOfLayers-1].Node.length];
            ActualOutput = new double[NumberOfSamples][Layer[NumberOfLayers-1].Node.length];

            // Assign input set
            for (i = 0; i < NumberOfSamples; i++)
                for (j = 0; j < noIndependientes + noDependientes; j++)
                    if(j<noIndependientes)
                        Input[i][j] = InputSamples.get(i)[j];
                    else
                        ExpectedOutput[i][j - noIndependientes] = InputSamples.get(i)[j];

            // Assign output set
            /*
            for (i = 0; i < NumberOfSamples; i++)
                    for (j = 0; j < Layer[NumberOfLayers-1].Node.length; j++)
                            ExpectedOutput[i][j] = OutputSamples[i][j];
            */
    }

    public void TrainNetwork() {

            int i,j;
            long k=0;

            do{
                    // For each pattern
                    for (SampleNumber = 0; SampleNumber < NumberOfSamples; SampleNumber++) {
                            for (i = 0; i < Layer[0].Node.length; i++)
                                    Layer[0].Input[i] = Input[SampleNumber][i];

                            FeedForward();
                            // Assign calculated output vector from network to ActualOutput
                            for (i = 0; i < Layer[NumberOfLayers-1].Node.length; i++)
                                    ActualOutput[SampleNumber][i] = 
                                            Layer[NumberOfLayers-1].Node[i].Output;
                            UpdateWeights();
                    }
                    k++;
                    // System.out.println("Epocas " + k);
                    // Calculate Error Function
                    CalculateOverallError();
            } while ((OverallError > MinimumError) && (k < MaximumNumberOfIterations));
            System.out.println("Termino entrenamiento en " + k + " num. de epocas, error: " + OverallError);
    }

    // needed to implement the drawing of the network.
    public LAYER[] get_layers() { return Layer; }

    // called when testing the network.
    public double[] test(double[] input) {
            int winner = 0;
            NODE[] output_nodes;

            for (int j = 0; j < Layer[0].Node.length; j++)
                    Layer[0].Input[j] = input[j];

            FeedForward();

            // get the last layer of nodes (the outputs)
            output_nodes = (Layer[Layer.length - 1]).get_nodes();
            
            double[] salida = new double[output_nodes.length];
            
            
            for (int k=0; k < output_nodes.length; k++) {
                //if (output_nodes[winner].Output < 
                //                output_nodes[k].Output) {
                //        winner = k;
                //} // if
                salida[k]=output_nodes[k].Output;
            } // for
            
            
            // if (parent != null) { parent.draw(); }

            return salida;
    } 
}
