package customer

import (
	"net/http"

	"customers-api-go/internal/util"

	"github.com/gin-gonic/gin"
)

type Handler struct {
	svc Service
}

func NewHandler(svc Service) *Handler {
	return &Handler{svc: svc}
}

func (h *Handler) GetByID(c *gin.Context) {
	id := c.Param("id")
	cust, err := h.svc.GetCustomerByID(c.Request.Context(), id)
	if err != nil {
		if err == ErrNotFound {
			util.ErrorJSON(c, http.StatusNotFound, "customer not found")
			return
		}
		util.ErrorJSON(c, http.StatusInternalServerError, "internal error")
		return
	}
	util.JSON(c, http.StatusOK, cust)
}
