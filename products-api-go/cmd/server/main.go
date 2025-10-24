package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"products-api-go/config"
	app "products-api-go/internal/app"
	"products-api-go/internal/app/product"
	"products-api-go/internal/database"
	"products-api-go/internal/health"
	pkglogger "products-api-go/pkg/logger"
)

func main() {
	// Load configuration
	cfg := config.Load()

	// Initialize logger
	logger := pkglogger.New(cfg.LogLevel)
	logger.Infof("starting %s", cfg.AppName)

	// Initialize mongo database (and seed products if needed)
	db, err := database.NewDatabase(cfg)
	if err != nil {
		logger.Fatalf("failed to connect to database: %v", err)
	}

	// Wire repository -> service -> handler for product
	prodRepo := product.NewRepository(db)
	prodSvc := product.NewService(prodRepo)
	prodHandler := product.NewHandler(prodSvc)

	// Create router and server
	r := app.NewRouter(logger)

	// register handlers
	health.Register(r)
	product.RegisterRoutes(r, prodHandler)

	srv := app.NewServer(r, cfg)

	// Start server
	go func() {
		logger.Infof("listening on :%s", cfg.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatalf("server error: %v", err)
		}
	}()

	// Graceful shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	logger.Info("shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		logger.Errorf("server forced to shutdown: %v", err)
	}
	logger.Info("server stopped")
}
