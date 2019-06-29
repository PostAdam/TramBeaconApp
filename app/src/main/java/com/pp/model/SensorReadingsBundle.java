package com.pp.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorReadingsBundle {
    private List<SensorReadingBase> sensorsReadings;
    private boolean useNeuralNetwork;
    private boolean useLocation;
}