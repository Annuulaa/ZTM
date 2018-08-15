package szczech.anna.ztm;

import com.google.common.base.Objects;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class FindCommunication {

    public static String findRoute(String initialStop, String finalStop, String city, int[] time) throws SQLException {

        DatabaseStuff.removeData();

        if (Metro.isTravelByMetroPossible(initialStop, finalStop)) {
            Metro.addToDatabase(initialStop, finalStop);
        } else {

            ChromeOptions chromeOptions = new ChromeOptions();
//            chromeOptions.addArguments("--headless");
            ChromeDriver d = new ChromeDriver(chromeOptions);
            d.get("http://www.ztm.waw.pl/rozklad_nowy.php?c=183&l=1");

            if (!isStopExist(initialStop, city, d)) {
                d.quit();
                return "Przystanek: " + initialStop + " nie istnieje";
            }
            if (!isStopExist(finalStop, city, d)) {
                d.quit();
                return "Przystanek: " + finalStop + " nie istnieje";
            }

            findAllRoute(initialStop, city, time, d);

        }
        if (DatabaseStuff.isFinish(finalStop)) {
            String[][] selectedFromDB = selectFromDatabase(initialStop, finalStop);

            return findData(selectedFromDB);

        } else {
            return "NIE MA BEZPOŚREDNIEGO POŁĄCZENIA";
        }
    }

    private static boolean isStopExist(String stop, String city, ChromeDriver d) {
        try {
            d.findElementByCssSelector("[title='" + stop + " - " + city + "']");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String spellingCorrection(String stop) {
        String[] correctedStop = new String[stop.length()];
        for (int i = 0; i < stop.length(); i++) {
            correctedStop[i] = stop.substring(i, i + 1);
        }
        correctedStop[0] = correctedStop[0].toUpperCase();
        for (int i = 0; i < correctedStop.length; i++) {
            if (correctedStop[i].contains(" ")) {
                correctedStop[i + 1] = correctedStop[i + 1].toUpperCase();
            }
        }
        stop = correctedStop[0];
        for (int i = 1; i < correctedStop.length; i++) {
            stop = stop + correctedStop[i];
        }
        stop = stop.replace("Dworzec ", "Dw.");
        stop = stop.replace("Plac ", "Pl.");
        return stop;
    }

    public static String[][] findRoute(String stop, String city, ChromeDriver d) {
        String route[][] = new String[0][2];
        try {
            d.findElementByCssSelector("[title='" + stop + " - " + city + "']").click();

            WebElement webElementWhereIsStop = d.findElementById("RozkladContent");
            List<WebElement> stopList, webElementWhereIsRouteNumber, stopNumber;
            stopList = webElementWhereIsStop.findElements(By.cssSelector(".PrzystanekKierunek [href^='rozklad'] strong"));

            webElementWhereIsRouteNumber = webElementWhereIsStop.findElements(By.className("PrzystanekKierunek"));
            int nextRoute = 0;
            for (int i = 0; i < webElementWhereIsRouteNumber.size(); i++) {
                stopNumber = webElementWhereIsRouteNumber.get(i).findElements(By.cssSelector(".PrzystanekLineList [href^='rozklad']"));
                route = enlargeTable(route, stopNumber.size(), 2);
                for (int j = 0; j < stopNumber.size(); j++) {
                    route[nextRoute][0] = (stopList.get(i)).getText();
                    route[nextRoute][1] = stopNumber.get(j).getText();
                    nextRoute++;
                }
            }
        } finally {
//            d.quit();
        }
        return route;
    }

    public static String[][] enlargeTable(String[][] table, int howMuchEnlargeTable, int howManyColumns) {
        String[][] newTable = new String[table.length + howMuchEnlargeTable][howManyColumns];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < howManyColumns; j++) {
                newTable[i][j] = table[i][j];
            }
        }
        return newTable;
    }

    private static ChromeDriver findStop(ChromeDriver d, String number, String[] stopName) {
        try {
            d.findElementByCssSelector("#TwoColContent [href='rozklad_nowy.php?c=182&l=1&q=" + number + "']").click();

            for (int i = 0; true; i++) {
                List<WebElement> stopList = d.findElements(By.className("pn"));
                List<WebElement> stopNumberList = d.findElements(By.className("op"));

                for (int j = 0; j < stopList.size(); j++) {
                    if (stopList.get(j).getText().equals(stopName[0]) && stopNumberList.get(j).getText().equals(stopName[1])) {
                        String oldWeb = d.getCurrentUrl();
                        stopList.get(j).click();
                        String newWeb = d.getCurrentUrl();
                        if (!oldWeb.equals(newWeb)) {
                            return d;
                        }
                    }
                }
            }
        } catch (Exception e) {
            d.findElementByCssSelector(".st:first-of-type").click();
        }
        d.findElement(By.className("op")).click();
        return d;

    }

    private static ChromeDriver findRightTime(int[] time, ChromeDriver d) {
        try {
            List<WebElement> webElementWhereIsTime = d.findElements(By.cssSelector("#PrzystanekRozklad [href^='rozklad']"));
            for (int i = 2; i < webElementWhereIsTime.size(); i++) {
                String Time = webElementWhereIsTime.get(i).getAttribute("href");
                for (int j = Time.length() - 1; j > 0; j--) {
                    if (Time.substring(j - 1, j).equals("=")) {
                        Time = Time.substring(j, Time.length());
                        break;
                    }
                }
                Time = Time.replace(".", ":");
                int[] foundTime = new int[]{Integer.parseInt(Time.split(":")[0]), Integer.parseInt(Time.split(":")[1])};
                foundTime = isAfterMidnight(foundTime);

                if (foundTime[0] > time[0]) {
                    webElementWhereIsTime.get(i).click();
                    return d;
                }
                if (foundTime[0] == time[0]) {
                    if (foundTime[1] >= time[1]) {
                        webElementWhereIsTime.get(i).click();
                        return d;
                    }
                }
            }
        } catch (Exception e) {
        }

        return d;
    }

    private static int[] isAfterMidnight(int[] time) {
        if (time[0] < 4) {
            time[0] += 24;
        }
        return time;
    }

    private static String[] splitOnNumberAndStopName(String fullStopName) {
        String[] splitFullStopName = fullStopName.split(" ");
        int howMuchWords = splitFullStopName.length;
        String stopName = splitFullStopName[0];
        String stopNumber = splitFullStopName[howMuchWords - 1];
        for (int i = 1; i < splitFullStopName.length - 1; i++) {
            stopName = stopName + " " + splitFullStopName[i];
        }
        return new String[]{stopName, stopNumber};
    }

    private static ArrayList<VariableInDB> whatAddToDatabase(ChromeDriver d, String[] stopName, int routeNumber, String lineNumber) {
        boolean isRightStop = false;

        String conveyance = "";

        ArrayList<VariableInDB> data = new ArrayList<>();

        List<WebElement> stations = d.findElementsByClassName("pn");
        List<WebElement> stationNumber = d.findElementsByClassName("op");
        List<WebElement> webElementTime = d.findElementsByClassName("gd");
        List<WebElement> webElementRouteTime = d.findElementsByClassName("ti");

        for (int i = 0; i < stations.size() - 1; i++) {
            String from = stations.get(i).getText();
            String fromNumber = stationNumber.get(i).getText();

            if (from.equals(stopName[0]) && fromNumber.equals(stopName[1])) {
                isRightStop = true;
            }
            if (isRightStop) {
                fromNumber = stationNumber.get(i).getText();
                String toWhere = stations.get(i + 1).getText();
                String toWhereNumber = stationNumber.get(i + 1).getText();
                String time = webElementTime.get(i).getText();
                int routeTime = Integer.parseInt(webElementRouteTime.get(i + 1).getText());
                VariableInDB wiersz = new VariableInDB(conveyance, lineNumber, routeNumber, time, routeTime, from, toWhere, fromNumber, toWhereNumber);
                data.add(wiersz);
            }

        }
        return data;
    }

    private static void findAllRoute(String stop, String city, int[] time, ChromeDriver d) {

        String[][] route = findRoute(stop, city, d);
        int routeNumber = 1;

        for (int i = 0; i < route.length; i++) {
            String lineNumber = route[i][1];
            String fullStopName = route[i][0];
            String[] stopName = splitOnNumberAndStopName(fullStopName);
            d.findElementByCssSelector("[title='szukaj według linii']").click();
            d = findStop(d, lineNumber, stopName);
            d = findRightTime(time, d);

            try (Connection connectionWithDatabase = DatabaseStuff.connectWithDB()) {
                for (VariableInDB data : whatAddToDatabase(d, stopName, routeNumber, lineNumber)) {
                    DatabaseStuff.addToDB(data, connectionWithDatabase);
                }
            } catch (SQLException ex) {
            }
            routeNumber++;
        }
        d.quit();
    }

    private static String[][] selectFromDatabase(String initialStop, String finalStop) throws SQLException {
        String[][] selectedStopsFromDB = DatabaseStuff.selectFromDB(initialStop, finalStop);

        String[][] selectedRouteFromDB = new String[selectedStopsFromDB.length / 2][selectedStopsFromDB[0].length];
        int whichRow = 0;
        if (selectedStopsFromDB.length == 1) {
            return selectedStopsFromDB;
        }
        for (int i = 0; i < selectedRouteFromDB.length; i++) {
            selectedRouteFromDB[i][0] = selectedStopsFromDB[whichRow][0];
            selectedRouteFromDB[i][1] = selectedStopsFromDB[whichRow][1];
            selectedRouteFromDB[i][2] = selectedStopsFromDB[whichRow][2];
            selectedRouteFromDB[i][3] = selectedStopsFromDB[whichRow + 1][3];
            selectedRouteFromDB[i][4] = selectedStopsFromDB[whichRow + 1][4];
            selectedRouteFromDB[i][5] = selectedStopsFromDB[whichRow][5];
            selectedRouteFromDB[i][6] = selectedStopsFromDB[whichRow + 1][6];
            whichRow += 2;
        }

        int index = 0;
        int howMuchReduceTable = 0;
        for (int i = 0; i < selectedRouteFromDB.length; i++) {
            if (selectedRouteFromDB[i][1].equals(initialStop) && selectedRouteFromDB[i][3].equals(finalStop)) {
                for (int j = 0; j < selectedRouteFromDB[i].length; j++) {
                    selectedRouteFromDB[index][j] = selectedRouteFromDB[i][j];
                }
                index++;
            } else {
                howMuchReduceTable++;
            }
        }
        selectedRouteFromDB = reduceTable(selectedRouteFromDB, howMuchReduceTable, selectedRouteFromDB[0].length);
        return selectedRouteFromDB;
    }

    private static String[][] reduceTable(String[][] table, int howMuchReduceTable, int howManyColumns) {
        String[][] newTable = new String[table.length - howMuchReduceTable][howManyColumns];
        for (int i = 0; i < newTable.length; i++) {
            for (int j = 0; j < howManyColumns; j++) {
                newTable[i][j] = table[i][j];
            }
        }
        return newTable;
    }

    private static String findData(String[][] selectedStopsFromDB) {
        String data = "";
        for (int i = 0; i < selectedStopsFromDB.length; i++) {
            data = data + (i + 1) + ". Numer linii: " + selectedStopsFromDB[i][0] + " z przystanku: "
                    + selectedStopsFromDB[i][1] + " " + selectedStopsFromDB[i][2] + ", o godzinie: " + selectedStopsFromDB[i][5] + "\n na przystanek: "
                    + selectedStopsFromDB[i][3] + " " + selectedStopsFromDB[i][4] + ". Czas podróży: " + selectedStopsFromDB[i][6] + "min. \n\n";
        }
        return data;
    }

}
