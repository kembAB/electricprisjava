import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ElectricityPricesApp {

    private static boolean isrunning = true;
    private static final Scanner scanner = new Scanner(System.in);
    private static Prices[] price;

    public static void main(String[] args) {

        while (appIsActive) {

            DisplayMenu();
            ManageMenu(getPrices());
        }
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("Electricity prices");

        System.out.println("1. Insert price");
        System.out.println("2. Min, Max och Average");
        System.out.println("3. Sort Increasing order");
        System.out.println("4. Best car chargin interval(4h)");
        System.out.println("e. Exit program");
        System.out.println();
        System.out.print("what would you like to choose?: ");
    }

    private static String getPrices() {
        return scanner.nextLine();
    }

    private static void ManageMenu(String input) {
        switch (input) {
            case "E", "e" ->  Exit();
            case "1" -> Insertprices();
            case "2" -> ShowStat();
            case "3" -> Sortincreasingorder();
            case "4" -> BestTimetoCharge();
            default -> ManageMenu();
        }
    }

    private static void Insertprices() {
     //comment 12
        System.out.println("\n please insert price in all intervals of the day .\n");

        price = new Prices[24];

        for (int i = 0; i < 24; i++) {

            String timeInterval = Prices.formatTimeInterval(i, 1);
            System.out.printf("%s : ", timeInterval);
            String userInput = getUserInput();
            boolean inputIsValid = validateDataInput(userInput);

            if (inputIsValid) dataStore[i] = new PricePoint(timeInterval, Integer.parseInt(userInput));
            else {
                System.out.println();
                handleIllegalInputValue();
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
                i--;
            }
        }

    }

    private static boolean validatePriceFormat(String input) {

        Pattern regexp = Pattern.compile("^\\d{1,4}$");
        return regexp.matcher(input).find();
    }

    private static void printStatistics() {

        if (dataStore == null)
            printDataStoreErrorMsg();
        else {
            double avgPrice = PricePoint.calcAverage(dataStore);
            System.out.printf("\nMedelpris: \n\t%.2f SEK/kWh\n", (avgPrice+0.5) / 100);

            PricePoint[] minMaxPricePoints = PricePoint.findMinMax(dataStore);
            PricePoint min = minMaxPricePoints[0];
            PricePoint max = minMaxPricePoints[1];
            System.out.printf("Lägsta pris: \n\t%s\n\t%.2f SEK/kWh\n", min.timeInterval(), ( min.price() / 100.0 ) );
            System.out.printf("Högsta pris: \n\t%s\n\t%.2f SEK/kWh\n", max.timeInterval(), ( max.price() / 100.0 ) );

            System.out.println();
            createChart(dataStore);
            System.out.print("\n\n");
        }
    }

    private static void printDataStoreErrorMsg() {
        System.err.println("Det finns ännu ingen data i systemet. Vänligen återkom hit när du har fyllt i ny data (Menyval 1, \"inmatning\").\n");
    }

    private static void printSortedByLowest() {

        if (dataStore == null)
            printDataStoreErrorMsg();
        else {
            PricePoint[] sorted = Arrays.copyOf(dataStore, dataStore.length);
            Arrays.sort(sorted);
            System.out.println("\nPrislista - lägst pris:");

            for (PricePoint pricePoint : sorted) {
                System.out.printf("%s  -->  %.2f SEK/kWh\n", pricePoint.timeInterval(), ( pricePoint.price() / 100.0 ) );
            }
        }
    }

    private static void printOptimalTimeToCharge() {

        if (dataStore == null)
            printDataStoreErrorMsg();
        else {
            int formerTotal = Arrays.stream(Arrays.copyOf(dataStore, 4)).map(PricePoint::price).reduce(0,(total, price) -> total + price);

            int iOptimalStart = 0;
            int optimalTotal = formerTotal;

            for (int first = 1, last = 4; last < dataStore.length; first++, last++) {

                int contender = formerTotal - dataStore[first - 1].price() + dataStore[last].price();

                if (contender < optimalTotal) {
                    iOptimalStart = first;
                    optimalTotal = contender;
                }
                formerTotal = contender;
            }
            System.out.println("\nBästa laddningstid (4h) är:");
            System.out.printf("\n%s\n", PricePoint.formatTimeInterval(iOptimalStart, 4));
            System.out.printf("Medelpris: %.2f SEK\n", ( (optimalTotal / 4.0)+0.5 ) / 100.0);
        }
    }

    private static void Exit() {
        appIsActive = false;
    }

    private static void handleIllegalInputValue() {
        System.err.println("Ogiltigt val. Försök igen!\n");
    }

    private static void createChart(PricePoint[] array) {
        PricePoint[] minMaxPricePoints = PricePoint.findMinMax(array);
        int minPrice = minMaxPricePoints[0].price();
        int maxPrice = minMaxPricePoints[1].price();
        int[] chartIntervals = calcChartIntervals(minPrice, maxPrice);

        for (int i = 0; i < chartIntervals.length; i++) {
            int interval = chartIntervals[chartIntervals.length - 1 - i];
            System.out.printf("%4d|", interval);
            for (PricePoint pricePoint : array) {
                if (pricePoint.price() >= interval)
                    System.out.print(" x ");
                else
                    System.out.print("   ");
            }
            System.out.println();
        }

        System.out.printf("%s|%s\n", " ".repeat(4), "-".repeat(71));
        System.out.printf("%s|00", " ".repeat(4));
        for (int i = 1; i < array.length; i++) {
            System.out.printf(" %02d", i);
        }
    }

    private static int[] calcChartIntervals(int minPrice, int maxPrice) {

        int priceSpread = maxPrice - minPrice;

        if (priceSpread == 0)
            return new int[] {minPrice};
        if (priceSpread <= 5)
            return new int[] {minPrice, maxPrice};
        if (priceSpread <= 30)
            return new int[] {minPrice, minPrice+(priceSpread/2), maxPrice};

        int[] chartIntervals = new int[6];
        int intervalLength = priceSpread / (chartIntervals.length - 1);

        for (int i = 0; i < chartIntervals.length; i++) {
            chartIntervals[i] = minPrice + (intervalLength * i);
        }
        return chartIntervals;
    }
}
