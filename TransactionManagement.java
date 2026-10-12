import java.util.*;
import java.sql.*;

class JDBC {
    String url = "jdbc:mysql://localhost:3306/MyDatabase";
    String user = "root";
    String password = "...";
    Scanner in = new Scanner(System.in);

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    void tranferBal() throws SQLException {
        String debit = "UPDATE Bank SET balance=balance-? WHERE acc_no=?";
        String credit = "UPDATE Bank SET balance=balance+? WHERE acc_no=?";

        Connection con= null;
        try  {
            con = getConnection();
            con.setAutoCommit(false);
            System.out.print("\nEnter Source Account number: ");
            int src = in.nextInt();
            if(checkAcc(src)){
               System.out.print("Enter Destination Account number: ");
               int dest= in.nextInt();
               System.out.print("Enter Amount to Trasnfer: ");
               int amt= in.nextInt();
               if(src==dest || amt<=0){
                   System.out.println("Invalid accounts or Amount!...");
               }
               String q= "SELECT balance FROM Bank WHERE acc_no=?";
               PreparedStatement ps= con.prepareStatement(q);
               ps.setInt(1,src);
               ResultSet rs= ps.executeQuery();
               int bal=0;
               if(rs.next())
                   bal= rs.getInt(1);
               if(bal>=amt){
                   //Debit
                   PreparedStatement ps1= con.prepareStatement(debit);
                   ps1.setInt(1,amt);
                   ps1.setInt(2,src);
                   ps1.executeUpdate();

                   //Credit
                   PreparedStatement ps2= con.prepareStatement(credit);
                   ps2.setInt(1,amt);
                   ps2.setInt(2,dest);
                   int count= ps2.executeUpdate();
                   if(count>0){
                       System.out.println("\nTransaction Successful!...\n");
                       con.commit();
                   }
                   else{
                       System.out.println("\nTransaction Failed!...\n");
                       con.rollback();
                   }

               }
                else{
                    System.out.println("\nInsufficient Balance!...\n");
                    con.rollback();
               }

            }
            else{
                System.out.println("\nAccount not found! Please enter a valid account number...\n");

            }
            con.close();

        } catch(SQLException s){
            System.out.println(s);
            con.rollback();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    void display(){
        String q= "SELECT * FROM Bank";
        try(Connection con= getConnection();
            PreparedStatement ps= con.prepareStatement(q);
            ResultSet rs= ps.executeQuery()) {
            System.out.printf("\n%-10s %-15s %-10s%n","Account No.","Account Holder","Balance");
            while(rs.next()){
                System.out.printf("%-10d %-15s %-10d%n",rs.getInt("acc_no"),rs.getString("acc_holder"),rs.getInt("balance"));
            }
            System.out.println();
            con.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    boolean checkAcc(int acc_no){
        String q="SELECT acc_no FROM Bank WHERE acc_no=?";
        try(Connection con= getConnection();
            PreparedStatement ps= con.prepareStatement(q)){
            ps.setInt(1,acc_no);
            ResultSet rs= ps.executeQuery();
            if(!rs.next())
                return false;
            con.close();
        }catch(Exception e){
            System.out.println(e);
        }
        return true;
    }
}


public class TransactionManagement {
    public static void main(String[] args) throws SQLException {
        Scanner in= new Scanner(System.in);
       JDBC jdbc= new JDBC();
       int ch;
       do{
           System.out.println("1.Transfer Amount");
           System.out.println("2.Display Details");
           System.out.println("3.Exit");
           System.out.print("Enter Choice: ");
           ch=in.nextInt();
           switch(ch){
               case 1: jdbc.tranferBal();
                        break;
               case 2: jdbc.display();
                        break;
               case 3: System.out.println("Exiting...");
                        break;
               default: System.out.println("\nInvalid choice, Please try again...\n");
           }
       }while(ch!=3);
    }
}
