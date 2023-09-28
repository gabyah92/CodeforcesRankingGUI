package codeforcesranking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class CodeforcesRanking extends javax.swing.JFrame {
    
    static private boolean generated;
    private JTextArea searchTokenField;
    
    public CodeforcesRanking() {
        try {
            setTitle("Codeforces Leaderboard App");
            setSize(600, 300);
            setIconImage(new ImageIcon("src/codeforcesranking/logo.jpg").getImage());
            getContentPane().setBackground(Color.gray);
            setResizable(false);
            setLayout(null);
            
            JLabel TM = new JLabel("APP BY : gabyah92 || Pyramid ");
            TM.setFont(new Font("Arial", Font.BOLD, 12));
            TM.setForeground(Color.white);
            TM.setFont(TM.getFont().deriveFont(Font.BOLD));
            TM.setBounds(395, 235, 250, 30);
            add(TM);
            
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JLabel contestIdLabel = new JLabel("Enter Tokens For Codeforces Ranks!");
            contestIdLabel.setFont(new Font("Arial", Font.BOLD, 24));
            contestIdLabel.setForeground(Color.black);
            contestIdLabel.setFont(contestIdLabel.getFont().deriveFont(Font.BOLD));
            contestIdLabel.setBounds(90, 20, 1120, 30);
            add(contestIdLabel);
            
            searchTokenField = new JTextArea();
            searchTokenField.setFont(new Font("Arial", Font.BOLD, 18));
            searchTokenField.setBackground(Color.getHSBColor(255, 153, 153));
            searchTokenField.setBounds(20, 65, 550, 100);
            searchTokenField.setLineWrap(true);
            searchTokenField.setWrapStyleWord(true);
            add(searchTokenField);
            
            JScrollPane scrollPane = new JScrollPane(searchTokenField);
            scrollPane.setBounds(20, 65, 550, 100);
            add(scrollPane);
            
            JButton downloadButton = new JButton("Download Leaderboard");
            downloadButton.setFont(new Font("Arial", Font.BOLD, 20));
            downloadButton.setBounds(20, 190, 550, 50);
            downloadButton.setBackground(Color.orange);
            add(downloadButton);
            
            downloadButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    downloadButton.setBackground(Color.DARK_GRAY);
                    downloadButton.setForeground(Color.WHITE);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    downloadButton.setBackground(Color.ORANGE);
                    downloadButton.setForeground(Color.BLACK);
                }
            });
            
            downloadButton.addActionListener((ActionEvent e) -> {
                String searchToken = searchTokenField.getText();
                if (searchToken.replace(" ", "").equals("")) {
                    JOptionPane.showMessageDialog(null, "Enter some Tokens!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Show the "Please wait" dialog
                JDialog pleaseWaitDialog = new JDialog();
                pleaseWaitDialog.setPreferredSize(new Dimension(200, 100));
                pleaseWaitDialog.getContentPane().setBackground(Color.gray);
                pleaseWaitDialog.setTitle("Retrieving Data");
                pleaseWaitDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                pleaseWaitDialog.setLocationRelativeTo(null);
                pleaseWaitDialog.setResizable(false);
                pleaseWaitDialog.setIconImage(new ImageIcon("src/codeforcesranking/logo.jpg").getImage());
                
                pleaseWaitDialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        pleaseWaitDialog.dispose(); // Close the dialog
                        CodeforcesRanking.this.setEnabled(true);
                        CodeforcesRanking.this.setVisible(true);
                    }
                });
                
                JLabel label = new JLabel("Please wait...");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setSize(new Dimension(200, 100));
                label.setForeground(Color.white);
                label.setFont(new Font("Arial", Font.BOLD, 20));
                pleaseWaitDialog.add(label);

                // Start the other functions in a separate thread
                Thread thread = new Thread(() -> {
                    List<Participant> curr_leaderboard = null;
                    // Download leaderboard and filter the results
                    try {
                        List<Participant> leaderboard = downloadLeaderboard(searchToken);
                        curr_leaderboard = filterLeaderboard(leaderboard);
                    } catch (Exception p) {
                        JOptionPane.showMessageDialog(null, "InvalidSearchToken", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    // Sort and assign ranks
                    sortLeaderboard(curr_leaderboard);
                    assignRanks(curr_leaderboard);

                    // Display the leaderboard in console
                    displayLeaderboard(curr_leaderboard);
                    assert curr_leaderboard != null;
                    exportParticipantsToExcel((ArrayList<Participant>) curr_leaderboard);

                    // Close the "Please wait" dialog
                    pleaseWaitDialog.dispose();

                    // Enable access to the previous window
                    CodeforcesRanking.this.setEnabled(true);
                    CodeforcesRanking.this.setVisible(true);
                    generated = false;
                });
                thread.start();

                // Disable access to the previous window
                CodeforcesRanking.this.setEnabled(false);

                // Show the "Please wait" dialog
                pleaseWaitDialog.pack();
                pleaseWaitDialog.setVisible(true);
            });
            setLocationRelativeTo(null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something Unexpected Happened", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    static void exportParticipantsToExcel(ArrayList<Participant> participants) {
        try {
            // Create a new Workbook
            XSSFWorkbook workbook = new XSSFWorkbook();

            // Create a new Sheet
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Current Codeforces Leaderboard");

            // Create bold font with size 18 for column headers
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 20);
            
            org.apache.poi.ss.usermodel.Font boldFont2 = workbook.createFont();
            boldFont2.setBold(true);
            boldFont2.setFontHeightInPoints((short) 14);

            // Create bold centered cell style with 14 font size for normal cells
            CellStyle boldCenteredCellStyle = workbook.createCellStyle();
            boldCenteredCellStyle.setAlignment(HorizontalAlignment.CENTER);
            boldCenteredCellStyle.setFont(boldFont);
            boldCenteredCellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE1.getIndex());
            boldCenteredCellStyle.setBorderBottom(BorderStyle.THICK);
            boldCenteredCellStyle.setBorderTop(BorderStyle.THICK);
            boldCenteredCellStyle.setBorderLeft(BorderStyle.THICK);
            boldCenteredCellStyle.setBorderRight(BorderStyle.THICK);
            boldCenteredCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Create bold cell style with 14 font size for normal cells
            CellStyle boldCellStyle = workbook.createCellStyle();
            boldCellStyle.setAlignment(HorizontalAlignment.CENTER);
            boldCellStyle.setFont(boldFont2);
            boldCellStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
            boldCellStyle.setBorderBottom(BorderStyle.THICK);
            boldCellStyle.setBorderTop(BorderStyle.THICK);
            boldCellStyle.setBorderLeft(BorderStyle.THICK);
            boldCellStyle.setBorderRight(BorderStyle.THICK);
            boldCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Add column headers
            Row headerRow = sheet.createRow(0);
            Cell rankHeaderCell = headerRow.createCell(0);
            rankHeaderCell.setCellValue("Rank");
            rankHeaderCell.setCellStyle(boldCenteredCellStyle);
            Cell codeforcesIdHeaderCell = headerRow.createCell(1);
            codeforcesIdHeaderCell.setCellValue("Handle");
            codeforcesIdHeaderCell.setCellStyle(boldCenteredCellStyle);
            Cell scoreHeaderCell = headerRow.createCell(2);
            scoreHeaderCell.setCellValue("Rating");
            scoreHeaderCell.setCellStyle(boldCenteredCellStyle);

            // Add participants' data
            for (int i = 0; i < participants.size(); i++) {
                Participant participant = participants.get(i);
                Row row = sheet.createRow(i + 1);
                Cell rankCell = row.createCell(0);
                rankCell.setCellValue(participant.getRank());
                rankCell.setCellStyle(boldCellStyle);
                Cell codeforcesIdCell = row.createCell(1);
                codeforcesIdCell.setCellValue(participant.getHandle());
                codeforcesIdCell.setCellStyle(boldCellStyle);
                Cell scoreCell = row.createCell(2);
                scoreCell.setCellValue(participant.getRating());
                scoreCell.setCellStyle(boldCellStyle);
            }
            
            File folder = new File("Leaderboards");
            if (!folder.exists()) {
                folder.mkdir();
            }

            // Resize columns to fit the content
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            // Create FileOutputStream to write to the file
            try ( FileOutputStream fileOut = new FileOutputStream("Leaderboards/CurrentCodeforcesRatings.xlsx")) {
                // Write the workbook to the output stream  
                if (generated == true) {
                    workbook.write(fileOut);
                    System.out.println("Excel file created successfully!");
                    // check if CodeforcesRankingCLI is running
                    if (CodeforcesRankingCLI.isRunningCLI()) {
                        System.out.println("Exiting...");
                        return;
                    }
                    JOptionPane.showMessageDialog(null, "Generated! ", "Finished Generating Leaderboard!", JOptionPane.INFORMATION_MESSAGE);
                }
                // Close the workbook
                generated = false;
                workbook.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something Went Wrong! ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(null, "Something Went Wrong!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static List<Participant> downloadLeaderboard(String searchToken) throws Exception {
        String url = "https://codeforces.com/api/user.ratedList?activeOnly=false&includeRetired=false";
        JSONArray rows = null;
        try {
            URL websiteUrl = new URL(url);
            URLConnection connection = new URL(url).openConnection();
            HttpURLConnection o = (HttpURLConnection) websiteUrl.openConnection();
            o.setRequestMethod("GET");
            if (o.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND || o.getResponseCode() == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                JOptionPane.showMessageDialog(null, "No Internet | Could not connect!", "Error", JOptionPane.ERROR_MESSAGE);
                return new ArrayList<>();
            }
            InputStream inputStream = connection.getInputStream();
            try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonContent.append(line);
                }
                JSONObject jsonObject = new JSONObject(jsonContent.toString());
                String status = jsonObject.getString("status");
                if (status.equals("OK")) {
                    rows = jsonObject.getJSONArray("result");
                }
            }
        } catch (HeadlessException | IOException | NumberFormatException | JSONException e) {
            JOptionPane.showMessageDialog(null, "No Internet OR Invalid Contest ID!", "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
        JSONArray standings = rows;
        List<Participant> handlePointsList = new ArrayList<>();
        try {
            String splitter[] = searchToken.replace(" ", "").split(",");
            if (splitter.length == 0 && splitter[0].equals("")) {
                throw new Exception("WTH");
            }
            if (standings != null) {
                for (int j = 0; j < standings.length(); j++) {
                    JSONObject member = standings.getJSONObject(j);
                    String handle = member.getString("handle");
                    int points = member.getInt("rating");
                    for (String chk : splitter) {
                        if (handle.toLowerCase().contains(chk.toLowerCase())) {
                            handlePointsList.add(new Participant(handle, points));
                            break;
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "InvalidTokens!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Enter Proper Tokens!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        generated = true;
        return handlePointsList;
    }

    static List<Participant> filterLeaderboard(List<Participant> leaderboard) {
        return leaderboard; // Placeholder
    }

    static void sortLeaderboard(List<Participant> leaderboard) {
        try {
            Collections.sort(leaderboard, (Participant p1, Participant p2) -> Integer.compare(p2.getRating(), p1.getRating()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Input/Handle Does Not Exist!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void assignRanks(List<Participant> leaderboard) {
        try {
            for (int i = 0; i < leaderboard.size(); i++) {
                leaderboard.get(i).setRank(i + 1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Input/Handle Does Not Exist!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    static void displayLeaderboard(List<Participant> leaderboard) {
        try {
            for (Participant participant : leaderboard) {
                System.out.println("Rank: " + participant.getRank() + ", Handle: " + participant.getHandle() + ", Rating: " + participant.getRating());
            }
        } catch (Exception E) {
            JOptionPane.showMessageDialog(null, "Invalid Input/Handle Does Not Exist!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
// Contact me here if you find any bugs : Instagram => gabyah92