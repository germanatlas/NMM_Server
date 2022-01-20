package main.local;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.Manager;

public class LiteSQL {
	
	private Connection conn;
	private Statement statement;
	
	private File file;
	
	private Manager man;
	
	public LiteSQL(Manager man) {
		
		this.man = man;
		
		file = new File("users.db");
		
		try {
			
			if(!file.exists()) {
				
				file.createNewFile();
				man.print("Created Database");
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void init() {
		
		update("CREATE TABLE IF NOT EXISTS userdata(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, username VARCHAR, pass INTEGER)");
		
	}
	
	public void connect() {
		
		
		try {
			
			String url = "jdbc:sqlite:" + file.getPath();
			conn = DriverManager.getConnection(url);
			man.print("Connected to Database.");
			statement = conn.createStatement();
			init();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void disconnect() {
		
			try {
				if(conn != null) {
					conn.close();
					man.print("Closed Database.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
	}
	
	public void update(String sql) {
		
		try {
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public ResultSet search(String sql) {
		
		try {
			return statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
