package app

import (
	"net/http"

	"products-api-go/config"
	pkglogger "products-api-go/pkg/logger"

	"github.com/gin-gonic/gin"
)

func NewServer(handler http.Handler, cfg config.Config) *http.Server {
	addr := "0.0.0.0:8080"
	return &http.Server{
		Addr:    addr,
		Handler: handler,
	}
}

func NewRouter(logger *pkglogger.Logger) *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(func(c *gin.Context) {
		logger.Infof("%s %s", c.Request.Method, c.Request.URL.Path)
		c.Next()
	})
	return r
}
