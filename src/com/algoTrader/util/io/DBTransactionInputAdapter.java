package com.algoTrader.util.io;

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


public class DBTransactionInputAdapter extends AbstractCoordinatedAdapter {

    private Iterator<Transaction> transactiontIterator;

    public DBTransactionInputAdapter(Collection<Transaction> transactions) {

        super(EsperService.getEPServiceInstance(), true, true);

        this.transactiontIterator = transactions.iterator();
    }

    protected void close() {
        //do nothing
    }

    protected void replaceFirstEventToSend() {
        this.eventsToSend.remove(this.eventsToSend.first());
        SendableEvent event = read();
        if(event != null) {
            this.eventsToSend.add(event);
        }
    }

    protected void reset() {
        // do nothing
    }

    @SuppressWarnings("unchecked")
    public SendableEvent read() throws EPException {
        if(this.stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if(this.eventsToSend.isEmpty()) {

            if (this.transactiontIterator.hasNext()) {
                Transaction transaction = this.transactiontIterator.next();

                Order order = new OrderImpl();
                order.setRequestedQuantity(Math.abs(transaction.getQuantity()));
                order.setTransactionType(transaction.getType());
                order.setStatus(OrderStatus.PREARRANGED);
                order.getTransactions().add(transaction);

                // need to get the security by id, because the TransactionObject might be detached
                Security security = ServiceLocator.instance().getLookupService().getSecurity(transaction.getSecurity().getId());
                order.setSecurity(security);
                transaction.setSecurity(security);

                return new OrderSendableEvent(order, transaction.getDateTime().getTime(), this.scheduleSlot);

            } else {
                return null;
            }
        } else {
            SendableEvent event = this.eventsToSend.first();
            this.eventsToSend.remove(event);
            return event;
        }
    }
}
