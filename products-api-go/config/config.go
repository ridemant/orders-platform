package config

import (
	"bufio"
	"os"
	"strings"
)

// Config holds application configuration loaded from env or .env file
type Config struct {
	AppName  string
	Port     string
	MongoURI string
	MongoDB  string
	LogLevel string
}

// Load reads .env if present and returns configuration with defaults
func Load() Config {
	// Try to read .env in project root
	if f, err := os.Open(".env"); err == nil {
		defer f.Close()
		s := bufio.NewScanner(f)
		for s.Scan() {
			line := strings.TrimSpace(s.Text())
			if line == "" || strings.HasPrefix(line, "#") {
				continue
			}
			parts := strings.SplitN(line, "=", 2)
			if len(parts) == 2 {
				key := strings.TrimSpace(parts[0])
				val := strings.TrimSpace(parts[1])
				os.Setenv(key, val)
			}
		}
	}

	cfg := Config{
		AppName:  getEnv("APP_NAME", "products-api"),
		Port:     getEnv("PORT", "8083"),
		MongoURI: getEnv("MONGO_URI", "mongodb://localhost:27017"),
		MongoDB:  getEnv("MONGO_DB", "productsdb"),
		LogLevel: getEnv("LOG_LEVEL", "info"),
	}

	return cfg
}

func getEnv(key, def string) string {
	if v, ok := os.LookupEnv(key); ok && v != "" {
		return v
	}
	return def
}
