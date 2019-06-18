package com.revature.nooriz.projects.project0.car_dealership;

import java.sql.CallableStatement;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Scanner;
import org.apache.log4j.Logger;


import com.revature.util.ConnectionFactory;

public class Employee extends User {
	final static Logger logger = Logger.getLogger(CentralSystem.class);
	private static Connection conn = ConnectionFactory.getConnection();
	
	public Employee(String username, String password) {
		super(username, password);
	}
	
	@Override
	public String toString() {
		return "Employee Name: "+ getUsername();
	}

	@Override
	public void printChoices() {
		System.out.println("Press: 1 to add a car, 2 to remove a car,"
				+ " 3 to manage offers, 4 to view all payments, or 5 to cancel.");
	}
	
	
	public void choices (CarLot lot, UserList users) {
		System.out.println("What would you like to do?");
		printChoices();
		
		Scanner reader = new Scanner(System.in);
		String choice = reader.nextLine();
		
		while (!choice.equals("5")) {
			switch (choice) {
			case "1": 
				lot = addCar(lot);
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "2":
				lot = removeCar(lot);
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "3":
				lot = manageOffers(lot, users);
				System.out.println("Anything else?");
				printChoices();
				choice = reader.nextLine();
				break;
			case "4":
				viewAllPayments(users);
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
	
	
	
	public CarLot addCar(CarLot lot) {
		Car car = new Car();

		Scanner reader = new Scanner(System.in);
		System.out.println("Enter make and model.");
		car.setMakeModel(reader.nextLine());
		System.out.println("Enter mileage.");
		car.setMileage(reader.nextLine());
		System.out.println("Enter year.");
		car.setYear(reader.nextLine());
		
		try {
			PreparedStatement pstmt = conn.prepareStatement("select newCar(? , ?, ?);");
			pstmt.setString(1, car.getMakeModel());
			pstmt.setString(2, car.getMileage());
			pstmt.setString(3, car.getYear());
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			car.setCarID(String.valueOf(rs.getInt(1)));
			lot.addCar(car);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Failed to add new car to database.");
		}
		
		return lot;
	}
	
	
	
	public CarLot removeCar(CarLot lot) {
	
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter the car ID.");
		String id = reader.nextLine();
		
		for (Car car : lot.getCarLot()) {
			if (car.getCarID().equals(id)) {
				try {
					PreparedStatement pstmt = conn.prepareStatement("update car set active = 'f' where carid = ?;");
					pstmt.setInt(1, Integer.parseInt(id));
					pstmt.executeUpdate();
					lot.removeCar(car);
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("Failed to soft delete car for CarID = " + car.getCarID());
				}
				return lot;
			}
		}
		System.out.println("Car not found.");
		return lot;
	}
	
	
	
	public CarLot manageOffers (CarLot lot, UserList users) {
		
		Scanner reader = new Scanner(System.in);
		System.out.println("Please enter the car ID.");
		String carId = reader.nextLine();

		for (Car car : lot.getCarLot()) {
			if (car.getCarID().equals(carId)) {
				car.printOffers();
				System.out.println("Type a customer's name to accept an offer, or enter cancel");
				String customer = reader.nextLine();
				
				for (Customer cust : users.getCustomers()) {
					if (cust.getUsername().equalsIgnoreCase(customer)) {
						
						try {
							String sql = "{call approveoffer(?, ?, ?)}";
							CallableStatement call = conn.prepareCall(sql);
							call.setInt(1, Integer.parseInt(car.getCarID()));
							call.setString(2, cust.getUsername());
							call.setString(3, this.getUsername());
							call.executeQuery();
//							PreparedStatement pstmt = conn.prepareStatement("select approveoffer(?, ?, ?);");
//							pstmt.setInt(1, Integer.parseInt(car.getCarID()));
//							pstmt.setString(2, cust.getUsername());
//							pstmt.setString(3, this.getUsername());
//							pstmt.executeQuery();
						} catch (SQLException e) {
							e.printStackTrace();
							logger.error("Failed to accept offer for carID = " + car.getCarID());
							return lot;
						}
						
						car.setOwner(cust);
						double price = car.getOffers().get(cust);
						logger.info("Car sold to " + cust.getUsername()
								+ " for $" + car.getOffers().get(cust) + ".");
						car.getOffers().clear();
						car.setPrice(price);
						cust.getOwnedCars().add(car);
						lot.removeCar(car);
						car.setNewSale(true);

						return lot;
					}
				}
				System.out.println("Car not sold.");
				return lot;
			}
		}
		System.out.println("Car not found");
		return lot;
	}
	
	
	
	public void viewAllPayments (UserList users) {
		for (Customer cust : users.getCustomers()) {
			
			if (!cust.getOwnedCars().isEmpty()) {
				
				for (Car car : cust.getOwnedCars()) {
					System.out.println(cust + ", Car: " + car);
					System.out.println("		 Payments made: $" + car.getPaymentsMade()
					+ " Payments remaining: $" + car.getRemainingPayments());
				}
			}
		}
	}
	
}
