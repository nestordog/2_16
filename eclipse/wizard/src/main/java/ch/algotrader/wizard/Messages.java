package ch.algotrader.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ch.algotrader.wizard.messages"; //$NON-NLS-1$
    public static String ALGOTRADER_VERSION;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
