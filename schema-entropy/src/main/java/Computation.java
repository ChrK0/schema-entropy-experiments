import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Computation {

    private final int[][] table;
    private final List<FunctionalDependency> funcDeps = new ArrayList<>();
    private boolean showProcess;
    private boolean identifyOnes;
    private boolean considerSubtables;
    private int randomisation;
    private long processedCount = 0;
    private static int toCompute;
    private static int processed = -1;

    Computation(int[][] table, boolean identifyOnes, boolean considerSubtables, int randomisation) {
        this.table = table;
        this.identifyOnes = identifyOnes;
        this.considerSubtables = considerSubtables;
        this.randomisation = randomisation;
    }

    void enableProcessedCount() {
        showProcess = true;
    }

    void setIdentifyOnes(boolean identifyOnes) {
        this.identifyOnes = identifyOnes;
    }

    void setConsiderSubtables(boolean considerSubtables) {
        this.considerSubtables = considerSubtables;
    }

    void setRandomisation(int randomisation) {
        this.randomisation = randomisation;
    }

    void addFuncDep(FunctionalDependency fd) {
        funcDeps.add(fd);
    }

    String getFdsString() {
        int remaining = funcDeps.size();

        if (remaining == 0) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();

        for (FunctionalDependency fd : funcDeps) {
            builder.append(fd.toString()).append(--remaining > 0 ? ", " : "");
        }

        return builder.toString();
    }

    boolean coversOtherFuncDep(FunctionalDependency fd) {
        for (FunctionalDependency funcDep : funcDeps) {
            if (fd.covers(funcDep)) {
                return true;
            }
        }

        return false;
    }

    void addTransitiveClosure() {
        List<FunctionalDependency> combinedFds;
        boolean terminate = false;

        while (!terminate) {
            combinedFds = getCombinedFds();

            if (!deriveFd(combinedFds)) {
                terminate = true;
            }
        }
    }

    private boolean deriveFd(List<FunctionalDependency> combinedFds) {
        for (FunctionalDependency fd2 : combinedFds) {
            for (FunctionalDependency fd1 : combinedFds) {
                if (fd1.getRightSide().containsAll(fd2.getLeftSide())) {
                    FunctionalDependency derivedFd = new FunctionalDependency(fd1.getLeftSide(), fd2.getRightSide());

                    if (!derivedFd.isTrivial()) {
                        if (addSimpleFuncDeps(derivedFd.getSimpleFunctionalDependencies())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean addSimpleFuncDeps(List<FunctionalDependency> simpleFds) {
        boolean added = false;

        for (FunctionalDependency fd : simpleFds) {
            if (!(fd.isTrivial() || coversOtherFuncDep(fd))) {
                addFuncDep(fd);
                added = true;
            }
        }

        return added;
    }

    private List<FunctionalDependency> getCombinedFds() {
        Map<String, List<Integer>> fdMap = new HashMap<>();

        for (FunctionalDependency fd : funcDeps) {
            String left = fd.leftSideToString();

            if (fdMap.containsKey(left)) {
                List<Integer> right = new ArrayList<>(fdMap.get(left));
                right.add(fd.getSimpleRightSide());
                fdMap.put(left, right);
            } else {
                fdMap.put(left, List.of(fd.getSimpleRightSide()));
            }
        }

        List<FunctionalDependency> combinedFds = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : fdMap.entrySet()) {
            Set<Integer> leftSide = Arrays.stream(entry.getKey().split(",")).mapToInt(x -> Integer.parseInt(x) - 1).boxed().collect(Collectors.toSet());
            combinedFds.add(new FunctionalDependency(leftSide, Set.copyOf(entry.getValue())));
        }

        return combinedFds;
    }

    double[][] getInformationContentMatrix() {
        int rows = table.length;

        if (rows == 0) {
            return new double[0][];
        }

        int cols = table[0].length;

        if (considerSubtables) {
            boolean[] isFdsRightSide = new boolean[cols];

            for (FunctionalDependency funcDep : funcDeps) {
                isFdsRightSide[funcDep.getSimpleRightSide()] = true;
            }

            int[] redundantRows = getRedundantRows(isFdsRightSide);
            int[] redundantCols = getRedundantCols();
            Computation subtableComputation = getSubtableComputation(redundantRows, redundantCols);
            return embedSubtableComputation(subtableComputation.getInformationContentMatrix(), redundantRows, redundantCols);
        }

        double[][] matrix = new double[rows][cols];
        toCompute = rows * cols;

        if (identifyOnes) {
            boolean[] isFdsRightSide = new boolean[cols];

            for (FunctionalDependency funcDep : funcDeps) {
                isFdsRightSide[funcDep.getSimpleRightSide()] = true;
            }

            for (int i = 0; i < rows * cols; i++) {
                if (isOne(i, isFdsRightSide)) {
                    matrix[i / cols][i % cols] = 1;
                    toCompute--;
                }
            }
        }
        
        for (int i = 0; i < rows * cols; i++) {
            if (matrix[i / cols][i % cols] == 1) {
                continue;
            }

            matrix[i / cols][i % cols] = randomisation > 0 ? informationContentRandomised(i) : informationContent(i);
            int processed_new = (int) (processedCount * 100 / ((randomisation > 0 ? randomisation : Math.pow(2, (rows * cols - 1))) * toCompute));

            if (showProcess && processed_new > processed) {
                System.out.print("\033[2K\033[1G");
                System.out.print("Processed: " + (processed = processed_new) + "%");
            }
        }
        
        System.out.println();
        return matrix;
    }

    private double informationContent(int position) {
        return informationContentRec(position, new boolean[]{});
    }

    private double informationContentRandomised(int position) {
        boolean[] arr = new boolean[table.length * table[0].length - 1];
        double sum = 0;
        Random random = new Random();

        for (int i = 0; i < randomisation; i++) {
            for (int j = 0; j < arr.length; j++) {
                arr[j] = random.nextBoolean();
            }
            sum += informationContentRec(position, arr);
        }

        return sum / randomisation;
    }

    private double informationContentRec(int position, boolean[] arr) {
        if (arr.length == table.length * table[0].length - 1) {
            double result = entropy(position, arr);
            processedCount++;
            int processed_new = (int) (processedCount * 100 / ((randomisation > 0 ? randomisation : (Math.pow(2, arr.length))) * toCompute));

            if (showProcess && processed_new > processed) {
                System.out.print("\033[2K\033[1G");
                System.out.print("Processed: " + (processed = processed_new) + "%");
            }

            return result;
        }

        return (informationContentRec(position, concat(true, arr)) + informationContentRec(position, concat(false, arr))) / 2;
    }

    private double entropy(int position, boolean[] hasValue) {
        if (hasValue.length != table[0].length * table.length - 1) {
            throw new RuntimeException("Boolean array has wrong size.");
        }

        int[][] tableTmp = createTable(position, hasValue);
        tableTmp[position / table[0].length][position % table[0].length] = getMaxEntry(tableTmp) + 1;
        return checkFuncDeps(tableTmp) ? 1 : 0;
    }

    boolean checkFuncDeps(int[][] table) {
        for (FunctionalDependency funcDep : funcDeps) {
            Map<String, Integer> relevantCols = new HashMap<>();

            for (int row = 0; row < table.length; row++) {
                int[] leftValues = getLeftValues(row, funcDep, table);

                if (arrayContainsInt(leftValues, 0)) {
                    continue;
                }

                int rightValue = getRightValue(row, funcDep, table);

                if (rightValue == 0) {
                    continue;
                }

                String leftValuesStr = Arrays.toString(leftValues);

                if (relevantCols.containsKey(leftValuesStr)) {
                    if (relevantCols.get(leftValuesStr) != rightValue) {
                        return false;
                    }
                } else {
                    relevantCols.put(leftValuesStr, rightValue);
                }
            }
        }

        return true;
    }

    private int[][] createTable(int position, boolean[] hasValue) {
        int rows = table.length;
        int cols = table[0].length;
        int[][] tableTmp = new int[rows][cols];
        int boolPos = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (boolPos < position) {
                    tableTmp[i][j] = hasValue[boolPos] ? table[i][j] : 0;
                } else if (boolPos > position) {
                    tableTmp[i][j] = hasValue[boolPos - 1] ? table[i][j] : 0;
                }

                boolPos++;
            }
        }

        return tableTmp;
    }

    private Computation getSubtableComputation(int[] rowsToDelete, int[] colsToDelete) {
        int[][] newTable = new int[table.length - rowsToDelete.length][table[0].length - colsToDelete.length];
        int iOld = 0;

        for (int i = 0; i < newTable.length; i++) {
            while (arrayContainsInt(rowsToDelete, iOld)) {
                iOld++;
            }

            int jOld = 0;

            for (int j = 0; j < newTable[i].length; j++) {
                while (arrayContainsInt(colsToDelete, jOld)) {
                    jOld++;
                }

                newTable[i][j] = table[iOld][jOld++];
            }

            iOld++;
        }

        Computation computation = new Computation(newTable, identifyOnes, false, randomisation);

        if (showProcess) {
            computation.enableProcessedCount();
        }

        for (FunctionalDependency funcDep : funcDeps) {
            computation.addFuncDep(funcDep.convertToSubtable(colsToDelete));
        }

        return computation;
    }

    private double[][] embedSubtableComputation(double[][] subtable, int[] deletedRows, int[] deletedCols) {
        int rows = table.length;
        int cols = table[0].length;
        double[][] entropies = new double[rows][cols];
        int subtableRow = 0;

        for (int i = 0; i < rows; i++) {
            if (arrayContainsInt(deletedRows, i)) {
                for (int j = 0; j < cols; j++) {
                    entropies[i][j] = 1;
                }
            } else {
                int subtableCol = 0;

                for (int j = 0; j < cols; j++) {
                    if (arrayContainsInt(deletedCols, j)) {
                        entropies[i][j] = 1;
                    } else {
                        entropies[i][j] = subtable[subtableRow][subtableCol++];
                    }
                }

                subtableRow++;
            }
        }

        return entropies;
    }

    private int[] getRedundantRows(boolean[] isFdsRightSide) {
        List<Integer> rows = new ArrayList<>();

        for (int i = 0; i < table.length; i++) {
            if (rowIsOne(i, isFdsRightSide)) {
                rows.add(i);
            }
        }

        int[] redundantRows = new int[rows.size()];

        for (int i = 0; i < redundantRows.length; i++) {
            redundantRows[i] = rows.get(i);
        }

        return redundantRows;
    }

    private boolean rowIsOne(int row, boolean[] isFdsRightSide) {
        int cols = table[0].length;
        int firstPos = row * cols;

        for (int i = firstPos; i < firstPos + cols; i++) {
            if (!isOne(i, isFdsRightSide)) {
                return false;
            }
        }

        return true;
    }

    private boolean isOne(int position, boolean[] isFdsRightSide) {
        int rows = table.length;
        int cols = table[0].length;
        int row = position / cols;
        int col = position % cols;

        if (!isFdsRightSide[col]) {
            return true;
        }

        for (FunctionalDependency funcDep : funcDeps) {
            if (funcDep.getSimpleRightSide() == col) {
                int[] leftSideValues = getLeftValues(row, funcDep);

                for (int i = 0; i < rows; i++) {
                    if (i == row) {
                        continue;
                    }

                    if (Arrays.equals(leftSideValues, getLeftValues(i, funcDep))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int[] getLeftValues(int row, FunctionalDependency fd) {
        return getLeftValues(row, fd, table);
    }

    private int[] getLeftValues(int row, FunctionalDependency fd, int[][] table) {
        int[] cols = fd.getLeftSideArray();
        int l = cols.length;
        int[] values = new int[l];

        for (int i = 0; i < l; i++) {
            values[i] = table[row][cols[i]];
        }

        return values;
    }

    private int getRightValue(int row, FunctionalDependency fd, int[][] table) {
        return table[row][fd.getSimpleRightSide()];
    }

    private int[] getRedundantCols() {
        Set<Integer> relevantCols = new HashSet<>();

        for (FunctionalDependency funcDep : funcDeps) {
            relevantCols.addAll(funcDep.getAttributeIndices());
        }

        int[] redundantCols = new int[table[0].length - relevantCols.size()];
        int j = 0;

        for (int i = 0; i < table[0].length; i++) {
            if (!relevantCols.contains(i)) {
                redundantCols[j++] = i;
            }
        }

        return redundantCols;
    }

    private static boolean arrayContainsInt(int[] arr, int val) {
        for (int a : arr) {
            if (a == val) {
                return true;
            }
        }

        return false;
    }

    private static int getMaxEntry(int[][] table) {
        int max = -1;

        for (int[] row : table) {
            for (int cell : row) {
                max = Math.max(max, cell);
            }
        }

        return max;
    }

    private static boolean[] concat(boolean elem, boolean[] arr) {
        boolean[] result = new boolean[arr.length + 1];
        result[0] = elem;
        System.arraycopy(arr, 0, result, 1, arr.length);
        return result;
    }

}
