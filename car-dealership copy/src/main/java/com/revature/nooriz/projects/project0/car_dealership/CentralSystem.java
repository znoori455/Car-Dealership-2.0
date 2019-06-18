package com.revature.nooriz.projects.project0.car_dealership;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.revature.util.ConnectionFactory;

public class CentralSystem {
	Connection conn = ConnectionFactory.getConnection();
	final static Logger logger = Logger.getLogger(CentralSystem.class);
	
	public void start(CarLot lot, UserList users) {
		Scanner reader = new Scanner(System.in);
		String choice = "";
		
		while (!choice.equals("3")) {
			System.out.println("Welcome to Revature's Repo/Resale Shop! "
					+ "Press 1 to login, 2 to create an account, "
					+ "or 3 to terminate application.");
			
			choice = reader.nextLine();
			switch (choice) {
			
			case "1":
				login(lot, users);
				break;
				
			case "2": 
				accountRegister(users);
				break;
				
			case "3":
				break;
				
			default:
				System.out.println("Invalid choice.");
				break;
			}
		}		
	}
	

	//Login: Case 1 for start
	public void login(CarLot lot, UserList users) {
		Scanner reader = new Scanner(System.in);
		String username, password, type;
		System.out.println("Please enter 1 to login as a customer, 2 for employee, "
				+ "or 3 to cancel.");
		type = reader.nextLine();
		
		
		while (!type.equals("3")) {
			

			switch (type) {
			case "1": //customer
				
				System.out.println("Please enter your username.");
				username = reader.nextLine();
				System.out.println("Please enter your password.");
				password = reader.nextLine();
				
				for (Customer cust : users.getCustomers()) {
					if (cust.getUsername().equals(username) && cust.getPassword().equals(password)) {
						logger.trace(cust.getUsername() + " logged in.");
						cust.newCar();
						cust.choices(lot);
						return;
					}
				}
				System.out.println("Not a valid customer.");
				return;
				
			case "2": //employee
				System.out.println("Please enter your username.");
				username = reader.nextLine();
				System.out.println("Please enter your password.");
				password = reader.nextLine();
				
				for (Employee emp : users.getEmployees()) {
					if (emp.getUsername().equals(username) && emp.getPassword().equals(password)) {
						logger.trace(emp.getUsername() + " logged in.");
						emp.choices(lot, users);
						return;
					}
				}
				System.out.println("Not a valid employee.");
				return;
				
			case "3": //cancel
				return;
				
			default: //wrong input
				System.out.println("Not a valid response. "
						+ "Please enter 1 to login as a customer, 2 for employee, or 3 to cancel.");
				type = reader.nextLine();
				break;
			}
		}
	}
	
	
	//Account registration: case 2 for start
	public void accountRegister(UserList users) {
		
		Scanner reader = new Scanner(System.in);
		String username, password, type;
		System.out.println("Please enter a username for your account.");
		username = reader.nextLine();
		System.out.println("Please enter a password for your account.");
		password = reader.nextLine();
		System.out.println("What type of account would you like to create?"); 
		System.out.println("Press 1 for customer, 2 for employee, or 3 to cancel.");
		type = reader.nextLine();
		
		while (!type.equals("3")) {
			
			switch (type) {
			
			case "1": //new customer account
				for (Customer c : users.getCustomers()) {
					if (c.getUsername().equals(username)) {
						System.out.println("Error! Customer username already exists. Please make another choice.");
						return;
					}
				}
				
				Customer cust = new Customer(username, password);
				users.addCustomer(cust);
				logger.info(cust.getUsername() + " created a new customer account.");
				return;
				
			case "2": //new employee account
				for (Employee e : users.getEmployees()) {
					if (e.getUsername().equals(username)) {
						System.out.println("Error! Employee username already exists. Please make another choice.");
						return;
					}
				}
				
				System.out.println("Action must be approved by an employee.");
				if (approveNewEmployee(users)) {
					Employee emp = new Employee(username, password);
					users.addEmployee(emp);
					logger.info(emp.getUsername() + " created a new employee account.");
					return;
				}
				return;
				
			default:
				System.out.println("Not a valid response. "
						+ "Press 1 for customer, 2 for employee, or 3 to cancel.");
				type = reader.nextLine();
				break;
			}
		}	
	}
	
	
	
	//called from accountRegister case 2
	public boolean approveNewEmployee(UserList users) {
		Scanner reader = new Scanner(System.in);
		String username, password;
		
		System.out.println("Please enter your username.");
		username = reader.nextLine();
		System.out.println("Please enter your password.");
		password = reader.nextLine();
		
		for (Employee emp : users.getEmployees()) {
			if (emp.getUsername().equals(username) && emp.getPassword().equals(password)) {
				return true;
			} 
		}
		
		System.out.println("Employee login authentication failed.");
		return false;
	}
	
	
	

	
	
	
	public void load() {
		UserList users = UserList.loadUsers();
		CarLot lot = CarLot.loadLot(users);
		logger.info("Dealership loaded successfully.");

//			System.out.println(lot.getCarLot());
//			System.out.println(users.getEmployees());
//			System.out.println(users.getCustomers());
			start(lot, users);

		
	}	

}
