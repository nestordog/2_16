package com.algoTrader.client;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class WarningProducer {

    private static JDialog dialog;

    /**
     * displays a dialog containing all information around a Throwable
     * @param t
     */
    public static void produceWarning(Throwable t) {

        // get the "last" cause
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        // print the header (multiline)
        StringBuffer buffer = new StringBuffer();
        buffer.append(t.toString().replace("-->", "\n-->"));
        buffer.append("\n\n");

        // print the stacktrace
        StackTraceElement[] trace = cause.getStackTrace();
        for (int i = 0; i < Math.min(20, trace.length); i++) {
            buffer.append("\tat " + trace[i] + "\n");
        }

        produceWarning(buffer.toString());
    }

    /**
     * displays a new dialog (if it is not already visible) and plays a sound
     */
    public static void produceWarning(String text) {

        if (dialog == null || !dialog.isVisible()) {

            JOptionPane jOptionPane = new JOptionPane(text, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
            dialog = jOptionPane.createDialog("warning");
            jOptionPane.selectInitialValue();
            dialog.setModal(false);
            dialog.setVisible(true);
        }

        try {
            // load clip from file
            URL url = WarningProducer.class.getClassLoader().getResource("warning.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();

            // dispose line when clip is finished (to close daemon thread)
            clip.addLineListener(new LineListener() {
                public void update(LineEvent evt) {
                    if (evt.getType() == LineEvent.Type.STOP) {
                        evt.getLine().close();
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
