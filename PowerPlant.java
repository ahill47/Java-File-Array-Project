
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class PowerPlant {

    // this holds the entries read from the file
    private ArrayList<Entry> entries = new ArrayList<Entry>();

    // keep the data file name so we can convert it to the stats file name
    private String dataFileName;

    /**
     * Class to hold power plant data entries.
     */
    class Entry implements Comparable<Entry> {
        private String month;
        private int day;
        private int year;
        private double powerOutput;

        public Entry(String m, int d, int y, double pO) {
            this.month = m;
            this.day = d;
            this.year = y;
            this.powerOutput = pO;
        }

        // Accessor methods
        public String getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }

        public int getYear() {
            return year;
        }

        public double getPowerOutput() {
            return powerOutput;
        }

        // mutator methods
        public void setMonth(String month) {
            this.month = month;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public void setpower(double powerOutput) {
            this.powerOutput = powerOutput;
        }

        /**
         * This makes entries orderable by power output and is used when generating the
         * statistics file.x
         */
        @Override
        public int compareTo(Entry other) {
            if (this.powerOutput == other.powerOutput)
                return 0;
            if (this.powerOutput < other.powerOutput)
                return -1;
            return 1;
        }
    }

    /**
     * Convert a line from the data file into an Entry instance
     *
     * @param line one line of text from the data file
     * @return an Entry corresponding to the line
     */
    private Entry createEntryFromLine(String line) {
        // the data file is separated by spaces, so split it and then convert
        // the individual parts into the right types.
        String[] parts = line.split(" ");

        String month = parts[0];
        int day = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        double powerOutput = Double.parseDouble(parts[3]);

        Entry entry = new Entry(month, day, year, powerOutput);

        return entry;
    }

    /**
     * Read in file and store it into an list of Entry objects
     *
     * @throws IOException
     */
    private void uploadData() throws IOException {
        Scanner inputScanner = new Scanner(System.in);

        System.out.print("Enter a file name: ");
        dataFileName = inputScanner.nextLine();
        File file = new File(dataFileName);

        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            Entry entry = createEntryFromLine(line);
            entries.add(entry);
        }
        fileScanner.close();

        System.out.println("uploaded " + entries.size());
    }

    /**
     * Format an Entry to writing to screen or file
     * @param e an Entry instance
     * @return a String with Entry data
     */
    private String getPrintFormat(Entry e) {
        StringBuilder s = new StringBuilder();

        s.append("Date: ");
        s.append(e.getMonth());
        s.append(" ");
        s.append(e.getDay());
        s.append(", ");
        s.append(e.getYear());
        s.append(" Output: ");
        s.append(e.getPowerOutput());

        return s.toString();
    }

    /**
     * Print all entries as they were uploaded from the file
     */
    private void printData() {
        for (Entry e: entries) {
            String s = getPrintFormat(e);
            System.out.println(s);
        }
    }

    /**
     * Prompts for a month and prints corresponding entries
     */
    private void printMonth() {
        System.out.println("Enter a month:");
        Scanner input = new Scanner(System.in);
        String month = input.nextLine();

        List<Entry> monthly = new ArrayList<Entry>();
        for (Entry e: entries) {
            if (e.getMonth().equals(month)) {
                monthly.add(e);
            }
        }

        if (monthly.size() < 1) {
            System.out.println("No entries for " + month);
        } else {
            for (Entry e : monthly) {
                String s = getPrintFormat(e);
                System.out.println(s);
            }
        }
    }

    /**
     * Display the menu and ensure that the user has entered a valid selection.
     *
     * @return an int corresponding to the menu item
     */
    private int getMainMenuSelection() {
        Scanner input = new Scanner(System.in);

        int selection = -1;
        boolean validEntry = false;

        while (!validEntry) {
            System.out.println("1. Upload Data");
            System.out.println("2. View Data");
            System.out.println("3. Download Statistics");
            System.out.println("4. Print Month");
            System.out.println("0. Exit Program");

            while (!input.hasNextInt()) {
                input.next();
                System.out.println("Enter a number");
            }
            selection = input.nextInt();

            // is the entry actually one of the menu items?
            validEntry = selection >= 0 && selection < 5;
        }

        return selection;
    }

    /**
     * Returns a new list of entries ordered by power output
     * @return
     */
    private List<Entry> getEntriesByPowerOutput() {
        List<Entry> orderedEntries = new ArrayList<Entry>(this.entries);
        Collections.sort(orderedEntries);
        return orderedEntries;
    }

    /**
     * Sums output by month
     * @return a Map associating a month to a total power output
     */
    private Map<String, Double> getTotalOutputByMonth() {
        Map<String, Double> totals = new HashMap<String, Double>();

        for (Entry e : entries) {
            // Find the total for the month
            Double total = totals.get(e.getMonth());
            if (total == null) {
                // if it's the first time, set it to the current entry's output
                total = e.getPowerOutput();
            } else {
                // otherwise add it to what's already there
                total = total + e.getPowerOutput();
            }
            totals.put(e.getMonth(), total);
        }

        return totals;
    }

    /**
     * Calculate the average power output of entries
     *
     * @return the arithmetic mean of power output, or null if empty
     */
    private Double getAverageOutput() {
        // can't compute an average of empty or we will
        // try to divide by zero
        if (entries.size() < 1)
            return null;

        double total = 0;
        for (Entry e : entries) {
            total = total + e.getPowerOutput();
        }
        double avg = total / entries.size();
        return avg;
    }

    /**
     * Writes formatted statistics to a file ending in _stats.txt
     * @throws IOException
     */
    private void writeStatsFile() throws IOException {
        // Don't attempt to do anything when we don't have any entries
        if (entries.size() < 1) {
            System.out.println("No entries, aborting.");
            return;
        }

        List<Entry> orderedEntries = getEntriesByPowerOutput();

        System.out.println("data file name: " + dataFileName);

        // we could use substr, but it splitting the file name on the dot is clearer
        String[] parts = dataFileName.split("\\.");
        String statsFileName = parts[0] + "_stats.txt";
        FileWriter output = new FileWriter(statsFileName);

        // write entries ordered by power output
        for (Entry e : orderedEntries) {
            String s = getPrintFormat(e);
            output.write(s);
            output.write("\n");
        }

        // write the highest output entry
        Entry highestOutput = orderedEntries.get(orderedEntries.size() - 1);
        output.write("Highest Output on ");
        output.write(String.valueOf(highestOutput.getMonth()));
        output.write(" ");
        output.write(String.valueOf(highestOutput.getDay()));
        output.write(", ");
        output.write(String.valueOf(highestOutput.getYear()));
        output.write("\n");

        Map<String, Double> byMonth = getTotalOutputByMonth();
        for (Map.Entry<String, Double> pair : byMonth.entrySet()) {
            output.write("Total for ");
            output.write(pair.getKey());
            output.write(": ");
            output.write(pair.getValue().toString());
            output.write("\n");
        }

        // use a big-D Double because it could be null
        Double avgOutput = getAverageOutput();
        if (avgOutput != null) {
            output.write("Average ouput: ");
            output.write(avgOutput.toString());
            output.write("\n");
        }

        output.close();
        System.out.println("wrote to " + statsFileName);
    }

    /**
     * Main program. Displays the menu, prompts user, and initiates actions.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // This is the main PowerPlant instance
        PowerPlant pp = new PowerPlant();

        int selection;
        do {
            selection = pp.getMainMenuSelection();
            if (selection == 1) {
                pp.uploadData();
            } else if (selection == 2) {
                pp.printData();
            } else if (selection == 3) {
                pp.writeStatsFile();
            } else if (selection == 4) {
                pp.printMonth();
            }
        } while (selection != 0);
    }

}
