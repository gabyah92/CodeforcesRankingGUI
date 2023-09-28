package codeforcesranking;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Running in CLI mode...");
            // print args
            for (String arg : args) {
                System.out.println(arg);
            }
            CodeforcesRankingCLI.handler(args);
            return;
        }
        try {
            SwingUtilities.invokeLater(() -> {
                new CodeforcesRanking().setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
