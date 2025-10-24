package product

import (
	"context"
	"errors"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var ErrNotFound = errors.New("product not found")

// Repository provides access to products collection.
type Repository struct {
	db *mongo.Database
}

// NewRepository creates a new product repository.
func NewRepository(db *mongo.Database) *Repository {
	return &Repository{db: db}
}

// FindByID finds a product by productId.
func (r *Repository) FindByID(ctx context.Context, productID string) (*Product, error) {
	col := r.db.Collection("products")
	filter := bson.M{"productId": productID}
	opts := options.FindOne().SetProjection(bson.M{"_id": 0})
	var p Product
	if err := col.FindOne(ctx, filter, opts).Decode(&p); err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, ErrNotFound
		}
		return nil, err
	}
	return &p, nil
}
