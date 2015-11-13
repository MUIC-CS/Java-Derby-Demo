import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;



public class DemoApp
{
    private String framework = "embedded";
    private String protocol = "jdbc:derby:";

    public static void main(String[] args)
    {
        new DemoApp().go();
        System.out.println("Done exit");
    }

    void go()
    {

        System.out.println("Start!!!!!!");

        Connection conn = null;
        PreparedStatement ps;
        Statement s;
        ResultSet rs = null;
        try
        {
 
            //all you data will be store in demoDB directory
            String dbName = "demoDB"; // the name of the database
            conn = DriverManager.getConnection(protocol + dbName
                    + ";create=true");

            System.out.println("Connected to and created database " + dbName);

            s = conn.createStatement();
            
            // We create a table...
            //read sql tutorial or something
            s.execute("create table students(name varchar(255), money integer)");
            System.out.println("Created table student");

            ArrayList<String> kids = new ArrayList<String>();
            kids.add("May");
            kids.add("Sea");
            kids.add("Ice");

            ps = conn.prepareStatement(
                "INSERT INTO students values (?, ?)");
            
            for(int i=0;i<kids.size();i++){
                ps.setString(1,kids.get(i));//replace first question mark safely
                ps.setInt(2,i*100);//replace second question mark safely
                //If you want to know why we don't just add the string and execute
                //google up sql injection.
                ps.executeUpdate();
            }

            //read sql
            rs = s.executeQuery(
                    "SELECT name, money FROM students"
                );

            while(rs.next()){
                String name = rs.getString(1);//get first column as string
                int money = rs.getInt(2); //get second column as int
                System.out.println("---"+name+" -- $"+money);
            }

            s.executeUpdate(
                    "UPDATE students SET money=999 WHERE name='Sea'"
                );

            System.out.println("---------UPDATED---------");

            rs = s.executeQuery(
                    "SELECT name, money FROM students"
                );

            while(rs.next()){
                String name = rs.getString(1);//get first column as string
                int money = rs.getInt(2); //get second column as int
                System.out.println("+++"+name+" -- $"+money);
            }

          
            s.execute("drop table students");
            System.out.println("Dropped table");

            /*
               We commit the transaction. Any changes will be persisted to
               the database now.
             */
            conn.commit();
            System.out.println("Committed the transaction");

           
            shutdownDB();
        }
        catch (SQLException sqle)
        {
            printSQLException(sqle);
        } finally {
            //see properway to do this in SimpleApp
            try{
                rs.close();
                conn.close();
            }
            catch(SQLException sqle){
                printSQLException(sqle);
            }
        }
    }


    //I just copy this from SimpleApp
    public static void shutdownDB(){
        try
        {
            // the shutdown=true attribute shuts down Derby
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        }
        catch (SQLException se)
        {
            if (( (se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState()) ))) {
                // we got the expected exception
                System.out.println("Derby shut down normally");
                // Note that for single database shutdown, the expected
                // SQL state is "08006", and the error code is 45000.
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                System.err.println("Derby did not shut down normally");
                printSQLException(se);
            }
        }
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }


}
