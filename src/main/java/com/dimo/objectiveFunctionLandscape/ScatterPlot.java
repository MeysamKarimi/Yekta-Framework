package com.dimo.objectiveFunctionLandscape;

import java.util.List;
import java.util.Random;

import com.dimo.gwo.Wolf;
import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.AWTPainterFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.chart.factories.IPainterFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

public class ScatterPlot extends AWTAbstractAnalysis {
    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new ScatterPlot());
    }

    public void visualize(List<Wolf> wolves) {
        int size = wolves.size();
        float a;

        Coord3d[] points = new Coord3d[size];

        for (int i = 0; i < size; i++) {
            points[i] = new Coord3d(wolves.get(i).getFitnessFunction(), 0.0F, 0.0F);
            a = 0.99f;
        }

        Scatter scatter = new Scatter(points, new Color(0, 0, 0));
        scatter.setWidth(3);

        Quality q = Quality.Advanced();
        q.setPreserveViewportSize(true);

        GLCapabilities c = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        chart = chartFactory().newChart(q);
        chart.getScene().add(scatter);
    }

    // Add this method to provide the chart factory
    public static IChartFactory chartFactory() {
        return new AWTChartFactory() {
            protected IPainterFactory newPainterFactory(GLCapabilities capabilities) {
                return new AWTPainterFactory(capabilities);
            }
        };
    }

    @Override
    public void init() {
//        int size = 50;
//        float x;
//        float y;
//        float z;
//        float a;
//
//        Coord3d[] points = new Coord3d[size];
//        Color[] colors = new Color[size];
//
//        Random r = new Random();
//        r.setSeed(0);
//
//        for (int i = 0; i < size; i++) {
//            x = r.nextFloat() - 0.5f;
//            y = r.nextFloat() - 0.5f;
//            z = r.nextFloat() - 0.5f;
//            points[i] = new Coord3d(x, y, z);
//            a = 0.99f;
//            colors[i] = new Color(0, 0, 0, a); //new Color(x, y, z, a);
//        }
//
//        Scatter scatter = new Scatter(points, colors);
//        scatter.setWidth(3);
//
//        Quality q = Quality.Advanced();
//         q.setPreserveViewportSize(true);
//
//        GLCapabilities c = new GLCapabilities(GLProfile.get(GLProfile.GL2));
//        IPainterFactory p = new AWTPainterFactory(c);
//        IChartFactory f = new AWTChartFactory(p);
//
//        chart = f.newChart(q);
//        chart.getScene().add(scatter);
    }
}