FROM golang:1.23.0-alpine AS builder
RUN apk add --no-cache git
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go build -o customers-api ./cmd/server

FROM alpine:3
WORKDIR /app
COPY --from=builder /app/customers-api .
EXPOSE 8084
CMD ["./customers-api"]