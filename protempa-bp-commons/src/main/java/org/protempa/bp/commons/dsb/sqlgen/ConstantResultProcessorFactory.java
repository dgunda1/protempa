package org.protempa.bp.commons.dsb.sqlgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.protempa.proposition.Constant;
import org.protempa.proposition.UniqueId;

/**
 *
 * @author Andrew Post
 */
class ConstantResultProcessorFactory
        extends SQLGenResultProcessorFactory<Constant> {

    @Override
    MainResultProcessor<Constant> getInstance(
            String dataSourceBackendId, EntitySpec entitySpec, ResultCache<Constant> cache) {

        ConstantResultProcessor resultProcessor =
                new ConstantResultProcessor();
        resultProcessor.setResults(cache);
        resultProcessor.setDataSourceBackendId(dataSourceBackendId);
        resultProcessor.setEntitySpec(entitySpec);
        return resultProcessor;
    }

    @Override
    RefResultProcessor<Constant> getRefInstance(
            String dataSourceBackendId, EntitySpec entitySpec,
            ReferenceSpec referenceSpec,
            ResultCache<Constant> cache) {
        ConstantRefResultProcessor resultProcessor =
                new ConstantRefResultProcessor();
        resultProcessor.setCache(cache);
        resultProcessor.setReferenceSpec(referenceSpec);
        resultProcessor.setDataSourceBackendId(dataSourceBackendId);
        resultProcessor.setEntitySpec(entitySpec);
        return resultProcessor;
    }

}
