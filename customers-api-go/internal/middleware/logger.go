package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

func RequestLogger() gin.HandlerFunc {
	log := logrus.StandardLogger()
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()
		latency := time.Since(start)
		entry := log.WithFields(logrus.Fields{
			"method":  c.Request.Method,
			"path":    c.Request.URL.Path,
			"status":  c.Writer.Status(),
			"latency": latency.String(),
		})
		entry.Info("request finished")
	}
}
