import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static boolean identifyOnes = false;
    private static boolean considerSubtables = false;
    private static int randomisation = 0;

    public static void main(String[] args) throws IOException {
        String outputFileName = "entropy.csv";
        int i = 1;
        int[][] table;
        String tableName;

        if (args[0].matches(".+\\.csv")) {
            String tableFile = Path.of(args[0]).getFileName().toString();
            tableName = tableFile.split("\\.")[0];
            outputFileName = "entropy_" + tableFile;
            table = readCsv(args[0]);
        } else {
            tableName = null;
            table = getTable(args[0]);
        }

        Computation computation = new Computation(table, identifyOnes, considerSubtables, randomisation);

        if (args.length > i + 1 && args[i].equals("--name")) {
            outputFileName = args[i + 1];
            i = i + 2;
        }

        if (args.length > i && args[i].equals("--no-output-file")) {
            outputFileName = "";
            i++;
        }

        if (args.length > i && args[i].equals("--show-process")) {
            computation.enableProcessedCount();
            i++;
        }

        if (args.length > i && args[i].equals("-i")) {
            identifyOnes = true;
            i++;
        }

        if (args.length > i && args[i].equals("-s")) {
            considerSubtables = true;
            i++;
        }

        if (args.length > i + 1 && args[i].equals("-r")) {
            randomisation = Integer.parseInt(args[i + 1]);
            i = i + 2;
        }

        boolean addClosure = false;

        if (args.length > i && args[i].equals("--closure")) {
            addClosure = true;
            i++;
        }

        computation.setIdentifyOnes(identifyOnes);
        computation.setConsiderSubtables(considerSubtables);
        computation.setRandomisation(randomisation);

        while (i < args.length) {
            String[] leftRight = args[i++].split("->");

            if (leftRight.length != 2) {
                System.out.println("FD must contain exactly one left and one right side separated by an arrow \"->\".");
                return;
            }

            int[] left;

            try {
                left = Stream.of(leftRight[0].split(",")).mapToInt(Integer::parseInt).map(x -> x - 1).toArray();
            } catch (NumberFormatException e) {
                System.out.println("Left side of FD can only contain integers separated by commas.");
                return;
            }

            try {
                int right = Integer.parseInt(leftRight[1]) - 1;
                computation.addFuncDep(new FunctionalDependency(Arrays.stream(left).boxed().collect(Collectors.toSet()), Set.of(right)));
            } catch (NumberFormatException e) {
                System.out.println("Right side of FD can only contain a single integer.");
                return;
            }
        }

        if (addClosure) {
            long start = System.nanoTime();
            computation.addTransitiveClosure();
            long end = System.nanoTime();
            System.out.printf("%d ms for computing the transitive closure.%n", (end - start) / 1000000);
        }

        if (!computation.checkFuncDeps(table)) {
            System.out.println("Given FDs not fulfilled.");
            return;
        }

        long start = System.nanoTime();
        double[][] infContMat = computation.getInformationContentMatrix();
        long end = System.nanoTime();
        long runtime = (end - start) / 1000000 ;

        if (!outputFileName.isEmpty()) {
            String outputFile = determineFilename(outputFileName);
            writeMatrixToCsv(outputFile, infContMat);
        }

        System.out.println(getOutputString(tableName, computation.getFdsString(), infContMat, runtime));
    }

    private static String getOutputString(String tableName, String fdsString, double[][] infContMat, long runtime) {
        StringBuilder builder = new StringBuilder();

        if (tableName != null) {
            builder.append("Table: ").append(tableName).append("\n");
        }

        builder.append("FDs: ").append(fdsString).append("\n")
                .append(arrayToString(infContMat))
                .append("Runtime: ").append(runtime).append("ms");
        return builder.toString();
    }

    private static String determineFilename(String filename) {
        while (new File(filename).exists()) {
            int l = filename.lastIndexOf(".");
            String filenameWithoutEnding = l == -1 ? filename : filename.substring(0, l);
            String ending = l == -1 ? "" : filename.substring(l + 1);
            Matcher m = Pattern.compile("^.*\\((\\d+)\\)$").matcher(filenameWithoutEnding);

            if (m.find()) {
                int n = filenameWithoutEnding.lastIndexOf("(");
                String filenameOrig = filenameWithoutEnding.substring(0, n);
                filenameWithoutEnding = String.format("%s(%d)", filenameOrig, Integer.parseInt(m.group(1)) + 1);
            } else {
                filenameWithoutEnding += "(1)";
            }

            filename = String.format("%s.%s", filenameWithoutEnding, ending);
        }

        return filename;
    }

    private static int[][] readCsv(String fileName) throws IOException {
        try (FileReader fileReader = new FileReader(fileName);
                CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(0).build()) {
            List<String[]> cells = csvReader.readAll();
            return stringArrListToIntMatrix(cells);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found: " + fileName);
        } catch (IOException e) {
            throw new IOException("Error reading file: " + fileName);
        } catch (CsvException e) {
            throw new RuntimeException("Error reading file: " + fileName);
        }
    }

    private static int[][] getTable(String tableStr) {
        String[] lines = tableStr.split(";");
        List<String[]> cells = new ArrayList<>();
        int cols = 0;

        for (String lineStr : lines) {
            String[] line = lineStr.split(",");
            cells.add(line);
            int c = line.length;

            if (c == 0 || line[0].isEmpty()) {
                continue;
            }

            if (cols == 0) {
                cols = c;
            } else {
                if (cols != c) {
                    throw new RuntimeException("Each line must have the same number of cells.");
                }
            }
        }

        return stringArrListToIntMatrix(cells);
    }

    private static int[][] stringArrListToIntMatrix(List<String[]> cells) {
        int[][] table = new int[cells.size()][cells.get(0).length];
        int j = 0;

        for (String[] row : cells) {
            if (row.length > 0 && !row[0].isEmpty()) {
                for (int k = 0; k < row.length; k++) {
                    try {
                        table[j][k] = Integer.parseInt(row[k]);

                        if (table[j][k] <= 0) {
                            return convertCells(cells);
                        }
                    } catch (NumberFormatException e) {
                        return convertCells(cells);
                    }
                }
                j++;
            }
        }

        return table;
    }

    private static int[][] convertCells(List<String[]> cells) {
        Map<String, Integer> stringToInt = new HashMap<>();
        int[][] table = new int[cells.size()][cells.get(0).length];
        int nextNumber = 1;

        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < cells.get(0).length; j++) {
                if (stringToInt.containsKey(cells.get(i)[j])) {
                    table[i][j] = stringToInt.get(cells.get(i)[j]);
                } else {
                    table[i][j] = nextNumber;
                    stringToInt.put(cells.get(i)[j], nextNumber++);
                }
            }
        }

        return table;
    }

    private static String arrayToString(double[][] array) {
        StringBuilder builder = new StringBuilder();

        for (double[] ints : array) {
            for (int j = 0; j < ints.length; j++) {
                builder.append(ints[j] == 1 ? "1" : ints[j]).append(j == ints.length - 1 ? "\n" : "   ");
            }
        }

        return builder.toString();
    }

    private static void writeMatrixToCsv(String filename, double[][] matrix) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (double[] row : matrix) {
                boolean first = true;
                for (double cell : row) {
                    writer.write(first ? "" : ",");
                    writer.write(cell == 1 ? "1" : String.valueOf(cell));
                    first = false;
                }
                writer.write("\n");
            }
        }
    }

}
