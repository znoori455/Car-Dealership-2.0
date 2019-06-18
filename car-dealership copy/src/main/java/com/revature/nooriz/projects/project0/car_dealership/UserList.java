package com.revature.nooriz.projects.project0.car_dealership;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.revature.util.ConnectionFactory;

public class UserList {
	final static Logger logger = Logger.getLogger(CentralSystem.class);
	private static Connection conn = ConnectionFactory.getConnection();

	
	private static List<Customer> customers = new ArrayList<>();
	private static List<Employee> employees = new ArrayList<>();

	public List<Customer> getCustomers() {
		return customers;
	}
	public void setCustomers(ArrayList<Customer> customers) {
		UserList.customers = customers;
	}
	public List<Employee> getEmployees() {
		return employees;
	}
	public void setEmployees(ArrayList<Employee> employees) {
		UserList.employees = employees;
	}
	
	
	
	public static UserList loadUsers() {
		UserList users = new UserList();
		
		try {
			PreparedStatement pstmt = conn.prepareStatement("select * from customer;");
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				customers.add(new Customer(rs.getString(2), rs.getString(3)));
			}
			
			for(Customer cust: customers) {
				cust.loadOwnedCars();
			}
			
			
			
			pstmt = conn.prepareStatement("select * from employee;");
			rs = pstmt.executeQuery();
			while(rs.next()) {
				employees.add(new Employee(rs.getString(2), rs.getString(3)));
			}
			
		} catch (SQLException e) {
				e.printStackTrace();
				logger.fatal("Users failed to load properly.");
				System.exit(0);
			}
		
		return users;	
	}
	
	
	
	public void addCustomer(Customer cust) {
		try {
			PreparedStatement pstmt = conn.prepareStatement("select newCustomer(? , ?);");
			pstmt.setString(1, cust.getUsername());
			pstmt.setString(2, cust.getPassword());
			pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Failed to add customer: " + cust.getUsername());
			return;
		}
		
		customers.add(cust);
	}
	
	
	
	public void addEmployee(Employee emp) {
		try {
			PreparedStatement pstmt = conn.prepareStatement("select newEmployee(? , ?);");
			pstmt.setString(1, emp.getUsername());
			pstmt.setString(2, emp.getPassword());
			pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Failed to add employee: " + emp.getUsername());
			return;
		}
		
		employees.add(emp);
	}
	

}
