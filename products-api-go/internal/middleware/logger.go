package middleware

import (
	"products-api-go/pkg/logger"

	"github.com/gin-gonic/gin"
)

// Logger returns a gin middleware that logs requests via the provided logger.
func Logger(l *logger.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		l.Infof("%s %s", c.Request.Method, c.Request.URL.Path)
		c.Next()
	}
}
