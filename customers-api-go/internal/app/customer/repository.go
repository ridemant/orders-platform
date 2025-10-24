package customer

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var (
	ErrNotFound = errors.New("customer not found")
)

type Repository struct {
	col *mongo.Collection
}

func NewRepository(col *mongo.Collection) *Repository {
	return &Repository{col: col}
}

func (r *Repository) FindByID(ctx context.Context, id string) (*Customer, error) {
	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	filter := bson.M{"customerId": id}
	var c Customer
	err := r.col.FindOne(ctx, filter).Decode(&c)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, ErrNotFound
		}
		return nil, err
	}
	return &c, nil
}

func (r *Repository) SeedCustomers(ctx context.Context) error {
	ctx, cancel := context.WithTimeout(ctx, 10*time.Second)
	defer cancel()

	idxModel := mongo.IndexModel{
		Keys:    bson.D{{Key: "customerId", Value: 1}},
		Options: options.Index().SetUnique(true),
	}
	_, _ = r.col.Indexes().CreateOne(ctx, idxModel)

	count, err := r.col.CountDocuments(ctx, bson.D{})
	if err != nil {
		return err
	}
	if count > 0 {
		return nil
	}

	docs := []interface{}{
		bson.M{"customerId": "C001", "name": "Ana Torres", "email": "ana.torres@gmail.com", "active": true},
		bson.M{"customerId": "C002", "name": "Luis Gómez", "email": "luis.gomez@gmail.com", "active": true},
		bson.M{"customerId": "C003", "name": "Carla Pérez", "email": "carla.perez@gmail.com", "active": false},
	}

	_, err = r.col.InsertMany(ctx, docs)
	return err
}
