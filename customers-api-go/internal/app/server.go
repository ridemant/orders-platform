package app

import (
	"context"
	customer2 "customers-api-go/internal/app/customer"
	"net/http"
	"time"

	"customers-api-go/config"
	"customers-api-go/internal/health"
	"customers-api-go/internal/middleware"
	"customers-api-go/internal/util"

	"github.com/gin-gonic/gin"
)

type Server struct {
	cfg     *config.Config
	engine  *gin.Engine
	httpSrv *http.Server
	svc     customer2.Service
}

func NewServer(cfg *config.Config, svc customer2.Service) *Server {
	engine := gin.New()
	engine.Use(gin.Recovery())
	engine.Use(middleware.RequestLogger())

	router := NewRouter(engine, svc)

	httpSrv := &http.Server{
		Addr:         "0.0.0.0:8080",
		Handler:      router,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  30 * time.Second,
	}

	return &Server{
		cfg:     cfg,
		engine:  engine,
		httpSrv: httpSrv,
		svc:     svc,
	}
}

func (s *Server) Start() error {
	return s.httpSrv.ListenAndServe()
}

func (s *Server) Shutdown(ctx context.Context) error {
	return s.httpSrv.Shutdown(ctx)
}

func NewRouter(engine *gin.Engine, svc customer2.Service) http.Handler {
	engine.GET("/health", health.Handler)

	h := customer2.NewHandler(svc)
	engine.GET("/customers/:id", h.GetByID)

	engine.GET("/", func(c *gin.Context) {
		util.JSON(c, http.StatusOK, gin.H{"status": "ok"})
	})

	return engine
}
