package config

import (
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	AppName  string
	Port     string
	MongoURI string
	MongoDB  string
	LogLevel string
}

func Load() (*Config, error) {
	_ = godotenv.Load()

	cfg := &Config{
		AppName:  getEnv("APP_NAME", "customers-api"),
		Port:     getEnv("PORT", "8084"),
		MongoURI: getEnv("MONGO_URI", "mongodb://localhost:27017"),
		MongoDB:  getEnv("MONGO_DB", "customersdb"),
		LogLevel: getEnv("LOG_LEVEL", "info"),
	}
	return cfg, nil
}

func getEnv(key, fallback string) string {
	if v, ok := os.LookupEnv(key); ok {
		return v
	}
	return fallback
}
