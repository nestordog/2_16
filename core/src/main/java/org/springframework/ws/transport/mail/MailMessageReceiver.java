/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.mail;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.util.Assert;
import org.springframework.ws.transport.mail.monitor.MonitoringStrategy;
import org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy;
import org.springframework.ws.transport.mail.support.MailTransportUtils;
import org.springframework.ws.transport.support.AbstractAsyncStandaloneMessageReceiver;

/**
 * Server-side component for receiving email messages using JavaMail.  Requires a {@link #setTransportUri(String)
 * transport} URI, {@link #setStoreUri(String) store} URI, and {@link #setMonitoringStrategy(MonitoringStrategy)
 * monitoringStrategy} to be set, in addition to the {@link #setMessageFactory(WebServiceMessageFactory) messageFactory}
 * and {@link #setMessageReceiver(WebServiceMessageReceiver) messageReceiver} required by the base class.
 * <p/>
 * The {@link MonitoringStrategy} is used to detect new incoming email request. If the <code>monitoringStrategy</code>
 * is not explicitly set, this receiver will use the {@link Pop3PollingMonitoringStrategy} for POP3 servers, and the
 * {@link PollingMonitoringStrategy} for IMAP servers.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class MailMessageReceiver extends AbstractAsyncStandaloneMessageReceiver {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private Folder folder;

    private Store store;

    private MonitoringStrategy monitoringStrategy;

    private Set<Disposition> dispositions = new HashSet<Disposition>();

    private int reconnectTimeout = 0;

    /**
     * Set JavaMail properties for the {@link Session}.
     * <p/>
     * A new {@link Session} will be created with those properties. Use either this method or {@link #setSession}, but
     * not both.
     * <p/>
     * Non-default properties in this instance will override given JavaMail properties.
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        this.session = Session.getInstance(javaMailProperties, null);
    }

    /**
     * Set the JavaMail <code>Session</code>, possibly pulled from JNDI.
     * <p/>
     * Default is a new <code>Session</code> without defaults, that is completely configured via this instance's
     * properties.
     * <p/>
     * If using a pre-configured <code>Session</code>, non-default properties in this instance will override the
     * settings in the <code>Session</code>.
     *
     * @see #setJavaMailProperties
     */
    public void setSession(Session session) {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    /**
     * Sets the JavaMail Store URI to be used for retrieving request messages. Typically takes the form of
     * <code>[imap|pop3]://user:password@host:port/INBOX</code>. Setting this property is required.
     * <p/>
     * For example, <code>imap://john:secret@imap.example.com/INBOX</code>
     *
     * @see Session#getStore(URLName)
     */
    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    /**
     * Sets the monitoring strategy to use for retrieving new requests. Default is the {@link
     * PollingMonitoringStrategy}.
     */
    public void setMonitoringStrategy(MonitoringStrategy monitoringStrategy) {
        this.monitoringStrategy = monitoringStrategy;
    }

    public void setDisposition(Set<Disposition> dispositions) {

        this.dispositions = dispositions;
    }

    public void setReconnectTimeout(int reconnectTimeout) {
        this.reconnectTimeout = reconnectTimeout;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.storeUri, "Property 'storeUri' is required");
        if (this.monitoringStrategy == null) {
            String protocol = this.storeUri.getProtocol();
            if ("imap".equals(protocol)) {
                this.monitoringStrategy = new PollingMonitoringStrategy();
            }
            else {
                throw new IllegalArgumentException("Cannot determine monitoring strategy for \"" + protocol + "\". " +
                        "Set the 'monitoringStrategy' explicitly.");
            }
        }
        super.afterPropertiesSet();
    }

    @Override
    protected void onActivate() throws MessagingException {
        openSession();
        openFolder();
    }

    @Override
    protected void onStart() {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Starting mail receiver [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
        }
        execute(new MonitoringRunnable());
    }

    @Override
    protected void onStop() {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Stopping mail receiver [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
        }
        closeFolder();
    }

    @Override
    protected void onShutdown() {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Shutting down mail receiver [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
        }
        closeFolder();
        closeSession();
    }

    private void openSession() throws MessagingException {
        this.store = this.session.getStore(this.storeUri);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Connecting to store [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
        }
        this.store.connect();
    }

    private void openFolder() throws MessagingException {
        if (this.folder != null && this.folder.isOpen()) {
            return;
        }
        this.folder = this.store.getFolder(this.storeUri);
        if (this.folder == null || !this.folder.exists()) {
            throw new IllegalStateException("No default folder to receive from");
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Opening folder [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
        }
        this.folder.open(this.monitoringStrategy.getFolderOpenMode());
    }

    private void closeFolder() {
        MailTransportUtils.closeFolder(this.folder, true);
    }

    private void closeSession() {
        MailTransportUtils.closeService(this.store);
    }

    private class MonitoringRunnable implements SchedulingAwareRunnable {

        @Override
        public void run() {
            try {
                openFolder();
                while (isRunning()) {
                    try {
                        Message[] messages = MailMessageReceiver.this.monitoringStrategy.monitor(MailMessageReceiver.this.folder);
                        for (Message message : messages) {

                            boolean match = false;
                            for (Disposition disposition : MailMessageReceiver.this.dispositions) {

                                if (disposition.getFrom() != null && !containsAddress(message.getFrom(), disposition.getFrom())) {
                                    continue;
                                } else if (disposition.getTo() != null && !containsAddress(message.getAllRecipients(), disposition.getTo())) {
                                    continue;
                                } else if (disposition.getSubject() != null && !message.getSubject().contains(disposition.getSubject())) {
                                    continue;
                                }

                                // only if all defined dispositions match execute the service
                                match = true;
                                MailMessageReceiver.this.logger.info("processing message \"" + message.getSubject() + "\" from " + message.getFrom()[0] + " received on " + message.getReceivedDate()
                                        + " by disposition " + disposition.getName());
                                MessageHandler handler = new MessageHandler(message, disposition.getName(), disposition.getService());
                                execute(handler);
                                break; // not need to look at the rest of the dispositions
                            }

                            if (!match) {
                                MailMessageReceiver.this.logger.info("ignoring message \"" + message.getSubject() + "\" from " + message.getFrom()[0] + " received on " + message.getReceivedDate());
                            }
                        }
                    }
                    catch (FolderClosedException ex) {
                        MailMessageReceiver.this.logger.debug("Folder closed, trying to reopen");
                        if (isRunning()) {
                            while(true) {
                                try {
                                    openFolder();
                                    break;
                                } catch (StoreClosedException e) {
                                    Thread.sleep(MailMessageReceiver.this.reconnectTimeout);
                                    MailMessageReceiver.this.logger.debug("Store closed, trying to reopen");
                                }
                            }
                        }
                    }
                    catch (MessagingException ex) {
                        MailMessageReceiver.this.logger.warn(ex);
                    }
                }
            }
            catch (InterruptedException ex) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
            catch (MessagingException ex) {
                MailMessageReceiver.this.logger.error(ex);
            }
        }

        private boolean containsAddress(Address[] addresses, String addressString) throws MessagingException {

            for (Address address : addresses) {
                if (address.toString().contains(addressString)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isLongLived() {
            return true;
        }
    }

}
