package database

import (
	"context"
	"time"

	"products-api-go/config"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

// NewDatabase connects to MongoDB and returns a *mongo.Database instance.
func NewDatabase(cfg config.Config) (*mongo.Database, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	clientOpts := options.Client().ApplyURI(cfg.MongoURI)
	client, err := mongo.Connect(ctx, clientOpts)
	if err != nil {
		return nil, err
	}
	// ping
	if err := client.Ping(ctx, nil); err != nil {
		return nil, err
	}
	db := client.Database(cfg.MongoDB)
	// seed products on first run
	if err := seedProducts(ctx, db); err != nil {
		return nil, err
	}
	return db, nil
}

func seedProducts(ctx context.Context, db *mongo.Database) error {
	col := db.Collection("products")
	// check if collection has documents
	cnt, err := col.CountDocuments(ctx, bson.M{})
	if err != nil {
		return err
	}
	if cnt > 0 {
		return nil
	}
	products := []interface{}{
		bson.M{"productId": "P001", "name": "Laptop", "description": "14-inch laptop Intel i7", "price": 999.99, "active": true},
		bson.M{"productId": "P002", "name": "Mouse", "description": "Wireless mouse", "price": 29.99, "active": true},
		bson.M{"productId": "P003", "name": "Monitor", "description": "24-inch LED monitor", "price": 199.99, "active": true},
		bson.M{"productId": "P004", "name": "Teclado", "description": "Mechanical keyboard", "price": 79.99, "active": true},
		bson.M{"productId": "P005", "name": "Smartphone", "description": "Android smartphone", "price": 599.99, "active": true},
	}
	_, err = col.InsertMany(ctx, products)
	return err
}
