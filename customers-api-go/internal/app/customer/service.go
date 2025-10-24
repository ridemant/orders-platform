package customer

import "context"

type Service interface {
	GetCustomerByID(ctx context.Context, id string) (*Customer, error)
}

type service struct {
	repo *Repository
}

func NewService(r *Repository) Service {
	return &service{repo: r}
}

func (s *service) GetCustomerByID(ctx context.Context, id string) (*Customer, error) {
	return s.repo.FindByID(ctx, id)
}
