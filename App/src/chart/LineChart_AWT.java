/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chart;

/**
 *
 * @author HUGO
 */
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class LineChart_AWT extends JFrame {

    private List<List<String>> listOfLists;
    private List<Integer> distaciaTotal;
    

    public LineChart_AWT(String applicationTitle, String chartTitle) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createLineChart(
                chartTitle,
                "Years", "Number of Schools",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
    }

    public LineChart_AWT(List<List<String>> listOfLists) {
        this.listOfLists = listOfLists;
        
        JFreeChart barChart = ChartFactory.createBarChart(
         "NºOperações por Aeroporto",           
         "Aeroporto",            
         "Operações",            
         createDataset(),          
         PlotOrientation.VERTICAL,           
         true, true, false);
         
      ChartPanel chartPanel = new ChartPanel( barChart );        
      chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) );        
      setContentPane( chartPanel ); 
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        distaciaTotal = new ArrayList<>();
        for (int o = 0; o < listOfLists.size(); o++) {
            int soma = 0;
            String[] pieces = null;
            String text = listOfLists.get(o).toString();
            String[]operacoes = text.split(",");
            for(int i=0; i<operacoes.length; i++){
            pieces = text.split(";");
            if(pieces.length>=13){
                System.out.println("Vou somar: "+soma+" com: "+pieces[13]);
                soma = soma + Integer.valueOf(pieces[13]);
            }
            } 
                distaciaTotal.add(soma);
            //dataset.addValue(Integer.valueOf(listOfLists.get(o).size()), "nºoperações", "Aeroporto: "+String.valueOf(o));
        }
        for(int a=0; a<distaciaTotal.size(); a++){
            System.out.println("Distancia total do aeroporto: "+a+" é de: "+distaciaTotal.get(a));
            dataset.addValue(Integer.valueOf(distaciaTotal.get(a)), "nºoperações", "Aeroporto: "+String.valueOf(a));
        }
        distaciaTotal.clear();
        /*dataset.addValue(15, "schools", "1970");
        dataset.addValue(30, "schools", "1980");
        dataset.addValue(60, "schools", "1990");
        dataset.addValue(120, "schools", "2000");
        dataset.addValue(240, "schools", "2010");
        dataset.addValue(300, "schools", "2014");*/
        return dataset;
    }

}
