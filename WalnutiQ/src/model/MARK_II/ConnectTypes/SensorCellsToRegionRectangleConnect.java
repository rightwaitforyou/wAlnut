package model.MARK_II.ConnectTypes;

import model.MARK_II.SensorCell;
import model.MARK_II.Cell;
import model.MARK_II.Column;
import model.MARK_II.Neuron;
import model.MARK_II.Region;
import model.MARK_II.Synapse;

/**
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version MARK II | June 7, 2013
 */
public class SensorCellsToRegionRectangleConnect implements SensorCellsToRegionConnect {
    @Override
    public void connect(SensorCell[][] sensorCells, Region region,
	    int numberOfColumnsToOverlapAlongXAxisOfSensorCells,
	    int numberOfColumnsToOverlapAlongYAxisOfSensorCells) {
	if (region == null) {
	    throw new IllegalArgumentException(
		    "region in SensorCellsToRegionRectangleConnect class" +
		    "connect method cannot be null");
	} else if (sensorCells == null) {
	    throw new IllegalArgumentException(
		    "sensorCells in SensorCellsToRegionRectangleConnect class" +
		    "connect method cannot be null");
	}
	Column[][] regionColumns = region.getColumns();
	int regionXAxisLength = regionColumns.length; // = 8
	int regionYAxisLength = regionColumns[0].length; // = 8

	SensorCell[][] sensorCellLayer = sensorCells;
	int sensorCellsXAxisLength = sensorCellLayer.length; // = 66
	int sensorCellsYAxisLength = sensorCellLayer[0].length; // = 66

	// TODO: add missing exceptions for connectingRectangle dimension >= 8

	// TODO: view formula derivation and details at www.walnutiq.com/...
	int connectingRectangleXAxisLength = Math
		.round((sensorCellsXAxisLength
			+ numberOfColumnsToOverlapAlongXAxisOfSensorCells
			* regionXAxisLength - numberOfColumnsToOverlapAlongXAxisOfSensorCells)
			/ regionXAxisLength); // = 10
	int connectingRectangleYAxisLength = Math
		.round((sensorCellsYAxisLength
			+ numberOfColumnsToOverlapAlongYAxisOfSensorCells
			* regionYAxisLength - numberOfColumnsToOverlapAlongYAxisOfSensorCells)
			/ regionYAxisLength); // = 10

	int shiftAmountXAxis = connectingRectangleXAxisLength - numberOfColumnsToOverlapAlongXAxisOfSensorCells; // = 10 - 2
	int shiftAmountYAxis = connectingRectangleYAxisLength - numberOfColumnsToOverlapAlongYAxisOfSensorCells; // = 10 - 2
	for (int columnX = 0; columnX < (regionXAxisLength-shiftAmountXAxis); columnX++) {
	    for (int columnY = 0; columnY < (regionYAxisLength-shiftAmountYAxis); columnY++) {

		// xStart = 0, 8, 16, 24, 32, 40, 48, 56
		// yStart = 0, 8, 16, 24, 32, 40, 48, 56
		int xStart = columnX * shiftAmountXAxis;
		int yStart = columnY * shiftAmountYAxis;

		// xEnd = 10, 18, 26, 34, 42, 50, 58, 66
		// yEnd = 10, 18, 26, 34, 42, 50, 58, 66
		int xEnd = xStart + connectingRectangleXAxisLength;
		int yEnd = yStart + connectingRectangleYAxisLength;

		Column column = regionColumns[columnX][columnY];

		for (int sensorCellX = xStart; sensorCellX < xEnd; sensorCellX++) {
		    for (int sensorCellY = yStart; sensorCellY < yEnd; sensorCellY++) {
			// # of Synapses connected/add to this proximal Segment
			// = connectingRectangleXAxisLength *
			// connectingRectangleYAxisLength
			column.getProximalSegment().addSynapse(new Synapse<Cell>(sensorCellLayer[sensorCellX][sensorCellY], sensorCellX, sensorCellY));
		    }
		}
	    }
	}
    }
}