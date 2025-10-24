package health

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Register registers health route on router
func Register(r *gin.Engine) {
	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})
}
