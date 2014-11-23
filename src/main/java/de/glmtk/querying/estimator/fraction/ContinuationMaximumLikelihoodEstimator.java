package de.glmtk.querying.estimator.fraction;

import static de.glmtk.utils.NGram.WSKP_NGRAM;
import de.glmtk.utils.NGram;

public class ContinuationMaximumLikelihoodEstimator extends FractionEstimator {

    @Override
    protected double calcNumerator(NGram sequence, NGram history, int recDepth) {
        NGram contFullSequence =
                WSKP_NGRAM.concat(getFullSequence(sequence, history)
                        .convertSkpToWskp());
        long contFullSequenceCount =
                countCache.getContinuation(contFullSequence).getOnePlusCount();
        logDebug(recDepth, "contFullSequence = {} ({})", contFullSequence,
                contFullSequenceCount);
        return contFullSequenceCount;
    }

    @Override
    protected double
        calcDenominator(NGram sequence, NGram history, int recDepth) {
        NGram contFullHistory =
                WSKP_NGRAM.concat(getFullHistory(sequence, history)
                        .convertSkpToWskp());
        long contFullHistoryCount =
                countCache.getContinuation(contFullHistory).getOnePlusCount();
        logDebug(recDepth, "contFullHistory = {} ({})", contFullHistory,
                contFullHistoryCount);
        return contFullHistoryCount;
    }

}
