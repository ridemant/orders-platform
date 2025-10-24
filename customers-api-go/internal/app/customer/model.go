package customer

import "go.mongodb.org/mongo-driver/bson/primitive"

type Customer struct {
	ID         primitive.ObjectID `bson:"_id,omitempty" json:"-"`
	CustomerID string             `bson:"customerId" json:"customerId"`
	Name       string             `bson:"name" json:"name"`
	Email      string             `bson:"email" json:"email"`
	Active     bool               `bson:"active" json:"active"`
}
