/**
 * Created by ACS on 2016/2/21.
 * java -jar  KMeansDataGenerator.jar arg0 arg1
 */

import java.lang.Math;
/*
* The rows of the training text files must be vector data in the form
        * `[x1,x2,x3,...,xn]`
        * Where n is the number of dimensions.
*/
public class KMeansTrainDataGenerator {
    private static final double upperLimit = 50;
    private static final double lowerLimit = -50;
    public static void main(String[] args)
    {
        int n, N;
        //N=50;
        N = Integer.parseInt(args[0]);  //Rows
        n = Integer.parseInt(args[1]);   //columns
       // System.out.println("Generating data for N = "+N+" , n="+n);
        double points[][] = new double[N][n];
        double tmp;
        //to generate the points
        for(int i=0;i<N;i++)
        {
            for(int j=0;j<n;j++)
            {
                tmp=lowerLimit+((float)Math.random())*((upperLimit-lowerLimit)+1);
                tmp=Math.floor(tmp*10)/10;
               // points[i][j]= lowerLimit+((float)Math.random())*((upperLimit-lowerLimit)+1);
                points[i][j]=tmp;
            }
        }

        //to display the points
        for(int i=0;i<N;i++)
        {
            System.out.print("[" );
            int j=0;
            while(j<n-1)
            {
                j++;
                System.out.print(points[i][j]+",");
            }
            System.out.print(points[i][j]+"]\n");
   /*
            for(int j=0;j<n;j++)
            {
                System.out.print(points[i][j]+" ");
            }

             System.out.println("\n");
             */
        }
    }




}
