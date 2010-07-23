package com.algoTrader.util.csv;

import java.util.Collection;
import java.util.Iterator;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.OrderImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPException;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.AdapterState;
import com.espertech.esperio.SendableEvent;


public class TransactionInputAdapter extends AbstractCoordinatedAdapter {

    private Iterator<Transaction> transactiontIterator;

    public TransactionInputAdapter(Collection<Transaction> transactions) {

        super(EsperService.getEPServiceInstance(), true, true);

        transactiontIterator = transactions.iterator();
    }

    protected void close() {
        //do nothing
    }

    protected void replaceFirstEventToSend() {
        eventsToSend.remove(eventsToSend.first());
        SendableEvent event = read();
        if(event != null) {
            eventsToSend.add(event);
        }
    }

    protected void reset() {
        // do nothing
    }

    @SuppressWarnings("unchecked")
    public SendableEvent read() throws EPException {
        if(stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if(eventsToSend.isEmpty()) {

            if (transactiontIterator.hasNext()) {
                Transaction transaction = transactiontIterator.next();

                Order order = new OrderImpl();
                order.setRequestedQuantity(Math.abs(transaction.getQuantity()));
                order.setTransactionType(transaction.getType());
                order.setStatus(OrderStatus.PREARRANGED);
                order.getTransactions().add(transaction);

                // need to get the security by id, because the TransactionObject might be detached
                Security security = ServiceLocator.instance().getLookupService().getSecurity(transaction.getSecurity().getId());
                order.setSecurity(security);
                transaction.setSecurity(security);

                return new OrderSendableEvent(order, transaction.getDateTime().getTime(), scheduleSlot);

            } else {
                return null;
            }
        } else {
            SendableEvent event = eventsToSend.first();
            eventsToSend.remove(event);
            return event;
        }
    }
}
