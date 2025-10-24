package product

import (
	"context"
)

// Service provides business logic for products.
type Service struct {
	repo *Repository
}

// NewService creates a new product service.
func NewService(r *Repository) *Service {
	return &Service{repo: r}
}

// GetByID returns a product by its productId using provided context.
func (s *Service) GetByID(ctx context.Context, productID string) (*Product, error) {
	// validation: basic
	if productID == "" {
		return nil, ErrNotFound
	}
	return s.repo.FindByID(ctx, productID)
}
