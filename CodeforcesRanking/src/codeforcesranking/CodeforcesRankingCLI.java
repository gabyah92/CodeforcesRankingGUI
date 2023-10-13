package codeforcesranking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeforcesRankingCLI extends CodeforcesRanking {

    static boolean cliRun = false;

    public static void handler(String[] args) {

        cliRun = true;

        // if args are empty, print error message
        if (args.length == 0) {
            System.err.println("Error: No arguments provided!");
            return;
        }

        // combine all args into one string
        StringBuilder searchToken = new StringBuilder();
        for (String arg : args) {
            searchToken.append(arg).append(", ");
        }

        try {
            // print downloading message
            System.out.println("Downloading leaderboard for " + searchToken.toString() + "...");
            List<Participant> currLeaderboard = downloadLeaderboard(searchToken.toString());
            // print sorting message
            System.out.println("Sorting leaderboard...");
            sortLeaderboard(currLeaderboard);
            // print assigning ranks message
            System.out.println("Assigning ranks...");
            assignRanks(currLeaderboard);
            // print displaying leaderboard message
            System.out.println("Displaying leaderboard...");
            displayLeaderboard(currLeaderboard);
            exportParticipantsToExcel((ArrayList<Participant>) currLeaderboard);
            // export participants to CSV
            exportParticipantsToCSV((ArrayList<Participant>) currLeaderboard);
            // END
            System.out.println("Done!");
            // exit with code 0
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static boolean isRunningCLI() {
        return cliRun;
    }

    static void exportParticipantsToCSV(ArrayList<Participant> participants) {
        try {
            File folder = new File("Leaderboards");
            if (!folder.exists()) {
                folder.mkdir();
            }

            File outputFile = new File("Leaderboards/CurrentCodeforcesRatings.csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                // Write CSV header
                writer.write("Rank,Handle,Rating");
                writer.newLine();

                // Write participants' data
                for (Participant participant : participants) {
                    writer.write(participant.getRank() + "," + participant.getHandle() + "," + participant.getRating());
                    writer.newLine();
                }

                System.out.println("CSV file created successfully!");
            } catch (IOException e) {
                System.err.println("Something Went Wrong!");
            }
        } catch (Exception e) {
            System.err.println("Something Went Wrong!");
        }
    }

}
