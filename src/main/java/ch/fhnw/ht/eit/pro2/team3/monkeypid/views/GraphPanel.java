package ch.fhnw.ht.eit.pro2.team3.monkeypid.views;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IModelListener;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.ClosedLoop;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.Plant;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

import java.awt.*;

/**
 * GraphPanel is a JPanel which includes the plot of the simulations. For the
 * plot JFreeChart is used.
 * 
 * @author Josua
 *
 */
public class GraphPanel extends JPanel implements IModelListener, ChartMouseListener{
    private XYSeriesCollection dataCollection = null;
    private JFreeChart chart = null;

	/**
	 * 
	 * @param controller
	 */
	public GraphPanel() {
		// super(new GridBagLayout());
		super(new BorderLayout());

        // collection holds XY data series
		dataCollection = new XYSeriesCollection();

		// axes
	    NumberAxis xAxis = new NumberAxis("Zeit");
	    NumberAxis yAxis = new NumberAxis("y(t)");
	    
		// renderer
		XYItemRenderer renderer = new StandardXYItemRenderer();
	    
	    // create plot
	 	 XYPlot plot = new XYPlot(dataCollection, xAxis, yAxis, renderer);

		// add plot into a new chart
		chart = new JFreeChart("Sprungantwort Geschlossener Regelkreis", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        // don't need the chart
        chart.getLegend().setVisible(false);

		// need a panel to add the chart to
		ChartPanel panel = new ChartPanel(chart);
		panel.addChartMouseListener(this);

		// TODO beste variante?
		// set prefered size 
		//panel.setPreferredSize(new java.awt.Dimension (800, 600));
		//panel.setMinimumSize(new Dimension(800, 600));

		// finally, add panel as an element in our GraphPanel
		this.add(panel);
		
		
		
	}
	
	public void autoScaleAxis(){
		chart.getXYPlot().getDomainAxis().setAutoRange(true);
		chart.getXYPlot().getRangeAxis().setAutoRange(true);
	}
	
	private XYItemRenderer getDatasetRenderer() {
        return chart.getXYPlot().getRendererForDataset(dataCollection);
    }

    private int getSeriesIndex(ClosedLoop loop) {
        return dataCollection.getSeriesIndex(loop.getStepResponse().getKey());
    }

    private void setSeriesVisible(ClosedLoop loop, boolean flag) {
        getDatasetRenderer().setSeriesVisible(getSeriesIndex(loop), flag);
    }

    @Override
    public void onAddCalculation(ClosedLoop closedLoop, boolean visible) {
        SwingUtilities.invokeLater(() -> {
            try {
            	/*
            	if(dataCollection.getSeriesCount() >= getSeriesIndex(closedLoop)){
            		if(closedLoop.getName().equals(dataCollection.getSeries(getSeriesIndex(closedLoop)).getKey())){
            			 dataCollection.removeSeries(getSeriesIndex(closedLoop));
            		}
            	}
            	*/
            	
            	/*
            	try{
            		XYSeries seriesToReplace = dataCollection.getSeries(closedLoop.getStepResponse().getKey());
            		dataCollection.removeSeries(seriesToReplace);
            	}
            	catch(UnknownKeyException e){
            		System.out.println(e.getMessage());
            	}
            	*/
            	for(int i = 0; i < dataCollection.getSeriesCount(); i++){
            		if(closedLoop.getName().equals(dataCollection.getSeries(i).getKey())){
            			dataCollection.removeSeries(i);
            			break;
            		}
            	}
            	
            	dataCollection.addSeries(closedLoop.getStepResponse());
            	
                // The closedLoop object specifies what color it wants to be rendered in
                getDatasetRenderer().setSeriesPaint(getSeriesIndex(closedLoop), closedLoop.getColor());
                //getDatasetRenderer().setSeriesOutlinePaint(getSeriesIndex(closedLoop), new Color(255, 128, 0));
                //getDatasetRenderer().setSeriesStroke(getSeriesIndex(closedLoop), new BasicStroke(5.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {1.0f, 0.0f}, 0.0f));
                getDatasetRenderer().setSeriesToolTipGenerator(getSeriesIndex(closedLoop), new XYToolTipGenerator() {
					private static final long serialVersionUID = 1L;
					@Override
					public String generateToolTip(XYDataset dataset, int series, int item) {
						String toolTipStr = closedLoop.getName();
						return toolTipStr;
					}
				});
                
                // See issue #21 - make visible again
                setSeriesVisible(closedLoop, visible);

            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @Override
    public void onRemoveCalculation(ClosedLoop closedLoop) {
        SwingUtilities.invokeLater(() -> {
            if (closedLoop.getStepResponse() != null) {
                dataCollection.removeSeries(closedLoop.getStepResponse());
            }
        });
    }

    @Override
    public void onSimulationBegin(int numberOfStepResponses) {
    }

    @Override
    public void onSimulationComplete() {}

    @Override
    public void onHideCalculation(ClosedLoop closedLoop) {
        setSeriesVisible(closedLoop, false);
    }

    @Override
    public void onShowCalculation(ClosedLoop closedLoop) {
        setSeriesVisible(closedLoop, true);
    }

	@Override
	public void onSetPlant(Plant plant) {}

	@Override
	public void chartMouseClicked(ChartMouseEvent e) {
		if(e.getEntity().getClass() == XYItemEntity.class){
			for (int i = 0; i < dataCollection.getSeriesCount(); i++) {
				getDatasetRenderer().setSeriesStroke(i, 
						new BasicStroke(1.0f)
						);
			}
			XYItemEntity entity = (XYItemEntity) e.getEntity();
			getDatasetRenderer().setSeriesStroke(entity.getSeriesIndex(), 
					new BasicStroke(3.0f)
					);
		}
		else{
			for (int i = 0; i < dataCollection.getSeriesCount(); i++) {
				getDatasetRenderer().setSeriesStroke(i, 
						new BasicStroke(1.0f)
						);
			}
		}
		
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub
		
	}
}
