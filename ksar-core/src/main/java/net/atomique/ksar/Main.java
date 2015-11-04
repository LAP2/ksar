/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.atomique.ksar;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.atomique.ksar.Graph.Graph;
import net.atomique.ksar.Graph.List;
import net.atomique.ksar.Parser.Linux;
import net.atomique.ksar.UI.Desktop;
import net.atomique.ksar.UI.SplashScreen;

/**
 * 
 * @author Max
 */
public class Main {

    static Config config = null;
    static GlobalOptions globaloptions = null;
    static ResourceBundle resource = ResourceBundle.getBundle("net/atomique/ksar/Language/Message");

    ;

    public static void usage() {
        show_version();
    }

    public static void show_version() {
        System.err
                .println("ksar Version : " + VersionNumber.getVersionNumber());
    }

    private static void set_lookandfeel() {
        for (UIManager.LookAndFeelInfo laf : UIManager
                .getInstalledLookAndFeels()) {
            if (Config.getLandf().equals(laf.getName())) {
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Desktop.class.getName()).log(Level.SEVERE,
                            null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(Desktop.class.getName()).log(Level.SEVERE,
                            null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Desktop.class.getName()).log(Level.SEVERE,
                            null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(Desktop.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
    }

    public static void make_ui() {
        SplashScreen mysplash = new SplashScreen(null, 3000);
        while (mysplash.isVisible()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        set_lookandfeel();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                GlobalOptions.setUI(new Desktop());
                SwingUtilities.updateComponentTreeUI(GlobalOptions.getUI());
                GlobalOptions.getUI().add_window();
                GlobalOptions.getUI().maxall();
            }
        });

    }

    public static void main(String[] args) {
        int i = 0;
        String arg;
        // / load default
        String mrjVersion = System.getProperty("mrj.version");
        if (mrjVersion != null) {
            System.setProperty("com.apple.mrj.application.growbox.intrudes",
                    "false");
            System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name", "kSar");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        config = Config.getInstance();
        globaloptions = GlobalOptions.getInstance();

        if (args.length > 0) {
            while (i < args.length && args[i].startsWith("-")) {
                arg = args[i++];
                if ("-version".equals(arg)) {
                    show_version();
                    System.exit(0);
                }
                if ("-help".equals(arg)) {
                    usage();
                    continue;
                }
                if ("-test".equals(arg)) {
                    GlobalOptions.setDodebug(true);
                    continue;
                }
                if ("-input".equals(arg)) {
                    if (i < args.length) {
                        GlobalOptions.setCLfilename(args[i++]);
                    } else {
                        exit_error(resource.getString("INPUT_REQUIRE_ARG"));
                    }
                    continue;
                }
                if ("-graph".equals(arg)) {
                    if (i < args.length) {
                        StringTokenizer t = new StringTokenizer(args[i++], ",");
                        while (t.hasMoreTokens()) {
                            GlobalOptions.getGraphList().add(t.nextToken());
                        }
                    } else {
                        exit_error("-graph requires a graph list");
                    }
                }

                if ("-outputPNG".equals(arg)) {
                    if (i < args.length) {
                        GlobalOptions.setOutputPNGBase(args[(i++)]);
                    } else {
                        exit_error("-outputPNG requires a base filename");
                    }
                }
            }
        }
        System.out.println("MIZH");
        if (GlobalOptions.getOutputPNGBase() == null)
            make_ui();
        else {
            System.setProperty("java.awt.headless", "true");
            kSar mysar = new kSar();
            mysar.do_fileread(GlobalOptions.getCLfilename());

            try {
                mysar.launched_action.join();
            } catch (Exception e) {
            }

            System.out.println(((Linux) mysar.myparser).ListofGraph);
            for (Map.Entry<String, Object> entry : ((Linux) mysar.myparser).ListofGraph
                    .entrySet()) {
                if (entry.getValue() instanceof Graph) {
                    if (GlobalOptions.getGraphList().size() > 0
                            && !GlobalOptions.getGraphList().contains(
                                    entry.getKey()))
                        continue;
                    System.out.println("GRAPH");
                    ((Graph) entry.getValue()).savePNG(
                            null,
                            null,
                            GlobalOptions.getOutputPNGBase() + "_"
                                    + entry.getKey() + ".png", 800, 600);
                }
                if (entry.getValue() instanceof List) {
                    System.out.println("LIST");
                    System.out.println(((List) entry.getValue()).nodeHashList);
                    for (Map.Entry<String, Graph> ent : ((List) entry
                            .getValue()).nodeHashList.entrySet()) {
                        if (GlobalOptions.getGraphList().size() > 0
                                && !GlobalOptions.getGraphList().contains(
                                        entry.getKey())
                                && !GlobalOptions.getGraphList().contains(
                                        entry.getKey() + "_" + ent.getKey()))
                            continue;
                        ent.getValue().savePNG(
                                null,
                                null,
                                GlobalOptions.getOutputPNGBase() + "_"
                                        + entry.getKey() + "_" + ent.getKey()
                                        + ".png", 800, 400);
                    }
                }
            }
        }

        System.out.println("exit");
    }

    public static void exit_error(final String message) {
        System.err.println(message);
        System.exit(1);

    }
}
