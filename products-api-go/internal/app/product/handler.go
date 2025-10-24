package product

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Handler handles HTTP requests for products.
type Handler struct {
	svc *Service
}

// NewHandler creates a new Handler.
func NewHandler(s *Service) *Handler {
	return &Handler{svc: s}
}

// RegisterRoutes registers product routes on the provided gin engine.
func RegisterRoutes(r *gin.Engine, h *Handler) {
	g := r.Group("/products")
	g.GET(":id", h.GetByID)
}

// GetByID handles GET /products/:id
func (h *Handler) GetByID(c *gin.Context) {
	id := c.Param("id")
	ctx := c.Request.Context()
	p, err := h.svc.GetByID(ctx, id)
	if err != nil {
		if err == ErrNotFound {
			c.JSON(http.StatusNotFound, gin.H{"error": "product not found"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "internal error"})
		return
	}
	c.JSON(http.StatusOK, p)
}
