package com.example.demo.Services;

import com.example.demo.Config.MongoClientConnectionExample;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MongoConnectionService {
    
    @Autowired
    private MongoClientConnectionExample mongoClientConnection;
    
    @Autowired
    private MongoClient mongoClient;
    
    @Value("${spring.data.mongodb.database}")
    private String databaseName;
    
    public boolean testConnection() {
        try {
            mongoClientConnection.testConnection();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            return false;
        }
    }
    
    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(databaseName);
    }
    
    public void insertTestDocument() {
        try {
            MongoDatabase database = getDatabase();
            var collection = database.getCollection("test");
            
            Document doc = new Document("name", "Test Document")
                    .append("timestamp", System.currentTimeMillis())
                    .append("message", "Hello from Spring Boot!");
                    
            collection.insertOne(doc);
            System.out.println("Test document inserted successfully!");
        } catch (Exception e) {
            System.err.println("Failed to insert test document: " + e.getMessage());
        }
    }
}