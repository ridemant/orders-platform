FROM golang:1.23.0-alpine AS builder
RUN apk add --no-cache git
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go build -o products-api ./cmd/server

FROM alpine:3
WORKDIR /app
COPY --from=builder /app/products-api .
EXPOSE 8083
CMD ["./products-api"]