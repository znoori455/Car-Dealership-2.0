package com.revature.nooriz.projects.project0.car_dealership;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.revature.util.ConnectionFactory;

public class Customer extends User {
	final static Logger logger = Logger.getLogger(CentralSystem.class);
	private static Connection conn = ConnectionFactory.getConnection();
	
	public Customer(String username, String password) {
		super(username, password);
	}


	private ArrayList<Car> ownedCars = new ArrayList<Car>();
	

	public ArrayList<Car> getOwnedCars() {
		return ownedCars;
	}
	public void setOwnedCars(ArrayList<Car> ownedCars) {
		this.ownedCars = ownedCars;
	}
	
	
	@Override
	public String toString() {
		return "Customer Name: "+ getUsername();
	}
	
	@Override
	public void printChoices() {
		System.out.println("Press: 1 to view cars, 2 to make an offer, "
				+ "3 to view owned cars, 4 to view remaining payments,"
				+ " 5 to make a payment, or 6 to cancel.");
	}
	
	
	
	public void choices(CarLot lot) {
		System.out.println("What would you like to do?");
		printChoices();
		
		Scanner reader = new Scanner(System.in);
		String choice = reader.nextLine();
		
		while (!choice.equals("6")) {
			switch (choice) {
			case "1": 
				viewCars(lot);
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "2":
				makeOffer(lot);
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "3":
				viewOwnedCars();
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "4":
				viewRemainingPayments();
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "5":
				makePayment();
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			default:
				System.out.println("Error! Not a valid choice.");
				printChoices();
				choice = reader.nextLine();
				break;
			}
		}
		
	}
	
	
	
	public void viewCars(CarLot lot) {
		for (Car car : lot.getCarLot())
		System.out.println(car);
	}
	
	
	
	public void makeOffer (CarLot lot) {
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter the car ID.");
		String carId = reader.nextLine();
		
		for (Car car : lot.getCarLot()) {
			if (car.getCarID().equals(carId)) {
				
				if (car.getOffers().containsKey(this) ) {
					System.out.println("Duplicate offers are not allowed on the same car!");
					return;
				}
				
				System.out.println("How much are you offering?");
				Double offer = reader.nextDouble();
				
				try {
					PreparedStatement pstmt = conn.prepareStatement("select newOffer(?, ?, ?);");
					pstmt.setString(1, this.getUsername());
					pstmt.setInt(2, Integer.parseInt(carId));
					pstmt.setDouble(3, offer);
					pstmt.executeQuery();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				car.getOffers().put(this, offer);
				logger.info(this.getUsername() + " made an offer on CarID: " + car.getCarID());
				return;
			}
		}
		System.out.println("Car not found.");
	}
	
	
	
	
	public void viewOwnedCars() {
		if (ownedCars.isEmpty()) {
			System.out.println("You have not purchased a car yet.");
		} else {
			System.out.println(ownedCars);
		}
		
	}
	
	public void viewRemainingPayments() {
		if (ownedCars.isEmpty()) {
			System.out.println("You have not purchased a car yet.");
			
		} else {
			for (Car car : ownedCars) {

				System.out.println(car + "Payments remaining: $" + car.getRemainingPayments());
			}
		}
		
	}
	
	
	
	public void makePayment() {
		if (ownedCars.isEmpty()) {
			System.out.println("You have not purchased a car yet.");
			
			
		} else {
			Scanner reader = new Scanner(System.in);
			System.out.println("Please enter the car ID.");
			String carID = reader.nextLine();
			
			for (Car car : ownedCars) {
				if (car.getCarID().equals(carID)) {
					System.out.println("How much would you like to pay?");
					double amount = reader.nextDouble();
					
					try {
						PreparedStatement pstmt = conn.prepareStatement("select makePayment(?, ?);");
						pstmt.setInt(1, Integer.parseInt(carID));
						pstmt.setDouble(2, amount);
						ResultSet rs = pstmt.executeQuery();
						rs.next();
						logger.info(this.getUsername() + " made a payment of $" + amount + 
								" on carID: " + car.getCarID());
						System.out.println("Total Payments made: $" + rs.getDouble(1));
						
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error("Failed to save " + this.getUsername() + "'s payment to database.");
					}
					
					car.setPaymentsMade( car.getPaymentsMade() + amount );
					return;
				}
			}
			System.out.println("Car not found!");
		}
	}
	
	
	
	public void newCar() {
		for (Car car : ownedCars) {
		
			if (car.isNewSale()) {
				Scanner reader = new Scanner(System.in);
				System.out.println("Congratulations! Your offer for " +car+ " was accepted.");
				System.out.println("How many months would you like to pay it off in?");
				car.calculateMonthlyPayments(reader.nextDouble());
				
				try {
					PreparedStatement pstmt = conn.prepareStatement("select set_monthly(?, ?);");
					pstmt.setInt(1, Integer.parseInt(car.getCarID()));
					pstmt.setDouble(2, car.getMonthlyPayments());
					pstmt.executeQuery();
					logger.info(this.getUsername() + " chose monthly payments of $" 
					+ car.getMonthlyPayments() + " a month on carID: " + car.getCarID());
					
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("Failed to set monthly payments on " + car.getCarID());
				}
				car.setNewSale(false);
			}
		}
	}
	

	
	public void loadOwnedCars() {
		try {
			PreparedStatement pstmt = conn.prepareStatement("select * from car where ownerid in "
					+ "(select customerID from customer where username = ?);");
			pstmt.setString(1, this.getUsername());
			ResultSet rs = pstmt.executeQuery();

				while (rs.next()) {
					Car car;
					ownedCars.add(car = new Car(String.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5)));
					try {
						
						//loading payments made
						pstmt = conn.prepareStatement("select SUM(amount) from payment where carid = ?;");
						pstmt.setInt(1, Integer.parseInt(car.getCarID()));
						ResultSet rs2 = pstmt.executeQuery();
						rs2.next();
						car.setPaymentsMade(rs2.getDouble(1));
						car.setRemainingPayments(car.getPrice()-car.getPaymentsMade());
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			
		} catch (SQLException e) {
			logger.error(this.getUsername() + "'s owned cars failed to load properly.");
			e.printStackTrace();
		}
	}
	
	

}
