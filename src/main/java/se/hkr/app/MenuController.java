package se.hkr.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;

import javafx.scene.control.Alert;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.awt.Color;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import org.knowm.xchart.*;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static se.hkr.app.DatabaseApiSelect.RetrieveMode.MOOD_TENSION;

public class MenuController {
    @FXML
    public void moodChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildXYChart();
            }
        });
    }



    @FXML
    public void tensionChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    buildPieChart();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    Slider moodSlider;

    @FXML
    Slider tensionSlider;

    @FXML
    TextArea journalEntry;

    @FXML
    LineChart<String, Number> graph;

    @FXML
    private AnchorPane chartArea;


    public void onLogoutBtnClick(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("welcome-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        String css = this.getClass().getResource("welcome.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void onSubmitMT(ActionEvent event) throws IOException {
        double mood = moodSlider.getValue();
        double tension = tensionSlider.getValue();
        User user = User.getInstance();
        Data.insertMood(mood,user);
        Data.insertTension(tension, user);
        Data.submissionCompleteNote();

        /* LineChart test
        graph.getData().clear();

        XYChart.Series<String, Number> moodSeries = new XYChart.Series<String, Number>();
        moodSeries.getData().add(new XYChart.Data<String, Number>("January", 7));
        moodSeries.getData().add(new XYChart.Data<String, Number>("February", 6));
        moodSeries.getData().add(new XYChart.Data<String, Number>("March", 8));
        moodSeries.getData().add(new XYChart.Data<String, Number>("April", 8));
        moodSeries.getData().add(new XYChart.Data<String, Number>("May", 7));
        moodSeries.getData().add(new XYChart.Data<String, Number>("June", 5));
        moodSeries.getData().add(new XYChart.Data<String, Number>("July", 6));
        moodSeries.getData().add(new XYChart.Data<String, Number>("September", 4));
        moodSeries.getData().add(new XYChart.Data<String, Number>("October", 3));
        moodSeries.getData().add(new XYChart.Data<String, Number>("November", 7));
        moodSeries.getData().add(new XYChart.Data<String, Number>("December", 8));
        moodSeries.setName("Mood");
        graph.getData().add(moodSeries);

        XYChart.Series<String, Number> tensionSeries = new XYChart.Series<String, Number>();
        tensionSeries.getData().add(new XYChart.Data<String, Number>("January", 7));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("February", 10));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("March", 10));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("April", 8));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("May", 6));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("June", 2));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("July", 3));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("September", 4));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("October", 3));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("November", 5));
        tensionSeries.getData().add(new XYChart.Data<String, Number>("December", 8));
        tensionSeries.setName("Tension");
        graph.getData().add(tensionSeries); */

    }


    public void onSubmitJournalEntry(ActionEvent event) throws IOException {
        String savedJournalEntry = journalEntry.getText().replaceAll("\n", System.getProperty("line.separator"));
        User user = User.getInstance("","","");
        Data.insertJournal(savedJournalEntry, user);
        Data.journalSubmittedNote();
        Data.clearOutJournalEntry(journalEntry);
        }
// Charts
    private void showChart(Chart chart) {
        JPanel chartPanel = new XChartPanel<>(chart);

        // for embedding swing in javafx
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        // for resizing plot to window
        AnchorPane.setLeftAnchor(swingNode, 0.0);
        AnchorPane.setRightAnchor(swingNode, 0.0);
        AnchorPane.setTopAnchor(swingNode, 0.0);
        AnchorPane.setBottomAnchor(swingNode, 0.0);

        // add chart to the chart area
        chartArea.getChildren().add(swingNode);

    }

    private void buildPieChart() throws SQLException {
        // Create Chart
        XYChart chart = new XYChartBuilder().theme(Styler.ChartTheme.XChart).width(766).height(516).title("Day Scale").build();
        // Colors
        Color DarkBlue = new Color(50, 168, 140, 171);
        Color lightGreen = new Color(0, 255, 0, 124);
        Color purple = new Color(239, 7, 239, 151);
        // Customize Chart
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setToolTipsEnabled(true);
        Color[] colorsSeries = new Color[]{DarkBlue,purple, lightGreen, Color.CYAN};
        chart.getStyler().setSeriesColors(colorsSeries);

        // Series
        Connection con = DatabaseConnection.getInstance().getCon();
        ArrayList<ArrayList> data = DatabaseApiSelect.getMTDataChart(con, User.getInstance().getPersonnummer());
        ArrayList<ArrayList> dataAvg = DatabaseApiSelect.getAvgMTDataChart(con, User.getInstance().getPersonnummer());
        ArrayList<Double> mood = data.get(0);
        ArrayList<Double> tension = data.get(1);
        ArrayList<Date> dates = data.get(2);
        ArrayList<Double> AvgMood = dataAvg.get(0);
        ArrayList<Double> AvgTension = dataAvg.get(1);
        ArrayList<Date> AvgDates = dataAvg.get(2);




        XYSeries tensionSeries = chart.addSeries("Tension",dates, tension);
        XYSeries moodSeries = chart.addSeries("Mood",dates, mood);
        XYSeries AvgtensionSeries = chart.addSeries("Average Tension",AvgDates, AvgTension);
        XYSeries AvgmoodSeries = chart.addSeries("Average Mood",AvgDates, AvgMood);

        tensionSeries.setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        moodSeries.setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        AvgtensionSeries.setXYSeriesRenderStyle(XYSeriesRenderStyle.Area);
        AvgmoodSeries.setXYSeriesRenderStyle(XYSeriesRenderStyle.Line);





        showChart(chart);
    }

    private void buildXYChart() {
        double[] xData = new double[] { 0.0, 1.0, 2.0 };
        double[] yData = new double[] { 2.0, 1.0, 0.0 };

        // Create Chart
        XYChart chart = new XYChartBuilder().width(766).height(516).title("Day Scale").build();
        chart.addSeries("blah", xData, yData);

        showChart(chart);
    }

}
