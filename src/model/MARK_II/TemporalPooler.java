package model.MARK_II;

import java.util.HashSet;

import java.util.Set;

/**
 * Provides implementation for running the temporal pooling algorithm on a
 * Region with and without learning. The TemporalPooler computes the active and
 * predictive state for each Neuron at the current timestep t.
 *
 * Input into TemporalPooler: activeColumns at time t computed by the
 * SpatialPooler.
 *
 * Output from TemporalPooler: boolean OR of the active and predictive states
 * for each neuron forms the output of the TemporalPooler for the next Region in
 * the hierarchy.
 *
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version MARK II | August 3, 2013
 */
public class TemporalPooler extends Pooler {

    // list of Synapses and Segments to update in this.region

    public TemporalPooler(Region newRegion) {
	this.region = newRegion;
    }

    public void performInferenceOnRegion(Set<Column> activeColumns) {
	this.computeActiveStateOfAllNeurons(activeColumns); // Phase 1
	this.computePredictiveStateOfAllNeurons(activeColumns); // Phase 2
    }

    void computeActiveStateOfAllNeurons(Set<Column> activeColumns) {
	for (Column column : activeColumns) {
	    boolean bottomUpPredicted = false;
	    Neuron[] neurons = column.getNeurons();
	    for (int i = 0; i < neurons.length; i++) {
		if (neurons[i].getPreviousPredictingState() == true) {
		    DistalSegment bestSegment = neurons[i]
			    .getBestPreviousActiveSegment();
		    if (bestSegment != null && bestSegment.getSequenceState()) {
			bottomUpPredicted = true;
			neurons[i].setActiveState(true);
		    }
		}
	    }
	    if (bottomUpPredicted == false) {
		for (Neuron neuron : column.getNeurons()) {
		    neuron.setActiveState(true);
		}
	    }
	}
    }

    void computePredictiveStateOfAllNeurons(Set<Column> activeColumns) {
	for (Column column : activeColumns) {
	    for (Neuron neuron : column.getNeurons()) {
		for (Segment segment : neuron.getDistalSegments()) {
		    if (segment.getActiveState()) {
			neuron.setPredictingState(true);
		    }
		}
	    }
	}
    }

    /**
     * The algorithm in split into 3 distinct phases that occur in sequence:
     *
     * Phase 3 is only required for learning. However, unlike spatial pooling,
     * Phases 1 and 2 contain some learning-specific operations when learning is
     * turned on. TemporalPooler is significantly more complicated than
     * SpatialPooler.
     */
    public Region performTemporalPoolingOnRegion(Set<Column> activeColumns) {
	// Phase 1) compute the active state for each Neuron
	this.phaseOne(activeColumns);

	// Phase 2) compute the predictive state for each Neuron

	// Phase 3) update Synapse permanences
	return this.region;
    }

    void phaseOne(Set<Column> activeColumns) {
	for (Column column : activeColumns) {
	    boolean bottomUpPredicted = false;
	    boolean learningCellChosen = false;
	    Neuron[] neurons = column.getNeurons();
	    for (int i = 0; i < neurons.length; i++) {
		if (neurons[i].getPreviousPredictingState() == true) {
		    DistalSegment bestSegment = neurons[i]
			    .getBestPreviousActiveSegment();
		    if (bestSegment != null && bestSegment.getSequenceState()) {
			bottomUpPredicted = true;
			neurons[i].setActiveState(true);
			if (bestSegment.getPreviousActiveState()) {// from
								   // learning
			    learningCellChosen = true;
			    column.setLearningNeuronPosition(i);
			}
		    }
		}
	    }
	    if (bottomUpPredicted == false) {
		for (Neuron neuron : column.getNeurons()) {
		    neuron.setActiveState(true);
		}
	    }
	    if (learningCellChosen == false) {
		int bestNeuronIndex = this.getBestMatchingNeuronIndex(column);
		column.setLearningNeuronPosition(bestNeuronIndex);

		// a new Segment is added to the best Neuron???

		// segmentUpdateInfo =
		// getPreviouslyActiveSynapsesToUpdate(segment)???

		// sUpdate.sequenceSegment = true
		// segmentUpdateList.add(sUpdate)

		// ----------pseudocode----------
		// l,s = getBestMatchingCell(c, t-1)
		// learnState(c, i, t) = 1
		// sUpdate = getSegmentActiveSynapses(c, i, s, t-1, true)
		// sUpdate.sequenceSegment = true
		// segmentUpdateList.add(sUpdate)
	    }
	}
    }

    Segment getBestActiveSegment(Neuron neuron) {
	if (neuron == null) {
	    throw new IllegalArgumentException(
		    "neuron in TemporalPooler class getBestActiveSegment method"
			    + " cannot be null");
	}
	Segment bestSegment = null;

	// covers the case where there is a distalSegment with 0 active Synapses
	int greatestNumberOfActiveSynapses = -1;
	for (Segment distalSegment : neuron.getDistalSegments()) {
	    if (distalSegment.getNumberOfActiveSynapses() > greatestNumberOfActiveSynapses) {
		bestSegment = distalSegment;
		greatestNumberOfActiveSynapses = distalSegment
			.getNumberOfActiveSynapses();
	    }
	}
	return bestSegment;
    }

    /**
     * @return The index of the Neuron with the best matching Segment. Where
     *         best matching Segment is the segment with the largest number of
     *         active Synapses.
     */
    int getBestMatchingNeuronIndex(Column column) {
	if (column == null) {
	    throw new IllegalArgumentException(
		    "column in TemporalPooler class getBestMatchingNeuronIndex method"
			    + " cannot be null");
	}
	Neuron[] neurons = column.getNeurons();
	Cell bestCell = neurons[0];
	int greatestNumberOfActiveSynapses = 0;
	int bestMatchingNeuronIndex = 0;
	for (int i = 0; i < neurons.length; i++) {
	    Segment bestSegment = this.getBestActiveSegment(neurons[i]);
	    if (bestSegment.getNumberOfActiveSynapses() > greatestNumberOfActiveSynapses) {
		greatestNumberOfActiveSynapses = bestSegment
			.getNumberOfActiveSynapses();
		bestMatchingNeuronIndex = i;
	    }
	}
	return bestMatchingNeuronIndex;
    }

    void getPreviousSynapsesToUpdate(Segment segment) {
	Set<Synapse<Cell>> synapses = segment.getSynapses();
	Set<Synapse<Cell>> synapseWithPreviousActiveCells = new HashSet<Synapse<Cell>>();
	for (Synapse synapse : synapses) {
	    if (synapse.getCell().getPreviousActiveState()) {
		synapseWithPreviousActiveCells.add(synapse);
	    }
	}
    }

    /**
     * Calculates the predictive state for each Neuron. A Neuron's
     * predictiveState will be true if one if it's distalSegments becomes
     * active.
     */
    void phaseTwo(Set<Column> activeColumns) {
	for (Column column : activeColumns) {
	    for (Neuron neuron : column.getNeurons()) {
		for (Segment segment : neuron.getDistalSegments()) {
		    if (segment.getActiveState()) {
			neuron.setPredictingState(true);

			// reinforcement of the currently active distalSegment

			// listOfSynapsesToUpdate =
			// getActiveSyanpsesToUpdate(segment)
			// segmentUpdateList.add(listOfSynapsesToUpdate)

			// reinforcement of a distalSegment that could have
			// predicted this activation

			Segment predictingSegment = neuron
				.getBestPreviousActiveSegment();
			// predUpdate = getSegmentActiveSynapses(c, i,
			// predSegment, t-1, true)
			// segmentUpdateList.add(predUpdate)
		    }
		}
	    }
	}
    }

    void phaseThree(Set<Column> activeColumns) {
	for (Column column : activeColumns) {
	    for (Neuron neuron : column.getNeurons()) {
		// if learnState(s, i, t) == 1 then
		// adaptSegments(segmentUpdateList(c, i), true)
		// segmentUpdateList(c, i).delete()
		// else if predictiveState(c, i, t) == 0 and predictiveState(c,
		// i, t-1) == 1 then
		// adaptSegments(segmentUpdateList(c,i), false)
		// segmentUpdateList(c,i).delete()
	    }
	}
    }
}