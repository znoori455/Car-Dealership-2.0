package com.revature.nooriz.projects.project0.car_dealership;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.revature.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Driver {
	
	public static void main(String[] args) {
		
		CentralSystem sys = new CentralSystem();
		sys.load();
	}

}
