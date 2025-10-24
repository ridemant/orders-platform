package database

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func Connect(ctx context.Context, uri string) (*mongo.Client, error) {
	const defaultAttempts = 10
	return ConnectWithRetry(ctx, uri, defaultAttempts)
}
func ConnectWithRetry(ctx context.Context, uri string, maxAttempts int) (*mongo.Client, error) {
	var client *mongo.Client
	var err error
	for attempt := 1; attempt <= maxAttempts; attempt++ {
		ctxConnect, cancel := context.WithTimeout(ctx, 10*time.Second)
		client, err = mongo.Connect(ctxConnect, options.Client().ApplyURI(uri))
		cancel()
		if err == nil {
			ctxPing, cancelPing := context.WithTimeout(ctx, 5*time.Second)
			err = client.Ping(ctxPing, nil)
			cancelPing()
			if err == nil {
				return client, nil
			}
		}
		sleep := time.Duration(attempt*500) * time.Millisecond
		if sleep > 5*time.Second {
			sleep = 5 * time.Second
		}
		fmt.Printf("mongo connect attempt %d failed: %v -> retrying in %s\n", attempt, err, sleep)
		time.Sleep(sleep)
	}
	return nil, fmt.Errorf("mongo connect failed after %d attempts: %w", maxAttempts, err)
}
