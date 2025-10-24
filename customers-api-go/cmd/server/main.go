package main

import (
	"context"
	customer2 "customers-api-go/internal/app/customer"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	"customers-api-go/config"
	app "customers-api-go/internal/app"
	"customers-api-go/internal/database"
	pkglogger "customers-api-go/pkg/logger"

	"github.com/sirupsen/logrus"
)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	cfg, err := config.Load()
	if err != nil {
		fmt.Println("failed loading config:", err)
		os.Exit(1)
	}

	pkglogger.Init(cfg.LogLevel)
	log := logrus.StandardLogger()
	log.Infof("starting %s", cfg.AppName)

	mongoClient, err := database.Connect(ctx, cfg.MongoURI)
	if err != nil {
		log.Fatalf("mongo connect error: %v", err)
	}
	defer func() {
		_ = mongoClient.Disconnect(context.Background())
	}()

	db := mongoClient.Database(cfg.MongoDB)

	repo := customer2.NewRepository(db.Collection("customers"))
	service := customer2.NewService(repo)

	if err := repo.SeedCustomers(ctx); err != nil {
		log.Errorf("seed error: %v", err)
	}

	srv := app.NewServer(cfg, service)

	go func() {
		if err := srv.Start(); err != nil {
			log.Fatalf("server error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Info("shutting down server")

	ctxShut, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctxShut); err != nil {
		log.Errorf("shutdown error: %v", err)
	}
}
