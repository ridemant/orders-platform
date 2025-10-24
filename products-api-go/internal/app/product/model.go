package product

import "go.mongodb.org/mongo-driver/bson/primitive"

// Product represents a product document in MongoDB
type Product struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"-"`
	ProductID   string             `bson:"productId" json:"productId"`
	Name        string             `bson:"name" json:"name"`
	Description string             `bson:"description" json:"description"`
	Price       float64            `bson:"price" json:"price"`
	Active      bool               `bson:"active" json:"active"`
}
