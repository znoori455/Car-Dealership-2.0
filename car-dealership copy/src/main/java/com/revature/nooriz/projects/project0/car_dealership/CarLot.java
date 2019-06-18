package com.revature.nooriz.projects.project0.car_dealership;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import com.revature.util.ConnectionFactory;

public class CarLot implements Serializable{
	final static Logger logger = Logger.getLogger(CentralSystem.class);


	private static Connection conn = ConnectionFactory.getConnection();

	
	private static List<Car> carLot = new LinkedList<Car>();

	public List<Car> getCarLot() {
		return carLot;
	}
	public void setCarLot(LinkedList<Car> carLot) {
		CarLot.carLot = carLot;
	}
	
	public void addCar(Car a) {
		carLot.add(a);
	}
	
	public void removeCar(Car a) {
		carLot.remove(a);
	}
	
	//Loads all cars on the lot
	public static CarLot loadLot(UserList users) {
		CarLot lot = new CarLot();
		
		try {
			PreparedStatement pstmt = conn.prepareStatement("select * from car where active = 't';");
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				carLot.add(new Car(String.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3), rs.getString(4)));
			}
			for (Car car: lot.getCarLot()) {
				car.loadOffers(users);
			}
			
		} catch (Exception e) {
			logger.fatal("Carlot failed to load properly.");
			System.exit(0);
			
		}
		return lot;
	}

}
