/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package szczech.anna.ztm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Ania
 */
public class Metro {

    private static boolean isTravelByMetroPossible(String stop) {
        return ((stop.contains("Metro")) || stop.equals("Centrum") || stop.equals("Dw.Gdański") || stop.equals("Pl.Wilsona")
                || stop.equals("Rondo Daszyńskiego") || stop.equals("Rondo ONZ") || stop.equals("Nowy Świat – Uniwersytet") || stop.equals("Dw.Wileński"));
    }

    public static boolean isTravelByMetroPossible(String initialStop, String finalStop) {
        return (Metro.isTravelByMetroPossible(initialStop) && Metro.isTravelByMetroPossible(finalStop));
    }

    private static boolean isMetroM1(String initialStop, String finalStop) {
        for (int i = 0; i < M1Mlociny.length; i++) {
            if (M1Mlociny[i][0].contains(initialStop)) {
                for (int j = 0; j < M1Mlociny.length; j++) {
                    if (M1Mlociny[j][0].contains(finalStop)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String[][] whichDirection(String initialStop, String finalStop) {
        if (isMetroM1(initialStop, finalStop)) {
            for (int i = 0; i < M1Mlociny.length; i++) {
                if (M1Mlociny[i][0].contains(initialStop)) {
                    return M1Mlociny;
                }
                if (M1Mlociny[i][0].contains(finalStop)) {
                    return M1Kabaty;
                }
            }
        } else {
            for (int i = 0; i < M2DworzecWilenski.length; i++) {
                if (M2DworzecWilenski[i][0].contains(initialStop)) {
                    return M2DworzecWilenski;
                }
                if (M2DworzecWilenski[i][0].contains(finalStop)) {
                    return M2RondoDaszynskiego;
                }
            }
        }
        return new String[][]{{"BŁĄD"}};
    }

    private static ArrayList<VariableInDB> whatAddToDB(String initialStop, String finalStop) {
        boolean isInitialStop = false;
        String[][] route = whichDirection(initialStop, finalStop);

        String conveyance = "";
        String lineNumber = "";
        if (route == M1Kabaty || route == M1Mlociny) {
            lineNumber = "M1";
        } else {
            lineNumber = "M2";
        }
        int routeNumber = 1;
        int initialRouteTime = 0;

        ArrayList<VariableInDB> data = new ArrayList<>();

        for (int i = 0; i < route.length - 1; i++) {
            if (route[i][0].contains(initialStop)) {
                isInitialStop = true;
                initialRouteTime = Integer.parseInt(route[i][1]);

            }
            if (isInitialStop) {
                String from = route[i][0];
                String fromNumber = "";
                String direction = route[i + 1][0];
                String directionNumber = "";
                String time = "Odjazd co 4 min";
                int routeTime = Integer.parseInt(route[i + 1][1]) - initialRouteTime;
                VariableInDB row = new VariableInDB(conveyance, lineNumber, routeNumber, time, routeTime, from, direction, fromNumber, directionNumber);
                data.add(row);
            }
        }
        return data;
    }

    public static void addToDatabase(String initialStop, String finalStop) {
        try (Connection connectionWithDatabase = DatabaseStuff.connectWithDB()) {
            for (VariableInDB dane : whatAddToDB(initialStop, finalStop)) {
                DatabaseStuff.addToDB(dane, connectionWithDatabase);
            }
        } catch (SQLException ex) {
        }

    }

    private static String[][] M1Kabaty = new String[][]{{"Metro Kabaty", "0"},
    {"Metro Natolin", "2"},
    {"Metro Imielin", "4"},
    {"Metro Stokłosy", "6"},
    {"Metro Ursynów", "8"},
    {"Metro Służew", "10"},
    {"Metro Wilanowska", "12"},
    {"Metro Wierzbno", "13"},
    {"Metro Racławicka", "15"},
    {"Metro Pole Mokotowskie", "17"},
    {"Metro Politechnika", "18"},
    {"Centrum", "21"},
    {"Metro Świętokrzyska", "22"},
    {"Metro Ratusz Arsenał", "24"},
    {"Dw.Gdański", "27"},
    {"Pl.Wilsona", "30"},
    {"Metro Marymont", "32"},
    {"Metro Słodowiec", "33"},
    {"Metro Stare Bielany", "35"},
    {"Metro Wawrzyszew", "37"},
    {"Metro Młociny", "39"},};

    private static String[][] M1Mlociny = new String[][]{{"Metro Młociny", "0"},
    {"Metro Wawrzyszew", "2"},
    {"Metro Stare Bielany", "4"},
    {"Metro Słodowiec", "6"},
    {"Metro Marymont", "7"},
    {"Pl.Wilsona", "9"},
    {"Dw.Gdański", "12"},
    {"Metro Ratusz Arsenał", "15"},
    {"Metro Świętokrzyska", "17"},
    {"Centrum", "18"},
    {"Metro Politechnika", "21"},
    {"Metro Pole Mokotowskie", "22"},
    {"Metro Racławicka", "24"},
    {"Metro Wierzbno", "26"},
    {"Metro Wilanowska", "27"},
    {"Metro Służew", "29"},
    {"Metro Ursynów", "31"},
    {"Metro Stokłosy", "33"},
    {"Metro Imielin", "35"},
    {"Metro Natolin", "37"},
    {"Metro Kabaty", "39"}};

    private static String[][] M2RondoDaszynskiego = new String[][]{{"Rondo Daszyńskiego", "0"},
    {"Rondo ONZ", "2"},
    {"Metro Świętokrzyska", "4"},
    {"Nowy Świat - Uniwersytet", "5"},
    {"Metro Centrum Nauki Kopernik", "7"},
    {"Metro Stadion Narodowy", "9"},
    {"Dw.Wileński", "11"}};

    private static String[][] M2DworzecWilenski = new String[][]{{"Dw.Wileński", "0"},
    {"Metro Stadion Narodowy", "2"},
    {"Metro Centrum Nauki Kopernik", "2"},
    {"Nowy Świat - Uniwersytet", "2"},
    {"Metro Świętokrzyska", "1"},
    {"Rondo ONZ", "2"},
    {"Rondo Daszyńskiego", "2"}};

}
