package com.thecomet.monkeypid.controllers;

import com.thecomet.monkeypid.interfaces.MathBlockInterface;
import com.thecomet.monkeypid.models.MathChainFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alex Murray
 */
public class MainWindowController implements Initializable {
    @FXML private LineChart<Number, Number> lineChart;

    @FXML
    protected void handleTestButtonAction(ActionEvent event) {
        System.out.println("hello!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        XYChart.Series<Number, Number> series = new XYChart.Series();
        double timeStep = 0.01;

        double ks = 2.0;
        double tu = 1.25;
        double tg = 6.95;

        // faustregel 20%
        // http://de.wikipedia.org/wiki/Faustformelverfahren_%28Automatisierungstechnik%29
        double kp = 0.6 * tg / (tu * tg);
        double ki = 0.2 * tu;
        double kd = 0.42 * tu;

        MathBlockInterface pid = MathChainFactory.pidController(kp, ki, kd);
        MathBlockInterface controlledSystem = MathChainFactory.controlledSystem(ks, tu, tg);
        MathBlockInterface closedSystem = MathChainFactory.closedSystem(pid, controlledSystem);

        series.getData().add(new XYChart.Data(0, 0));
        for(double time = timeStep; time < 100; time += timeStep) {
            double result = closedSystem.stepAll(1, timeStep);
            series.getData().add(new XYChart.Data(time, result));
        }
        lineChart.getData().add(series);
    }
}
