/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package szczech.anna.ztm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static javax.swing.text.html.HTML.Tag.SELECT;

/**
 *
 * @author Ania
 */
public class DatabaseStuff {

    public static Connection connectWithDB() throws SQLException {
        Connection connectionWithDB = DriverManager.getConnection("jdbc:sqlite:mojabaza.db");
        Statement statement = connectionWithDB.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `ZTM` (\n"
                + "	`id_ZTM`	INTEGER,\n"
                + "	`srodek_transportu`	TEXT,\n"
                + "	`nr_linii`	TEXT,\n"
                + "	`nr_trasy`	INTEGER,\n"
                + "	`skad`	TEXT,\n"
                + "	`nr_skad`	TEXT,\n"
                + "	`dokad`	TEXT,\n"
                + "	`nr_dokad`	TEXT,\n"
                + "	`godzina`	TEXT,\n"
                + "	`czas_trasy`	INTEGER,\n"
                + "	PRIMARY KEY(`id_ZTM`)\n"
                + ");");
        return connectionWithDB;
    }

    public static void removeData() {
        try {
            Connection connectionWithDB = connectWithDB();
            PreparedStatement statement = connectionWithDB.prepareStatement("DELETE FROM ZTM");
            statement.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addToDB(VariableInDB data, Connection connectionWithDB) throws SQLException {
        PreparedStatement statement = connectionWithDB.prepareStatement("insert into ZTM (srodek_transportu,nr_linii, nr_trasy, skad, nr_skad, dokad, nr_dokad, godzina, czas_trasy) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, data.getConveyance());
        statement.setString(2, data.getLineNumber());
        statement.setInt(3, data.getRouteNumber());
        statement.setString(4, data.getInitialStop());
        statement.setString(5, data.getInitialStopNumber());
        statement.setString(6, data.getFinalStop());
        statement.setString(7, data.getFinalStopNumber());
        statement.setString(8, data.getTime());
        statement.setInt(9, data.getRouteTime());
        statement.executeUpdate();
    }

    public static String[][] selectFromDB(String initialStop, String finalStop) throws SQLException {
        try {
            Connection connectionWithDB = connectWithDB();
            PreparedStatement statement = connectionWithDB.prepareStatement("SELECT DISTINCT* FROM (SELECT nr_trasy FROM ZTM WHERE dokad = '" + finalStop + "') A LEFT JOIN ZTM ON A.nr_trasy=ZTM.nr_trasy WHERE  dokad = '" + finalStop + "' OR skad='" + initialStop + "' ORDER BY id_ZTM");

            ResultSet result = statement.executeQuery();

            String[][] selectedFromDB = new String[0][7];

            for (int i = 0; result.next(); i++) {
                selectedFromDB = FindCommunication.enlargeTable(selectedFromDB, 1, 7);
                selectedFromDB[i][0] = result.getString("nr_linii");
                selectedFromDB[i][1] = result.getString("skad");
                selectedFromDB[i][2] = result.getString("nr_skad");
                selectedFromDB[i][3] = result.getString("dokad");
                selectedFromDB[i][4] = result.getString("nr_dokad");
                selectedFromDB[i][5] = result.getString("godzina");
                selectedFromDB[i][6] = result.getString("czas_trasy");
            }
            return selectedFromDB;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static boolean isFinish(String finalStop) {
        try {
            Connection connectionWithDB = connectWithDB();
            PreparedStatement statement = connectionWithDB.prepareStatement("SELECT DISTINCT dokad FROM ZTM WHERE dokad='" + finalStop + "'");

            ResultSet result = statement.executeQuery();

            ArrayList<String> goal = new ArrayList<>();

            while (result.next()) {
                goal.add(result.getString("dokad"));
            }
            return (!goal.get(0).equals(""));
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

}
