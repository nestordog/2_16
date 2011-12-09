package com.algoTrader.entity.combination;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import com.algoTrader.vo.CombinationTickVO;

public class CombinationTickDaoImpl {

    public CombinationTickVO toCombinationTickVO(final CombinationTick entity) {

        CombinationTickVO target = new CombinationTickVO();
        if (entity != null) {

            target = new CombinationTickVO();
            target.setId(entity.getCombination().getId());
            target.setDateTime(entity.getDateTime());
            target.setVolBid(entity.getVolBid());
            target.setVolAsk(entity.getVolAsk());
            target.setBid(entity.getBid());
            target.setCurrentValue(entity.getCurrentValue());
            target.setAsk(entity.getAsk());
        }
        return target;
    }

    /**
     * method from CombinationTickDaoBASE (which is not generated due to the nonPersistent entity CombiantionTick)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Collection toCombinationTickVOCollection(Collection<?> entities) {

        Collection result = new ArrayList<CombinationTickVO>();
        if (entities != null) {
            CollectionUtils.transform(entities, this.COMBINATIONTICKVO_TRANSFORMER);
            result.addAll(entities);
        }
        return result;
    }

    /**
     * method from CombinationTickDaoBASE (which is not generated due to the nonPersistent entity CombiantionTick)
     */
    private Transformer COMBINATIONTICKVO_TRANSFORMER = new Transformer() {
        @Override
        public Object transform(Object input) {
            Object result = null;
            if (input instanceof CombinationTick) {
                result = toCombinationTickVO((CombinationTick) input);
            }
            return result;
        }
    };
}
